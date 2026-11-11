package dev.htmeta.mgm.native

import com.intellij.openapi.diagnostic.thisLogger
import dev.htmeta.mgm.util.PlatformDetector

class GPUProviderFactory {
    private val LOG = thisLogger()

    fun createProvider(): GPUProvider {
        if (!PlatformDetector.isMacOS()) {
            LOG.info("Not macOS — GPU monitoring unavailable")
            return NoopGPUProvider()
        }

        return try {
            val iokit = IOKitGPUProvider()
            if (iokit.initialize()) {
                LOG.info("IOKit GPU provider initialized successfully")
                iokit
            } else {
                LOG.warn("IOKit GPU provider failed to initialize, using noop")
                NoopGPUProvider()
            }
        } catch (e: LinkageError) {
            LOG.warn("JNA not available for IOKit, using noop", e)
            NoopGPUProvider()
        } catch (e: Exception) {
            LOG.error("Failed to create GPU provider", e)
            NoopGPUProvider()
        }
    }
}
