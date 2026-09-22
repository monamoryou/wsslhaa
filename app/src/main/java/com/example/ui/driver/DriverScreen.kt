package com.example.ui.driver

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.LiveTrackingMap
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DriverScreen(
    isOnline: Boolean,
    availableOrders: List<OrderEntity>,
    allOrders: List<OrderEntity>,
    onToggleOnline: () -> Unit,
    onAcceptDelivery: (OrderEntity) -> Unit,
    onConfirmPickup: (String, OrderEntity) -> Unit,
    onConfirmDelivered: (String) -> Unit,
    onSimulateDriverStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedDriverTab by remember { mutableIntStateOf(0) }

    // Active order currently taken by this driver
    val activeDelivery = allOrders.find {
        (it.driverId == "driver_1" || it.driverName?.contains("كابتن") == true) &&
                (it.status == OrderStatus.PICKING_UP.name || it.status == OrderStatus.DRIVER_ASSIGNED.name || it.status == OrderStatus.IN_TRANSIT.name)
    }

    val completedTrips = allOrders.filter {
        (it.driverId == "driver_1" || it.driverName?.contains("كابتن") == true) &&
                it.status == OrderStatus.COMPLETED.name
    }
    val todayEarnings = completedTrips.sumOf { it.deliveryFee } + 65.0 // baseline

    Column(modifier = modifier.fillMaxSize()) {
        // Driver Header with online switch & earnings
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) EmeraldContainer else Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛵", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("كابتن التوصيل (الموصلاتي)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = if (isOnline) "متصل وجاهز للطلبات 🟢" else "غير متاح حالياً ⚪",
                                fontSize = 11.sp,
                                color = if (isOnline) EmeraldDark else TextSecondary
                            )
                        }
                    }

                    Switch(
                        checked = isOnline,
                        onCheckedChange = { onToggleOnline() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = EmeraldPrimary
                        ),
                        modifier = Modifier.testTag("driver_online_switch")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Stats row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DriverStatCard(
                        title = "أرباح اليوم",
                        value = "${todayEarnings.toInt()} ج.م",
                        icon = "💵",
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1.2f)
                    )
                    DriverStatCard(
                        title = "مشاوير منجزة",
                        value = "${completedTrips.size + 4}",
                        icon = "🏁",
                        color = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                    DriverStatCard(
                        title = "التقييم العام",
                        value = "4.9 ⭐",
                        icon = "⭐",
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Tabs: المشوار النشط vs الطلبات المتاحة للتوصيل
        TabRow(
            selectedTabIndex = selectedDriverTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedDriverTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedDriverTab == 0,
                onClick = { selectedDriverTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (activeDelivery != null) "المشوار النشط (1) 🟢" else "المشوار النشط",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            )
            Tab(
                selected = selectedDriverTab == 1,
                onClick = { selectedDriverTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الطلبات المتاحة (${availableOrders.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        if (selectedDriverTab == 0) {
            // Active Delivery View
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (activeDelivery == null) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🛵", fontSize = 42.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text("لا يوجد مشوار توصيل نشط حالياً", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("تصفح تبويب \"الطلبات المتاحة\" واقبل طلب جديد لبدء التوصيل والربح!", color = TextSecondary, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { selectedDriverTab = 1 },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                                ) {
                                    Text("عرض طلبات التوصيل المتاحة (${availableOrders.size})")
                                }
                            }
                        }
                    }
                } else {
                    // Active delivery details + interactive map
                    item {
                        Text(
                            text = "خريطة ومسار التوصيل المباشر 🗺️",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                    }

                    item {
                        LiveTrackingMap(
                            order = activeDelivery,
                            onSimulateStep = onSimulateDriverStep
                        )
                    }

                    item {
                        DriverActiveOrderActionCard(
                            order = activeDelivery,
                            onConfirmPickup = { onConfirmPickup(activeDelivery.orderId, activeDelivery) },
                            onConfirmDelivered = { onConfirmDelivered(activeDelivery.orderId) }
                        )
                    }
                }
            }
        } else {
            // Available Orders Pool
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "طلبات بانتظار موصلاتي (${availableOrders.size}) 📢",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }

                if (availableOrders.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("✨", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("تم تغطية جميع الطلبات!", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("يمكنك التبديل لدور البائع وطلب موصلاتي لأي طلب جاهز لترى إشعاره هنا فوراً", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(availableOrders) { order ->
                        AvailableOrderCard(
                            order = order,
                            onAccept = {
                                onAcceptDelivery(order)
                                selectedDriverTab = 0
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DriverActiveOrderActionCard(
    order: OrderEntity,
    onConfirmPickup: () -> Unit,
    onConfirmDelivered: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("مشوار جاري: طلب رقم ${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("أجرتك: ${order.deliveryFee} ج.م • تحصيل: ${order.totalPrice} ج.م", fontSize = 12.sp, color = EmeraldDark, fontWeight = FontWeight.Bold)
                }
                OrderStatusBadge(statusString = order.status)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Pickup & Dropoff details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("نقطة الاستلام (المتجر): ${order.sellerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(order.pickupAddress, fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text("نقطة التسليم (العميل): ${order.buyerName} • ${order.buyerPhone}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Text(order.dropoffAddress, fontSize = 11.sp, color = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Button depending on whether picked up or in transit
            if (order.status == OrderStatus.PICKING_UP.name || order.status == OrderStatus.DRIVER_ASSIGNED.name) {
                Button(
                    onClick = onConfirmPickup,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("driver_confirm_pickup_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black)
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("وصلت للمتجر واستلمت الطلب ✅", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            } else if (order.status == OrderStatus.IN_TRANSIT.name) {
                Button(
                    onClick = onConfirmDelivered,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("driver_confirm_delivered_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("تم تسليم الطلب للعميل وتحصيل المبلغ 💵", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
fun AvailableOrderCard(
    order: OrderEntity,
    onAccept: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🏪", fontSize = 18.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(order.sellerName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(order.pickupAddress, fontSize = 11.sp, color = TextSecondary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldContainer
                ) {
                    Text(
                        text = "أجرتك: ${order.deliveryFee} ج.م",
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldPrimary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("التسليم إلى: ${order.dropoffAddress}", fontSize = 12.sp, color = TextPrimary)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "الأصناف: ${order.itemsSummary}",
                fontSize = 11.sp,
                color = TextSecondary,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAccept,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("driver_accept_order_${order.orderId}"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("قبول التوصيل الآن 🚀", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun DriverStatCard(
    title: String,
    value: String,
    icon: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = color)
            Text(title, fontSize = 10.sp, color = TextSecondary, maxLines = 1)
        }
    }
}
