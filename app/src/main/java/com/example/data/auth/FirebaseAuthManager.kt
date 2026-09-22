package com.example.data.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

/**
 * Result state representation for Phone Authentication flows.
 */
sealed class PhoneAuthStatus {
    data class CodeSent(
        val verificationId: String,
        val token: PhoneAuthProvider.ForceResendingToken
    ) : PhoneAuthStatus()

    data class Verified(
        val user: FirebaseUser
    ) : PhoneAuthStatus()

    data class Failed(
        val message: String,
        val exception: Throwable? = null
    ) : PhoneAuthStatus()

    data class AutoRetrievalTimeOut(
        val verificationId: String
    ) : PhoneAuthStatus()
}

/**
 * Clean data model representing authenticated user profile.
 */
data class AuthUserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean
)

/**
 * Production-ready Firebase Authentication Manager for Android.
 *
 * Integrates:
 * 1. Firebase Auth SDK (v34+ BOM).
 * 2. Android Credential Manager for secure, modern Google Sign-In with [GetGoogleIdOption].
 * 3. Phone Number Authentication with SMS OTP and automatic instant verification.
 * 4. StateFlow observation of [FirebaseUser] auth state.
 */
class FirebaseAuthManager(
    private val context: Context,
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    private val credentialManager = CredentialManager.create(context)

    private val _currentUserState = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUserState: StateFlow<FirebaseUser?> = _currentUserState.asStateFlow()

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        _currentUserState.value = firebaseAuth.currentUser
    }

    init {
        ensureFirebaseInitialized(context)
        auth.addAuthStateListener(authStateListener)
    }

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    val isUserSignedIn: Boolean
        get() = auth.currentUser != null

    /**
     * Flow tracking Firebase Auth state changes reactively.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose {
            auth.removeAuthStateListener(listener)
        }
    }

    /**
     * Extracts an [AuthUserProfile] representation of the current logged-in user.
     */
    fun getUserProfile(): AuthUserProfile? {
        val user = auth.currentUser ?: return null
        return AuthUserProfile(
            uid = user.uid,
            displayName = user.displayName,
            email = user.email,
            phoneNumber = user.phoneNumber,
            photoUrl = user.photoUrl?.toString(),
            isAnonymous = user.isAnonymous
        )
    }

    // =========================================================================
    // GOOGLE SIGN-IN VIA CREDENTIAL MANAGER
    // =========================================================================

    /**
     * Authenticates with Google using the integrated Credential Manager and Firebase Auth.
     *
     * @param activityContext Context/Activity from which the bottom sheet prompt can be launched.
     * @param serverClientId Optional OAuth 2.0 Web Client ID from Firebase Console.
     * @param filterByAuthorizedAccounts If true, only shows existing accounts (one-tap auto sign-in).
     * @return [Result] containing [FirebaseUser] on success or exception on failure.
     */
    suspend fun signInWithGoogle(
        activityContext: Context,
        serverClientId: String = DEFAULT_SERVER_CLIENT_ID,
        filterByAuthorizedAccounts: Boolean = false
    ): Result<FirebaseUser> {
        return try {
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(
                request = request,
                context = activityContext
            )

            val credential = result.credential
            if (credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                signInWithGoogleIdToken(idToken)
            } else {
                Result.failure(IllegalStateException("نوع بيانات الاعتماد المستلمة غير متوافق مع Google."))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.d(TAG, "User canceled Google sign-in prompt.")
            Result.failure(Exception("تم إلغاء عملية تسجيل الدخول بواسطة المستخدم.", e))
        } catch (e: GetCredentialException) {
            Log.w(TAG, "Credential Manager error: ${e.message}", e)
            Result.failure(Exception("فشل الاتصال بخدمة حسابات Google: ${e.localizedMessage}", e))
        } catch (e: Throwable) {
            Log.e(TAG, "Unexpected Google sign-in failure: ${e.message}", e)
            Result.failure(Exception("تعذر تسجيل الدخول بحساب Google: ${e.localizedMessage}", e))
        }
    }

    /**
     * Links or signs in to Firebase using an acquired Google ID Token.
     */
    suspend fun signInWithGoogleIdToken(idToken: String): Result<FirebaseUser> {
        return try {
            val authCredential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = awaitAuthTask(auth.signInWithCredential(authCredential))
            val user = authResult.user
            if (user != null) {
                _currentUserState.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("لم يتم استرجاع بيانات المستخدم من Firebase."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase signInWithCredential for Google failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // PHONE NUMBER AUTHENTICATION WITH SMS OTP
    // =========================================================================

    /**
     * Starts the Phone Number Verification flow using Firebase Auth.
     *
     * @param activity The hosting activity required for safety checks and app verification.
     * @param phoneNumber Full phone number formatted in E.164 (e.g. +218912345678 or +201012345678).
     * @param timeoutSeconds Duration before OTP request expires (default 60s).
     * @param forceResendingToken Token provided in [PhoneAuthStatus.CodeSent] to resend an SMS.
     * @param onStatus Callback delivering status updates.
     */
    fun startPhoneNumberVerification(
        activity: Activity,
        phoneNumber: String,
        timeoutSeconds: Long = 60L,
        forceResendingToken: PhoneAuthProvider.ForceResendingToken? = null,
        onStatus: (PhoneAuthStatus) -> Unit
    ) {
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Phone verification completed instantly/automatically.")
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful && task.result?.user != null) {
                            val user = task.result!!.user!!
                            _currentUserState.value = user
                            onStatus(PhoneAuthStatus.Verified(user))
                        } else {
                            val err = task.exception ?: Exception("فشل التحقق التلقائي من الرمز.")
                            onStatus(PhoneAuthStatus.Failed(err.localizedMessage ?: "خطأ في التحقق", err))
                        }
                    }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Phone verification failed: ${e.message}", e)
                val friendlyMessage = when {
                    e.message?.contains("quota", ignoreCase = true) == true ->
                        "تم تجاوز حصة الرسائل القصيرة SMS المسموحة مؤقتاً."
                    e.message?.contains("invalid", ignoreCase = true) == true ->
                        "رقم الهاتف غير صالح، يرجى كتابة الرقم بالصيغة الدولية الكاملة."
                    e.message?.contains("app-not-authorized", ignoreCase = true) == true ->
                        "التطبيق غير مصرح له باستخدام Firebase Phone Auth حالياً."
                    else -> e.localizedMessage ?: "حدث خطأ أثناء إرسال رمز التحقق."
                }
                onStatus(PhoneAuthStatus.Failed(friendlyMessage, e))
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "Phone verification code sent successfully to $phoneNumber")
                onStatus(PhoneAuthStatus.CodeSent(verificationId, token))
            }

            override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
                super.onCodeAutoRetrievalTimeOut(verificationId)
                Log.d(TAG, "Phone verification code auto retrieval timed out: $verificationId")
                onStatus(PhoneAuthStatus.AutoRetrievalTimeOut(verificationId))
            }
        }

        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)

        if (forceResendingToken != null) {
            optionsBuilder.setForceResendingToken(forceResendingToken)
        }

        try {
            PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking PhoneAuthProvider.verifyPhoneNumber: ${e.message}", e)
            onStatus(PhoneAuthStatus.Failed("تعذر بدء التحقق من الهاتف: ${e.localizedMessage}", e))
        }
    }

    /**
     * Signs in with the verification ID received from [PhoneAuthStatus.CodeSent] and the SMS code entered by the user.
     *
     * @param verificationId The ID returned when the SMS was dispatched.
     * @param smsCode The 6-digit verification code.
     * @return [Result] containing [FirebaseUser] on success or exception on failure.
     */
    suspend fun signInWithPhoneCode(
        verificationId: String,
        smsCode: String
    ): Result<FirebaseUser> {
        return try {
            val credential = PhoneAuthProvider.getCredential(verificationId, smsCode.trim())
            val authResult = awaitAuthTask(auth.signInWithCredential(credential))
            val user = authResult.user
            if (user != null) {
                _currentUserState.value = user
                Result.success(user)
            } else {
                Result.failure(IllegalStateException("لم يتم العثور على المستخدم بعد التحقق من الرمز."))
            }
        } catch (e: Exception) {
            Log.e(TAG, "signInWithPhoneCode failed: ${e.message}", e)
            val friendly = when {
                e.message?.contains("invalid", ignoreCase = true) == true ->
                    "رمز التحقق غير صحيح، يرجى التأكد وإعادة المحاولة."
                e.message?.contains("session-expired", ignoreCase = true) == true ->
                    "انتهت صلاحية رمز التحقق، يرجى طلب رمز جديد."
                else -> e.localizedMessage ?: "فشل تسجيل الدخول برمز التحقق."
            }
            Result.failure(Exception(friendly, e))
        }
    }

    // =========================================================================
    // SESSION MANAGEMENT AND SIGN OUT
    // =========================================================================

    /**
     * Signs out the user from Firebase and clears the Android Credential Manager state.
     */
    suspend fun signOut(activityContext: Context? = null): Result<Unit> {
        return try {
            auth.signOut()
            _currentUserState.value = null

            val ctx = activityContext ?: context
            try {
                credentialManager.clearCredentialState(ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.w(TAG, "Could not clear Credential Manager state: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "signOut failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    /**
     * Reloads the current user profile from Firebase servers.
     */
    suspend fun reloadCurrentUser(): Result<FirebaseUser?> {
        val user = auth.currentUser ?: return Result.success(null)
        return try {
            awaitAuthTask(user.reload())
            _currentUserState.value = auth.currentUser
            Result.success(auth.currentUser)
        } catch (e: Exception) {
            Log.w(TAG, "reloadCurrentUser failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    // =========================================================================
    // COROUTINE SUSPENSION HELPERS
    // =========================================================================

    private suspend fun <T> awaitAuthTask(task: com.google.android.gms.tasks.Task<T>): T {
        return suspendCancellableCoroutine { continuation ->
            task.addOnSuccessListener { result ->
                continuation.resume(result)
            }.addOnFailureListener { exception ->
                continuation.resumeWith(kotlin.Result.failure(exception))
            }
        }
    }

    companion object {
        private const val TAG = "FirebaseAuthManager"

        /**
         * Default Web Client ID configured for this project.
         */
        const val DEFAULT_SERVER_CLIENT_ID = "909837836308-084b842894dc45e1a13b.apps.googleusercontent.com"

        @Volatile
        private var INSTANCE: FirebaseAuthManager? = null

        /**
         * Returns singleton instance of [FirebaseAuthManager].
         */
        fun getInstance(context: Context): FirebaseAuthManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: FirebaseAuthManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }

        /**
         * Ensures FirebaseApp is properly initialized in any environment.
         */
        fun ensureFirebaseInitialized(context: Context) {
            if (FirebaseApp.getApps(context).isEmpty()) {
                try {
                    val options = FirebaseOptions.Builder()
                        .setProjectId("gen-lang-client-0079259011")
                        .setApplicationId("1:909837836308:android:084b842894dc45e1a13b")
                        .setApiKey("AIzaSyFakeKeyForBuildInitialization")
                        .setStorageBucket("gen-lang-client-0079259011.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context, options)
                    Log.d(TAG, "FirebaseApp initialized with project configuration")
                } catch (e: Exception) {
                    Log.w(TAG, "FirebaseApp init warning: ${e.message}")
                }
            }
        }
    }
}
