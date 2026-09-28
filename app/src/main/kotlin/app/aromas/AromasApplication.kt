package app.aromas

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient

@HiltAndroidApp
class AromasApplication :
    Application(),
    ImageLoaderFactory {
    // Wikimedia rejects the default OkHttp User-Agent (their UA policy returns 403),
    // so the Commons photos need a descriptive one to load.
    override fun newImageLoader(): ImageLoader =
        ImageLoader
            .Builder(this)
            .okHttpClient {
                OkHttpClient
                    .Builder()
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain
                                .request()
                                .newBuilder()
                                .header("User-Agent", USER_AGENT)
                                .build(),
                        )
                    }.build()
            }.build()

    private companion object {
        const val USER_AGENT = "japanese-aromas/1.0 (github.com/stephanpesch/japanese-aromas; educational)"
    }
}
