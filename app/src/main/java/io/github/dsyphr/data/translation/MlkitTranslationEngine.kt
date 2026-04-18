package io.github.dsyphr.data.translation

import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import io.github.dsyphr.core.translation.CoreTranslationLanguage
import io.github.dsyphr.core.translation.TranslationEngine
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class MlkitTranslationEngine @JvmOverloads constructor(
    private val context: Context,
    private val downloadOnWifiOnly: Boolean = true
) : TranslationEngine {

    companion object {
        private const val MODEL_DOWNLOAD_TAG = "translation_model"
    }

    // Track model download status
    private val modelStatus = mutableMapOf<String, AtomicBoolean>()
    private val translators = mutableMapOf<String, Translator>()

    // Get or create translator
    private fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val key = "$sourceLang-$targetLang"
        
        return translators.getOrPut(key) {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(sourceLang)
                .setTargetLanguage(targetLang)
                .build()
            Translation.getClient(options)
        }
    }

    // Download translation model if needed
    private suspend fun ensureModelDownloaded(sourceLang: String, targetLang: String) {
        val modelKey = "$sourceLang-$targetLang"

        if (modelStatus[modelKey]?.get() != true) {
            val translator = getTranslator(sourceLang, targetLang)
            val conditions = if (downloadOnWifiOnly) {
                DownloadConditions.Builder()
                    .requireWifi()
                    .build()
            } else {
                DownloadConditions.Builder().build()
            }

            try {
                translator.downloadModelIfNeeded(conditions).await()
                modelStatus[modelKey] = AtomicBoolean(true)
            } catch (e: Exception) {
                modelStatus[modelKey] = AtomicBoolean(false)
                throw e
            }
        }
    }

    override suspend fun translateHindiToEnglish(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.HINDI, TranslateLanguage.ENGLISH)
            val translator = getTranslator(TranslateLanguage.HINDI, TranslateLanguage.ENGLISH)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateEnglishToHindi(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI)
            val translator = getTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateBengaliToEnglish(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH)
            val translator = getTranslator(TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translateEnglishToBengali(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI)
            val translator = getTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText.trim())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun translate(
        text: String,
        source: CoreTranslationLanguage,
        target: CoreTranslationLanguage
    ): Result<String> {
        return when {
            source == CoreTranslationLanguage.HINDI && target == CoreTranslationLanguage.ENGLISH -> {
                translateHindiToEnglish(text)
            }
            source == CoreTranslationLanguage.ENGLISH && target == CoreTranslationLanguage.HINDI -> {
                translateEnglishToHindi(text)
            }
            source == CoreTranslationLanguage.BENGALI && target == CoreTranslationLanguage.ENGLISH -> {
                translateBengaliToEnglish(text)
            }
            source == CoreTranslationLanguage.ENGLISH && target == CoreTranslationLanguage.BENGALI -> {
                translateEnglishToBengali(text)
            }
            else -> Result.success(text)
        }
    }

    // Optional: Delete downloaded models to free up space
    suspend fun deleteAllModels() {
        val modelManager = RemoteModelManager.getInstance()
        val translators = listOf(
            TranslateLanguage.HINDI,
            TranslateLanguage.ENGLISH,
            TranslateLanguage.BENGALI
        )

        translators.forEach { language ->
            val model = com.google.mlkit.nl.translate.TranslateRemoteModel.Builder(language).build()
            try {
                modelManager.deleteDownloadedModel(model).await()
                modelStatus.clear()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun close() {
        translators.values.forEach { it.close() }
        translators.clear()
    }
}
