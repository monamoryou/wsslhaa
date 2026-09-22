package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.OrderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    fun getOrderById(orderId: String): Flow<OrderEntity?>

    @Query("SELECT * FROM orders WHERE buyerId = :buyerId ORDER BY createdAt DESC")
    fun getOrdersForBuyer(buyerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE sellerId = :sellerId ORDER BY createdAt DESC")
    fun getOrdersForSeller(sellerId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE status = 'SEARCHING_FOR_DRIVER' ORDER BY createdAt DESC")
    fun getAvailableOrdersForDrivers(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId AND status != 'COMPLETED' AND status != 'CANCELLED' ORDER BY createdAt DESC")
    fun getActiveOrdersForDriver(driverId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE driverId = :driverId ORDER BY createdAt DESC")
    fun getAllOrdersForDriver(driverId: String): Flow<List<OrderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("""
        UPDATE orders 
        SET driverId = :driverId, 
            driverName = :driverName, 
            driverPhone = :driverPhone, 
            driverVehicle = :driverVehicle, 
            status = :status 
        WHERE orderId = :orderId
    """)
    suspend fun assignDriver(
        orderId: String,
        driverId: String,
        driverName: String,
        driverPhone: String,
        driverVehicle: String,
        status: String
    )

    @Query("UPDATE orders SET currentDriverLat = :lat, currentDriverLng = :lng WHERE orderId = :orderId")
    suspend fun updateDriverLocation(orderId: String, lat: Double, lng: Double)
}
