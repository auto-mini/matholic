package com.local.matholickiosk.kiosk.domain

internal class ClassRosterSelectionState {
    class LoadRequest internal constructor(
        val classId: String,
        internal val generation: Long,
    )

    class SelectionSnapshot internal constructor(
        internal val revision: Long,
    )

    var selectedClassId: String? = null
        private set

    var membershipStudentIds: Set<String> = emptySet()
        private set

    var isLoading: Boolean = false
        private set

    private var generation: Long = 0
    private var selectionRevision: Long = 0

    fun select(classId: String?): LoadRequest? {
        if (classId == selectedClassId) return null
        generation += 1
        selectionRevision += 1
        selectedClassId = classId
        membershipStudentIds = emptySet()
        isLoading = classId != null
        return classId?.let { LoadRequest(it, generation) }
    }

    fun resolve(classId: String?, studentIds: Set<String>) {
        generation += 1
        if (classId != selectedClassId) {
            selectionRevision += 1
        }
        selectedClassId = classId
        membershipStudentIds = studentIds.toSet()
        isLoading = false
    }

    fun snapshotSelection(): SelectionSnapshot =
        SelectionSnapshot(selectionRevision)

    fun resolveRefresh(
        snapshot: SelectionSnapshot,
        loadedClassId: String?,
        loadedStudentIds: Set<String>,
        availableClassIds: Set<String>,
        forceLoadedSelection: Boolean,
    ): Boolean {
        val newerSelectionIsAvailable = snapshot.revision != selectionRevision &&
            selectedClassId?.let(availableClassIds::contains) == true
        if (!forceLoadedSelection && newerSelectionIsAvailable) {
            return false
        }
        resolve(loadedClassId, loadedStudentIds)
        return true
    }

    fun apply(request: LoadRequest, studentIds: Set<String>): Boolean {
        if (request.generation != generation || request.classId != selectedClassId) return false
        membershipStudentIds = studentIds.toSet()
        isLoading = false
        return true
    }

    fun replaceIfSelected(classId: String, studentIds: Set<String>): Boolean {
        if (classId != selectedClassId) return false
        generation += 1
        membershipStudentIds = studentIds.toSet()
        isLoading = false
        return true
    }
}

internal class SingleFlightGate {
    var isActive: Boolean = false
        private set

    fun tryStart(): Boolean {
        if (isActive) return false
        isActive = true
        return true
    }

    fun finish() {
        isActive = false
    }
}
