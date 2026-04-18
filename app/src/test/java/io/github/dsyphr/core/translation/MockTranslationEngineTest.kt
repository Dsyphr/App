package io.github.dsyphr.core.translation

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MockTranslationEngineTest {

    @Test
    fun testTranslateHindiToEnglishReturnsMockResult() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translateHindiToEnglish("नमस्ते")
        
        assertTrue(result.isSuccess)
        assertEquals("Mock Hindi to English: नमस्ते", result.getOrNull())
    }

    @Test
    fun testTranslateEnglishToHindiReturnsMockResult() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translateEnglishToHindi("Hello")
        
        assertTrue(result.isSuccess)
        assertEquals("Mock English to Hindi: Hello", result.getOrNull())
    }

    @Test
    fun testTranslateBengaliToEnglishReturnsMockResult() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translateBengaliToEnglish("নমস্")
        
        assertTrue(result.isSuccess)
        assertEquals("Mock Bengali to English: নমস্", result.getOrNull())
    }

    @Test
    fun testTranslateEnglishToBengaliReturnsMockResult() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translateEnglishToBengali("Hello")
        
        assertTrue(result.isSuccess)
        assertEquals("Mock English to Bengali: Hello", result.getOrNull())
    }

    @Test
    fun testTranslateWithMatchingLanguagesReturnsOriginalText() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translate(
            text = "Hello",
            source = CoreTranslationLanguage.ENGLISH,
            target = CoreTranslationLanguage.ENGLISH
        )
        
        assertTrue(result.isSuccess)
        assertEquals("Hello", result.getOrNull())
    }

    @Test
    fun testEmptyStringTranslationReturnsEmptyMockResult() = runTest {
        val engine = MockTranslationEngine()
        val result = engine.translateHindiToEnglish("")
        
        assertTrue(result.isSuccess)
        assertEquals("Mock Hindi to English: ", result.getOrNull())
    }
}
