package io.github.dsyphr.core.translation

class MockTranslationEngine : TranslationEngine {
    override suspend fun translateHindiToEnglish(text: String): Result<String> {
        return Result.success("Mock Hindi to English: $text")
    }

    override suspend fun translateEnglishToHindi(text: String): Result<String> {
        return Result.success("Mock English to Hindi: $text")
    }

    override suspend fun translateBengaliToEnglish(text: String): Result<String> {
        return Result.success("Mock Bengali to English: $text")
    }

    override suspend fun translateEnglishToBengali(text: String): Result<String> {
        return Result.success("Mock English to Bengali: $text")
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
}
