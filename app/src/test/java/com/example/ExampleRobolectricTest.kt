package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Wasselha", appName)
  }

  @Test
  fun `verify semhoud delivery zone location check`() {
    // Exact center of Semhoud (0 meters away)
    val isInsideCenter = com.example.wasselha.utils.LocationUtils.isInsideDeliveryZone(26.050631, 32.124564)
    org.junit.Assert.assertTrue(isInsideCenter)

    // Point approx 2 km away in Qena
    val isInsideNear = com.example.wasselha.utils.LocationUtils.isInsideDeliveryZone(26.06, 32.13)
    org.junit.Assert.assertTrue(isInsideNear)

    // Far away (e.g. Cairo ~ 500 km)
    val isInsideCairo = com.example.wasselha.utils.LocationUtils.isInsideDeliveryZone(30.0444, 31.2357)
    org.junit.Assert.assertFalse(isInsideCairo)
  }

  @Test
  fun `verify dummyProducts and product adapter binding`() {
    val dummyList = com.example.wasselha.seller.ProductAdapter.dummyProducts
    assertEquals(3, dummyList.size)
    assertEquals("وجبة كرسبي عائلية", dummyList[0].name)
    assertEquals(150.0, dummyList[0].price, 0.001)

    val adapter = com.example.wasselha.seller.ProductAdapter(dummyList)
    assertEquals(3, adapter.itemCount)
  }
}
