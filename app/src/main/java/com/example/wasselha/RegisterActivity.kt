package com.example.wasselha

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.ui.account.AuthDialog
import com.example.ui.account.AuthTab
import com.example.ui.account.WhatsAppDark
import com.example.ui.account.WhatsAppGreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme

/**
 * RegisterActivity receives USER_ROLE from RoleSelectionActivity
 * ("buyer", "seller", or "driver") and completes registration/login.
 */
class RegisterActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val role = intent?.getStringExtra("USER_ROLE") ?: "buyer"

        setContent {
            MyApplicationTheme {
                RegisterScreen(
                    userRole = role,
                    onBack = { finish() },
                    onComplete = {
                        val mainIntent = Intent(this, MainActivity::class.java).apply {
                            flags = Intent.createChooser(Intent(), null).flags or Intent.FLAG_ACTIVITY_CLEAR_TOP
                            putExtra("USER_ROLE", role)
                        }
                        startActivity(mainIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    userRole: String,
    onBack: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var storeOrVehicleName by remember { mutableStateOf("") }
    var addressCity by remember { mutableStateOf("طرابلس") }
    var showAuthDialog by remember { mutableStateOf(false) }
    var selectedAuthTab by remember { mutableStateOf(AuthTab.PHONE_WHATSAPP) }
    val context = LocalContext.current

    val roleTitle = when (userRole) {
        "seller" -> "شريك تاجر (متجر)"
        "driver" -> "كابتن موصلاتي"
        else -> "شاري (تسوق واطلب)"
    }

    val roleBadge = when (userRole) {
        "seller" -> "🏪"
        "driver" -> "🛵"
        else -> "🛍️"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("إنشاء حساب جديد", fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Selected Role Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(roleBadge, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "نوع الحساب المختار",
                            fontSize = 12.sp,
                            color = EmeraldDark
                        )
                        Text(
                            text = roleTitle,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Auth via WhatsApp or Google Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "التسجيل أو الدخول المباشر بنقرة واحدة:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE8F9EE),
                            border = BorderStroke(1.dp, WhatsAppGreen.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    selectedAuthTab = AuthTab.PHONE_WHATSAPP
                                    showAuthDialog = true
                                }
                                .testTag("register_btn_whatsapp")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("💬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("تأكيد بالواتساب", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = WhatsAppDark)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF8FAFC),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .clickable {
                                    selectedAuthTab = AuthTab.GMAIL_GOOGLE
                                    showAuthDialog = true
                                }
                                .testTag("register_btn_google")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text("🇬", fontSize = 14.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("حساب Google", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Full Name Field
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("الاسم الكامل") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = EmeraldPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_fullname"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Phone Number Field
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("رقم الهاتف") },
                placeholder = { Text("09X XXXXXXX") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = EmeraldPrimary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_phone"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Extra field based on role
            if (userRole == "seller") {
                OutlinedTextField(
                    value = storeOrVehicleName,
                    onValueChange = { storeOrVehicleName = it },
                    label = { Text("اسم المتجر أو النشاط التجاري") },
                    leadingIcon = { Icon(Icons.Default.Business, contentDescription = null, tint = EmeraldPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_store_name"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            } else if (userRole == "driver") {
                OutlinedTextField(
                    value = storeOrVehicleName,
                    onValueChange = { storeOrVehicleName = it },
                    label = { Text("نوع المركبة (دراجة نارية / سيارة)") },
                    leadingIcon = { Icon(Icons.Default.TwoWheeler, contentDescription = null, tint = EmeraldPrimary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_vehicle_type"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // City / Location
            OutlinedTextField(
                value = addressCity,
                onValueChange = { addressCity = it },
                label = { Text("المدينة / المنطقة") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = EmeraldPrimary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_city"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("كلمة المرور") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = EmeraldPrimary) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_password"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("btn_register_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "بدء الاستخدام الآن",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showAuthDialog) {
        AuthDialog(
            isOpen = showAuthDialog,
            onDismiss = { showAuthDialog = false },
            onLoginPhoneSuccess = { verifiedPhone, name ->
                showAuthDialog = false
                phone = verifiedPhone
                if (fullName.isBlank()) fullName = name
                Toast.makeText(context, "تم توثيق الرقم عبر واتساب بنجاح ✅", Toast.LENGTH_SHORT).show()
            },
            onLoginGoogleSuccess = { email, name ->
                showAuthDialog = false
                if (fullName.isBlank()) fullName = name
                Toast.makeText(context, "تم ربط حساب Google ($email) بنجاح ✅", Toast.LENGTH_SHORT).show()
            },
            initialTab = selectedAuthTab
        )
    }
}
