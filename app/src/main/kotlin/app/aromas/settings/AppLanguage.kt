package app.aromas.settings

import android.app.LocaleManager
import android.content.Context
import android.os.LocaleList
import app.aromas.core.model.Language

/**
 * Reads and switches the per-app language. Uses the platform per-app locales
 * (API 33+, guaranteed by minSdk 34), which Android persists across restarts,
 * so both the UI resources (values / values-ja) and — via [current] — the
 * displayed content follow the same choice without extra storage.
 */
object AppLanguage {
    fun current(context: Context): Language {
        val locales = context.resources.configuration.locales
        val tag = if (locales.isEmpty) null else locales[0].language
        return Language.fromTag(tag)
    }

    fun set(
        context: Context,
        language: Language,
    ) {
        val manager = context.getSystemService(LocaleManager::class.java)
        manager.applicationLocales = LocaleList.forLanguageTags(language.tag)
    }

    fun toggle(context: Context) {
        val next = if (current(context) == Language.JAPANESE) Language.GERMAN else Language.JAPANESE
        set(context, next)
    }
}
