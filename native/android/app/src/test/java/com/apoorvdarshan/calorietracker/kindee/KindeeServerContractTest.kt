package com.apoorvdarshan.calorietracker.kindee

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class KindeeServerContractTest {
    @Test fun acceptsHttpsApiAndNormalizesTrailingSlash() {
        assertEquals("https://api.example.test/api/v1",validKindeeBase("https://api.example.test/api/v1/",false))
    }
    @Test fun localHttpIsOnlyForExplicitDevelopmentLoopback() {
        assertEquals("http://127.0.0.1:18765/api/v1",validKindeeBase("http://127.0.0.1:18765/api/v1",true))
        assertThrows(IllegalArgumentException::class.java) { validKindeeBase("http://127.0.0.1:18765/api/v1",false) }
        assertThrows(IllegalArgumentException::class.java) { validKindeeBase("http://192.168.1.2/api/v1",true) }
    }
    @Test fun refusesCredentialsQueryAndUnexpectedApiPath() {
        for (url in listOf("https://user:pass@example.test/api/v1","https://example.test/api/v1?token=x","https://example.test/api/v1#x","https://example.test/other")) {
            assertThrows(IllegalArgumentException::class.java) { validKindeeBase(url,false) }
        }
    }
}
