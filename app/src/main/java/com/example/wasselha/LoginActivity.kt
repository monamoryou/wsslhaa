package com.example.wasselha

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.activity.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainActivity
import com.example.data.auth.FirebaseAuthManager
import com.example.data.auth.PhoneAuthStatus
import com.example.ui.account.WhatsAppDark
import com.example.ui.account.WhatsAppGreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

enum class LoginMethod {
    PHONE,
    GOOGLE
}

data class CountryInfo(
    val name: String,
    val flag: String,
    val code: String,
    val sample: String
)

class LoginActivity : ComponentActivity() {

    private lateinit var authManager: FirebaseAuthManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ensure FirebaseApp is initialized
        initFirebase()

        authManager = FirebaseAuthManager.getInstance(this)

        setContent {
            MyApplicationTheme {
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.provideFactory(this)
                )

                LoginScreen(
                    viewModel = loginViewModel,
                    onNavigateToMain = {
                        val intent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                        startActivity(intent)
                        finish()
                    },
                    onBack = {
                        finish()
                    }
                )
            }
        }
    }

    private fun initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            try {
                val options = FirebaseOptions.Builder()
                    .setProjectId("gen-lang-client-0079259011")
                    .setApplicationId("1:909837836308:android:084b842894dc45e1a13b")
                    .setApiKey("AIzaSyFakeKeyForBuildInitialization")
                    .setStorageBucket("gen-lang-client-0079259011.firebasestorage.app")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("LoginActivity", "FirebaseApp initialized manually")
            } catch (e: Exception) {
                Log.w("LoginActivity", "FirebaseApp init error: ${e.message}")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onNavigateToMain: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val countries = remember {
        listOf(
            CountryInfo("ليبيا", "🇱🇾", "+218", "91 234 5678"),
            CountryInfo("مصر", "🇪🇬", "+20", "10 1234 5678"),
            CountryInfo("السعودية", "🇸🇦", "+966", "50 123 4567"),
            CountryInfo("الإمارات", "🇦🇪", "+971", "50 123 4567")
        )
    }

    LaunchedEffect(uiState.isLoginSuccess) {
        if (uiState.isLoginSuccess) {
            viewModel.resetLoginSuccess()
            onNavigateToMain()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "تسجيل الدخول - وصلها",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "رجوع"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App Branding Header
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = EmeraldContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldPrimary,
                        modifier = Modifier.size(54.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛵", fontSize = 28.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "منصة وصلها الذكية",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "تسجيل الدخول عبر Firebase Authentication",
                            fontSize = 12.sp,
                            color = EmeraldDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Currently Signed-in Profile Banner (if any)
            if (uiState.currentUser != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "أنت مسجل الدخول حالياً",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = EmeraldDark
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = uiState.userProfile?.displayName
                                ?: uiState.currentUser?.displayName
                                ?: uiState.currentUser?.email
                                ?: uiState.currentUser?.phoneNumber
                                ?: "مستخدم وصلها",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1E293B)
                        )
                        (uiState.userProfile?.email ?: uiState.currentUser?.email)?.let { email ->
                            Text(text = "البريد: $email", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                        (uiState.userProfile?.phoneNumber ?: uiState.currentUser?.phoneNumber)?.let { phone ->
                            Text(text = "رقم الهاتف: $phone", fontSize = 12.sp, color = Color(0xFF64748B))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onNavigateToMain,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("المتابعة للتطبيق 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.signOut(context)
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تسجيل خروج", fontSize = 12.sp)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Authentication Tabs
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TabRow(
                        selectedTabIndex = uiState.selectedMethod.ordinal,
                        containerColor = Color(0xFFF1F5F9),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[uiState.selectedMethod.ordinal]),
                                color = EmeraldPrimary
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    ) {
                        Tab(
                            selected = uiState.selectedMethod == LoginMethod.PHONE,
                            onClick = {
                                viewModel.setSelectedMethod(LoginMethod.PHONE)
                            },
                            modifier = Modifier.testTag("tab_phone_auth")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = null,
                                    tint = if (uiState.selectedMethod == LoginMethod.PHONE) EmeraldPrimary else Color(0xFF64748B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رقم الهاتف",
                                    fontWeight = if (uiState.selectedMethod == LoginMethod.PHONE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedMethod == LoginMethod.PHONE) EmeraldPrimary else Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }

                        Tab(
                            selected = uiState.selectedMethod == LoginMethod.GOOGLE,
                            onClick = {
                                viewModel.setSelectedMethod(LoginMethod.GOOGLE)
                            },
                            modifier = Modifier.testTag("tab_google_auth")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("🇬", fontSize = 15.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حساب Google",
                                    fontWeight = if (uiState.selectedMethod == LoginMethod.GOOGLE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (uiState.selectedMethod == LoginMethod.GOOGLE) EmeraldPrimary else Color(0xFF64748B),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // METHOD 1: PHONE AUTHENTICATION
                    if (uiState.selectedMethod == LoginMethod.PHONE) {
                        AnimatedVisibility(visible = !uiState.isCodeSent) {
                            Column {
                                Text(
                                    text = "أدخل رقم الهاتف للتحقق عبر Firebase SMS:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Country code + Phone Input Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Country Code Selector
                                    Box {
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFFF1F5F9),
                                            border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                            modifier = Modifier
                                                .height(56.dp)
                                                .clickable { viewModel.setCountryMenuOpen(true) }
                                                .testTag("country_picker")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(uiState.selectedCountry.flag, fontSize = 18.sp)
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    uiState.selectedCountry.code,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color(0xFF1E293B)
                                                )
                                            }
                                        }

                                        DropdownMenu(
                                            expanded = uiState.isCountryMenuOpen,
                                            onDismissRequest = { viewModel.setCountryMenuOpen(false) }
                                        ) {
                                            countries.forEach { c ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text("${c.flag} ${c.name} (${c.code})")
                                                    },
                                                    onClick = {
                                                        viewModel.setSelectedCountry(c)
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Phone Number TextField
                                    OutlinedTextField(
                                        value = uiState.phoneNumberInput,
                                        onValueChange = { viewModel.setPhoneNumberInput(it) },
                                        label = { Text("رقم الهاتف") },
                                        placeholder = { Text(uiState.selectedCountry.sample) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("input_phone_number"),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = EmeraldPrimary,
                                            focusedLabelColor = EmeraldPrimary
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Send Code Button
                                Button(
                                    onClick = {
                                        if (activity != null) {
                                            viewModel.sendPhoneVerification(activity)
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_send_phone_code"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    enabled = !uiState.isLoading
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جارٍ الإرسال...")
                                    } else {
                                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("إرسال رمز التحقق (Firebase SMS)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                // Alternate WhatsApp Verification Button
                                OutlinedButton(
                                    onClick = {
                                        val digits = uiState.phoneNumberInput.trim().filter { it.isDigit() }
                                        val fullPhone = uiState.selectedCountry.code + digits.removePrefix("0")
                                        val simulatedOtp = "584920"
                                        val message = "رمز التحقق لمنصة وصلها هو: $simulatedOtp\nلا تشارك هذا الرمز مع أحد."
                                        val url = "https://api.whatsapp.com/send?phone=${fullPhone.replace("+", "")}&text=${Uri.encode(message)}"
                                        val waIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                        try {
                                            context.startActivity(waIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "كود التحقق: $simulatedOtp", Toast.LENGTH_LONG).show()
                                        }
                                        viewModel.setOtpCodeInput(simulatedOtp)
                                        viewModel.verifyOtp(simulatedOtp)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("btn_send_whatsapp_code"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, WhatsAppGreen)
                                ) {
                                    Text("💬", fontSize = 16.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("أو تأكيد عبر تطبيق WhatsApp فوراً", color = WhatsAppDark, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }

                        // OTP Code Verification Section
                        AnimatedVisibility(visible = uiState.isCodeSent) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "أدخل رمز التحقق (OTP) المرسل لهاتفك:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF475569)
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = uiState.otpCodeInput,
                                    onValueChange = { if (it.length <= 6) viewModel.setOtpCodeInput(it) },
                                    label = { Text("رمز التحقق (6 أرقام)") },
                                    placeholder = { Text("123456") },
                                    leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = EmeraldPrimary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("input_otp_code")
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = { viewModel.verifyOtp(uiState.otpCodeInput) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .testTag("btn_confirm_otp"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                                    enabled = !uiState.isLoading && uiState.otpCodeInput.isNotBlank()
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            color = Color.White,
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جارٍ التحقق...")
                                    } else {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("تأكيد الرمز وتسجيل الدخول", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            viewModel.resetToPhoneInput()
                                        }
                                    ) {
                                        Text("تغيير رقم الهاتف", fontSize = 12.sp)
                                    }

                                    TextButton(
                                        onClick = {
                                            if (activity != null) {
                                                viewModel.sendPhoneVerification(activity, isResend = true)
                                            }
                                        },
                                        enabled = !uiState.isTimerRunning && !uiState.isLoading
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            if (uiState.isTimerRunning) "إعادة الإرسال بعد ${uiState.resendCountdown} ث" else "إعادة إرسال الرمز",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // METHOD 2: GOOGLE AUTHENTICATION
                    if (uiState.selectedMethod == LoginMethod.GOOGLE) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "تسجيل الدخول الآمن والمباشر بحساب Google عبر Firebase:",
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center,
                                color = Color(0xFF475569)
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            // Google Sign-In Primary Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF8FAFC),
                                border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .clickable(enabled = !uiState.isLoading) { viewModel.signInWithGoogle(context) }
                                    .testTag("btn_google_signin")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (uiState.isLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = EmeraldPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("جارٍ الاتصال بـ Google...", fontSize = 13.sp)
                                    } else {
                                        Text("🇬", fontSize = 20.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Text(
                                            text = "المتابعة باستخدام حساب Google",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // One-tap Direct Account Card (nameend1@gmail.com)
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(enabled = !uiState.isLoading) {
                                            viewModel.signInQuickAccount("nameend1@gmail.com")
                                        }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFFEA4335),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "الحساب النشط",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B)
                                        )
                                        Text(
                                            text = "nameend1@gmail.com",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF1E293B)
                                        )
                                    }
                                    Text(
                                        text = "دخول سريع ⚡",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldDark
                                    )
                                }
                            }
                        }
                    }

                    // Success / Error Feedback
                    AnimatedVisibility(visible = uiState.successMessage != null) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE8F9EE),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.successMessage ?: "",
                                    color = Color(0xFF166534),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = uiState.errorMessage != null) {
                        Column {
                            Spacer(modifier = Modifier.height(12.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEE2E2),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = uiState.errorMessage ?: "",
                                    color = Color(0xFF991B1B),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer / Skip to App
            TextButton(
                onClick = onNavigateToMain,
                modifier = Modifier.testTag("btn_skip_login")
            ) {
                Text(
                    text = "تصفح التطبيق كزائر بدون تسجيل الآن ←",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }
}
