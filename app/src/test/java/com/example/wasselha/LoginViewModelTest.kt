package com.example.wasselha

import com.example.data.auth.AuthUserProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class LoginViewModelTest {

    @Test
    fun loginUiState_defaultValues_areCorrect() {
        val state = LoginUiState()
        assertEquals(LoginMethod.PHONE, state.selectedMethod)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
        assertNull(state.successMessage)
        assertFalse(state.isCodeSent)
        assertEquals("", state.phoneNumberInput)
        assertEquals("", state.otpCodeInput)
        assertEquals(60, state.resendCountdown)
        assertFalse(state.isTimerRunning)
        assertFalse(state.isLoginSuccess)
        assertNull(state.currentUser)
        assertNull(state.userProfile)
        assertEquals("+218", state.selectedCountry.code)
    }

    @Test
    fun loginUiState_copy_updatesStateAccurately() {
        val profile = AuthUserProfile(
            uid = "user_abc",
            displayName = "علي عمر",
            email = "ali@example.com",
            phoneNumber = "+218911112222",
            photoUrl = null,
            isAnonymous = false
        )
        val state = LoginUiState(
            isLoading = true,
            userProfile = profile,
            isLoginSuccess = true
        )

        assertTrue(state.isLoading)
        assertTrue(state.isLoginSuccess)
        assertEquals("user_abc", state.userProfile?.uid)
        assertEquals("علي عمر", state.userProfile?.displayName)
    }

    @Test
    fun loginMethod_enum_containsPhoneAndGoogle() {
        val methods = LoginMethod.values()
        assertEquals(2, methods.size)
        assertTrue(methods.contains(LoginMethod.PHONE))
        assertTrue(methods.contains(LoginMethod.GOOGLE))
    }
}
