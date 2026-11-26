package com.keepnc

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.Coil
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.google.android.material.color.DynamicColors
import com.keepnc.data.settings.AppTheme
import com.keepnc.data.settings.SettingsStorage
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * Application class. @HiltAndroidApp triggers Hilt's code generation and sets up
 * the application-level dependency injection component.
 *
 * Implements:
 * - [Configuration.Provider] so WorkManager uses HiltWorkerFactory.
 * - [ImageLoaderFactory] so Coil automatically uses our authenticated, disk-cached ImageLoader.
 */
@HiltAndroidApp
class KeepNcApp : Application(), Configuration.Provider, ImageLoaderFactory {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var settingsStorage: SettingsStorage

    @Inject
    lateinit var imageLoader: ImageLoader

    override fun newImageLoader(): ImageLoader = imageLoader

    override fun onCreate() {
        super.onCreate()
        Coil.setImageLoader(imageLoader)
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppTheme.apply(settingsStorage.getAppTheme())
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
