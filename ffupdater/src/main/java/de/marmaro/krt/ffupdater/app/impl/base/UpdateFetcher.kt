package de.marmaro.krt.ffupdater.app.impl.base

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.WorkerThread
import de.marmaro.krt.ffupdater.app.entity.LatestUpdate
import de.marmaro.krt.ffupdater.network.file.CacheBehaviour

@Keep
interface UpdateFetcher {

    @WorkerThread
    suspend fun fetchLatestUpdate(context: Context, cacheBehaviour: CacheBehaviour): LatestUpdate
}