package io.github.dsyphr
import android.content.Context
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.tasks.await
import java.util.concurrent.atomic.AtomicBoolean

class TranslationManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: TranslationManager? = null

        fun getInstance(context: Context): TranslationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TranslationManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }

    // Track model download status
    private val modelStatus = mutableMapOf<String, AtomicBoolean>()

    // Get or create translator
    private fun getTranslator(sourceLang: String, targetLang: String): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()

        return Translation.getClient(options)
    }

    // Download translation model if needed
    private suspend fun ensureModelDownloaded(sourceLang: String, targetLang: String) {
        val modelKey = "$sourceLang-$targetLang"

        if (modelStatus[modelKey]?.get() != true) {
            val translator = getTranslator(sourceLang, targetLang)
            val conditions = DownloadConditions.Builder()
                .requireWifi() // Can change to .requireWifi() if needed
                .build()

            translator.downloadModelIfNeeded(conditions).await()
            modelStatus[modelKey] = AtomicBoolean(true)
        }
    }

    // 1. Hindi to English translation
    suspend fun translateHindiToEnglish(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.HINDI, TranslateLanguage.ENGLISH)
            val translator = getTranslator(TranslateLanguage.HINDI, TranslateLanguage.ENGLISH)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. English to Hindi translation
    suspend fun translateEnglishToHindi(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI)
            val translator = getTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.HINDI)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 3. Bengali to English translation
    suspend fun translateBengaliToEnglish(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH)
            val translator = getTranslator(TranslateLanguage.BENGALI, TranslateLanguage.ENGLISH)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 4. English to Bengali translation
    suspend fun translateEnglishToBengali(text: String): Result<String> {
        return try {
            ensureModelDownloaded(TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI)
            val translator = getTranslator(TranslateLanguage.ENGLISH, TranslateLanguage.BENGALI)
            val translatedText = translator.translate(text).await()
            Result.success(translatedText)
        } catch (e: Exception) {
            Result.failure(e)
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
}