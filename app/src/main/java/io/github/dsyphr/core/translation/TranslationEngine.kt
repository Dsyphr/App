package io.github.dsyphr.core.translation

interface TranslationEngine {
    suspend fun translateHindiToEnglish(text: String): Result<String>
    suspend fun translateEnglishToHindi(text: String): Result<String>
    suspend fun translateBengaliToEnglish(text: String): Result<String>
    suspend fun translateEnglishToBengali(text: String): Result<String>
    
    suspend fun translate(
       text: String,
       source: CoreTranslationLanguage,
       target: CoreTranslationLanguage
   ): Result<String>
}

enum class CoreTranslationLanguage {
    HINDI,
    BENGALI,
    ENGLISH
}
