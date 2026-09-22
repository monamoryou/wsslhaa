package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.WasselhaDatabase
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.Store
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.WasselhaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class WasselhaViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: WasselhaRepository
    val stores = WasselhaRepository.storesList

    private val _currentRole = MutableStateFlow(UserRole.BUYER)
    val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

    private val _showAccountSelection = MutableStateFlow(true)
    val showAccountSelection: StateFlow<Boolean> = _showAccountSelection.asStateFlow()

    private val _selectedStoreId = MutableStateFlow<String?>(null)
    val selectedStoreId: StateFlow<String?> = _selectedStoreId.asStateFlow()

    private val _selectedCategory = MutableStateFlow("الكل")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Cart items: ProductEntity -> quantity
    private val _cartItems = MutableStateFlow<Map<ProductEntity, Int>>(emptyMap())
    val cartItems: StateFlow<Map<ProductEntity, Int>> = _cartItems.asStateFlow()

    private val _cartStore = MutableStateFlow<Store?>(null)
    val cartStore: StateFlow<Store?> = _cartStore.asStateFlow()

    // Active order tracked by buyer or driver
    private val _trackedOrderId = MutableStateFlow<String?>("ORD-8821")
    val trackedOrderId: StateFlow<String?> = _trackedOrderId.asStateFlow()

    // Driver online status
    private val _driverIsOnline = MutableStateFlow(true)
    val driverIsOnline: StateFlow<Boolean> = _driverIsOnline.asStateFlow()

    // Snackbar notification
    private val _notificationMessage = MutableStateFlow<String?>(null)
    val notificationMessage: StateFlow<String?> = _notificationMessage.asStateFlow()

    // Authentication State (WhatsApp & Google Gmail)
    private val _currentUser = MutableStateFlow<User?>(
        User(
            id = "user_default",
            name = "أحمد مصطفى",
            phone = "01091234567",
            role = UserRole.BUYER,
            email = "nameend1@gmail.com",
            authProvider = "google"
        )
    )
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isAuthDialogOpen = MutableStateFlow(false)
    val isAuthDialogOpen: StateFlow<Boolean> = _isAuthDialogOpen.asStateFlow()

    val allProducts: StateFlow<List<ProductEntity>>
    val allOrders: StateFlow<List<OrderEntity>>
    val availableDeliveryOrders: StateFlow<List<OrderEntity>>

    init {
        val database = WasselhaDatabase.getInstance(application)
        repository = WasselhaRepository(
            orderDao = database.orderDao(),
            productDao = database.productDao(),
            appScope = viewModelScope
        )

        allProducts = repository.allProducts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        allOrders = repository.allOrders.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        availableDeliveryOrders = repository.getAvailableOrdersForDrivers().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        showNotification("تم التبديل إلى واجهة: ${role.titleAr}")
    }

    fun openAccountSelection() {
        _showAccountSelection.value = true
    }

    fun selectAccountType(role: UserRole) {
        _currentRole.value = role
        _showAccountSelection.value = false
        showNotification("مرحباً بك في وصلها بصفتك: ${role.titleAr}")
    }

    fun openAuthDialog() {
        _isAuthDialogOpen.value = true
    }

    fun closeAuthDialog() {
        _isAuthDialogOpen.value = false
    }

    fun loginWithGoogle(email: String, name: String) {
        _currentUser.value = User(
            id = "user_google_${System.currentTimeMillis()}",
            name = name,
            phone = "",
            role = _currentRole.value,
            email = email,
            authProvider = "google"
        )
        _isAuthDialogOpen.value = false
        showNotification("تم تسجيل الدخول بنجاح بحساب Google ($email) ✅")
    }

    fun loginWithPhone(phone: String, name: String = "مستخدم وصلها") {
        _currentUser.value = User(
            id = "user_wa_${System.currentTimeMillis()}",
            name = name,
            phone = phone,
            role = _currentRole.value,
            email = null,
            authProvider = "whatsapp"
        )
        _isAuthDialogOpen.value = false
        showNotification("تم توثيق الرقم $phone بنجاح عبر واتساب ✅")
    }

    fun logout() {
        _currentUser.value = null
        showNotification("تم تسجيل الخروج")
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedStore(storeId: String?) {
        _selectedStoreId.value = storeId
    }

    fun addToCart(product: ProductEntity, store: Store) {
        val current = _cartItems.value.toMutableMap()
        val currentStore = _cartStore.value

        // If cart has items from different store, reset or switch
        if (currentStore != null && currentStore.id != store.id && current.isNotEmpty()) {
            current.clear()
            showNotification("تم بدء سلة جديدة لمتجر ${store.name}")
        }
        _cartStore.value = store

        val qty = current[product] ?: 0
        current[product] = qty + 1
        _cartItems.value = current
        showNotification("تمت إضافة ${product.name} إلى السلة")
    }

    fun removeFromCart(product: ProductEntity) {
        val current = _cartItems.value.toMutableMap()
        val qty = current[product] ?: 0
        if (qty <= 1) {
            current.remove(product)
        } else {
            current[product] = qty - 1
        }
        _cartItems.value = current
        if (current.isEmpty()) {
            _cartStore.value = null
        }
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _cartStore.value = null
    }

    fun placeOrder(address: String, notes: String) {
        val currentCart = _cartItems.value
        val store = _cartStore.value
        if (currentCart.isEmpty() || store == null) return

        val itemsSummary = currentCart.entries.joinToString("، ") { "${it.value}x ${it.key.name}" }
        val itemsCount = currentCart.values.sum()
        val subtotal = currentCart.entries.sumOf { it.key.price * it.value }
        val deliveryFee = store.deliveryFee
        val total = subtotal + deliveryFee

        viewModelScope.launch {
            val orderId = repository.createOrder(
                buyerId = "buyer_default",
                buyerName = "أحمد مصطفى (العميل)",
                buyerPhone = "01091234567",
                sellerId = store.id,
                sellerName = store.name,
                itemsSummary = itemsSummary,
                itemsCount = itemsCount,
                totalPrice = total,
                deliveryFee = deliveryFee,
                dropoffAddress = address.ifBlank { "شارع مصطفى النحاس، مدينة نصر" },
                notes = notes
            )
            clearCart()
            _trackedOrderId.value = orderId
            showNotification("تم إرسال الطلب $orderId بنجاح إلى ${store.name}!")
        }
    }

    fun sellerAcceptOrder(orderId: String) {
        viewModelScope.launch {
            repository.sellerAcceptOrder(orderId)
            showNotification("تم قبول الطلب وجاري التجهيز بنجاح ✅")
        }
    }

    fun sellerRequestDriver(orderId: String) {
        viewModelScope.launch {
            repository.sellerRequestDriver(orderId)
            showNotification("تم إرسال طلب الموصلاتي! سيظهر في شبكة الموصلاتية 🛵")
        }
    }

    fun driverAcceptDelivery(order: OrderEntity) {
        viewModelScope.launch {
            repository.driverAcceptDelivery(
                orderId = order.orderId,
                driverId = "driver_1",
                driverName = "كابتن سيف الدين",
                driverPhone = "01123344556",
                driverVehicle = "سكوتر ياماها رمادي 🛵"
            )
            _trackedOrderId.value = order.orderId
            showNotification("مبروك! قبلت توصيل الطلب ${order.orderId}")
        }
    }

    fun driverConfirmPickup(orderId: String, order: OrderEntity?) {
        viewModelScope.launch {
            repository.driverConfirmPickup(orderId)
            showNotification("تم استلام الطلب من المتجر وجاري التوصيل للعميل 🚀")
            if (order != null) {
                repository.startLiveMovementSimulation(
                    orderId = orderId,
                    pickupLat = order.pickupLatitude,
                    pickupLng = order.pickupLongitude,
                    dropoffLat = order.dropoffLatitude,
                    dropoffLng = order.dropoffLongitude
                )
            }
        }
    }

    fun driverConfirmDelivered(orderId: String) {
        viewModelScope.launch {
            repository.driverConfirmDelivered(orderId)
            showNotification("تم تسليم الطلب للعميل بنجاح وتحصيل المبلغ! 🎉")
        }
    }

    fun toggleDriverOnline() {
        _driverIsOnline.value = !_driverIsOnline.value
        val state = if (_driverIsOnline.value) "متصل وجاهز للطلبات" else "غير متصل"
        showNotification("حالة الكابتن: $state")
    }

    fun setTrackedOrder(orderId: String?) {
        _trackedOrderId.value = orderId
    }

    fun addNewProduct(
        name: String,
        price: Double,
        category: String,
        description: String,
        sellerId: String,
        sellerName: String
    ) {
        viewModelScope.launch {
            val newProduct = ProductEntity(
                id = "p_${System.currentTimeMillis()}",
                name = name,
                description = description,
                price = price,
                imageUrl = "custom",
                sellerId = sellerId,
                sellerName = sellerName,
                category = category,
                rating = 5.0
            )
            repository.addNewProduct(newProduct)
            showNotification("تمت إضافة المنتج \"$name\" إلى المتجر بنجاح!")
        }
    }

    fun showNotification(message: String) {
        _notificationMessage.value = message
    }

    fun clearNotification() {
        _notificationMessage.value = null
    }
}
