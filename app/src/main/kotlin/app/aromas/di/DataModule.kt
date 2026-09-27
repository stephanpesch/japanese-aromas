package app.aromas.di

import android.content.Context
import app.aromas.core.data.PlaceJsonParser
import app.aromas.core.data.PlaceRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
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
}
