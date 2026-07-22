package com.local.matholickiosk.probe

object TargetPackagePolicy {
    fun isAllowed(packageName: CharSequence?): Boolean =
        packageName?.toString() == ProbeConstants.TARGET_PACKAGE
}
