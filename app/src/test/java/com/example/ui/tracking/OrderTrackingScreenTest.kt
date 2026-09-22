package com.example.ui.tracking

import com.example.data.model.OrderEntity
import com.example.data.model.OrderStatus
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class OrderTrackingScreenTest {

    private fun createDummyOrder(status: String = OrderStatus.IN_TRANSIT.name): OrderEntity {
        return OrderEntity(
            orderId = "ORD-TEST-100",
            buyerId = "buyer_1",
            buyerName = "أحمد مصطفى",
            buyerPhone = "01091234567",
            sellerId = "store_1",
            sellerName = "مطعم ومشويات البركة",
            driverId = "driver_1",
            driverName = "كابتن سيف الدين",
            driverPhone = "01123344556",
            driverVehicle = "سكوتر دايون سريع",
            itemsSummary = "2x كباب وكفتة مشكل",
            itemsCount = 2,
            totalPrice = 285.0,
            deliveryFee = 15.0,
            status = status,
            pickupLatitude = 26.0620,
            pickupLongitude = 32.1310,
            dropoffLatitude = 26.0690,
            dropoffLongitude = 32.1380,
            currentDriverLat = 26.0650,
            currentDriverLng = 32.1340,
            pickupAddress = "سمهود - بجوار المحطة",
            dropoffAddress = "شارع الجمهورية، سمهود",
            createdAt = 1700000000000L,
            notes = "الرجاء عدم الاتصال بالجرس"
        )
    }

    @Test
    fun orderEntity_inTransit_hasValidDriverDetails() {
        val order = createDummyOrder(OrderStatus.IN_TRANSIT.name)
        assertEquals("ORD-TEST-100", order.orderId)
        assertEquals("كابتن سيف الدين", order.driverName)
        assertEquals("01123344556", order.driverPhone)
        assertEquals(OrderStatus.IN_TRANSIT.name, order.status)
        assertEquals(285.0, order.totalPrice, 0.01)
    }

    @Test
    fun orderStatus_stepProgression_isSequential() {
        assertTrue(OrderStatus.PENDING_ACCEPTANCE.stepIndex < OrderStatus.PREPARING.stepIndex)
        assertTrue(OrderStatus.PREPARING.stepIndex < OrderStatus.DRIVER_ASSIGNED.stepIndex)
        assertTrue(OrderStatus.DRIVER_ASSIGNED.stepIndex < OrderStatus.IN_TRANSIT.stepIndex)
        assertTrue(OrderStatus.IN_TRANSIT.stepIndex < OrderStatus.COMPLETED.stepIndex)
    }

    @Test
    fun timelineStepInfo_creation_isAccurate() {
        val step = TimelineStepInfo(
            title = "الطلب في الطريق إليك",
            subtitle = "الموصلاتي يتجه نحو عنوانك",
            timestamp = "جاري الآن",
            isCompleted = true,
            isCurrent = true,
            icon = Icons.Default.TwoWheeler
        )

        assertEquals("الطلب في الطريق إليك", step.title)
        assertTrue(step.isCompleted)
        assertTrue(step.isCurrent)
    }
}
