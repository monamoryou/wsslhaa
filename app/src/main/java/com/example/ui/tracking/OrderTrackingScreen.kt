package com.example.ui.tracking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.Fastfood
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.ui.components.OrderStatusBadge
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data structure representing a milestone item in the delivery status timeline.
 */
data class TimelineStepInfo(
    val title: String,
    val subtitle: String,
    val timestamp: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

/**
 * Real-time Order Tracking Screen displaying:
 * 1. Top bar with order identity and quick actions
 * 2. Interactive Map placeholder with route visualization, animated driver beacon, and origin/destination markers
 * 3. Status Timeline for the current delivery with completion tracking
 * 4. Driver & Store contact details card
 * 5. Order summary breakdown
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingScreen(
    order: OrderEntity?,
    onBackClick: () -> Unit = {},
    onSimulateDriverStep: () -> Unit = {},
    onContactDriver: (String) -> Unit = {},
    onContactStore: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    if (order == null) {
        OrderTrackingEmptyState(onBackClick = onBackClick, modifier = modifier)
        return
    }

    val currentStatus = try {
        OrderStatus.valueOf(order.status)
    } catch (e: Exception) {
        OrderStatus.PENDING_ACCEPTANCE
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_tracking_screen"),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "تتبع الطلب مباشرة",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (currentStatus == OrderStatus.COMPLETED) EmeraldPrimary else AmberAccent)
                            )
                        }
                        Text(
                            text = "رقم الشحنة: #${order.orderId}",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("tracking_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSimulateDriverStep,
                        modifier = Modifier.testTag("tracking_simulate_step_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "تحديث / تحريك الموصلاتي",
                            tint = EmeraldPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. High-fidelity Interactive Real-time Map Placeholder
            item {
                InteractiveMapPlaceholder(
                    order = order,
                    currentStatus = currentStatus,
                    onSimulateStep = onSimulateDriverStep
                )
            }

            // 2. ETA & Live Status Overview Banner
            item {
                LiveDeliveryStatusBanner(
                    order = order,
                    currentStatus = currentStatus
                )
            }

            // 3. Driver Information and Live Contact Card (if assigned)
            item {
                DriverDeliveryCard(
                    order = order,
                    onContactDriver = onContactDriver
                )
            }

            // 4. Delivery Status Timeline Component
            item {
                DeliveryStatusTimelineCard(
                    order = order,
                    currentStatus = currentStatus
                )
            }

            // 5. Order Information & Delivery Destination Card
            item {
                OrderDetailsCard(
                    order = order,
                    onContactStore = onContactStore
                )
            }

            // 6. Action buttons (Simulate movement / support)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onSimulateDriverStep,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("simulate_driver_progress_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentStatus == OrderStatus.COMPLETED) "إعادة بدء المحاكاة" else "محاكاة تقدم خطوة التوصيل 🛵",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("back_to_orders_btn")
                    ) {
                        Text(
                            text = "العودة إلى قائمة الطلبات",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

/**
 * Empty state screen when no order is active for tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTrackingEmptyState(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("تتبع الطلب") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(EmeraldContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🛵", fontSize = 36.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "لا يوجد طلب محدد للتتبع حالياً",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "يرجى اختيار أحد طلباتك النشطة من قائمة الطلبات لمتابعة حركته وتفاصيله الحية.",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = onBackClick,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Text("الرجوع للطلبات")
                    }
                }
            }
        }
    }
}

/**
 * Map Placeholder with dynamic visual route, store & buyer pins, driver animation,
 * road grid, green parks, and river landmarks.
 */
@Composable
fun InteractiveMapPlaceholder(
    order: OrderEntity,
    currentStatus: OrderStatus,
    onSimulateStep: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "driver_radar")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 38f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp))
            .testTag("tracking_map_placeholder"),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE9ECEB))
            ) {
                val w = size.width
                val h = size.height

                // Draw map terrain (grid, waterways, green spaces)
                drawStyledMapCanvas(w, h)

                // Define geographic positions on the canvas
                val pickupPoint = Offset(w * 0.18f, h * 0.70f)
                val dropoffPoint = Offset(w * 0.82f, h * 0.28f)

                // Normalized driver route progress
                val progressFraction = when (currentStatus) {
                    OrderStatus.PENDING_ACCEPTANCE, OrderStatus.PREPARING -> 0.05f
                    OrderStatus.SEARCHING_FOR_DRIVER -> 0.12f
                    OrderStatus.DRIVER_ASSIGNED, OrderStatus.PICKING_UP -> 0.25f
                    OrderStatus.IN_TRANSIT -> 0.65f
                    OrderStatus.COMPLETED -> 1.0f
                    OrderStatus.CANCELLED -> 0.0f
                }

                // Bezier route curve control point
                val ctrlX = (pickupPoint.x + dropoffPoint.x) / 2f + 30f
                val ctrlY = (pickupPoint.y + dropoffPoint.y) / 2f - 50f

                val routePath = Path().apply {
                    moveTo(pickupPoint.x, pickupPoint.y)
                    quadraticTo(ctrlX, ctrlY, dropoffPoint.x, dropoffPoint.y)
                }

                // 1. Road bed background
                drawPath(
                    path = routePath,
                    color = Color(0xFF94A3B8).copy(alpha = 0.5f),
                    style = Stroke(width = 12f, cap = StrokeCap.Round)
                )

                // 2. Traveled active path (green dashed)
                drawPath(
                    path = routePath,
                    color = EmeraldPrimary,
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 10f), 0f)
                    )
                )

                // Calculate current driver position along quadratic Bezier curve
                val t = progressFraction
                val driverX = (1 - t) * (1 - t) * pickupPoint.x + 2 * (1 - t) * t * ctrlX + t * t * dropoffPoint.x
                val driverY = (1 - t) * (1 - t) * pickupPoint.y + 2 * (1 - t) * t * ctrlY + t * t * dropoffPoint.y
                val driverPoint = Offset(driverX, driverY)

                // Store Marker (Origin)
                drawCircle(color = Color(0xFF0F766E), radius = 18f, center = pickupPoint)
                drawCircle(color = Color.White, radius = 7f, center = pickupPoint)

                // Customer Dropoff Marker (Destination)
                drawCircle(color = Color(0xFFEA580C), radius = 20f, center = dropoffPoint)
                drawCircle(color = Color.White, radius = 8f, center = dropoffPoint)

                // Radar pulse around driver while in motion
                if (currentStatus != OrderStatus.COMPLETED && currentStatus != OrderStatus.CANCELLED) {
                    drawCircle(
                        color = AmberAccent.copy(alpha = pulseAlpha),
                        radius = pulseRadius,
                        center = driverPoint
                    )
                }

                // Driver pin icon badge
                drawCircle(color = AmberDark, radius = 18f, center = driverPoint)
                drawCircle(color = Color(0xFFFEF3C7), radius = 10f, center = driverPoint)
            }

            // Top Status Pill Bar & Recenter/Simulate Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(
                                    when (currentStatus) {
                                        OrderStatus.IN_TRANSIT -> EmeraldPrimary
                                        OrderStatus.COMPLETED -> Color(0xFF10B981)
                                        else -> AmberAccent
                                    }
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentStatus) {
                                OrderStatus.IN_TRANSIT -> "الطلب في الطريق إليك 🛵"
                                OrderStatus.PICKING_UP -> "الموصلاتي في الطريق للمتجر 🏃"
                                OrderStatus.COMPLETED -> "تم استلام الطلب بنجاح ✅"
                                OrderStatus.PREPARING -> "المتجر يجهز الأصناف 🍳"
                                else -> "تتبع حي ومباشر 📍"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    shadowElevation = 3.dp,
                    modifier = Modifier.clickable { onSimulateStep() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NearMe,
                            contentDescription = "محاكاة الحركة",
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تحريك",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            // Bottom Floating Origin and Destination Badges
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.96f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏪", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Text(
                                text = "المتجر: ${order.sellerName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F766E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White.copy(alpha = 0.96f),
                    shadowElevation = 3.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(5.dp))
                        Column {
                            Text(
                                text = "الوجهة: ${order.dropoffAddress.take(18)}...",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC2410C),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Draws realistic map elements: Nile/river channel, city blocks, parks, and arterial avenues.
 */
private fun DrawScope.drawStyledMapCanvas(w: Float, h: Float) {
    // Waterway / Nile canal
    val waterPath = Path().apply {
        moveTo(0f, h * 0.42f)
        cubicTo(w * 0.28f, h * 0.32f, w * 0.58f, h * 0.58f, w, h * 0.48f)
        lineTo(w, h * 0.57f)
        cubicTo(w * 0.58f, h * 0.67f, w * 0.28f, h * 0.41f, 0f, h * 0.51f)
        close()
    }
    drawPath(waterPath, color = Color(0xFFB9E2FE).copy(alpha = 0.85f))

    // Parks / Agricultural belts (Semhoud green spaces)
    drawRoundRect(
        color = Color(0xFFD1FAE5).copy(alpha = 0.8f),
        topLeft = Offset(w * 0.06f, h * 0.10f),
        size = Size(w * 0.26f, h * 0.28f),
        cornerRadius = CornerRadius(20f, 20f)
    )

    drawRoundRect(
        color = Color(0xFFD1FAE5).copy(alpha = 0.8f),
        topLeft = Offset(w * 0.64f, h * 0.60f),
        size = Size(w * 0.30f, h * 0.32f),
        cornerRadius = CornerRadius(20f, 20f)
    )

    // Minor streets
    val streetColor = Color(0xFFCBD5E1)
    for (i in 1..5) {
        val y = h * (i * 0.16f)
        drawLine(
            color = streetColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 4f
        )
    }
    for (j in 1..6) {
        val x = w * (j * 0.15f)
        drawLine(
            color = streetColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 4f
        )
    }

    // Major Boulevard highway (accent color)
    drawLine(
        color = Color(0xFFE2E8F0),
        start = Offset(0f, h * 0.80f),
        end = Offset(w, h * 0.15f),
        strokeWidth = 8f
    )
}

/**
 * Banner showing live ETA, speed, and real-time status indication.
 */
@Composable
fun LiveDeliveryStatusBanner(
    order: OrderEntity,
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val estimatedMinutes = when (currentStatus) {
        OrderStatus.PENDING_ACCEPTANCE -> "25-35 دقيقة"
        OrderStatus.PREPARING -> "20-30 دقيقة"
        OrderStatus.SEARCHING_FOR_DRIVER -> "15-25 دقيقة"
        OrderStatus.DRIVER_ASSIGNED, OrderStatus.PICKING_UP -> "12-18 دقيقة"
        OrderStatus.IN_TRANSIT -> "7-12 دقيقة"
        OrderStatus.COMPLETED -> "تم التسليم"
        OrderStatus.CANCELLED -> "ملغي"
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = EmeraldContainer,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "الوقت المتوقع للوصول",
                        fontSize = 12.sp,
                        color = EmeraldDark,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = estimatedMinutes,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = OnEmeraldContainer
                )
            }

            OrderStatusBadge(statusString = order.status)
        }
    }
}

