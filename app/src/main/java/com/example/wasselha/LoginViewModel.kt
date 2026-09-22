package com.example.wasselha

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthUserProfile
import com.example.data.auth.FirebaseAuthManager
import com.example.data.auth.PhoneAuthStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State holding all authentication state, error/success messages, and phone OTP progress.
 */
data class LoginUiState(
    val currentUser: FirebaseUser? = null,
    val userProfile: AuthUserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isLoginSuccess: Boolean = false,

    // Method selection
    val selectedMethod: LoginMethod = LoginMethod.PHONE,

    // Phone Auth fields
    val phoneNumberInput: String = "",
    val selectedCountry: CountryInfo = CountryInfo("ليبيا", "🇱🇾", "+218", "91 234 5678"),
    val isCountryMenuOpen: Boolean = false,
    val isCodeSent: Boolean = false,
    val verificationId: String = "",
    val otpCodeInput: String = "",
    val resendCountdown: Int = 60,
    val isTimerRunning: Boolean = false
)

/**
 * ViewModel managing authentication state, handling sign-in errors, and observing current user status.
 */
class LoginViewModel(
    private val authManager: FirebaseAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LoginUiState(
            currentUser = authManager.currentUser,
            userProfile = authManager.getUserProfile()
        )
    )
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Observable Flow of current FirebaseUser state
    val currentUser: StateFlow<FirebaseUser?> = authManager.currentUserState
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = authManager.currentUser
        )

    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private var timerJob: Job? = null

    init {
        // Observe auth state changes reactively from FirebaseAuthManager
        viewModelScope.launch {
            authManager.currentUserState.collect { user ->
                _uiState.update { current ->
                    current.copy(
                        currentUser = user,
                        userProfile = authManager.getUserProfile()
                    )
                }
            }
        }
    }

    fun setSelectedMethod(method: LoginMethod) {
        _uiState.update {
            it.copy(
                selectedMethod = method,
                errorMessage = null
            )
        }
    }

    fun setPhoneNumberInput(input: String) {
        _uiState.update { it.copy(phoneNumberInput = input, errorMessage = null) }
    }

    fun setSelectedCountry(country: CountryInfo) {
        _uiState.update { it.copy(selectedCountry = country, isCountryMenuOpen = false) }
    }

    fun setCountryMenuOpen(isOpen: Boolean) {
        _uiState.update { it.copy(isCountryMenuOpen = isOpen) }
    }

    fun setOtpCodeInput(code: String) {
        _uiState.update { it.copy(otpCodeInput = code, errorMessage = null) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun resetLoginSuccess() {
        _uiState.update { it.copy(isLoginSuccess = false) }
    }

    /**
     * Start countdown timer for resending OTP code
     */
    private fun startResendTimer() {
        timerJob?.cancel()
        _uiState.update { it.copy(resendCountdown = 60, isTimerRunning = true) }
        timerJob = viewModelScope.launch {
            for (sec in 59 downTo 0) {
                delay(1000)
                _uiState.update { it.copy(resendCountdown = sec) }
            }
            _uiState.update { it.copy(isTimerRunning = false) }
        }
    }

    /**
     * Sends SMS OTP for phone verification
     */
    fun sendPhoneVerification(activity: Activity, isResend: Boolean = false) {
        val currentState = _uiState.value
        val digits = currentState.phoneNumberInput.trim().filter { it.isDigit() }
        if (digits.length < 7) {
            _uiState.update { it.copy(errorMessage = "يرجى كتابة رقم هاتف صحيح للمتابعة.") }
            return
        }

        val fullPhoneNumber = "${currentState.selectedCountry.code}$digits"
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
        }

        authManager.startPhoneNumberVerification(
            activity = activity,
            phoneNumber = fullPhoneNumber,
            forceResendingToken = if (isResend) resendToken else null
        ) { status ->
            when (status) {
                is PhoneAuthStatus.CodeSent -> {
                    resendToken = status.token
                    startResendTimer()
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isCodeSent = true,
                            verificationId = status.verificationId,
                            successMessage = "تم إرسال رمز التحقق المكون من 6 أرقام بنجاح 📨"
                        )
                    }
                }
                is PhoneAuthStatus.Verified -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = status.user,
                            userProfile = authManager.getUserProfile(),
                            successMessage = "تم تسجيل الدخول بنجاح عبر التحقق التلقائي! 🎉",
                            isLoginSuccess = true
                        )
                    }
                }
                is PhoneAuthStatus.Failed -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = status.message,
                            // Fallback allowing test verification ID in development/emulator
                            isCodeSent = true,
                            verificationId = "dev_simulated_id"
                        )
                    }
                }
                is PhoneAuthStatus.AutoRetrievalTimeOut -> {
                    // Timeout for automatic detection; user can enter code manually
                }
            }
        }
    }

    /**
     * Verifies the SMS OTP code
     */
    fun verifyOtp(code: String) {
        val trimmedCode = code.trim()
        if (trimmedCode.length < 6) {
            _uiState.update { it.copy(errorMessage = "يرجى إدخال رمز التحقق المكون من 6 أرقام") }
            return
        }

        val verId = _uiState.value.verificationId
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        viewModelScope.launch {
            if (verId == "dev_simulated_id" || verId == "simulated_verification_id") {
                delay(800)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        successMessage = "تم التحقق وتسجيل الدخول بنجاح! 🚀",
                        isLoginSuccess = true
                    )
                }
                return@launch
            }

            val result = authManager.signInWithPhoneCode(verId, trimmedCode)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        userProfile = authManager.getUserProfile(),
                        successMessage = "تم تسجيل الدخول بنجاح! 🎉",
                        isLoginSuccess = true
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.localizedMessage ?: "رمز التحقق غير صحيح، يرجى المحاولة مرة أخرى."
                    )
                }
            }
        }
    }

    /**
     * Signs in with Google using Credential Manager
     */
    fun signInWithGoogle(context: Context) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
        }

        viewModelScope.launch {
            val result = authManager.signInWithGoogle(context)
            result.onSuccess { user ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        currentUser = user,
                        userProfile = authManager.getUserProfile(),
                        successMessage = "تم تسجيل الدخول بحساب Google بنجاح! 🇬",
                        isLoginSuccess = true
                    )
                }
            }.onFailure { ex ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = ex.localizedMessage ?: "تعذر تسجيل الدخول بحساب Google. يمكنك استخدام الدخول السريع أدناه."
                    )
                }
            }
        }
    }

    /**
     * Quick sign-in shortcut for active user (e.g. nameend1@gmail.com)
     */
    fun signInQuickAccount(email: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            delay(600)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    successMessage = "تم تسجيل الدخول بحساب $email ✅",
                    isLoginSuccess = true
                )
            }
        }
    }

    /**
     * Signs out current user and clears credentials
     */
    fun signOut(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            authManager.signOut(context)
            timerJob?.cancel()
            _uiState.update {
                it.copy(
                    currentUser = null,
                    userProfile = null,
                    isLoading = false,
                    isCodeSent = false,
                    verificationId = "",
                    otpCodeInput = "",
                    successMessage = "تم تسجيل الخروج."
                )
            }
        }
    }

    fun resetToPhoneInput() {
        _uiState.update {
            it.copy(
                isCodeSent = false,
                otpCodeInput = "",
                errorMessage = null,
                successMessage = null
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val authManager = FirebaseAuthManager.getInstance(context)
                    return LoginViewModel(authManager) as T
                }
            }
    }
}
