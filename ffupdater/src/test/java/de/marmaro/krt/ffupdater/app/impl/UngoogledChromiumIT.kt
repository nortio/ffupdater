package de.marmaro.krt.ffupdater.app.impl

import de.marmaro.krt.ffupdater.network.github.GithubConsumer
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
internal class UngoogledChromiumIT : BaseAppIT() {

    @Test
    fun checkForUpdateWithoutLoadingFromCacheAsync() {
        val ungoogledChromium = UngoogledChromium(GithubConsumer.INSTANCE, deviceAbiExtractor)
        val result =
            runBlocking { ungoogledChromium.checkForUpdateWithoutLoadingFromCacheAsync(context).await() }
        verifyThatDownloadLinkAvailable(result.downloadUrl)
        assertTrue(result.firstReleaseHasAssets)
    }
}