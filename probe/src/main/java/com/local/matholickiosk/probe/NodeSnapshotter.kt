package com.local.matholickiosk.probe

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.os.Build
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.ArrayDeque
import java.util.UUID

class NodeSnapshotter(
    private val context: Context,
    private val redactionPolicy: RedactionPolicy,
) {
    data class Result(
        val json: JSONObject,
        val nodeCount: Int,
        val truncated: Boolean,
    )

    private data class PendingNode(
        val node: AccessibilityNodeInfo,
        val path: String,
        val parentPath: String?,
        val depth: Int,
    )

    fun capture(root: AccessibilityNodeInfo, window: AccessibilityWindowInfo?): Result {
        require(TargetPackagePolicy.isAllowed(root.packageName)) {
            "Root package is not allowlisted"
        }

        val nodes = JSONArray()
        val queue = ArrayDeque<PendingNode>()
        queue.add(PendingNode(root, "0", null, 0))
        var truncated = false

        while (queue.isNotEmpty()) {
            if (nodes.length() >= ProbeConstants.MAX_NODES) {
                truncated = true
                break
            }

            val pending = queue.removeFirst()
            val node = pending.node
            if (!TargetPackagePolicy.isAllowed(node.packageName)) continue
            nodes.put(serializeNode(node, pending.path, pending.parentPath, pending.depth))

            if (pending.depth >= ProbeConstants.MAX_DEPTH) {
                if (node.childCount > 0) truncated = true
                continue
            }

            for (index in 0 until node.childCount) {
                val child = node.getChild(index) ?: continue
                if (!TargetPackagePolicy.isAllowed(child.packageName)) continue
                queue.addLast(
                    PendingNode(
                        node = child,
                        path = "${pending.path}.$index",
                        parentPath = pending.path,
                        depth = pending.depth + 1,
                    ),
                )
            }
        }

        val header = JSONObject()
            .put("schemaVersion", 2)
            .put("captureId", UUID.randomUUID().toString())
            .put("capturedAtUtc", Instant.now().toString())
            .put("targetPackage", ProbeConstants.TARGET_PACKAGE)
            .put("targetVersionName", targetVersionName())
            .put("targetVersionCode", targetVersionCode())
            .put("windowId", root.windowId)
            .put("windowType", windowTypeName(window?.type))
            .put("windowTitle", JSONObject.NULL)
            .put("nodeCount", nodes.length())
            .put("truncated", truncated)
            .put("rawTextStored", false)
            .put("boundsUsedAsSelector", false)

        return Result(
            json = JSONObject().put("header", header).put("nodes", nodes),
            nodeCount = nodes.length(),
            truncated = truncated,
        )
    }

    private fun serializeNode(
        node: AccessibilityNodeInfo,
        path: String,
        parentPath: String?,
        depth: Int,
    ): JSONObject {
        val rect = Rect()
        node.getBoundsInScreen(rect)
        val sensitive = node.isPassword || node.isEditable
        val checkedState = checkedState(node)
        val checkedStateSource = checkedStateSource(node)
        val actions = JSONArray()
        node.actionList.forEach { action ->
            actions.put(
                JSONObject()
                    .put("id", action.id)
                    .put("name", AccessibilityActionNames.nameOf(action.id)),
            )
        }
        val extrasKeys = JSONArray()
        node.extras.keySet().sorted().forEach(extrasKeys::put)
        val roleDescription = node.extras.getCharSequence(ROLE_DESCRIPTION_KEY)

        return JSONObject()
            .put("path", path)
            .put("parentPath", parentPath ?: JSONObject.NULL)
            .put("depth", depth)
            .put("package", node.packageName?.toString())
            .put("class", node.className?.toString())
            .put("viewIdResourceName", node.viewIdResourceName)
            .put("text", redactionPolicy.redact(node.text, sensitive))
            .put("contentDescription", redactionPolicy.redact(node.contentDescription, sensitive))
            .put("hintText", redactionPolicy.redact(node.hintText, sensitive))
            .put("paneTitle", redactionPolicy.redact(node.paneTitle, sensitive))
            .put("tooltipText", redactionPolicy.redact(node.tooltipText, sensitive))
            .put("stateDescription", redactionPolicy.redact(node.stateDescription, sensitive))
            .put("roleDescription", redactionPolicy.redact(roleDescription, sensitive))
            .put("extrasKeys", extrasKeys)
            .put("password", node.isPassword)
            .put("editable", node.isEditable)
            .put("clickable", node.isClickable)
            .put("longClickable", node.isLongClickable)
            .put("checkable", node.isCheckable)
            .put("checked", checkedState == CHECKED_STATE_TRUE)
            .put("checkedState", checkedStateName(checkedState))
            .put("checkedStateSource", checkedStateSource)
            .put("enabled", node.isEnabled)
            .put("visibleToUser", node.isVisibleToUser)
            .put("focusable", node.isFocusable)
            .put("focused", node.isFocused)
            .put("selected", node.isSelected)
            .put("scrollable", node.isScrollable)
            .put("inputType", node.inputType)
            .put("childCount", node.childCount)
            .put(
                "boundsInScreen",
                JSONObject()
                    .put("left", rect.left)
                    .put("top", rect.top)
                    .put("right", rect.right)
                    .put("bottom", rect.bottom),
            )
            .put("actions", actions)
    }

    private fun checkedState(node: AccessibilityNodeInfo): Int {
        val compatState = compatCheckedState(node)
        if (compatState != null) return compatState
        return if (Build.VERSION.SDK_INT >= 36) node.checked else legacyCheckedState(node)
    }

    private fun checkedStateSource(node: AccessibilityNodeInfo): String = when {
        compatCheckedState(node) != null -> "ANDROIDX_COMPAT_EXTRA"
        Build.VERSION.SDK_INT >= 36 -> "PLATFORM_TRI_STATE"
        else -> "PLATFORM_BOOLEAN"
    }

    private fun compatCheckedState(node: AccessibilityNodeInfo): Int? {
        if (!node.extras.containsKey(COMPAT_CHECKED_KEY)) return null
        return node.extras.getInt(COMPAT_CHECKED_KEY, UNKNOWN_CHECKED_STATE)
            .takeIf { it in CHECKED_STATE_FALSE..CHECKED_STATE_PARTIAL }
    }

    @Suppress("DEPRECATION")
    private fun legacyCheckedState(node: AccessibilityNodeInfo): Int =
        if (node.isChecked) CHECKED_STATE_TRUE else CHECKED_STATE_FALSE

    private fun checkedStateName(state: Int): String = when (state) {
        CHECKED_STATE_FALSE -> "FALSE"
        CHECKED_STATE_TRUE -> "TRUE"
        CHECKED_STATE_PARTIAL -> "PARTIAL"
        else -> "UNKNOWN_$state"
    }

    private fun targetVersionName(): String = try {
        context.packageManager.getPackageInfo(ProbeConstants.TARGET_PACKAGE, 0).versionName ?: "unknown"
    } catch (_: PackageManager.NameNotFoundException) {
        "not-installed"
    }

    private fun targetVersionCode(): Long = try {
        context.packageManager.getPackageInfo(ProbeConstants.TARGET_PACKAGE, 0).longVersionCode
    } catch (_: PackageManager.NameNotFoundException) {
        -1L
    }

    private fun windowTypeName(type: Int?): String = when (type) {
        AccessibilityWindowInfo.TYPE_APPLICATION -> "APPLICATION"
        AccessibilityWindowInfo.TYPE_INPUT_METHOD -> "INPUT_METHOD"
        AccessibilityWindowInfo.TYPE_SYSTEM -> "SYSTEM"
        AccessibilityWindowInfo.TYPE_ACCESSIBILITY_OVERLAY -> "ACCESSIBILITY_OVERLAY"
        AccessibilityWindowInfo.TYPE_SPLIT_SCREEN_DIVIDER -> "SPLIT_SCREEN_DIVIDER"
        AccessibilityWindowInfo.TYPE_MAGNIFICATION_OVERLAY -> "MAGNIFICATION_OVERLAY"
        null -> "UNKNOWN"
        else -> "TYPE_$type"
    }

    companion object {
        private const val ROLE_DESCRIPTION_KEY = "AccessibilityNodeInfo.roleDescription"
        private const val COMPAT_CHECKED_KEY =
            "androidx.view.accessibility.AccessibilityNodeInfoCompat.CHECKED_KEY"
        private const val UNKNOWN_CHECKED_STATE = -1
        private const val CHECKED_STATE_FALSE = 0
        private const val CHECKED_STATE_TRUE = 1
        private const val CHECKED_STATE_PARTIAL = 2
    }
}
