package com.example.ui.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User

@Composable
fun AccountSelectionScreen(
    onTypeSelected: (String) -> Unit,
    onGoogleSignInClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // عنوان الشاشة ترحيبي وواضح
            Text(
                text = "مرحباً بك في التطبيق",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "يرجى اختيار نوع الحساب للمتابعة",
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
                textAlign = TextAlign.Center
            )

            // أزرار أنواع الحسابات المطلوبة
            AccountTypeButton(title = "الشاري (الزبون)", icon = "🛒") { onTypeSelected("BUYER") }
            Spacer(modifier = Modifier.height(12.dp))

            AccountTypeButton(title = "التاجر (المحل)", icon = "🏪") { onTypeSelected("MERCHANT") }
            Spacer(modifier = Modifier.height(12.dp))

            AccountTypeButton(title = "الموصلاتي (الدليفري)", icon = "🏍️") { onTypeSelected("DELIVERY") }
            Spacer(modifier = Modifier.height(32.dp)) // مسافة تفصل الحساب السريع

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // زر المتابعة السريعة بحساب Google النشط بالأسفل
            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(text = "🌐", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                    Text(
                        text = "المتابعة كـ nameend1@gmail.com",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// تصميم مخصص وموحد لأزرار الخيارات الثلاثة لعدم تشتيت المستخدم
@Composable
fun AccountTypeButton(
    title: String,
    icon: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            Text(text = icon, fontSize = 24.sp, modifier = Modifier.padding(horizontal = 8.dp))
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Text(text = "◀", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
        }
    }
}

// التوافق مع النداءات السابقة في التطبيق
@Composable
fun AccountSelectionScreen(
    onSelectBuyer: () -> Unit,
    onSelectSeller: () -> Unit,
    onSelectDriver: () -> Unit,
    modifier: Modifier = Modifier,
    currentUser: User? = null,
    onLoginPhoneSuccess: ((String, String) -> Unit)? = null,
    onLoginGoogleSuccess: ((String, String) -> Unit)? = null
) {
    AccountSelectionScreen(
        onTypeSelected = { type ->
            when (type) {
                "BUYER" -> onSelectBuyer()
                "MERCHANT" -> onSelectSeller()
                "DELIVERY" -> onSelectDriver()
            }
        },
        onGoogleSignInClick = {
            onLoginGoogleSuccess?.invoke("nameend1@gmail.com", "nameend1")
            onSelectBuyer()
        }
    )
}
