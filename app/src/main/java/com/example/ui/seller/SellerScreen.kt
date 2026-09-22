package com.example.ui.seller

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ProductEntity
import com.example.data.model.Store
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
fun SellerScreen(
    stores: List<Store>,
    orders: List<OrderEntity>,
    products: List<ProductEntity>,
    onAcceptOrder: (String) -> Unit,
    onRequestDriver: (String) -> Unit,
    onAddNewProduct: (name: String, price: Double, category: String, desc: String, storeId: String, storeName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    // Current active store demo is store_1 or store_2
    var selectedStoreIndex by remember { mutableIntStateOf(0) }
    val currentStore = stores.getOrElse(selectedStoreIndex) { stores.first() }

    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddProductDialog by remember { mutableStateOf(false) }

    val storeOrders = orders.filter { it.sellerId == currentStore.id || it.sellerName == currentStore.name }
    val storeProducts = products.filter { it.sellerId == currentStore.id }

    val pendingOrders = storeOrders.filter { it.status == OrderStatus.PENDING_ACCEPTANCE.name }
    val preparingOrders = storeOrders.filter { it.status == OrderStatus.PREPARING.name }
    val inTransitOrders = storeOrders.filter {
        it.status == OrderStatus.SEARCHING_FOR_DRIVER.name ||
                it.status == OrderStatus.DRIVER_ASSIGNED.name ||
                it.status == OrderStatus.PICKING_UP.name ||
                it.status == OrderStatus.IN_TRANSIT.name
    }
    val completedOrders = storeOrders.filter { it.status == OrderStatus.COMPLETED.name }

    val totalRevenue = storeOrders.filter { it.status == OrderStatus.COMPLETED.name }
        .sumOf { it.totalPrice } + 385.0 // sample baseline

    Column(modifier = modifier.fillMaxSize()) {
        // Store Selector & Header Card
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
                                .clip(RoundedCornerShape(12.dp))
                                .background(EmeraldContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentStore.iconEmoji, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "🏪 لوحة تحكم المتجر (سمهود)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                modifier = Modifier.testTag("tvStoreName")
                            )
                            Text(
                                text = "${currentStore.name} • متصل ومفتوح 🟢",
                                fontSize = 12.sp,
                                color = EmeraldDark,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Switch store if multiple
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable {
                            selectedStoreIndex = (selectedStoreIndex + 1) % stores.size
                        }
                    ) {
                        Text(
                            text = "تبديل المتجر 🔄",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = EmeraldPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SellerStatBox(
                        title = "الطلبات الواردة",
                        value = "${pendingOrders.size + preparingOrders.size}",
                        icon = "📦",
                        color = AmberAccent,
                        modifier = Modifier.weight(1f)
                    )
                    SellerStatBox(
                        title = "قيد التوصيل",
                        value = "${inTransitOrders.size}",
                        icon = "🛵",
                        color = EmeraldPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    SellerStatBox(
                        title = "المبيعات اليوم",
                        value = "${totalRevenue.toInt()} ج.م",
                        icon = "💰",
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1.2f)
                    )
                }
            }
        }

        // Tabs: الطلبات الواردة vs منتجات المتجر
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = EmeraldPrimary,
                    height = 3.dp
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الطلبات الواردة (${storeOrders.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Inventory, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("قائمة المنتجات (${storeProducts.size})", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            )
        }

        if (selectedTab == 0) {
            // Orders List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (storeOrders.isEmpty()) {
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
                                Text("📋", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("لا توجد طلبات جديدة حالياً", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("يمكنك التبديل لدور المشتري وعمل طلب تجريبي من متجرك!", color = TextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    items(storeOrders) { order ->
                        SellerOrderCard(
                            order = order,
                            onAccept = { onAcceptOrder(order.orderId) },
                            onRequestDriver = { onRequestDriver(order.orderId) }
                        )
                    }
                }
            }
        } else {
            // Products Management (rvProducts & fabAddProduct)
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("rvProducts"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("المنتجات المعروضة للزبائن", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(
                                text = "${storeProducts.size} منتج متاح",
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }

                    if (storeProducts.isEmpty()) {
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
                                    Text("📦", fontSize = 36.sp)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("لا توجد منتجات مضافة بعد", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text("اضغط على زر الإضافة (+) بالأسفل لإضافة أول منتج لمتجرك", color = TextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    } else {
                        items(storeProducts) { product ->
                            // كارت المنتج المطابق لمخطط XML
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("product_item_card_${product.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // صورة المنتج بحواف دائرية (ivProductItemImage)
                                    Card(
                                        modifier = Modifier.size(80.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .testTag("ivProductItemImage"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Image,
                                                contentDescription = product.name,
                                                tint = Color(0xFF9E9E9E),
                                                modifier = Modifier.size(36.dp)
                                            )
                                        }
                                    }

                                    // تفاصيل المنتج (الاسم، الوصف)
                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 16.dp)
                                    ) {
                                        Text(
                                            text = product.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = Color(0xFF1A1A1A),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("tvProductItemName")
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Text(
                                            text = if (product.description.isNotBlank()) product.description else "وصف المنتج المختصر يظهر هنا...",
                                            fontSize = 13.sp,
                                            color = Color(0xFF757575),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("tvProductItemDesc")
                                        )
                                    }

                                    // السعر (tvProductItemPrice)
                                    Text(
                                        text = "${product.price} ج.م",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF4CAF50),
                                        modifier = Modifier.testTag("tvProductItemPrice")
                                    )
                                }
                            }
                        }
                    }
                }

                // زر إضافة منتج جديد عائم (fabAddProduct)
                FloatingActionButton(
                    onClick = { showAddProductDialog = true },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .testTag("fabAddProduct"),
                    containerColor = Color(0xFF2196F3),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة منتج",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    }

    // Add Product Dialog
    if (showAddProductDialog) {
        AddProductDialog(
            storeName = currentStore.name,
            onDismiss = { showAddProductDialog = false },
            onConfirm = { name, price, category, desc ->
                onAddNewProduct(name, price, category, desc, currentStore.id, currentStore.name)
                showAddProductDialog = false
            }
        )
    }
}

@Composable
fun SellerOrderCard(
    order: OrderEntity,
    onAccept: () -> Unit,
    onRequestDriver: () -> Unit
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
                Column {
                    Text(
                        text = "طلب رقم ${order.orderId}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "العميل: ${order.buyerName} • ${order.buyerPhone}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                OrderStatusBadge(statusString = order.status)
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFFF1F5F9))
            Spacer(modifier = Modifier.height(8.dp))

            // Items breakdown
            Text(
                text = "محتويات الطلب:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondary
            )
            Text(
                text = order.itemsSummary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )

            if (order.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "ملاحظة العميل: ${order.notes}",
                        fontSize = 11.sp,
                        color = AmberDark,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "العنوان: ${order.dropoffAddress.take(28)}...",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Text(
                    text = "${order.totalPrice} ج.م",
                    fontWeight = FontWeight.ExtraBold,
                    color = EmeraldPrimary,
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seller Action Buttons based on order state
            when (order.status) {
                OrderStatus.PENDING_ACCEPTANCE.name -> {
                    Button(
                        onClick = onAccept,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("seller_accept_order_${order.orderId}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("قبول وتجهيز الطلب الآن ✅", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                OrderStatus.PREPARING.name -> {
                    Button(
                        onClick = onRequestDriver,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("seller_request_driver_${order.orderId}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AmberAccent, contentColor = Color.Black)
                    ) {
                        Icon(Icons.Default.TwoWheeler, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("الطلب جاهز • طلب موصلاتي 🛵", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
                OrderStatus.SEARCHING_FOR_DRIVER.name -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFE0F2FE),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("جاري البحث عن موصلاتي في المنطقة... 🛵📡", fontSize = 12.sp, color = Color(0xFF0369A1), fontWeight = FontWeight.Bold)
                        }
                    }
                }
                OrderStatus.DRIVER_ASSIGNED.name, OrderStatus.PICKING_UP.name, OrderStatus.IN_TRANSIT.name -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("الموصلاتي: ${order.driverName ?: "كابتن زياد"}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = OnEmeraldContainer)
                            Text("في الطريق 🚀", fontSize = 11.sp, color = EmeraldDark, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
                OrderStatus.COMPLETED.name -> {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = EmeraldContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text("تم تسليم الطلب واستلام الحساب بنجاح 🎉", fontSize = 12.sp, color = EmeraldPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerStatBox(
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

@Composable
fun AddProductDialog(
    storeName: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, price: Double, category: String, desc: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("وجبات ومأكولات") }
    var desc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("إضافة صنف جديد لـ $storeName", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المنتج أو الوجبة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("السعر بالجنيه (ج.م)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("التصنيف (مثال: مشويات، حلويات)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("الوصف والمكونات") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 50.0
                    if (name.isNotBlank()) {
                        onConfirm(name, price, category, desc)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
            ) {
                Text("حفظ ونشر المنتج")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء")
            }
        }
    )
}
