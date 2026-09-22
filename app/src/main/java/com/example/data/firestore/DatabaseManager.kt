package com.example.data.firestore

import android.content.Context
import android.util.Log
import com.example.data.auth.FirebaseAuthManager
import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import com.example.data.model.Store
import com.example.data.model.User
import com.example.data.model.UserRole
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

// =============================================================================
// FIRESTORE DTO MODELS
// =============================================================================

/**
 * Firestore representation of a User Profile.
 */
data class UserProfile(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String? = null,
    val role: String = UserRole.BUYER.name,
    val avatarUrl: String = "",
    val authProvider: String = "phone", // "phone", "whatsapp", "google"
    val fcmToken: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toUser(): User {
        val parsedRole = try {
            UserRole.valueOf(role)
        } catch (_: Exception) {
            UserRole.BUYER
        }
        return User(
            id = id,
            name = name,
            phone = phone,
            role = parsedRole,
            avatarUrl = avatarUrl,
            email = email,
            authProvider = authProvider
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "phone" to phone,
        "email" to email,
        "role" to role,
        "avatarUrl" to avatarUrl,
        "authProvider" to authProvider,
        "fcmToken" to fcmToken,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromUser(user: User, fcmToken: String? = null): UserProfile {
            val now = System.currentTimeMillis()
            return UserProfile(
                id = user.id,
                name = user.name,
                phone = user.phone,
                email = user.email,
                role = user.role.name,
                avatarUrl = user.avatarUrl,
                authProvider = user.authProvider,
                fcmToken = fcmToken,
                createdAt = now,
                updatedAt = now
            )
        }

        fun fromSnapshot(snapshot: DocumentSnapshot): UserProfile? {
            if (!snapshot.exists()) return null
            return try {
                UserProfile(
                    id = snapshot.getString("id") ?: snapshot.id,
                    name = snapshot.getString("name").orEmpty(),
                    phone = snapshot.getString("phone").orEmpty(),
                    email = snapshot.getString("email"),
                    role = snapshot.getString("role") ?: UserRole.BUYER.name,
                    avatarUrl = snapshot.getString("avatarUrl").orEmpty(),
                    authProvider = snapshot.getString("authProvider") ?: "phone",
                    fcmToken = snapshot.getString("fcmToken"),
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.w("UserProfile", "Error parsing UserProfile from snapshot: ${e.message}")
                null
            }
        }
    }
}

/**
 * Firestore representation of Store information.
 */
data class StoreInfo(
    val id: String = "",
    val name: String = "",
    val category: String = "",
    val rating: Double = 4.8,
    val deliveryTimeMin: Int = 30,
    val deliveryFee: Double = 15.0,
    val address: String = "",
    val latitude: Double = 30.0444,
    val longitude: Double = 31.2357,
    val iconEmoji: String = "🏪",
    val isOpen: Boolean = true,
    val phone: String = "",
    val description: String = "",
    val ownerId: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toStore(): Store {
        return Store(
            id = id,
            name = name,
            category = category,
            rating = rating,
            deliveryTimeMin = deliveryTimeMin,
            deliveryFee = deliveryFee,
            address = address,
            latitude = latitude,
            longitude = longitude,
            iconEmoji = iconEmoji
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "name" to name,
        "category" to category,
        "rating" to rating,
        "deliveryTimeMin" to deliveryTimeMin,
        "deliveryFee" to deliveryFee,
        "address" to address,
        "latitude" to latitude,
        "longitude" to longitude,
        "iconEmoji" to iconEmoji,
        "isOpen" to isOpen,
        "phone" to phone,
        "description" to description,
        "ownerId" to ownerId,
        "updatedAt" to updatedAt
    )

    companion object {
        fun fromStore(
            store: Store,
            isOpen: Boolean = true,
            phone: String = "",
            description: String = "",
            ownerId: String? = null
        ): StoreInfo {
            return StoreInfo(
                id = store.id,
                name = store.name,
                category = store.category,
                rating = store.rating,
                deliveryTimeMin = store.deliveryTimeMin,
                deliveryFee = store.deliveryFee,
                address = store.address,
                latitude = store.latitude,
                longitude = store.longitude,
                iconEmoji = store.iconEmoji,
                isOpen = isOpen,
                phone = phone,
                description = description,
                ownerId = ownerId,
                updatedAt = System.currentTimeMillis()
            )
        }

        fun fromSnapshot(snapshot: DocumentSnapshot): StoreInfo? {
            if (!snapshot.exists()) return null
            return try {
                StoreInfo(
                    id = snapshot.getString("id") ?: snapshot.id,
                    name = snapshot.getString("name").orEmpty(),
                    category = snapshot.getString("category").orEmpty(),
                    rating = snapshot.getDouble("rating") ?: 4.8,
                    deliveryTimeMin = snapshot.getLong("deliveryTimeMin")?.toInt() ?: 30,
                    deliveryFee = snapshot.getDouble("deliveryFee") ?: 15.0,
                    address = snapshot.getString("address").orEmpty(),
                    latitude = snapshot.getDouble("latitude") ?: 30.0444,
                    longitude = snapshot.getDouble("longitude") ?: 31.2357,
                    iconEmoji = snapshot.getString("iconEmoji") ?: "🏪",
                    isOpen = snapshot.getBoolean("isOpen") ?: true,
                    phone = snapshot.getString("phone").orEmpty(),
                    description = snapshot.getString("description").orEmpty(),
                    ownerId = snapshot.getString("ownerId"),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } catch (e: Exception) {
                Log.w("StoreInfo", "Error parsing StoreInfo from snapshot: ${e.message}")
                null
            }
        }
    }
}

/**
 * Firestore representation of active delivery status and order telemetry.
 */
data class DeliveryStatusInfo(
    val orderId: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val buyerPhone: String = "",
    val sellerId: String = "",
    val sellerName: String = "",
    val driverId: String? = null,
    val driverName: String? = null,
    val driverPhone: String? = null,
    val driverVehicle: String? = null,
    val itemsSummary: String = "",
    val itemsCount: Int = 0,
    val totalPrice: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val status: String = OrderStatus.PENDING_ACCEPTANCE.name,
    val statusArabic: String = OrderStatus.PENDING_ACCEPTANCE.titleAr,
    val stepIndex: Int = OrderStatus.PENDING_ACCEPTANCE.stepIndex,
    val pickupLatitude: Double = 30.0444,
    val pickupLongitude: Double = 31.2357,
    val dropoffLatitude: Double = 30.0500,
    val dropoffLongitude: Double = 31.2400,
    val currentDriverLat: Double = 30.0444,
    val currentDriverLng: Double = 31.2357,
    val pickupAddress: String = "",
    val dropoffAddress: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val isDelivered: Boolean = false,
    val isCancelled: Boolean = false
) {
    val orderStatus: OrderStatus
        get() = try {
            OrderStatus.valueOf(status)
        } catch (_: Exception) {
            OrderStatus.PENDING_ACCEPTANCE
        }

    fun toOrderEntity(): OrderEntity {
        return OrderEntity(
            orderId = orderId,
            buyerId = buyerId,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            sellerId = sellerId,
            sellerName = sellerName,
            driverId = driverId,
            driverName = driverName,
            driverPhone = driverPhone,
            driverVehicle = driverVehicle,
            itemsSummary = itemsSummary,
            itemsCount = itemsCount,
            totalPrice = totalPrice,
            deliveryFee = deliveryFee,
            status = status,
            pickupLatitude = pickupLatitude,
            pickupLongitude = pickupLongitude,
            dropoffLatitude = dropoffLatitude,
            dropoffLongitude = dropoffLongitude,
            currentDriverLat = currentDriverLat,
            currentDriverLng = currentDriverLng,
            pickupAddress = pickupAddress,
            dropoffAddress = dropoffAddress,
            createdAt = createdAt,
            notes = notes
        )
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "orderId" to orderId,
        "buyerId" to buyerId,
        "buyerName" to buyerName,
        "buyerPhone" to buyerPhone,
        "sellerId" to sellerId,
        "sellerName" to sellerName,
        "driverId" to driverId,
        "driverName" to driverName,
        "driverPhone" to driverPhone,
        "driverVehicle" to driverVehicle,
        "itemsSummary" to itemsSummary,
        "itemsCount" to itemsCount,
        "totalPrice" to totalPrice,
        "deliveryFee" to deliveryFee,
        "status" to status,
        "statusArabic" to statusArabic,
        "stepIndex" to stepIndex,
        "pickupLatitude" to pickupLatitude,
        "pickupLongitude" to pickupLongitude,
        "dropoffLatitude" to dropoffLatitude,
        "dropoffLongitude" to dropoffLongitude,
        "currentDriverLat" to currentDriverLat,
        "currentDriverLng" to currentDriverLng,
        "pickupAddress" to pickupAddress,
        "dropoffAddress" to dropoffAddress,
        "notes" to notes,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "isDelivered" to isDelivered,
        "isCancelled" to isCancelled
    )

    companion object {
        fun fromOrderEntity(order: OrderEntity): DeliveryStatusInfo {
            val currentStatus = try {
                OrderStatus.valueOf(order.status)
            } catch (_: Exception) {
                OrderStatus.PENDING_ACCEPTANCE
            }
            return DeliveryStatusInfo(
                orderId = order.orderId,
                buyerId = order.buyerId,
                buyerName = order.buyerName,
                buyerPhone = order.buyerPhone,
                sellerId = order.sellerId,
                sellerName = order.sellerName,
                driverId = order.driverId,
                driverName = order.driverName,
                driverPhone = order.driverPhone,
                driverVehicle = order.driverVehicle,
                itemsSummary = order.itemsSummary,
                itemsCount = order.itemsCount,
                totalPrice = order.totalPrice,
                deliveryFee = order.deliveryFee,
                status = order.status,
                statusArabic = currentStatus.titleAr,
                stepIndex = currentStatus.stepIndex,
                pickupLatitude = order.pickupLatitude,
                pickupLongitude = order.pickupLongitude,
                dropoffLatitude = order.dropoffLatitude,
                dropoffLongitude = order.dropoffLongitude,
                currentDriverLat = order.currentDriverLat,
                currentDriverLng = order.currentDriverLng,
                pickupAddress = order.pickupAddress,
                dropoffAddress = order.dropoffAddress,
                notes = order.notes,
                createdAt = order.createdAt,
                updatedAt = System.currentTimeMillis(),
                isDelivered = currentStatus == OrderStatus.COMPLETED,
                isCancelled = currentStatus == OrderStatus.CANCELLED
            )
        }

        fun fromSnapshot(snapshot: DocumentSnapshot): DeliveryStatusInfo? {
            if (!snapshot.exists()) return null
            return try {
                val statusStr = snapshot.getString("status") ?: OrderStatus.PENDING_ACCEPTANCE.name
                val parsedStatus = try {
                    OrderStatus.valueOf(statusStr)
                } catch (_: Exception) {
                    OrderStatus.PENDING_ACCEPTANCE
                }
                DeliveryStatusInfo(
                    orderId = snapshot.getString("orderId") ?: snapshot.id,
                    buyerId = snapshot.getString("buyerId").orEmpty(),
                    buyerName = snapshot.getString("buyerName").orEmpty(),
                    buyerPhone = snapshot.getString("buyerPhone").orEmpty(),
                    sellerId = snapshot.getString("sellerId").orEmpty(),
                    sellerName = snapshot.getString("sellerName").orEmpty(),
                    driverId = snapshot.getString("driverId"),
                    driverName = snapshot.getString("driverName"),
                    driverPhone = snapshot.getString("driverPhone"),
                    driverVehicle = snapshot.getString("driverVehicle"),
                    itemsSummary = snapshot.getString("itemsSummary").orEmpty(),
                    itemsCount = snapshot.getLong("itemsCount")?.toInt() ?: 0,
                    totalPrice = snapshot.getDouble("totalPrice") ?: 0.0,
                    deliveryFee = snapshot.getDouble("deliveryFee") ?: 0.0,
                    status = statusStr,
                    statusArabic = snapshot.getString("statusArabic") ?: parsedStatus.titleAr,
                    stepIndex = snapshot.getLong("stepIndex")?.toInt() ?: parsedStatus.stepIndex,
                    pickupLatitude = snapshot.getDouble("pickupLatitude") ?: 30.0444,
                    pickupLongitude = snapshot.getDouble("pickupLongitude") ?: 31.2357,
                    dropoffLatitude = snapshot.getDouble("dropoffLatitude") ?: 30.0500,
                    dropoffLongitude = snapshot.getDouble("dropoffLongitude") ?: 31.2400,
                    currentDriverLat = snapshot.getDouble("currentDriverLat") ?: 30.0444,
                    currentDriverLng = snapshot.getDouble("currentDriverLng") ?: 31.2357,
                    pickupAddress = snapshot.getString("pickupAddress").orEmpty(),
                    dropoffAddress = snapshot.getString("dropoffAddress").orEmpty(),
                    notes = snapshot.getString("notes").orEmpty(),
                    createdAt = snapshot.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = snapshot.getLong("updatedAt") ?: System.currentTimeMillis(),
                    isDelivered = snapshot.getBoolean("isDelivered") ?: (parsedStatus == OrderStatus.COMPLETED),
                    isCancelled = snapshot.getBoolean("isCancelled") ?: (parsedStatus == OrderStatus.CANCELLED)
                )
            } catch (e: Exception) {
                Log.w("DeliveryStatusInfo", "Error parsing DeliveryStatusInfo: ${e.message}")
                null
            }
        }
    }
}

// =============================================================================
// DATABASE MANAGER
// =============================================================================

/**
 * Production-ready Firestore Database Manager for Wasselha.
 *
 * Manages:
 * 1. User Profiles (`users` collection)
 * 2. Store Information (`stores` collection)
 * 3. Active Delivery Statuses & Driver Telemetry (`deliveries` collection)
 */
class DatabaseManager(
    firestoreInstance: FirebaseFirestore? = null
) {
    private val firestore: FirebaseFirestore by lazy {
        firestoreInstance ?: FirebaseFirestore.getInstance()
    }

    // =========================================================================
    // 1. USER PROFILES
    // =========================================================================

    /**
     * Stores or updates a user profile in Firestore.
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_USERS).document(profile.id)
        val data = profile.copy(updatedAt = System.currentTimeMillis()).toMap()
        awaitTask(docRef.set(data, SetOptions.merge()))
        Log.d(TAG, "User profile saved: ${profile.id}")
    }

    /**
     * Stores or updates a domain [User] as a Firestore profile.
     */
    suspend fun saveUser(user: User, fcmToken: String? = null): Result<Unit> {
        val profile = UserProfile.fromUser(user, fcmToken)
        return saveUserProfile(profile)
    }

    /**
     * Fetches a user profile once from Firestore.
     */
    suspend fun getUserProfile(userId: String): Result<UserProfile?> = runCatching {
        val snapshot = awaitTask(firestore.collection(COLLECTION_USERS).document(userId).get())
        UserProfile.fromSnapshot(snapshot)
    }

    /**
     * Observes real-time updates to a user's profile.
     */
    fun observeUserProfile(userId: String): Flow<UserProfile?> = callbackFlow {
        val docRef = firestore.collection(COLLECTION_USERS).document(userId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeUserProfile failed for $userId: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.let { UserProfile.fromSnapshot(it) })
        }
        awaitClose { listener.remove() }
    }

    /**
     * Updates specific user profile fields (e.g., active role, phone, avatar).
     */
    suspend fun updateUserFields(userId: String, updates: Map<String, Any?>): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_USERS).document(userId)
        val payload = updates.toMutableMap()
        payload["updatedAt"] = System.currentTimeMillis()
        awaitTask(docRef.update(payload))
        Log.d(TAG, "User fields updated for: $userId")
    }

    /**
     * Updates user role (BUYER, SELLER, DRIVER).
     */
    suspend fun updateUserRole(userId: String, role: UserRole): Result<Unit> {
        return updateUserFields(userId, mapOf("role" to role.name))
    }

    /**
     * Deletes user profile document.
     */
    suspend fun deleteUserProfile(userId: String): Result<Unit> = runCatching {
        awaitTask(firestore.collection(COLLECTION_USERS).document(userId).delete())
        Log.d(TAG, "User profile deleted: $userId")
    }

    // =========================================================================
    // 2. STORE INFORMATION
    // =========================================================================

    /**
     * Stores or updates a store profile in Firestore.
     */
    suspend fun saveStore(storeInfo: StoreInfo): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_STORES).document(storeInfo.id)
        val data = storeInfo.copy(updatedAt = System.currentTimeMillis()).toMap()
        awaitTask(docRef.set(data, SetOptions.merge()))
        Log.d(TAG, "Store saved: ${storeInfo.id}")
    }

    /**
     * Stores a domain [Store] entity into Firestore.
     */
    suspend fun saveStore(store: Store, isOpen: Boolean = true): Result<Unit> {
        return saveStore(StoreInfo.fromStore(store, isOpen = isOpen))
    }

    /**
     * Fetches a store by its ID once.
     */
    suspend fun getStore(storeId: String): Result<StoreInfo?> = runCatching {
        val snapshot = awaitTask(firestore.collection(COLLECTION_STORES).document(storeId).get())
        StoreInfo.fromSnapshot(snapshot)
    }

    /**
     * Observes real-time changes to a specific store.
     */
    fun observeStore(storeId: String): Flow<StoreInfo?> = callbackFlow {
        val docRef = firestore.collection(COLLECTION_STORES).document(storeId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeStore failed for $storeId: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.let { StoreInfo.fromSnapshot(it) })
        }
        awaitClose { listener.remove() }
    }

    /**
     * Fetches all registered stores once.
     */
    suspend fun getAllStores(): Result<List<StoreInfo>> = runCatching {
        val snapshot = awaitTask(firestore.collection(COLLECTION_STORES).get())
        snapshot.documents.mapNotNull { StoreInfo.fromSnapshot(it) }
    }

    /**
     * Observes all stores in real time.
     */
    fun observeAllStores(): Flow<List<StoreInfo>> = callbackFlow {
        val query = firestore.collection(COLLECTION_STORES)
        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeAllStores failed: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val stores = snapshot?.documents?.mapNotNull { StoreInfo.fromSnapshot(it) } ?: emptyList()
            trySend(stores)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Updates store open/closed status.
     */
    suspend fun setStoreOpenStatus(storeId: String, isOpen: Boolean): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_STORES).document(storeId)
        awaitTask(docRef.update("isOpen", isOpen, "updatedAt", System.currentTimeMillis()))
    }

    /**
     * Seeds initial list of stores if the collection is empty.
     */
    suspend fun seedInitialStoresIfEmpty(stores: List<Store>): Result<Unit> = runCatching {
        val snapshot = awaitTask(firestore.collection(COLLECTION_STORES).limit(1).get())
        if (snapshot.isEmpty) {
            val batch = firestore.batch()
            for (store in stores) {
                val doc = firestore.collection(COLLECTION_STORES).document(store.id)
                batch.set(doc, StoreInfo.fromStore(store).toMap())
            }
            awaitTask(batch.commit())
            Log.d(TAG, "Seeded ${stores.size} stores to Firestore")
        }
    }

    // =========================================================================
    // 3. ACTIVE DELIVERY STATUSES & LIVE TRACKING
    // =========================================================================

    /**
     * Stores or updates an active delivery status document.
     */
    suspend fun saveDeliveryStatus(delivery: DeliveryStatusInfo): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_DELIVERIES).document(delivery.orderId)
        val data = delivery.copy(updatedAt = System.currentTimeMillis()).toMap()
        awaitTask(docRef.set(data, SetOptions.merge()))
        Log.d(TAG, "Delivery status saved for order: ${delivery.orderId}")
    }

    /**
     * Converts and saves a domain [OrderEntity] as an active delivery in Firestore.
     */
    suspend fun saveOrderAsDelivery(order: OrderEntity): Result<Unit> {
        return saveDeliveryStatus(DeliveryStatusInfo.fromOrderEntity(order))
    }

    /**
     * Fetches current delivery status for an order once.
     */
    suspend fun getDeliveryStatus(orderId: String): Result<DeliveryStatusInfo?> = runCatching {
        val snapshot = awaitTask(firestore.collection(COLLECTION_DELIVERIES).document(orderId).get())
        DeliveryStatusInfo.fromSnapshot(snapshot)
    }

    /**
     * Observes real-time delivery status for a specific order (used by OrderTrackingScreen).
     */
    fun observeDeliveryStatus(orderId: String): Flow<DeliveryStatusInfo?> = callbackFlow {
        val docRef = firestore.collection(COLLECTION_DELIVERIES).document(orderId)
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeDeliveryStatus error for $orderId: ${error.message}")
                trySend(null)
                return@addSnapshotListener
            }
            trySend(snapshot?.let { DeliveryStatusInfo.fromSnapshot(it) })
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observes all active (undelivered & uncancelled) deliveries in real time.
     */
    fun observeActiveDeliveries(): Flow<List<DeliveryStatusInfo>> = callbackFlow {
        val query = firestore.collection(COLLECTION_DELIVERIES)
            .whereEqualTo("isDelivered", false)
            .whereEqualTo("isCancelled", false)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeActiveDeliveries error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val deliveries = snapshot?.documents?.mapNotNull { DeliveryStatusInfo.fromSnapshot(it) } ?: emptyList()
            trySend(deliveries)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observes active deliveries assigned to a specific driver.
     */
    fun observeDriverDeliveries(driverId: String): Flow<List<DeliveryStatusInfo>> = callbackFlow {
        val query = firestore.collection(COLLECTION_DELIVERIES)
            .whereEqualTo("driverId", driverId)
            .whereEqualTo("isDelivered", false)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeDriverDeliveries error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { DeliveryStatusInfo.fromSnapshot(it) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Observes deliveries created for a specific buyer.
     */
    fun observeBuyerDeliveries(buyerId: String): Flow<List<DeliveryStatusInfo>> = callbackFlow {
        val query = firestore.collection(COLLECTION_DELIVERIES)
            .whereEqualTo("buyerId", buyerId)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "observeBuyerDeliveries error: ${error.message}")
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.documents?.mapNotNull { DeliveryStatusInfo.fromSnapshot(it) } ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    /**
     * Updates order lifecycle status (e.g. PREPARING, DRIVER_ASSIGNED, IN_TRANSIT, COMPLETED).
     */
    suspend fun updateDeliveryStatus(
        orderId: String,
        newStatus: OrderStatus,
        notes: String? = null
    ): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_DELIVERIES).document(orderId)
        val updates = mutableMapOf<String, Any?>(
            "status" to newStatus.name,
            "statusArabic" to newStatus.titleAr,
            "stepIndex" to newStatus.stepIndex,
            "isDelivered" to (newStatus == OrderStatus.COMPLETED),
            "isCancelled" to (newStatus == OrderStatus.CANCELLED),
            "updatedAt" to System.currentTimeMillis()
        )
        if (notes != null) {
            updates["notes"] = notes
        }
        awaitTask(docRef.update(updates))
        Log.d(TAG, "Delivery status updated to ${newStatus.name} for $orderId")
    }

    /**
     * Updates real-time GPS telemetry for the delivery driver on the active order.
     */
    suspend fun updateDriverLocation(
        orderId: String,
        latitude: Double,
        longitude: Double
    ): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_DELIVERIES).document(orderId)
        val updates = mapOf(
            "currentDriverLat" to latitude,
            "currentDriverLng" to longitude,
            "updatedAt" to System.currentTimeMillis()
        )
        awaitTask(docRef.update(updates))
    }

    /**
     * Assigns a driver (موصلاتي) to an active delivery order.
     */
    suspend fun assignDriver(
        orderId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        driverVehicle: String
    ): Result<Unit> = runCatching {
        val docRef = firestore.collection(COLLECTION_DELIVERIES).document(orderId)
        val updates = mapOf(
            "driverId" to driverId,
            "driverName" to driverName,
            "driverPhone" to driverPhone,
            "driverVehicle" to driverVehicle,
            "status" to OrderStatus.DRIVER_ASSIGNED.name,
            "statusArabic" to OrderStatus.DRIVER_ASSIGNED.titleAr,
            "stepIndex" to OrderStatus.DRIVER_ASSIGNED.stepIndex,
            "updatedAt" to System.currentTimeMillis()
        )
        awaitTask(docRef.update(updates))
        Log.d(TAG, "Driver $driverName ($driverId) assigned to $orderId")
    }

    // =========================================================================
    // COROUTINE EXTENSION HELPERS
    // =========================================================================

    private suspend fun <T> awaitTask(task: Task<T>): T = suspendCancellableCoroutine { continuation ->
        task.addOnSuccessListener { result ->
            if (continuation.isActive) {
                continuation.resume(result)
            }
        }.addOnFailureListener { exception ->
            if (continuation.isActive) {
                continuation.resumeWithException(exception)
            }
        }
    }

    companion object {
        const val TAG = "DatabaseManager"

        const val COLLECTION_USERS = "users"
        const val COLLECTION_STORES = "stores"
        const val COLLECTION_DELIVERIES = "deliveries"

        @Volatile
        private var INSTANCE: DatabaseManager? = null

        /**
         * Singleton accessor for [DatabaseManager].
         */
        fun getInstance(context: Context? = null): DatabaseManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    context?.let { FirebaseAuthManager.ensureFirebaseInitialized(it) }
                    DatabaseManager().also { INSTANCE = it }
                }
            }
        }
    }
}
