package app.aromas.di

import android.content.Context
import app.aromas.core.data.PlaceJsonParser
import app.aromas.core.data.PlaceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import java.time.ZoneId
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun providePlaceRepository(
        @ApplicationContext context: Context,
    ): PlaceRepository =
        PlaceRepository(
            context.assets.open("places.json").use { PlaceJsonParser.parse(it) },
        )

    /**
     * "Now" for the availability filter, injected so it stays testable. It runs in
     * Japan's time zone: every place is in Japan and the researched opening hours
     * are given in JST, so the filter must compare against the current time in
     * Japan, not the device's local time zone.
     */
    @Provides
    fun provideClock(): Clock = Clock.system(ZoneId.of("Asia/Tokyo"))
}
