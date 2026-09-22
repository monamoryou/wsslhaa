package com.example.data.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FirebaseAuthManagerTest {

    @Test
    fun authUserProfile_creation_isCorrect() {
        val profile = AuthUserProfile(
            uid = "test_uid_123",
            displayName = "أحمد مصطفى",
            email = "nameend1@gmail.com",
            phoneNumber = "+218912345678",
            photoUrl = "https://example.com/avatar.jpg",
            isAnonymous = false
        )

        assertEquals("test_uid_123", profile.uid)
        assertEquals("أحمد مصطفى", profile.displayName)
        assertEquals("nameend1@gmail.com", profile.email)
        assertEquals("+218912345678", profile.phoneNumber)
        assertEquals("https://example.com/avatar.jpg", profile.photoUrl)
        assertEquals(false, profile.isAnonymous)
    }

    @Test
    fun phoneAuthStatus_types_areHandled() {
        val failed = PhoneAuthStatus.Failed("خطأ في التحقق")
        assertEquals("خطأ في التحقق", failed.message)

        val timeout = PhoneAuthStatus.AutoRetrievalTimeOut("ver_123")
        assertEquals("ver_123", timeout.verificationId)
    }

    @Test
    fun firebaseAuthManager_defaultClientId_isConfigured() {
        assertNotNull(FirebaseAuthManager.DEFAULT_SERVER_CLIENT_ID)
        assertTrue(FirebaseAuthManager.DEFAULT_SERVER_CLIENT_ID.contains("909837836308"))
    }
}
