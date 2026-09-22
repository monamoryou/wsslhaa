package com.example.data.repository

import com.example.data.local.OrderDao
import com.example.data.local.ProductDao
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.ProductEntity
import com.example.data.model.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.util.UUID
import kotlin.random.Random

class WasselhaRepository(
    private val orderDao: OrderDao,
    private val productDao: ProductDao,
    private val appScope: CoroutineScope
) {

    val allOrders: Flow<List<OrderEntity>> = orderDao.getAllOrders()
    val allProducts: Flow<List<ProductEntity>> = productDao.getAllProducts()

    fun getOrderById(orderId: String): Flow<OrderEntity?> = orderDao.getOrderById(orderId)

    fun getOrdersForBuyer(buyerId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersForBuyer(buyerId)

    fun getOrdersForSeller(sellerId: String): Flow<List<OrderEntity>> =
        orderDao.getOrdersForSeller(sellerId)

    fun getAvailableOrdersForDrivers(): Flow<List<OrderEntity>> =
        orderDao.getAvailableOrdersForDrivers()

    fun getActiveOrdersForDriver(driverId: String): Flow<List<OrderEntity>> =
        orderDao.getActiveOrdersForDriver(driverId)

    fun getAllOrdersForDriver(driverId: String): Flow<List<OrderEntity>> =
        orderDao.getAllOrdersForDriver(driverId)

    suspend fun seedInitialDataIfEmpty() {
        if (productDao.getProductCount() == 0) {
            productDao.insertProducts(initialProducts)
            initialOrders.forEach { orderDao.insertOrder(it) }
        }
    }

    suspend fun createOrder(
        buyerId: String,
        buyerName: String,
        buyerPhone: String,
        sellerId: String,
        sellerName: String,
        itemsSummary: String,
        itemsCount: Int,
        totalPrice: Double,
        deliveryFee: Double,
        dropoffAddress: String,
        notes: String
    ): String {
        val orderId = "ORD-${Random.nextInt(1000, 9999)}"
        val store = storesList.find { it.id == sellerId } ?: storesList.first()

        val newOrder = OrderEntity(
            orderId = orderId,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            sellerId = sellerId,
            sellerName = sellerName,
            driverId = null,
            driverName = null,
            driverPhone = null,
            driverVehicle = null,
            itemsSummary = itemsSummary,
            itemsCount = itemsCount,
            totalPrice = totalPrice,
            deliveryFee = deliveryFee,
            status = OrderStatus.PENDING_ACCEPTANCE.name,
            pickupLatitude = store.latitude,
            pickupLongitude = store.longitude,
            dropoffLatitude = store.latitude + 0.022,
            dropoffLongitude = store.longitude + 0.024,
            currentDriverLat = store.latitude,
            currentDriverLng = store.longitude,
            pickupAddress = store.address,
            dropoffAddress = dropoffAddress,
            notes = notes
        )
        orderDao.insertOrder(newOrder)
        return orderId
    }

    suspend fun sellerAcceptOrder(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.PREPARING.name)
    }

    suspend fun sellerRequestDriver(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.SEARCHING_FOR_DRIVER.name)
    }

    suspend fun driverAcceptDelivery(
        orderId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        driverVehicle: String
    ) {
        orderDao.assignDriver(
            orderId = orderId,
            driverId = driverId,
            driverName = driverName,
            driverPhone = driverPhone,
            driverVehicle = driverVehicle,
            status = OrderStatus.PICKING_UP.name
        )
    }

    suspend fun driverConfirmPickup(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.IN_TRANSIT.name)
    }

    suspend fun driverConfirmDelivered(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.COMPLETED.name)
    }

    suspend fun cancelOrder(orderId: String) {
        orderDao.updateOrderStatus(orderId, OrderStatus.CANCELLED.name)
    }

    suspend fun addNewProduct(product: ProductEntity) {
        productDao.insertProduct(product)
    }

    fun startLiveMovementSimulation(orderId: String, pickupLat: Double, pickupLng: Double, dropoffLat: Double, dropoffLng: Double) {
        appScope.launch(Dispatchers.IO) {
            val steps = 20
            for (i in 1..steps) {
                val fraction = i.toDouble() / steps.toDouble()
                val currentLat = pickupLat + (dropoffLat - pickupLat) * fraction
                val currentLng = pickupLng + (dropoffLng - pickupLng) * fraction
                orderDao.updateDriverLocation(orderId, currentLat, currentLng)
                delay(2000)
            }
        }
    }

    companion object {
        val storesList = listOf(
            Store(
                id = "store_1",
                name = "مطعم وكبابجي البرنس",
                category = "مطاعم ومشويات",
                rating = 4.9,
                deliveryTimeMin = 25,
                deliveryFee = 15.0,
                address = "شارع المعز، الزمالك",
                latitude = 30.0511,
                longitude = 31.2355,
                iconEmoji = "🥩"
            ),
            Store(
                id = "store_2",
                name = "سوبرماركت الخيرات والبركة",
                category = "بقالة وسوبرماركت",
                rating = 4.8,
                deliveryTimeMin = 20,
                deliveryFee = 12.0,
                address = "شارع جامعة الدول، المهندسين",
                latitude = 30.0580,
                longitude = 31.2050,
                iconEmoji = "🛒"
            ),
            Store(
                id = "store_3",
                name = "صيدلية الدواء والعافية",
                category = "صيدليات وأدوية",
                rating = 4.9,
                deliveryTimeMin = 15,
                deliveryFee = 10.0,
                address = "ميدان الجيزة، الدقي",
                latitude = 30.0380,
                longitude = 31.2120,
                iconEmoji = "💊"
            ),
            Store(
                id = "store_4",
                name = "كافيه وحلواني العاصمة",
                category = "حلويات ومخبوزات",
                rating = 4.7,
                deliveryTimeMin = 30,
                deliveryFee = 14.0,
                address = "شارع الأهرام، مصر الجديدة",
                latitude = 30.0890,
                longitude = 31.3250,
                iconEmoji = "🍰"
            )
        )

        val initialProducts = listOf(
            ProductEntity(
                id = "p_samhoud_1",
                name = "وجبة كرسبي عائلية",
                description = "5 قطع دجاج مقرمش مع بطاطس وثومية",
                price = 150.0,
                imageUrl = "crispy",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "وجبات عائلية",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_samhoud_2",
                name = "كيلو دقيق فاخر",
                description = "دقيق متعدد الاستخدامات للمخبوزات",
                price = 25.0,
                imageUrl = "flour",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "مواد غذائية",
                rating = 4.8
            ),
            ProductEntity(
                id = "p_samhoud_3",
                name = "علبة عصير طبيعي",
                description = "عصير مانجو طبيعي طازج 1 لتر",
                price = 40.0,
                imageUrl = "juice",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "مشروبات",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_1",
                name = "وجبة كباب وكفتة مشوية فاخرة",
                description = "نصف كيلو مشكل كباب وكفتة على الفحم مع خبز بلدي وطحينة وسلطة خضراء",
                price = 145.0,
                imageUrl = "kebap",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "مشويات",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_2",
                name = "ساندوتش حواوشي مخصوص بالجبنة",
                description = "لحم مفروم بلدي متبل مع جبنة موتزاريلا ذائبة ومقرمش على الجريل",
                price = 45.0,
                imageUrl = "hawawshi",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "ساندوتشات",
                rating = 4.8
            ),
            ProductEntity(
                id = "p_3",
                name = "طاجن ملوخية بالطشة البلدي والأرز",
                description = "طاجن ملوخية خضراء ساخنة مع أرز أبيض بالشعرية ومخلل مشكل",
                price = 55.0,
                imageUrl = "molokhia",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                category = "طواجن",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_4",
                name = "صندوق فواكه طازجة مشكلة",
                description = "مجموعة فواكه موسمية ممتازة (تفاح لبناني، موز، برتقال، عنب) وزن 3 كجم",
                price = 75.0,
                imageUrl = "fruits",
                sellerId = "store_2",
                sellerName = "سوبرماركت الخيرات والبركة",
                category = "فواكه وخضار",
                rating = 4.8
            ),
            ProductEntity(
                id = "p_5",
                name = "حليب طبيعي كامل الدسم 1 لتر",
                description = "حليب طازج معقم مبستر غني بالكالسيوم والفيتامينات",
                price = 28.0,
                imageUrl = "milk",
                sellerId = "store_2",
                sellerName = "سوبرماركت الخيرات والبركة",
                category = "ألبان وأجبان",
                rating = 4.7
            ),
            ProductEntity(
                id = "p_6",
                name = "جبنة فيتا طبيعية فاخرة 500 جم",
                description = "جبنة بيضاء كريمية قليلة الملح غنية بالبروتين",
                price = 42.0,
                imageUrl = "cheese",
                sellerId = "store_2",
                sellerName = "سوبرماركت الخيرات والبركة",
                category = "ألبان وأجبان",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_7",
                name = "فيتامين سي 1000 مجم فوار",
                description = "أقراص فوارة بنكهة البرتقال لتعزيز المناعة والنشاط اليومي (20 قرص)",
                price = 35.0,
                imageUrl = "vitaminc",
                sellerId = "store_3",
                sellerName = "صيدلية الدواء والعافية",
                category = "فيتامينات",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_8",
                name = "مسكن باراسيتامول سريع المفعول",
                description = "علاج آمن وفعال للصداع وتخفيف الآلام وخفض الحرارة (شريطين)",
                price = 20.0,
                imageUrl = "panadol",
                sellerId = "store_3",
                sellerName = "صيدلية الدواء والعافية",
                category = "أدوية",
                rating = 4.8
            ),
            ProductEntity(
                id = "p_9",
                name = "تشيز كيك فراولة طازج",
                description = "قطعة تشيز كيك نيويورك مخبوزة مع صوص الفراولة الطبيعية",
                price = 55.0,
                imageUrl = "cheesecake",
                sellerId = "store_4",
                sellerName = "كافيه وحلواني العاصمة",
                category = "حلويات",
                rating = 4.9
            ),
            ProductEntity(
                id = "p_10",
                name = "قهوة لاتيه كراميل مثلج",
                description = "إسبريسو دبل شوت مع حليب مثلج وصوص الكراميل المركز المقرمش",
                price = 38.0,
                imageUrl = "coffee",
                sellerId = "store_4",
                sellerName = "كافيه وحلواني العاصمة",
                category = "مشروبات",
                rating = 4.8
            )
        )

        val initialOrders = listOf(
            OrderEntity(
                orderId = "ORD-8821",
                buyerId = "buyer_default",
                buyerName = "أحمد مصطفى",
                buyerPhone = "01091234567",
                sellerId = "store_1",
                sellerName = "مطعم وكبابجي البرنس",
                driverId = "driver_1",
                driverName = "كابتن زياد السعدي",
                driverPhone = "01289876543",
                driverVehicle = "سكوتر هوندا أبيض 🛵",
                itemsSummary = "1x وجبة كباب وكفتة مشوية فاخرة، 1x ساندوتش حواوشي مخصوص",
                itemsCount = 2,
                totalPrice = 190.0,
                deliveryFee = 15.0,
                status = OrderStatus.IN_TRANSIT.name,
                pickupLatitude = 30.0511,
                pickupLongitude = 31.2355,
                dropoffLatitude = 30.0725,
                dropoffLongitude = 31.2580,
                currentDriverLat = 30.0630,
                currentDriverLng = 31.2480,
                pickupAddress = "شارع المعز، الزمالك",
                dropoffAddress = "شارع النزهة، مصر الجديدة - شقة 402",
                notes = "يرجى الاتصال عند الوصول بدون رن الجرس"
            ),
            OrderEntity(
                orderId = "ORD-9904",
                buyerId = "buyer_other",
                buyerName = "سارة عبدالله",
                buyerPhone = "01145678901",
                sellerId = "store_2",
                sellerName = "سوبرماركت الخيرات والبركة",
                driverId = null,
                driverName = null,
                driverPhone = null,
                driverVehicle = null,
                itemsSummary = "1x صندوق فواكه طازجة مشكلة، 2x حليب طبيعي كامل الدسم",
                itemsCount = 3,
                totalPrice = 131.0,
                deliveryFee = 12.0,
                status = OrderStatus.SEARCHING_FOR_DRIVER.name,
                pickupLatitude = 30.0580,
                pickupLongitude = 31.2050,
                dropoffLatitude = 30.0440,
                dropoffLongitude = 31.2180,
                currentDriverLat = 30.0580,
                currentDriverLng = 31.2050,
                pickupAddress = "شارع جامعة الدول، المهندسين",
                dropoffAddress = "شارع التحرير، الدقي - عمارة الأمل",
                notes = "الطلب جاهز للاستلام الفوري من الكاشير"
            )
        )
    }
}
