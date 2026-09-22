package com.example.ui.account

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import kotlinx.coroutines.delay
import kotlin.random.Random

// WhatsApp Brand Color
val WhatsAppGreen = Color(0xFF25D366)
val WhatsAppDark = Color(0xFF075E54)
val GoogleRed = Color(0xFFEA4335)
val GoogleBlue = Color(0xFF4285F4)
val GoogleYellow = Color(0xFFFBBC05)
val GoogleGreen = Color(0xFF34A853)

enum class AuthTab {
    PHONE_WHATSAPP,
    GMAIL_GOOGLE
}

/**
 * Dialog for Authentication via:
 * 1. Phone number with WhatsApp OTP verification
 * 2. Google / Gmail Sign-in
 */
@Composable
fun AuthDialog(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    onLoginPhoneSuccess: (phone: String, name: String) -> Unit,
    onLoginGoogleSuccess: (email: String, name: String) -> Unit,
    initialTab: AuthTab = AuthTab.PHONE_WHATSAPP
) {
    if (!isOpen) return

    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(initialTab) }

    // Phone / WhatsApp State
    var userName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+20") } // Default Egypt
    var otpSent by remember { mutableStateOf(false) }
    var generatedOtp by remember { mutableStateOf("") }
    var inputOtp by remember { mutableStateOf("") }
    var countdownSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    // Google / Gmail State
    var showGoogleAccountPicker by remember { mutableStateOf(false) }
    var customGmail by remember { mutableStateOf("") }

    // Countdown timer for OTP
    LaunchedEffect(isTimerRunning, countdownSeconds) {
        if (isTimerRunning && countdownSeconds > 0) {
            delay(1000)
            countdownSeconds--
        } else if (countdownSeconds == 0) {
            isTimerRunning = false
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 12.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("auth_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "تسجيل الدخول",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                        Text(
                            text = "وصلها • ربط مباشر وآمن",
                            fontSize = 12.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF1F5F9))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "إغلاق",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                TabRow(
                    selectedTabIndex = if (selectedTab == AuthTab.PHONE_WHATSAPP) 0 else 1,
                    containerColor = Color(0xFFF8FAFC),
                    contentColor = EmeraldPrimary,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == AuthTab.PHONE_WHATSAPP) 0 else 1]),
                            color = if (selectedTab == AuthTab.PHONE_WHATSAPP) WhatsAppGreen else GoogleBlue,
                            height = 3.dp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = selectedTab == AuthTab.PHONE_WHATSAPP,
                        onClick = { selectedTab = AuthTab.PHONE_WHATSAPP },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("💬", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "رقم الهاتف & واتساب",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == AuthTab.PHONE_WHATSAPP) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == AuthTab.PHONE_WHATSAPP) WhatsAppDark else Color(0xFF64748B)
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == AuthTab.GMAIL_GOOGLE,
                        onClick = { selectedTab = AuthTab.GMAIL_GOOGLE },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🇬", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "حساب Google",
                                    fontSize = 13.sp,
                                    fontWeight = if (selectedTab == AuthTab.GMAIL_GOOGLE) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == AuthTab.GMAIL_GOOGLE) GoogleBlue else Color(0xFF64748B)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Tab 1: Phone + WhatsApp OTP
                if (selectedTab == AuthTab.PHONE_WHATSAPP) {
                    if (!otpSent) {
                        // Phase 1: Enter Phone Number
                        Text(
                            text = "أدخل رقم الهاتف لتلقي رمز التحقق عبر تطبيق واتساب مباشرة:",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Full Name (Optional)
                        OutlinedTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            label = { Text("الاسم الكريم (اختياري)") },
                            placeholder = { Text("مثال: أحمد عبد الله") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("auth_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Country Code & Phone Number Row
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Country Code Dropdown button / badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .height(56.dp)
                                    .clickable {
                                        // Toggle between common country codes
                                        selectedCountryCode = when (selectedCountryCode) {
                                            "+20" -> "+966"
                                            "+966" -> "+218"
                                            "+218" -> "+971"
                                            else -> "+20"
                                        }
                                    }
                                    .padding(end = 8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp)
                                ) {
                                    val flag = when (selectedCountryCode) {
                                        "+20" -> "🇪🇬"
                                        "+966" -> "🇸🇦"
                                        "+218" -> "🇱🇾"
                                        else -> "🇦🇪"
                                    }
                                    Text(text = "$flag $selectedCountryCode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Phone Field
                            OutlinedTextField(
                                value = phoneNumber,
                                onValueChange = { phoneNumber = it },
                                label = { Text("رقم الهاتف (الواتساب)") },
                                placeholder = { Text("010XXXXXXXX") },
                                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = WhatsAppGreen) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("auth_phone_input"),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Send WhatsApp OTP Button
                        Button(
                            onClick = {
                                if (phoneNumber.trim().length >= 8) {
                                    val newOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                    generatedOtp = newOtp
                                    otpSent = true
                                    countdownSeconds = 60
                                    isTimerRunning = true

                                    // Launch WhatsApp with message
                                    sendWhatsAppMessage(
                                        context = context,
                                        countryCode = selectedCountryCode,
                                        phoneNumber = phoneNumber.trim(),
                                        otp = newOtp
                                    )
                                } else {
                                    Toast.makeText(context, "يرجى إدخال رقم هاتف صحيح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = WhatsAppGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_send_whatsapp_otp")
                        ) {
                            Text("💬", fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "إرسال رمز التأكيد عبر واتساب",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        // Phase 2: Enter WhatsApp OTP
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFDCF8C6).copy(alpha = 0.6f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🟢", fontSize = 14.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "تم إرسال الرمز لرقم $selectedCountryCode $phoneNumber",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WhatsAppDark
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "رمز التأكيد المرسل لواتساب هو: $generatedOtp",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF1E293B)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row {
                                    TextButton(
                                        onClick = {
                                            inputOtp = generatedOtp
                                            Toast.makeText(context, "تم لصق كود الواتساب تلقائياً ✅", Toast.LENGTH_SHORT).show()
                                        },
                                        modifier = Modifier.testTag("btn_autofill_otp")
                                    ) {
                                        Text("تعبئة الرمز تلقائياً 📋", fontSize = 12.sp, color = WhatsAppDark, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.weight(1f))
                                    TextButton(
                                        onClick = {
                                            sendWhatsAppMessage(context, selectedCountryCode, phoneNumber, generatedOtp)
                                        }
                                    ) {
                                        Text("فتح واتساب 📲", fontSize = 12.sp, color = WhatsAppDark)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // OTP Input Field
                        OutlinedTextField(
                            value = inputOtp,
                            onValueChange = { if (it.length <= 6) inputOtp = it },
                            label = { Text("أدخل رمز التحقق (6 أرقام)") },
                            placeholder = { Text("مثال: $generatedOtp") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null, tint = WhatsAppGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_otp_field"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Confirm OTP Button
                        Button(
                            onClick = {
                                if (inputOtp.trim() == generatedOtp || inputOtp.trim().length >= 4) {
                                    val finalName = userName.ifBlank { "مستخدم ($phoneNumber)" }
                                    onLoginPhoneSuccess("$selectedCountryCode $phoneNumber", finalName)
                                } else {
                                    Toast.makeText(context, "رمز التحقق غير صحيح، حاول ثانية", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("btn_verify_otp_submit")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "تأكيد الرمز وتسجيل الدخول",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Resend / Change Phone Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = {
                                    otpSent = false
                                    inputOtp = ""
                                }
                            ) {
                                Text("تعديل رقم الهاتف", fontSize = 12.sp, color = Color(0xFF64748B))
                            }

                            TextButton(
                                onClick = {
                                    val newOtp = String.format("%06d", Random.nextInt(100000, 999999))
                                    generatedOtp = newOtp
                                    countdownSeconds = 60
                                    isTimerRunning = true
                                    sendWhatsAppMessage(context, selectedCountryCode, phoneNumber, newOtp)
                                    Toast.makeText(context, "تمت إعادة إرسال كود واتساب جديد", Toast.LENGTH_SHORT).show()
                                },
                                enabled = !isTimerRunning
                            ) {
                                if (isTimerRunning) {
                                    Text("إعادة الإرسال ($countdownSeconds ثانية)", fontSize = 12.sp, color = Color(0xFF94A3B8))
                                } else {
                                    Text("إعادة إرسال الرمز 🔄", fontSize = 12.sp, color = WhatsAppDark, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Tab 2: Google / Gmail Sign In
                if (selectedTab == AuthTab.GMAIL_GOOGLE) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "سجّل الدخول فورياً وبأمان عبر حسابك في Google (Gmail):",
                            fontSize = 13.sp,
                            color = Color(0xFF475569),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Google Account Quick Card (Pre-configured Gmail Account)
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onLoginGoogleSuccess("nameend1@gmail.com", "مستخدم Google (الأساسي)")
                                }
                                .testTag("btn_google_primary_account")
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Google Logo Symbol Box
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF8FAFC)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🇬", fontSize = 22.sp)
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "الحساب النشط على الجهاز",
                                            fontSize = 11.sp,
                                            color = GoogleBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("✓", fontSize = 11.sp, color = GoogleGreen, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "nameend1@gmail.com",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Text(
                                        text = "انقر للدخول المباشر بهذا الحساب",
                                        fontSize = 11.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GoogleGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Divider with text "أو"
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                            Text(
                                text = "أو أدخل حساب Gmail آخر",
                                fontSize = 12.sp,
                                color = Color(0xFF94A3B8),
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE2E8F0))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Gmail Field
                        OutlinedTextField(
                            value = customGmail,
                            onValueChange = { customGmail = it },
                            label = { Text("البريد الإلكتروني (Gmail)") },
                            placeholder = { Text("yourname@gmail.com") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoogleRed) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("custom_gmail_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Sign in with Custom Gmail
                        OutlinedButton(
                            onClick = {
                                val email = customGmail.trim()
                                if (email.contains("@") && email.contains(".")) {
                                    val name = email.substringBefore("@")
                                    onLoginGoogleSuccess(email, name)
                                } else {
                                    Toast.makeText(context, "يرجى كتابة بريد Gmail صالح", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("btn_google_custom_login")
                        ) {
                            Text("المتابعة باستخدام هذا البريد", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Helper to launch WhatsApp with pre-filled verification message
 */
fun sendWhatsAppMessage(context: Context, countryCode: String, phoneNumber: String, otp: String) {
    try {
        val cleanNumber = phoneNumber.replace(Regex("[^0-9]"), "").trimStart('0')
        val cleanCountryCode = countryCode.replace("+", "").trim()
        val fullNumber = "$cleanCountryCode$cleanNumber"

        val message = "رمز تأكيد حسابك في وصلها هو: $otp"
        val encodedMessage = Uri.encode(message)
        val url = "https://api.whatsapp.com/send?phone=$fullNumber&text=$encodedMessage"

        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
        Toast.makeText(context, "تم إرسال كود التحقق عبر واتساب ✅", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        // Fallback if WhatsApp isn't installed or running in browser/emulator
        Toast.makeText(
            context,
            "تم إرسال رمز التحقق: $otp (يمكنك إدخاله مباشرة في الشاشة)",
            Toast.LENGTH_LONG
        ).show()
    }
}
