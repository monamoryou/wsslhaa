package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val titleAr: String, val iconName: String) {
    BUYER("مشتري", "Person"),
    SELLER("بائع / متجر", "Storefront"),
    DRIVER("موصلاتي", "TwoWheeler")
}

data class User(
    val id: String,
    val name: String,
    val phone: String,
    val role: UserRole,
    val avatarUrl: String = "",
    val email: String? = null,
    val authProvider: String = "phone" // "whatsapp" or "google"
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val sellerId: String,
    val sellerName: String,
    val category: String,
    val rating: Double = 4.8
)

data class Store(
    val id: String,
    val name: String,
    val category: String,
    val rating: Double,
    val deliveryTimeMin: Int,
    val deliveryFee: Double,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val iconEmoji: String
)

data class OrderItem(
    val productId: String,
    val productName: String,
    val quantity: Int,
    val price: Double
)

enum class OrderStatus(val titleAr: String, val stepIndex: Int) {
    PENDING_ACCEPTANCE("بانتظار قبول المتجر", 0),
    PREPARING("المتجر يجهز طلبك", 1),
    SEARCHING_FOR_DRIVER("جاري البحث عن موصلاتي", 2),
    DRIVER_ASSIGNED("تم تعيين الموصلاتي", 3),
    PICKING_UP("الموصلاتي في طريقه للمتجر", 3),
    IN_TRANSIT("الطلب في الطريق إليك", 4),
    COMPLETED("تم التسليم بنجاح", 5),
    CANCELLED("ملغي", -1)
}

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val buyerId: String,
    val buyerName: String,
    val buyerPhone: String,
    val sellerId: String,
    val sellerName: String,
    val driverId: String?,
    val driverName: String?,
    val driverPhone: String?,
    val driverVehicle: String?,
    val itemsSummary: String,
    val itemsCount: Int,
    val totalPrice: Double,
    val deliveryFee: Double,
    val status: String, // from OrderStatus name
    val pickupLatitude: Double,
    val pickupLongitude: Double,
    val dropoffLatitude: Double,
    val dropoffLongitude: Double,
    val currentDriverLat: Double,
    val currentDriverLng: Double,
    val pickupAddress: String,
    val dropoffAddress: String,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)
