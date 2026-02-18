package dev.htmeta.mgm.util

import com.intellij.openapi.util.SystemInfo

object PlatformDetector {
    fun isMacOS(): Boolean = SystemInfo.isMac
    fun isAppleSilicon(): Boolean = isMacOS() && System.getProperty("os.arch") == "aarch64"
    fun isIntelMac(): Boolean = isMacOS() && System.getProperty("os.arch") == "x86_64"
}
