package de.marmaro.krt.ffupdater.app.impl

import android.content.Context
import android.os.Build
import androidx.annotation.MainThread
import de.marmaro.krt.ffupdater.R
import de.marmaro.krt.ffupdater.app.entity.Category
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.network.exceptions.NetworkException
import de.marmaro.krt.ffupdater.network.github.GithubConsumer

/**
 * https://api.github.com/repos/mozilla-lockwise/lockwise-android/releases
 * https://www.apkmirror.com/apk/mozilla/firefox-lockwise/
 */
class Lockwise(
    private val consumer: GithubConsumer = GithubConsumer.INSTANCE,
) : AppBase() {
    override val packageName = "mozilla.lockbox"
    override val title = R.string.lockwise__title
    override val description = R.string.lockwise__description
    override val downloadSource = R.string.github
    override val icon = R.mipmap.ic_logo_lockwise
    override val minApiLevel = Build.VERSION_CODES.N
    override val supportedAbis = ALL_ABIS

    @Suppress("SpellCheckingInspection")
    override val signatureHash = "64d26b507078deba2fee42d6bd0bfad41d39ffc4e791f281028e5e73d3c8d2f2"
    override val projectPage = "https://github.com/mozilla-lockwise/lockwise-android"
    override val eolReason = R.string.lockwise__eol_reason
    override val displayCategory = Category.EOL

    @MainThread
    @Throws(NetworkException::class)
    override suspend fun findLatestUpdate(context: Context): LatestUpdate {
        val result = try {
            consumer.updateCheck(
                repoOwner = "mozilla-lockwise",
                repoName = "lockwise-android",
                resultsPerPage = 5,
                isValidRelease = { release -> !release.isPreRelease },
                isSuitableAsset = { asset -> asset.name.endsWith(".apk") },
                dontUseApiForLatestRelease = false,
                context
            )
        } catch (e: NetworkException) {
            throw NetworkException("Fail to request the latest version of Lockwise.", e)
        }

        val extractVersion = {
            // tag_name can be: "release-v4.0.3", "release-v4.0.0-RC-2"
            val regexMatch = Regex("""^release-v((\d)+(\.\d+)*)""")
                .find(result.tagName)
            checkNotNull(regexMatch) {
                "Fail to extract the version with regex from string: \"${result.tagName}\""
            }
            val matchGroup = regexMatch.groups[1]
            checkNotNull(matchGroup) {
                "Fail to extract the version value from regex match: \"${regexMatch.value}\""
            }
            matchGroup.value
        }

        return LatestUpdate(
            downloadUrl = result.url,
            version = extractVersion(),
            publishDate = result.releaseDate,
            fileSizeBytes = result.fileSizeBytes,
            fileHash = null,
            firstReleaseHasAssets = result.firstReleaseHasAssets,
        )
    }
}