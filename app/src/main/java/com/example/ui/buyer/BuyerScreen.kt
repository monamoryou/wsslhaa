package com.example.ui.buyer

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ProductEntity
import com.example.data.model.Store
import com.example.wasselha.utils.LocationUtils
import com.example.ui.components.LiveTrackingMap
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuyerScreen(
    stores: List<Store>,
    products: List<ProductEntity>,
    orders: List<OrderEntity>,
    cartItems: Map<ProductEntity, Int>,
    cartStore: Store?,
    selectedCategory: String,
    searchQuery: String,
    trackedOrderId: String?,
    onCategorySelected: (String) -> Unit,
    onSearchChanged: (String) -> Unit,
    onAddToCart: (ProductEntity, Store) -> Unit,
    onRemoveFromCart: (ProductEntity) -> Unit,
    onPlaceOrder: (address: String, notes: String) -> Unit,
    onTrackOrder: (String?) -> Unit,
    onSimulateDriverStep: () -> Unit,
    onOpenFullTracking: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedBuyerTab by remember { mutableIntStateOf(if (trackedOrderId != null) 1 else 0) }
    var showCartSheet by remember { mutableStateOf(false) }

    val categories = listOf("الكل", "مطاعم ومشويات", "بقالة وسوبرماركت", "صيدليات وأدوية", "حلويات ومخبوزات")

    // Filter products
    val filteredProducts = products.filter { product ->
        val matchesCategory = if (selectedCategory == "الكل") true else {
            val store = stores.find { it.id == product.sellerId }
            store?.category == selectedCategory
        }
        val matchesSearch = if (searchQuery.isBlank()) true else {
            product.name.contains(searchQuery, ignoreCase = true) ||
                    product.description.contains(searchQuery, ignoreCase = true) ||
                    product.sellerName.contains(searchQuery, ignoreCase = true)
        }
        matchesCategory && matchesSearch
    }

    val cartCount = cartItems.values.sum()
    val cartSubtotal = cartItems.entries.sumOf { it.key.price * it.value }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Buyer Tabs: المتجر / التصفح vs طلباتي وتتبعها
            TabRow(
                selectedTabIndex = selectedBuyerTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = EmeraldPrimary,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedBuyerTab]),
                        color = EmeraldPrimary,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedBuyerTab == 0,
                    onClick = { selectedBuyerTab = 0 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تصفح المتاجر", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                )
                Tab(
                    selected = selectedBuyerTab == 1,
                    onClick = { selectedBuyerTab = 1 },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طلباتي والتتبع", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            if (orders.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(AmberAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${orders.size}", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                )
            }

            if (selectedBuyerTab == 0) {
                // Browse / Shop tab
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = if (cartCount > 0) 90.dp else 24.dp)
                ) {
                    // Search bar
                    item {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = onSearchChanged,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 10.dp)
                                .testTag("buyer_search_input"),
                            placeholder = { Text("ابحث عن وجبة، منتج، أو متجر...", color = TextSecondary, fontSize = 14.sp) },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "بحث", tint = EmeraldPrimary) },
                            trailingIcon = {
                                if (searchQuery.isNotBlank()) {
                                    IconButton(onClick = { onSearchChanged("") }) {
                                        Icon(Icons.Default.Close, contentDescription = "مسح")
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = EmeraldPrimary,
                                unfocusedBorderColor = Color(0xFFD1D5DB),
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    // Store Categories row
                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories) { cat ->
                                val isSelected = cat == selectedCategory
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (isSelected) EmeraldPrimary else MaterialTheme.colorScheme.surface,
                                    border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null,
                                    modifier = Modifier.clickable { onCategorySelected(cat) },
                                    shadowElevation = if (isSelected) 3.dp else 0.dp
                                ) {
                                    Text(
                                        text = cat,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Featured Stores Section
                    item {
                        Text(
                            text = "المتاجر المعتمدة في منطقتك 🏪",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(stores) { store ->
                                StoreCardItem(store = store)
                            }
                        }
                    }

                    // Products Title
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "قائمة المنتجات والأصناف (${filteredProducts.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    // Products List
                    items(filteredProducts) { product ->
                        val store = stores.find { it.id == product.sellerId } ?: stores.first()
                        val currentQtyInCart = cartItems[product] ?: 0
                        ProductItemCard(
                            product = product,
                            store = store,
                            qtyInCart = currentQtyInCart,
                            onAdd = { onAddToCart(product, store) },
                            onRemove = { onRemoveFromCart(product) }
                        )
                    }
                }
            } else {
                // My Orders and Tracking Tab
                BuyerOrdersAndTrackingView(
                    orders = orders,
                    trackedOrderId = trackedOrderId,
                    onSelectOrder = onTrackOrder,
                    onSimulateDriverStep = onSimulateDriverStep,
                    onOpenFullTracking = onOpenFullTracking
                )
            }
        }

        // Floating Bottom Cart Bar
        if (cartCount > 0 && selectedBuyerTab == 0) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(10.dp, RoundedCornerShape(20.dp))
                    .clickable { showCartSheet = true }
                    .testTag("floating_cart_bar"),
                shape = RoundedCornerShape(20.dp),
                color = EmeraldPrimary
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(AmberAccent),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$cartCount",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("السلة • ${cartStore?.name ?: ""}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(
                                text = "الإجمالي: ${cartSubtotal} ج.م",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    Button(
                        onClick = { showCartSheet = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("عرض السلة 🛒", color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        // Cart Bottom Sheet
        if (showCartSheet) {
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ModalBottomSheet(
                onDismissRequest = { showCartSheet = false },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CartSheetContent(
                    cartItems = cartItems,
                    cartStore = cartStore,
                    onAddToCart = { prod -> cartStore?.let { onAddToCart(prod, it) } },
                    onRemoveFromCart = onRemoveFromCart,
                    onCheckout = { address, notes ->
                        onPlaceOrder(address, notes)
                        showCartSheet = false
                        selectedBuyerTab = 1
                    },
                    onClose = { showCartSheet = false }
                )
            }
        }
    }
}

@Composable
fun StoreCardItem(store: Store) {
    Card(
        modifier = Modifier
            .width(230.dp)
            .height(130.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(store.iconEmoji, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = store.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = store.category,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF1F5F9))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = AmberAccent, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text("${store.rating}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Text(
                    text = "⏱️ ${store.deliveryTimeMin} دقيقة",
                    fontSize = 11.sp,
                    color = TextSecondary
                )

                Text(
                    text = "توصيل ${store.deliveryFee} ج.م",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldPrimary
                )
            }
        }
    }
}

@Composable
fun ProductItemCard(
    product: ProductEntity,
    store: Store,
    qtyInCart: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = product.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = product.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${product.price} ج.م",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "• ${product.sellerName}",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Cart modifiers
            if (qtyInCart == 0) {
                Button(
                    onClick = onAdd,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("add_product_${product.id}")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("أضف", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldLight)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(4.dp)
                    ) {
                        IconButton(
                            onClick = onRemove,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "إنقاص", tint = EmeraldDark)
                        }
                        Text(
                            text = "$qtyInCart",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldDark,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        )
                        IconButton(
                            onClick = onAdd,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "زيادة", tint = EmeraldDark)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CartSheetContent(
    cartItems: Map<ProductEntity, Int>,
    cartStore: Store?,
    onAddToCart: (ProductEntity) -> Unit,
    onRemoveFromCart: (ProductEntity) -> Unit,
    onCheckout: (address: String, notes: String) -> Unit,
    onClose: () -> Unit
) {
    var deliveryAddress by remember { mutableStateOf("الشارع الرئيسي، سمهود - مركز أبو تشت، قنا") }
    var orderNotes by remember { mutableStateOf("") }
    var showOutOfZoneDialog by remember { mutableStateOf(false) }

    // Coordinates of user selected delivery location on map (default to Semhoud vicinity)
    var userSelectedLat by remember { mutableStateOf(26.0621) }
    var userSelectedLng by remember { mutableStateOf(32.1315) }

    val subtotal = cartItems.entries.sumOf { it.key.price * it.value }
    val deliveryFee = cartStore?.deliveryFee ?: 15.0
    val total = subtotal + deliveryFee

    if (showOutOfZoneDialog) {
        AlertDialog(
            onDismissRequest = { showOutOfZoneDialog = false },
            title = {
                Text(
                    text = "خارج نطاق التوصيل 📍",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color(0xFF1A1A1A)
                )
            },
            text = {
                Text(
                    text = "عذراً، تطبيق 'وصلها' يخدم حالياً منطقة سمهود والقرى المجاورة لها في محيط 10 كم فقط من نقطة الانطلاق الرئيسية.",
                    fontSize = 14.sp,
                    color = Color(0xFF4A4A4A),
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { showOutOfZoneDialog = false }
                ) {
                    Text(
                        text = "فهمت",
                        fontWeight = FontWeight.Bold,
                        color = EmeraldPrimary,
                        fontSize = 15.sp
                    )
                }
            },
            shape = RoundedCornerShape(16.dp),
            containerColor = Color.White
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("سلة مشترياتك 🛒", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = TextPrimary)
                Text("المتجر: ${cartStore?.name ?: ""}", fontSize = 12.sp, color = TextSecondary)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "إغلاق")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Items List
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            cartItems.forEach { (product, count) ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(product.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${product.price} ج.م للواحد", fontSize = 11.sp, color = TextSecondary)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onRemoveFromCart(product) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = null, tint = EmeraldDark)
                            }
                            Text("$count", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp))
                            IconButton(onClick = { onAddToCart(product) }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = EmeraldDark)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "${product.price * count} ج.م",
                                fontWeight = FontWeight.Bold,
                                color = EmeraldPrimary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Delivery address input
        OutlinedTextField(
            value = deliveryAddress,
            onValueChange = { deliveryAddress = it },
            label = { Text("عنوان التوصيل بالتفصيل 📍") },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("delivery_address_input"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Quick location selector for testing/simulation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        userSelectedLat = 26.0621
                        userSelectedLng = 32.1315
                        deliveryAddress = "سمهود - بالقرب من مسجد النور، مركز أبو تشت"
                    }
                    .testTag("btn_location_inside_zone"),
                shape = RoundedCornerShape(8.dp),
                color = EmeraldContainer
            ) {
                Text(
                    text = "📍 سمهود (داخل النطاق)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = EmeraldDark,
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
            }

            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clickable {
                        userSelectedLat = 30.0444
                        userSelectedLng = 31.2357
                        deliveryAddress = "موقع تجريبي خارج النطاق (أكثر من 10 كم)"
                    }
                    .testTag("btn_location_outside_zone"),
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFFFEBEE)
            ) {
                Text(
                    text = "⚠️ خارج نطاق 10 كم (تجربة)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828),
                    modifier = Modifier.padding(vertical = 6.dp, horizontal = 8.dp),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = orderNotes,
            onValueChange = { orderNotes = it },
            label = { Text("ملاحظات خاصة للموصلاتي أو المتجر (اختياري)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Bill breakdown
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = Color(0xFFF8FAF9),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("المجموع الفرعي:", color = TextSecondary, fontSize = 13.sp)
                    Text("$subtotal ج.م", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("أجرة الموصلاتي (التوصيل):", color = TextSecondary, fontSize = 13.sp)
                    Text("$deliveryFee ج.م", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = AmberDark)
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color(0xFFE2E8F0))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("الإجمالي الكلي المستحق:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("$total ج.م", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = EmeraldPrimary)
                }
            }
        }

        // Delivery zone indicator chip
        val isInsideZone = LocationUtils.isInsideDeliveryZone(userSelectedLat, userSelectedLng)
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isInsideZone) EmeraldContainer else Color(0xFFFFEBEE),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isInsideZone) "📍 الموقع محدد داخل نطاق توصيل سمهود (أقل من 10 كم)" else "⚠️ الموقع المحدد خارج نطاق خدمة سمهود (أكثر من 10 كم)",
                    fontSize = 12.sp,
                    color = if (isInsideZone) EmeraldDark else Color(0xFFC62828),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (LocationUtils.isInsideDeliveryZone(userSelectedLat, userSelectedLng)) {
                    // الموقع ممتاز وداخل النطاق المسموح! أكمل العملية
                    onCheckout(deliveryAddress, orderNotes)
                } else {
                    // الموقع خارج الـ 10 كم! أظهر رسالة منع
                    showOutOfZoneDialog = true
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .testTag("confirm_order_button"),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
        ) {
            Text("تأكيد وإرسال الطلب (الدفع عند الاستلام 💵)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BuyerOrdersAndTrackingView(
    orders: List<OrderEntity>,
    trackedOrderId: String?,
    onSelectOrder: (String?) -> Unit,
    onSimulateDriverStep: () -> Unit,
    onOpenFullTracking: ((String) -> Unit)? = null
) {
    val activeTrackedOrder = orders.find { it.orderId == trackedOrderId } ?: orders.firstOrNull()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        if (activeTrackedOrder != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "التتبع الحي للطلب النشط 🛵",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    if (onOpenFullTracking != null) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = EmeraldContainer,
                            modifier = Modifier
                                .clickable { onOpenFullTracking(activeTrackedOrder.orderId) }
                                .testTag("btn_open_full_tracking_screen")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "شاشة التتبع الكاملة 🔍",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldDark
                                )
                            }
                        }
                    }
                }
            }

            item {
                LiveTrackingMap(
                    order = activeTrackedOrder,
                    onSimulateStep = onSimulateDriverStep
                )
            }

            item {
                LiveOrderDetailsCard(
                    order = activeTrackedOrder,
                    onSimulateDriverStep = onSimulateDriverStep
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "سجل طلباتي السابقة والحالية (${orders.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = TextPrimary
            )
        }

        if (orders.isEmpty()) {
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
                        Text("🛍️", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("لا توجد لديك طلبات حالية", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("تصفح المتاجر واختر أصنافك المفضلة لطلبها الآن", color = TextSecondary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(orders) { order ->
                val isSelected = order.orderId == activeTrackedOrder?.orderId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectOrder(order.orderId) }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) EmeraldPrimary else Color.Transparent,
                            shape = RoundedCornerShape(18.dp)
                        ),
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
                            Text(
                                text = "رقم الطلب: ${order.orderId}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            OrderStatusBadge(statusString = order.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Storefront, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(order.sellerName, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = order.itemsSummary,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "الإجمالي: ${order.totalPrice} ج.م",
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldPrimary,
                                fontSize = 14.sp
                            )

                            Button(
                                onClick = { onSelectOrder(order.orderId) },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) EmeraldPrimary else EmeraldContainer,
                                    contentColor = if (isSelected) Color.White else EmeraldDark
                                ),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(if (isSelected) "الطلب المحدد للتتبع" else "تتبع على الخريطة 📍", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LiveOrderDetailsCard(
    order: OrderEntity,
    onSimulateDriverStep: () -> Unit
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
                    Text("طلبك من ${order.sellerName}", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("رقم الطلب: ${order.orderId}", fontSize = 12.sp, color = TextSecondary)
                }
                OrderStatusBadge(statusString = order.status)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Delivery Driver Info card
            if (order.driverName != null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = EmeraldContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🛵", fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(order.driverName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = OnEmeraldContainer)
                                Text(order.driverVehicle ?: "سكوتر توصيل سريع", fontSize = 11.sp, color = EmeraldDark)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            modifier = Modifier.clickable { /* simulate phone call */ }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = "اتصال", tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldPrimary)
                            }
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AmberContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⏳", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (order.status == OrderStatus.PREPARING.name)
                                "المتجر يجهز الأصناف وسيقوم بطلب الموصلاتي قريباً"
                            else
                                "جاري البحث عن أقرب موصلاتي في شبكة وصلها...",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = AmberDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Order Steps Stepper
            OrderProgressStepper(statusString = order.status)

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(10.dp))

            // Address and notes
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AmberDark, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("التوصيل إلى: ${order.dropoffAddress}", fontSize = 12.sp, color = TextPrimary)
            }
        }
    }
}

@Composable
fun OrderProgressStepper(statusString: String) {
    val steps = listOf(
        "قبول الطلب",
        "تجهيز المتجر",
        "تعيين الموصلاتي",
        "جاري التوصيل",
        "تم التسليم"
    )

    val currentStep = when (statusString) {
        OrderStatus.PENDING_ACCEPTANCE.name -> 0
        OrderStatus.PREPARING.name -> 1
        OrderStatus.SEARCHING_FOR_DRIVER.name -> 1
        OrderStatus.DRIVER_ASSIGNED.name, OrderStatus.PICKING_UP.name -> 2
        OrderStatus.IN_TRANSIT.name -> 3
        OrderStatus.COMPLETED.name -> 4
        else -> 0
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, stepName ->
            val isDone = index <= currentStep
            val isCurrent = index == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isCurrent -> AmberAccent
                                isDone -> EmeraldPrimary
                                else -> Color(0xFFE2E8F0)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    } else {
                        Text("${index + 1}", fontSize = 10.sp, color = TextSecondary, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stepName,
                    fontSize = 9.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    color = if (isDone) TextPrimary else TextSecondary,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}
