package de.marmaro.krt.ffupdater

import android.annotation.SuppressLint
import android.content.Context
import androidx.annotation.Keep
import androidx.preference.PreferenceManager
import androidx.work.ExistingPeriodicWorkPolicy.*
import java.io.File

@Keep
object Migrator {

    private var restartBackgroundWorkNecessary = false

    @SuppressLint("ApplySharedPref")
    fun migrate(context: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
        val lastVersionCode = preferences.getInt(FFUPDATER_VERSION_CODE, 0)

        if (lastVersionCode != BuildConfig.VERSION_CODE) {
            restartBackgroundWorkNecessary = true
        }

        if (lastVersionCode < 148) { // 78.0.6
            PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                .edit()
                .remove("lastBackgroundCheckTimestamp")
                .commit()

            File(context.externalCacheDir, "crashlog.txt").delete()
        }

        preferences.edit()
            .putInt(FFUPDATER_VERSION_CODE, BuildConfig.VERSION_CODE)
            .apply()
    }

    fun isBackgroundWorkRestartNecessary(): Boolean {
        return restartBackgroundWorkNecessary
    }

    fun backgroundWorkHasBeenRestarted() {
        restartBackgroundWorkNecessary = false
    }

    private const val FFUPDATER_VERSION_CODE = "migrator_ffupdater_version_code"
}