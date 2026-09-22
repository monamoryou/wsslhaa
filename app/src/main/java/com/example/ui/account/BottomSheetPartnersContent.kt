package com.example.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * BottomSheetPartnersContent strictly matches:
 *
 * <LinearLayout ...
 *     android:padding="24dp"
 *     android:backgroundTint="#FFFFFF"
 *     app:layout_behavior="com.google.android.material.bottomsheet.BottomSheetBehavior">
 *
 *     <!-- شريط سحب علوي صغير لإعطاء إيحاء بإمكانية الإغلاق بالسحب -->
 *     <View android:layout_width="40dp" android:layout_height="4dp" android:background="#D6D6D6" android:layout_marginBottom="24dp"/>
 *
 *     <TextView android:text="انضم كشريك في وصلها" android:textSize="20sp" android:textStyle="bold" android:textColor="#1A1A1A" android:layout_marginBottom="24dp"/>
 *
 *     <!-- خيار البائع layoutSeller -->
 *     <!-- Divider 1dp #EAEAEA -->
 *     <!-- خيار الموصلاتي layoutDriver -->
 * </LinearLayout>
 */
@Composable
fun BottomSheetPartnersContent(
    onSelectSeller: () -> Unit,
    onSelectDriver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // شريط سحب علوي صغير لإعطاء إيحاء بإمكانية الإغلاق بالسحب
        Box(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFFD6D6D6))
                .testTag("drag_handle")
        )

        Spacer(modifier = Modifier.height(24.dp))

        // عنوان الـ BottomSheet
        Text(
            text = "انضم كشريك في وصلها",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A1A1A),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // خيار البائع (layoutSeller)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectSeller() }
                .padding(16.dp)
                .testTag("layoutSeller"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🏪",
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "صاحب متجر (بائع)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "اعرض منتجاتك وزد من مبيعاتك ونحن نتولى التوصيل",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // خط فاصل بين الخيارين
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color(0xFFEAEAEA))
                .padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // خيار الموصلاتي (layoutDriver)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable { onSelectDriver() }
                .padding(16.dp)
                .testTag("layoutDriver"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🚴",
                fontSize = 28.sp,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "كابتن توصيل (موصلاتي)",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A1A1A)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "ابدأ بزيادة دخلك عبر توصيل الطلبات في محيطك الجغرافي",
                    fontSize = 13.sp,
                    color = Color(0xFF757575)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
