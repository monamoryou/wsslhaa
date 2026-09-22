package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SwitchAccount
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import com.example.ui.account.AccountSelectionScreen
import com.example.ui.account.AuthDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.OrderStatus
import com.example.data.model.UserRole
import com.example.ui.WasselhaViewModel
import com.example.ui.buyer.BuyerScreen
import com.example.ui.components.RoleSelectorBar
import com.example.ui.driver.DriverScreen
import com.example.ui.seller.SellerScreen
import com.example.ui.tracking.OrderTrackingScreen
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldDark
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: WasselhaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val roleExtra = intent?.getStringExtra("USER_ROLE")
        if (roleExtra != null) {
            when (roleExtra) {
                "seller" -> viewModel.selectAccountType(UserRole.SELLER)
                "driver" -> viewModel.selectAccountType(UserRole.DRIVER)
                else -> viewModel.selectAccountType(UserRole.BUYER)
            }
        }

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    WasselhaApp(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WasselhaApp(viewModel: WasselhaViewModel) {
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allProducts by viewModel.allProducts.collectAsStateWithLifecycle()
    val allOrders by viewModel.allOrders.collectAsStateWithLifecycle()
    val availableDeliveryOrders by viewModel.availableDeliveryOrders.collectAsStateWithLifecycle()
    val cartItems by viewModel.cartItems.collectAsStateWithLifecycle()
    val cartStore by viewModel.cartStore.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val trackedOrderId by viewModel.trackedOrderId.collectAsStateWithLifecycle()
    val driverIsOnline by viewModel.driverIsOnline.collectAsStateWithLifecycle()
    val notificationMessage by viewModel.notificationMessage.collectAsStateWithLifecycle()
    val showAccountSelection by viewModel.showAccountSelection.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val isAuthDialogOpen by viewModel.isAuthDialogOpen.collectAsStateWithLifecycle()
    var fullScreenTrackingOrderId by remember { mutableStateOf<String?>(null) }

    // Auto-dismiss notification after 3 seconds
    LaunchedEffect(notificationMessage) {
        if (notificationMessage != null) {
            delay(3000)
            viewModel.clearNotification()
        }
    }

    if (showAccountSelection) {
        AccountSelectionScreen(
            onTypeSelected = { type ->
                when (type) {
                    "BUYER" -> viewModel.selectAccountType(UserRole.BUYER)
                    "MERCHANT" -> viewModel.selectAccountType(UserRole.SELLER)
                    "DELIVERY" -> viewModel.selectAccountType(UserRole.DRIVER)
                }
            },
            onGoogleSignInClick = {
                viewModel.loginWithGoogle("nameend1@gmail.com", "nameend1")
                viewModel.selectAccountType(UserRole.BUYER)
            }
        )
    } else if (fullScreenTrackingOrderId != null) {
        val trackingOrder = allOrders.find { it.orderId == fullScreenTrackingOrderId }
        OrderTrackingScreen(
            order = trackingOrder,
            onBackClick = { fullScreenTrackingOrderId = null },
            onSimulateDriverStep = {
                if (trackingOrder != null) {
                    viewModel.driverConfirmPickup(trackingOrder.orderId, trackingOrder)
                }
            },
            onContactDriver = { driverPhone ->
                viewModel.showNotification("جاري الاتصال بالموصلاتي: $driverPhone 📞")
            },
            onContactStore = { storeId ->
                viewModel.showNotification("جاري فتح المحادثة المباشرة مع المتجر 💬")
            }
        )
    } else {
        // Counts for badges
        val cartCount = cartItems.values.sum()
        val sellerPendingCount = allOrders.count {
            it.status == OrderStatus.PENDING_ACCEPTANCE.name || it.status == OrderStatus.PREPARING.name
        }
        val driverAvailableCount = availableDeliveryOrders.size

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                Column(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.surface)
                        .statusBarsPadding()
                ) {
                    // Main App Branding Header
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(EmeraldPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "وصلها",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 19.sp,
                                            color = EmeraldPrimary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "• Wasselha",
                                            fontWeight = FontWeight.Normal,
                                            fontSize = 14.sp,
                                            color = EmeraldDark
                                        )
                                    }
                                    Text(
                                        text = "توصيل سريع وربط مباشر بين المشتري والمتجر والموصلاتي",
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }
                        },
                        actions = {
                            // User Auth Status / Sign In Button
                            if (currentUser != null) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = EmeraldContainer,
                                    modifier = Modifier
                                        .clickable { viewModel.openAuthDialog() }
                                        .testTag("user_profile_chip")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = if (currentUser?.authProvider == "google") "🇬" else "💬",
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = currentUser?.name?.take(10) ?: "",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldDark
                                        )
                                    }
                                }
                            } else {
                                TextButton(
                                    onClick = { viewModel.openAuthDialog() },
                                    modifier = Modifier.testTag("login_button")
                                ) {
                                    Text(
                                        text = "دخول 💬/🇬",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(4.dp))

                            TextButton(
                                onClick = { viewModel.openAccountSelection() },
                                modifier = Modifier.testTag("switch_account_type_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwitchAccount,
                                    contentDescription = "تغيير نوع الحساب",
                                    tint = EmeraldPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "نوع الحساب",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    )

                    // Role Switcher Segmented Bar
                    RoleSelectorBar(
                        currentRole = currentRole,
                        cartItemCount = cartCount,
                        sellerPendingCount = sellerPendingCount,
                        driverAvailableCount = driverAvailableCount,
                        onRoleSelected = { viewModel.switchRole(it) }
                    )
                }
            }
        ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Role Content
            when (currentRole) {
                UserRole.BUYER -> {
                    BuyerScreen(
                        stores = viewModel.stores,
                        products = allProducts,
                        orders = allOrders,
                        cartItems = cartItems,
                        cartStore = cartStore,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        trackedOrderId = trackedOrderId,
                        onCategorySelected = { viewModel.setCategory(it) },
                        onSearchChanged = { viewModel.setSearchQuery(it) },
                        onAddToCart = { product, store -> viewModel.addToCart(product, store) },
                        onRemoveFromCart = { viewModel.removeFromCart(it) },
                        onPlaceOrder = { address, notes -> viewModel.placeOrder(address, notes) },
                        onTrackOrder = { viewModel.setTrackedOrder(it) },
                        onSimulateDriverStep = {
                            val active = allOrders.find { it.orderId == trackedOrderId } ?: allOrders.firstOrNull()
                            if (active != null) {
                                viewModel.driverConfirmPickup(active.orderId, active)
                            }
                        },
                        onOpenFullTracking = { orderId ->
                            fullScreenTrackingOrderId = orderId
                        }
                    )
                }

                UserRole.SELLER -> {
                    SellerScreen(
                        stores = viewModel.stores,
                        orders = allOrders,
                        products = allProducts,
                        onAcceptOrder = { viewModel.sellerAcceptOrder(it) },
                        onRequestDriver = { viewModel.sellerRequestDriver(it) },
                        onAddNewProduct = { name, price, cat, desc, storeId, storeName ->
                            viewModel.addNewProduct(name, price, cat, desc, storeId, storeName)
                        }
                    )
                }

                UserRole.DRIVER -> {
                    DriverScreen(
                        isOnline = driverIsOnline,
                        availableOrders = availableDeliveryOrders,
                        allOrders = allOrders,
                        onToggleOnline = { viewModel.toggleDriverOnline() },
                        onAcceptDelivery = { viewModel.driverAcceptDelivery(it) },
                        onConfirmPickup = { orderId, order -> viewModel.driverConfirmPickup(orderId, order) },
                        onConfirmDelivered = { viewModel.driverConfirmDelivered(it) },
                        onSimulateDriverStep = {
                            val active = allOrders.find {
                                (it.driverId == "driver_1" || it.driverName?.contains("كابتن") == true) &&
                                        it.status != OrderStatus.COMPLETED.name
                            }
                            if (active != null) {
                                viewModel.driverConfirmPickup(active.orderId, active)
                            }
                        }
                    )
                }
            }

            // In-app Notification Toast Banner
            AnimatedVisibility(
                visible = notificationMessage != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF1E293B),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AmberAccent)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = notificationMessage ?: "",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        IconButton(
                            onClick = { viewModel.clearNotification() },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق", tint = Color(0xFF94A3B8), modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
    }
}

    // Global WhatsApp & Google Auth Dialog
    if (isAuthDialogOpen) {
        AuthDialog(
            isOpen = isAuthDialogOpen,
            onDismiss = { viewModel.closeAuthDialog() },
            onLoginPhoneSuccess = { phone, name -> viewModel.loginWithPhone(phone, name) },
            onLoginGoogleSuccess = { email, name -> viewModel.loginWithGoogle(email, name) }
        )
    }
}

// Retained for tests compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme { Greeting("Android") }
}
