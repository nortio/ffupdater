package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.entity.Category
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.device.ABI
import de.marmaro.krt.ffupdater.device.DeviceAbiExtractor
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * https://github.com/ungoogled-software/ungoogled-chromium-android/releases
 */

class UngoogledChromium(
    private val consumer: GithubConsumer = GithubConsumer.INSTANCE,
    private val deviceAbiExtractor: DeviceAbiExtractor = DeviceAbiExtractor.INSTANCE,
) : AppBase() {
    override val packageName = "org.ungoogled.chromium.stable"
    override val title = R.string.ungoogled_chromium__title
    override val description = R.string.ungoogled_chromium__description
    override val installationWarning = R.string.ungoogled_chromium__warning
    override val downloadSource = R.string.github
    override val icon = R.mipmap.ic_logo_ungoogled_chromium
    override val minApiLevel = Build.VERSION_CODES.LOLLIPOP
    override val supportedAbis = listOf(ABI.ARM64_V8A, ABI.ARMEABI_V7A, ABI.X86)
    override val projectPage = "https://github.com/ungoogled-software/ungoogled-chromium-android"

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "7e6ba7bbb939fa52d5569a8ea628056adf8c75292bf4dee6b353fafaf2c30e19"
    override val eolReason = R.string.ungoogled_chromium__eol_reason
    override val category = Category.EOL

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun findLatestUpdate(context: Context): LatestUpdate {
        Log.d(LOG_TAG, "check for latest version")
        val fileName = when (deviceAbiExtractor.supportedAbis.first { abi -> abi in supportedAbis }) {
            ABI.ARMEABI_V7A -> "ChromeModernPublic_arm.apk"
            ABI.ARM64_V8A -> "ChromeModernPublic_arm64.apk"
            ABI.X86 -> "ChromeModernPublic_x86.apk"
            else -> throw IllegalArgumentException("ABI is not supported")
        }
        val result = try {
            consumer.updateCheck(
                repoOwner = "ungoogled-software",
                repoName = "ungoogled-chromium-android",
                resultsPerPage = 2,
                isValidRelease = { release -> !release.isPreRelease && "webview" !in release.name },
                isSuitableAsset = { asset -> asset.name == fileName },
                dontUseApiForLatestRelease = true,
                context
            )
        } catch (e: NetworkException) {
            throw NetworkException("Fail to request the latest version of Ungoogled Chromium.", e)
        }

        val extractVersion = {
            val regexMatch = Regex("""^([.0-9]+)-\d+$""")
                .find(result.tagName)
            checkNotNull(regexMatch) {
                "Fail to extract the version with regex from string '${result.tagName}'."
            }
            val matchGroup = regexMatch.groups[1]
            checkNotNull(matchGroup) {
                "Fail to extract the version value from regex match: '${regexMatch.value}'."
            }
            matchGroup.value
        }
        val version = extractVersion()
        Log.i(LOG_TAG, "found latest version $version")
        return LatestUpdate(
            downloadUrl = result.url,
            version = version,
            publishDate = result.releaseDate,
            fileSizeBytes = result.fileSizeBytes,
            firstReleaseHasAssets = result.firstReleaseHasAssets,
            fileHash = null
        )
    }

    companion object {
        private const val LOG_TAG = "UngooChromium"
    }
}