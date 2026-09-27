package app.aromas.di

import android.content.Context
import app.aromas.core.data.AromaJsonParser
import app.aromas.core.data.AromaRepository
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
    fun provideAromaRepository(
        @ApplicationContext context: Context,
    ): AromaRepository =
        AromaRepository(
            context.assets.open("aromas.json").use { AromaJsonParser.parse(it) },
        )
}
