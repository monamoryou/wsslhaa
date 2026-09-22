package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.AmberDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StatusCancelled
import com.example.ui.theme.StatusCompleted
import com.example.ui.theme.StatusInTransit
import com.example.ui.theme.StatusPending
import com.example.ui.theme.StatusSearching
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RoleSelectorBar(
    currentRole: UserRole,
    cartItemCount: Int,
    sellerPendingCount: Int,
    driverAvailableCount: Int,
    onRoleSelected: (UserRole) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserRole.values().forEach { role ->
                val isSelected = currentRole == role
                val badgeCount = when (role) {
                    UserRole.BUYER -> cartItemCount
                    UserRole.SELLER -> sellerPendingCount
                    UserRole.DRIVER -> driverAvailableCount
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            if (isSelected) EmeraldPrimary else Color.Transparent
                        )
                        .clickable { onRoleSelected(role) }
                        .padding(vertical = 10.dp, horizontal = 6.dp)
                        .testTag("role_tab_${role.name.lowercase()}"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        val icon = when (role) {
                            UserRole.BUYER -> Icons.Default.Person
                            UserRole.SELLER -> Icons.Default.Storefront
                            UserRole.DRIVER -> Icons.Default.TwoWheeler
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = role.titleAr,
                            tint = if (isSelected) Color.White else EmeraldDark,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = role.titleAr,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color.White else EmeraldDark
                        )

                        if (badgeCount > 0) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AmberAccent else EmeraldPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$badgeCount",
                                    color = if (isSelected) Color.Black else Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(
    statusString: String,
    modifier: Modifier = Modifier
) {
    val status = try {
        OrderStatus.valueOf(statusString)
    } catch (e: Exception) {
        OrderStatus.PENDING_ACCEPTANCE
    }

    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.PENDING_ACCEPTANCE -> Triple(
            AmberContainer,
            AmberDark,
            Icons.Default.HourglassTop
        )
        OrderStatus.PREPARING -> Triple(
            Color(0xFFEDE9FE),
            Color(0xFF6D28D9),
            Icons.Default.Storefront
        )
        OrderStatus.SEARCHING_FOR_DRIVER -> Triple(
            Color(0xFFE0F2FE),
            Color(0xFF0369A1),
            Icons.Default.Navigation
        )
        OrderStatus.DRIVER_ASSIGNED, OrderStatus.PICKING_UP -> Triple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            Icons.Default.DirectionsBike
        )
        OrderStatus.IN_TRANSIT -> Triple(
            Color(0xFFF3E8FF),
            Color(0xFF7E22CE),
            Icons.Default.LocalShipping
        )
        OrderStatus.COMPLETED -> Triple(
            EmeraldContainer,
            EmeraldPrimary,
            Icons.Default.CheckCircle
        )
        OrderStatus.CANCELLED -> Triple(
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            Icons.Default.HourglassTop
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = status.titleAr,
                color = textColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun LiveTrackingMap(
    order: OrderEntity,
    modifier: Modifier = Modifier,
    onSimulateStep: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE5E9E7))
            ) {
                drawMapBackground()

                // Scaled visual coordinates in the card canvas
                val w = size.width
                val h = size.height

                // Pickup location (Store)
                val pickupPoint = Offset(w * 0.20f, h * 0.72f)
                // Dropoff location (Customer)
                val dropoffPoint = Offset(w * 0.82f, h * 0.26f)

                // Driver position relative to progress
                val progressFraction = when (order.status) {
                    OrderStatus.PENDING_ACCEPTANCE.name, OrderStatus.PREPARING.name -> 0.05f
                    OrderStatus.SEARCHING_FOR_DRIVER.name -> 0.12f
                    OrderStatus.DRIVER_ASSIGNED.name, OrderStatus.PICKING_UP.name -> 0.25f
                    OrderStatus.IN_TRANSIT.name -> 0.65f
                    OrderStatus.COMPLETED.name -> 1.0f
                    else -> 0.5f
                }

                val driverPoint = Offset(
                    x = pickupPoint.x + (dropoffPoint.x - pickupPoint.x) * progressFraction,
                    y = pickupPoint.y + (dropoffPoint.y - pickupPoint.y) * progressFraction - 15f
                )

                // Draw Route Polyline
                val routePath = Path().apply {
                    moveTo(pickupPoint.x, pickupPoint.y)
                    quadraticTo(
                        (pickupPoint.x + dropoffPoint.x) / 2f + 25f,
                        (pickupPoint.y + dropoffPoint.y) / 2f - 40f,
                        dropoffPoint.x,
                        dropoffPoint.y
                    )
                }

                // Gray road background line
                drawPath(
                    path = routePath,
                    color = Color(0xFFB0BEC5),
                    style = Stroke(width = 10f, cap = StrokeCap.Round)
                )

                // Emerald dashed active line
                drawPath(
                    path = routePath,
                    color = EmeraldPrimary,
                    style = Stroke(
                        width = 6f,
                        cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(18f, 12f), 0f)
                    )
                )

                // Draw Store Marker (Green circle + base)
                drawCircle(
                    color = Color(0xFF0D9488),
                    radius = 18f,
                    center = pickupPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 7f,
                    center = pickupPoint
                )

                // Draw Dropoff Customer Marker (Amber/Red circle)
                drawCircle(
                    color = Color(0xFFEA580C),
                    radius = 20f,
                    center = dropoffPoint
                )
                drawCircle(
                    color = Color.White,
                    radius = 8f,
                    center = dropoffPoint
                )

                // Draw Animated Driver Pulse & Marker
                if (order.status != OrderStatus.COMPLETED.name) {
                    drawCircle(
                        color = AmberAccent.copy(alpha = pulseAlpha),
                        radius = pulseScale,
                        center = driverPoint
                    )
                }

                // Driver pin body
                drawCircle(
                    color = AmberDark,
                    radius = 18f,
                    center = driverPoint
                )
                drawCircle(
                    color = Color(0xFFFEF3C7),
                    radius = 10f,
                    center = driverPoint
                )
            }

            // Top Status & ETA Card overlay
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
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (order.status == OrderStatus.IN_TRANSIT.name) EmeraldPrimary else AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (order.status == OrderStatus.IN_TRANSIT.name) "تتبع حي: باقي 10 دقائق 🛵" else "تتبع حي ومباشر 📍",
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
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TwoWheeler,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "تحريك الموصلاتي",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary
                        )
                    }
                }
            }

            // Bottom Map Labels (Store and Customer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏪", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.sellerName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("📍", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = order.dropoffAddress.take(22) + "...",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C)
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawMapBackground() {
    val w = size.width
    val h = size.height

    // Decorative river / water channel
    val waterPath = Path().apply {
        moveTo(0f, h * 0.45f)
        cubicTo(w * 0.3f, h * 0.35f, w * 0.6f, h * 0.6f, w, h * 0.5f)
        lineTo(w, h * 0.58f)
        cubicTo(w * 0.6f, h * 0.68f, w * 0.3f, h * 0.43f, 0f, h * 0.53f)
        close()
    }
    drawPath(waterPath, color = Color(0xFFBFDBFE).copy(alpha = 0.6f))

    // Green park spaces
    drawRoundRect(
        color = Color(0xFFD1FAE5).copy(alpha = 0.7f),
        topLeft = Offset(w * 0.08f, h * 0.12f),
        size = androidx.compose.ui.geometry.Size(w * 0.22f, h * 0.3f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    drawRoundRect(
        color = Color(0xFFD1FAE5).copy(alpha = 0.7f),
        topLeft = Offset(w * 0.65f, h * 0.60f),
        size = androidx.compose.ui.geometry.Size(w * 0.28f, h * 0.28f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f, 16f)
    )

    // City grid roads
    val roadColor = Color(0xFFCBD5E1)
    for (i in 1..4) {
        val y = h * (i * 0.2f)
        drawLine(
            color = roadColor,
            start = Offset(0f, y),
            end = Offset(w, y),
            strokeWidth = 5f
        )
    }
    for (j in 1..5) {
        val x = w * (j * 0.18f)
        drawLine(
            color = roadColor,
            start = Offset(x, 0f),
            end = Offset(x, h),
            strokeWidth = 5f
        )
    }
}
