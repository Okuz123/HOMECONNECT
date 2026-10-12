package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.BookingEntity
import com.example.data.repository.LocalConnectRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.UUID

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var database: AppDatabase
  private lateinit var repository: LocalConnectRepository
  private val testDispatcher = UnconfinedTestDispatcher()
  private val testScope = TestScope(testDispatcher)

  @Before
  fun setup() {
    context = ApplicationProvider.getApplicationContext<Context>()
    database = AppDatabase.getDatabase(context, testScope)
    repository = LocalConnectRepository(database)
  }

  @Test
  fun `read app_name string resource matches My Connect`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("My Connect", appName)
  }

  @Test
  fun `database seed and repository data check`() = runTest {
    // Seed initial database
    AppDatabase.seedInitialData(database)

    val categories = repository.getActiveCategories().first()
    assertTrue(categories.isNotEmpty())

    val profiles = repository.getAllProfiles().first()
    assertTrue(profiles.any { it.role == "ADMINISTRATOR" })
    assertTrue(profiles.any { it.role == "CUSTOMER" })
    assertTrue(profiles.any { it.role == "BUSINESS_OWNER" })

    val businesses = repository.getAllBusinessesAdmin().first()
    assertTrue(businesses.isNotEmpty())
  }

  @Test
  fun `booking status lifecycle and commission calculation test`() = runTest {
    AppDatabase.seedInitialData(database)

    val bookingId = "bk_test_${UUID.randomUUID().toString().take(6)}"
    val booking = BookingEntity(
      id = bookingId,
      customerId = "usr_cust_1",
      businessId = "biz_apex_plumbing",
      serviceId = "srv_apex_1",
      description = "Test emergency pipe leak",
      preferredDate = "2026-10-20",
      preferredTime = "10:00 AM - 12:00 PM",
      serviceArea = "Bandra West",
      protectedServiceAddress = "101, Test Road, Mumbai",
      customerPhone = "+91 98201 12345",
      agreedPrice = 500.0,
      status = "PENDING"
    )

    repository.createBooking(booking)
    val fetched = repository.getBookingByIdSync(bookingId)
    assertNotNull(fetched)
    assertEquals("PENDING", fetched?.status)

    // Provider accepts -> transitions to SCHEDULED
    repository.updateBookingStatus(bookingId, "SCHEDULED")
    assertEquals("SCHEDULED", repository.getBookingByIdSync(bookingId)?.status)

    // Complete booking -> calculates and inserts commission
    repository.updateBookingStatus(bookingId, "COMPLETED")
    assertEquals("COMPLETED", repository.getBookingByIdSync(bookingId)?.status)

    val commissions = repository.getCommissionsForBusiness("biz_apex_plumbing").first()
    val matchingComm = commissions.firstOrNull { it.bookingId == bookingId }
    assertNotNull(matchingComm)
    // 10% of 500.0 = 50.0
    assertEquals(50.0, matchingComm!!.commissionAmount, 0.01)
  }

  @Test
  fun `24x7 in-app payment processing test`() = runTest {
    AppDatabase.seedInitialData(database)

    val bookingId = "bk_pay_${UUID.randomUUID().toString().take(6)}"
    val booking = BookingEntity(
      id = bookingId,
      customerId = "usr_cust_1",
      businessId = "biz_apex_plumbing",
      serviceId = "srv_apex_1",
      description = "24x7 Emergency plumbing fix",
      preferredDate = "2026-10-22",
      preferredTime = "11:00 PM - 01:00 AM",
      serviceArea = "Bandra West",
      protectedServiceAddress = "Flat 502, Sea Green, Mumbai",
      customerPhone = "+91 98201 12345",
      agreedPrice = 799.0,
      status = "SCHEDULED"
    )
    repository.createBooking(booking)

    // Pay 24/7 online
    repository.payForBooking(bookingId, "UPI", 799.0, "TXN_247_UPI_TEST123")

    val updatedBooking = repository.getBookingByIdSync(bookingId)
    assertNotNull(updatedBooking)
    assertEquals("PAID_ONLINE", updatedBooking?.paymentStatus)
    assertEquals("TXN_247_UPI_TEST123", updatedBooking?.paymentId)
    assertEquals("UPI", updatedBooking?.paymentMethod)
  }
}