/**
 * Driver contact card displaying driver name, vehicle, rating, and call button.
 */
@Composable
fun DriverDeliveryCard(
    order: OrderEntity,
    onContactDriver: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "بيانات الموصلاتي (كابتن التوصيل)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (order.driverName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🛵", fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = order.driverName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = order.driverVehicle ?: "سكوتر دايون سريع",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                            if (order.driverPhone != null) {
                                Text(
                                    text = order.driverPhone,
                                    fontSize = 11.sp,
                                    color = EmeraldPrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { onContactDriver(order.driverPhone ?: "01091234567") },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("call_driver_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "اتصال بالموصلاتي",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("اتصال", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = AmberDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "جاري تعيين موصلاتي من شبكة وصلها",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AmberDark
                            )
                            Text(
                                text = "سيتم إشعارك فور قبول أحد الموصلاتيين لطلبك.",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Vertical Status Timeline Component for the delivery lifecycle.
 */
@Composable
fun DeliveryStatusTimelineCard(
    order: OrderEntity,
    currentStatus: OrderStatus,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale("ar"))
    val createdTime = timeFormat.format(Date(order.createdAt))

    val timelineSteps = remember(currentStatus, order) {
        listOf(
            TimelineStepInfo(
                title = "تم استلام الطلب",
                subtitle = "تم إرسال الطلب إلى متجر ${order.sellerName}",
                timestamp = createdTime,
                isCompleted = currentStatus.stepIndex >= OrderStatus.PENDING_ACCEPTANCE.stepIndex,
                isCurrent = currentStatus == OrderStatus.PENDING_ACCEPTANCE,
                icon = Icons.Default.CheckCircle
            ),
            TimelineStepInfo(
                title = "المتجر يجهز طلبك",
                subtitle = "يتم تحضير وتغليف الأصناف المختارة بعناية",
                timestamp = if (currentStatus.stepIndex >= OrderStatus.PREPARING.stepIndex) "مكتمل" else "قيد الانتظار",
                isCompleted = currentStatus.stepIndex >= OrderStatus.PREPARING.stepIndex,
                isCurrent = currentStatus == OrderStatus.PREPARING,
                icon = Icons.Default.Storefront
            ),
            TimelineStepInfo(
                title = "تعيين واستلام الموصلاتي",
                subtitle = if (order.driverName != null) "استلم الكابتن ${order.driverName} الشحنة" else "جاري إسناد الطلب لأقرب كابتن",
                timestamp = if (currentStatus.stepIndex >= OrderStatus.DRIVER_ASSIGNED.stepIndex) "مكتمل" else "قيد الانتظار",
                isCompleted = currentStatus.stepIndex >= OrderStatus.DRIVER_ASSIGNED.stepIndex,
                isCurrent = currentStatus == OrderStatus.DRIVER_ASSIGNED || currentStatus == OrderStatus.PICKING_UP || currentStatus == OrderStatus.SEARCHING_FOR_DRIVER,
                icon = Icons.Default.TwoWheeler
            ),
            TimelineStepInfo(
                title = "الطلب في الطريق إليك",
                subtitle = "الموصلاتي يتجه الآن نحو عنوان التسليم المحدد",
                timestamp = if (currentStatus.stepIndex >= OrderStatus.IN_TRANSIT.stepIndex) "جاري الآن 🛵" else "لاحقاً",
                isCompleted = currentStatus.stepIndex >= OrderStatus.IN_TRANSIT.stepIndex,
                isCurrent = currentStatus == OrderStatus.IN_TRANSIT,
                icon = Icons.Default.LocalShipping
            ),
            TimelineStepInfo(
                title = "تم التسليم بنجاح",
                subtitle = "تم استلام الطلب ودفع القيمة نقداً عند الاستلام",
                timestamp = if (currentStatus == OrderStatus.COMPLETED) "تم بنجاح 🎉" else "الخطوة الأخيرة",
                isCompleted = currentStatus == OrderStatus.COMPLETED,
                isCurrent = currentStatus == OrderStatus.COMPLETED,
                icon = Icons.Default.Check
            )
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("status_timeline_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "مراحل مسار التوصيل (Timeline)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${timelineSteps.count { it.isCompleted }}/${timelineSteps.size}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            timelineSteps.forEachIndexed { index, step ->
                TimelineStepRow(
                    step = step,
                    isLast = index == timelineSteps.lastIndex
                )
            }
        }
    }
}

/**
 * Individual row within the Status Timeline.
 */
@Composable
fun TimelineStepRow(
    step: TimelineStepInfo,
    isLast: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth()
    ) {
        // Vertical indicator column (Icon + connecting line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(36.dp)
        ) {
            // Circle node
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            step.isCurrent -> AmberAccent
                            step.isCompleted -> EmeraldPrimary
                            else -> Color(0xFFE2E8F0)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = if (step.isCompleted || step.isCurrent) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(16.dp)
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(36.dp)
                        .background(
                            if (step.isCompleted) EmeraldPrimary else Color(0xFFE2E8F0)
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Step Content details
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = step.title,
                    fontSize = 14.sp,
                    fontWeight = if (step.isCurrent || step.isCompleted) FontWeight.Bold else FontWeight.SemiBold,
                    color = if (step.isCompleted || step.isCurrent) TextPrimary else TextSecondary
                )
                Text(
                    text = step.timestamp,
                    fontSize = 11.sp,
                    color = if (step.isCurrent) AmberDark else TextSecondary,
                    fontWeight = if (step.isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = step.subtitle,
                fontSize = 12.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

/**
 * Detailed order card with items summary, addresses, and pricing.
 */
@Composable
fun OrderDetailsCard(
    order: OrderEntity,
    onContactStore: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "تفاصيل الطلب والفاتورة",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Store info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "المتجر: ${order.sellerName}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                TextButton(
                    onClick = { onContactStore(order.sellerId) }
                ) {
                    Text("مراسلة المتجر", fontSize = 12.sp, color = EmeraldPrimary)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Dropoff Location
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEA580C),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "عنوان التسليم:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = order.dropoffAddress,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Items summary
            Text(
                text = "الأصناف المطلوبة (${order.itemsCount}):",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                color = TextPrimary,
                fontWeight = FontWeight.Medium
            )

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ملاحظات: ${order.notes}",
                    fontSize = 12.sp,
                    color = AmberDark
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Financial breakdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("أجرة التوصيل:", fontSize = 13.sp, color = TextSecondary)
                Text("${order.deliveryFee} ج.م", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "الإجمالي المستحق (الدفع عند الاستلام):",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Text(
                    text = "${order.totalPrice} ج.م",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary
                )
            }
        }
    }
}
