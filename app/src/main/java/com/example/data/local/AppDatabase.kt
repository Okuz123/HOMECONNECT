package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ProfileEntity::class,
    BusinessEntity::class,
    CategoryEntity::class,
    ServiceEntity::class,
    BookingEntity::class,
    SubscriptionEntity::class,
    SubscriptionPlanEntity::class,
    PaymentEntity::class,
    CommissionEntity::class,
    ReviewEntity::class,
    FavoriteEntity::class,
    NotificationEntity::class,
    ReportEntity::class,
    VerificationDocumentEntity::class,
    AdminAuditLogEntity::class,
    PlatformSettingsEntity::class,
  ],
  version = 2,
  exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun profileDao(): ProfileDao
  abstract fun businessDao(): BusinessDao
  abstract fun categoryDao(): CategoryDao
  abstract fun serviceDao(): ServiceDao
  abstract fun bookingDao(): BookingDao
  abstract fun subscriptionDao(): SubscriptionDao
  abstract fun paymentDao(): PaymentDao
  abstract fun commissionDao(): CommissionDao
  abstract fun reviewDao(): ReviewDao
  abstract fun favoriteDao(): FavoriteDao
  abstract fun notificationDao(): NotificationDao
  abstract fun reportDao(): ReportDao
  abstract fun documentDao(): DocumentDao
  abstract fun auditLogDao(): AuditLogDao
  abstract fun settingsDao(): SettingsDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "local_connect_marketplace.db"
        )
        .addCallback(DatabaseCallback(scope))
        .fallbackToDestructiveMigration()
        .build()
        INSTANCE = instance
        instance
      }
    }

    private class DatabaseCallback(
      private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
      override fun onCreate(db: SupportSQLiteDatabase) {
        super.onCreate(db)
        INSTANCE?.let { database ->
          scope.launch(Dispatchers.IO) {
            seedInitialData(database)
          }
        }
      }
    }

    suspend fun seedInitialData(database: AppDatabase) {
      val now = System.currentTimeMillis()

      // 1. Seed Profiles
      val profiles = listOf(
        ProfileEntity(
          id = "usr_cust_1",
          fullName = "Aarav Sharma",
          email = "aarav.sharma@example.in",
          phone = "+91 98201 12345",
          role = "CUSTOMER",
          preferredCity = "Mumbai",
          createdAt = now - 86400000L * 30
        ),
        ProfileEntity(
          id = "usr_cust_2",
          fullName = "Priya Patel",
          email = "priya.patel@example.in",
          phone = "+91 98450 54321",
          role = "CUSTOMER",
          preferredCity = "Bengaluru",
          createdAt = now - 86400000L * 15
        ),
        ProfileEntity(
          id = "usr_owner_1",
          fullName = "Rajesh Kumar",
          email = "rajesh@apexplumbing.in",
          phone = "+91 98111 22334",
          role = "BUSINESS_OWNER",
          preferredCity = "Mumbai",
          createdAt = now - 86400000L * 45
        ),
        ProfileEntity(
          id = "usr_owner_2",
          fullName = "Vikram Malhotra",
          email = "vikram@voltpro.in",
          phone = "+91 98765 67890",
          role = "BUSINESS_OWNER",
          preferredCity = "Mumbai",
          createdAt = now - 86400000L * 25
        ),
        ProfileEntity(
          id = "usr_provider_1",
          fullName = "Sunil Verma",
          email = "sunil.verma@servicepro.in",
          phone = "+91 99300 44556",
          role = "SERVICE_PROVIDER",
          preferredCity = "Mumbai",
          createdAt = now - 86400000L * 20
        ),
        ProfileEntity(
          id = "usr_admin_1",
          fullName = "Divya Singh (Administrator)",
          email = "admin@localconnect.app",
          phone = "+91 80000 99887",
          role = "ADMINISTRATOR",
          preferredCity = "Mumbai",
          createdAt = now - 86400000L * 60
        )
      )
      database.profileDao().insertProfiles(profiles)

      // 2. Seed Categories
      val categories = listOf(
        CategoryEntity("cat_plumbing", "Plumbing", "plumbing", null, "ic_plumbing", 1, true),
        CategoryEntity("cat_electrical", "Electrical Work", "electrical", null, "ic_electrical", 2, true),
        CategoryEntity("cat_cleaning", "Home Cleaning", "cleaning", null, "ic_cleaning", 3, true),
        CategoryEntity("cat_painting", "Painting", "painting", null, "ic_painting", 4, true),
        CategoryEntity("cat_appliance", "Appliance Repair", "appliance-repair", null, "ic_appliance", 5, true),
        CategoryEntity("cat_mobile", "Mobile Repair", "mobile-repair", null, "ic_mobile", 6, true),
        CategoryEntity("cat_computer", "Computer Repair", "computer-repair", null, "ic_computer", 7, true),
        CategoryEntity("cat_mechanics", "Mechanics & Auto", "mechanics", null, "ic_mechanic", 8, true),
        CategoryEntity("cat_restaurants", "Restaurants & Cafes", "restaurants", null, "ic_restaurant", 9, true),
        CategoryEntity("cat_salons", "Salons & Barbers", "salons", null, "ic_salon", 10, true),
        CategoryEntity("cat_tutors", "Tutors & Coaching", "tutors", null, "ic_tutor", 11, true),
        CategoryEntity("cat_tailoring", "Tailoring & Alterations", "tailoring", null, "ic_tailor", 12, true),
        CategoryEntity("cat_other", "Other Local Services", "other-services", null, "ic_other", 13, true),
      )
      database.categoryDao().insertCategories(categories)

      // 3. Seed Subscription Plans
      val plans = listOf(
        SubscriptionPlanEntity(
          id = "plan_free",
          name = "Free Plan",
          monthlyPrice = 0.0,
          currency = "INR",
          features = "Basic business profile, Standard directory listing, Limited profile customization, Standard search placement",
          active = true
        ),
        SubscriptionPlanEntity(
          id = "plan_featured",
          name = "Featured Plan",
          monthlyPrice = 299.0,
          currency = "INR",
          features = "Featured badge & top placement, Priority category listing, Up to 15 gallery photos, Verified badge highlight, Direct booking requests",
          active = true
        ),
        SubscriptionPlanEntity(
          id = "plan_premium",
          name = "Premium Growth Plan",
          monthlyPrice = 599.0,
          currency = "INR",
          features = "Top spot in Search & Category, Zero commission discount (5% off), Advanced Analytics & CRM tools, Dedicated account manager, SMS & WhatsApp alert triggers",
          active = true
        )
      )
      database.subscriptionDao().insertPlans(plans)

      // 4. Seed Businesses
      val businesses = listOf(
        BusinessEntity(
          id = "biz_apex_plumbing",
          ownerId = "usr_owner_1",
          name = "Apex Plumbing & Leak Specialists",
          slug = "apex-plumbing-mumbai",
          categoryId = "cat_plumbing",
          description = "Certified master plumbers with 12+ years experience in leak detection, water tank cleaning, bathroom fittings, and emergency pipe bursts.",
          publicPhone = "+91 98111 22334",
          publicEmail = "contact@apexplumbing.in",
          city = "Mumbai",
          serviceAreas = "Bandra, Andheri, Juhu, Powai, Santacruz",
          publicAddress = "Shop 14, Sunrise Plaza, Link Road, Bandra West, Mumbai 400050",
          openingHours = "Mon - Sat: 8:00 AM - 9:00 PM, Emergency 24/7",
          verificationStatus = "APPROVED",
          rejectionReason = "",
          listingStatus = "ACTIVE",
          isFeatured = true,
          rating = 4.9f,
          reviewCount = 48,
          createdAt = now - 86400000L * 40
        ),
        BusinessEntity(
          id = "biz_voltpro_electrical",
          ownerId = "usr_owner_2",
          name = "VoltPro Electricals & Smart Home",
          slug = "voltpro-electricals-mumbai",
          categoryId = "cat_electrical",
          description = "Licensed government electrical contractors. Wiring installation, circuit breakers, inverter setups, chandeliers, and home automation.",
          publicPhone = "+91 98765 67890",
          publicEmail = "service@voltpro.in",
          city = "Mumbai",
          serviceAreas = "Powai, Ghatkopar, Vikhroli, Thane, Mulund",
          publicAddress = "Unit 4B, Technopark Central, Powai, Mumbai 400076",
          openingHours = "Mon - Sun: 9:00 AM - 8:00 PM",
          verificationStatus = "APPROVED",
          rejectionReason = "",
          listingStatus = "ACTIVE",
          isFeatured = true,
          rating = 4.8f,
          reviewCount = 32,
          createdAt = now - 86400000L * 25
        ),
        BusinessEntity(
          id = "biz_sparkle_cleaning",
          ownerId = "usr_owner_1",
          name = "SparkleClean Deep Cleaning & Sanitization",
          slug = "sparkleclean-mumbai",
          categoryId = "cat_cleaning",
          description = "Eco-friendly residential and commercial deep cleaning. Sofa shampooing, kitchen chimney degreasing, bathroom scrubbing, and post-renovation cleanup.",
          publicPhone = "+91 98222 33445",
          publicEmail = "book@sparkleclean.in",
          city = "Mumbai",
          serviceAreas = "All Western & Central Mumbai Suburbs",
          publicAddress = "102, Green Avenue, Goregaon East, Mumbai 400063",
          openingHours = "Mon - Sun: 7:30 AM - 7:30 PM",
          verificationStatus = "APPROVED",
          rejectionReason = "",
          listingStatus = "ACTIVE",
          isFeatured = false,
          rating = 4.7f,
          reviewCount = 19,
          createdAt = now - 86400000L * 20
        ),
        BusinessEntity(
          id = "biz_cooltech_repairs",
          ownerId = "usr_owner_2",
          name = "CoolTech Appliance Repair Experts",
          slug = "cooltech-bengaluru",
          categoryId = "cat_appliance",
          description = "Expert technicians for AC servicing, refrigerator cooling fixes, washing machine motors, and microwave oven issues. 90-day service warranty.",
          publicPhone = "+91 98450 99881",
          publicEmail = "care@cooltechrepairs.in",
          city = "Bengaluru",
          serviceAreas = "Koramangala, Indiranagar, HSR Layout, Whitefield, Bellandur",
          publicAddress = "Shop 3, 100 Feet Road, Indiranagar, Bengaluru 560038",
          openingHours = "Mon - Sat: 9:00 AM - 8:30 PM",
          verificationStatus = "APPROVED",
          rejectionReason = "",
          listingStatus = "ACTIVE",
          isFeatured = true,
          rating = 4.9f,
          reviewCount = 41,
          createdAt = now - 86400000L * 35
        ),
        BusinessEntity(
          id = "biz_colorcraft_painting",
          ownerId = "usr_provider_1",
          name = "ColorCraft Interior & Exterior Painting",
          slug = "colorcraft-mumbai",
          categoryId = "cat_painting",
          description = "Professional house painters, waterproofing specialists, texture walls, and stencil artists with dust-free mechanical sanding tools.",
          publicPhone = "+91 99300 44556",
          publicEmail = "colorcraft@paintstudio.in",
          city = "Mumbai",
          serviceAreas = "Dadar, Worli, Lower Parel, Prabhadevi",
          publicAddress = "G-2, Industrial Estate, Worli, Mumbai 400018",
          openingHours = "Mon - Sat: 8:30 AM - 7:00 PM",
          verificationStatus = "UNDER_REVIEW", // Under review for admin approval verification!
          rejectionReason = "",
          listingStatus = "ACTIVE",
          isFeatured = false,
          rating = 4.6f,
          reviewCount = 0,
          createdAt = now - 86400000L * 2
        )
      )
      database.businessDao().insertBusinesses(businesses)

      // 5. Seed Services
      val services = listOf(
        ServiceEntity(
          id = "srv_apex_1",
          businessId = "biz_apex_plumbing",
          name = "Water Leakage & Tap Repair",
          description = "Inspection and immediate fixing of leaking faucets, flush valves, and joint sealants.",
          priceType = "FIXED",
          startingPrice = 299.0,
          fixedPrice = 299.0,
          estimatedDuration = "45 Mins",
          active = true
        ),
        ServiceEntity(
          id = "srv_apex_2",
          businessId = "biz_apex_plumbing",
          name = "Drain & Pipe Blockage Removal",
          description = "Mechanical snaking and chemical clearing of clogged kitchen sinks, showers, or main drain pipes.",
          priceType = "STARTING_AT",
          startingPrice = 599.0,
          fixedPrice = 0.0,
          estimatedDuration = "1 - 2 Hours",
          active = true
        ),
        ServiceEntity(
          id = "srv_apex_3",
          businessId = "biz_apex_plumbing",
          name = "Complete Bathroom Fittings Installation",
          description = "Mounting shower heads, geyser plumbing lines, diverters, health faucets, and vanity basins.",
          priceType = "INSPECTION_REQUIRED",
          startingPrice = 999.0,
          fixedPrice = 0.0,
          estimatedDuration = "2 - 4 Hours",
          active = true
        ),
        ServiceEntity(
          id = "srv_volt_1",
          businessId = "biz_voltpro_electrical",
          name = "Switchboard & Short Circuit Fix",
          description = "Safety inspection, MCB trip diagnostics, switchboard rewiring, and socket replacement.",
          priceType = "FIXED",
          startingPrice = 349.0,
          fixedPrice = 349.0,
          estimatedDuration = "1 Hour",
          active = true
        ),
        ServiceEntity(
          id = "srv_volt_2",
          businessId = "biz_voltpro_electrical",
          name = "Ceiling Fan / Chandelier Installation",
          description = "Secure ceiling hook drilling, wiring balancing, and regulator connection.",
          priceType = "FIXED",
          startingPrice = 249.0,
          fixedPrice = 249.0,
          estimatedDuration = "45 Mins",
          active = true
        ),
        ServiceEntity(
          id = "srv_sparkle_1",
          businessId = "biz_sparkle_cleaning",
          name = "Full Home Deep Cleaning (2 BHK)",
          description = "Machine scrubbing of floors, balcony washing, complete kitchen degreasing, and 2 bathrooms deep sanitized.",
          priceType = "FIXED",
          startingPrice = 2499.0,
          fixedPrice = 2499.0,
          estimatedDuration = "4 - 5 Hours",
          active = true
        ),
        ServiceEntity(
          id = "srv_cool_1",
          businessId = "biz_cooltech_repairs",
          name = "Split AC Foam Jet Deep Servicing",
          description = "High-pressure jet pump cleaning of indoor cooling coils and outdoor condenser unit.",
          priceType = "FIXED",
          startingPrice = 599.0,
          fixedPrice = 599.0,
          estimatedDuration = "1 Hour",
          active = true
        )
      )
      database.serviceDao().insertServices(services)

      // 6. Seed Subscriptions
      val subscriptions = listOf(
        SubscriptionEntity(
          id = "sub_apex_1",
          businessId = "biz_apex_plumbing",
          planId = "plan_featured",
          paymentProviderSubscriptionId = "sub_rzp_mock_881923",
          status = "ACTIVE",
          startDate = now - 86400000L * 15,
          renewalDate = now + 86400000L * 15,
          createdAt = now - 86400000L * 15
        ),
        SubscriptionEntity(
          id = "sub_volt_1",
          businessId = "biz_voltpro_electrical",
          planId = "plan_featured",
          paymentProviderSubscriptionId = "sub_rzp_mock_442918",
          status = "ACTIVE",
          startDate = now - 86400000L * 10,
          renewalDate = now + 86400000L * 20,
          createdAt = now - 86400000L * 10
        )
      )
      subscriptions.forEach { database.subscriptionDao().insertSubscription(it) }

      // 7. Seed Bookings
      val bookings = listOf(
        BookingEntity(
          id = "bk_001_comp",
          customerId = "usr_cust_1",
          businessId = "biz_apex_plumbing",
          serviceId = "srv_apex_1",
          description = "Kitchen sink valve leaking water onto cabinet continuously.",
          preferredDate = "2026-10-05",
          preferredTime = "10:00 AM - 12:00 PM",
          serviceArea = "Bandra West",
          protectedServiceAddress = "Flat 402, Sea View Heights, Hill Road, Bandra West, Mumbai",
          customerPhone = "+91 98201 12345",
          agreedPrice = 299.0,
          status = "COMPLETED",
          createdAt = now - 86400000L * 7,
          updatedAt = now - 86400000L * 6,
          completedAt = now - 86400000L * 6
        ),
        BookingEntity(
          id = "bk_002_sched",
          customerId = "usr_cust_1",
          businessId = "biz_voltpro_electrical",
          serviceId = "srv_volt_1",
          description = "Master bedroom main switch sparking when turning on AC.",
          preferredDate = "2026-10-15",
          preferredTime = "02:00 PM - 04:00 PM",
          serviceArea = "Powai",
          protectedServiceAddress = "Tower B, Hiranandani Gardens, Powai, Mumbai",
          customerPhone = "+91 98201 12345",
          agreedPrice = 349.0,
          status = "SCHEDULED",
          createdAt = now - 86400000L * 1,
          updatedAt = now - 86400000L * 1
        ),
        BookingEntity(
          id = "bk_003_pend",
          customerId = "usr_cust_2",
          businessId = "biz_apex_plumbing",
          serviceId = "srv_apex_2",
          description = "Bathroom shower floor drain blocked with slow drainage.",
          preferredDate = "2026-10-16",
          preferredTime = "11:00 AM - 01:00 PM",
          serviceArea = "Andheri West",
          protectedServiceAddress = "B-301, Lokhandwala Complex, Andheri West, Mumbai",
          customerPhone = "+91 98450 54321",
          agreedPrice = 599.0,
          status = "PENDING",
          createdAt = now - 3600000L * 3,
          updatedAt = now - 3600000L * 3
        )
      )
      database.bookingDao().insertBookings(bookings)

      // 8. Seed Reviews for completed booking
      val reviews = listOf(
        ReviewEntity(
          id = "rev_001",
          bookingId = "bk_001_comp",
          customerId = "usr_cust_1",
          customerName = "Aarav Sharma",
          businessId = "biz_apex_plumbing",
          rating = 5,
          reviewText = "Extremely fast service! Arrived in 30 minutes, diagnosed the pipe seal problem and fixed it neatly. Clean work and fair upfront pricing.",
          moderationStatus = "APPROVED",
          createdAt = now - 86400000L * 6
        )
      )
      database.reviewDao().insertReviews(reviews)

      // 9. Seed Verification Documents for Admin Review
      val docs = listOf(
        VerificationDocumentEntity(
          id = "doc_apex_gst",
          businessId = "biz_apex_plumbing",
          privateFilePath = "docs/businesses/apex_plumbing_gstin_cert.pdf",
          documentType = "GST_CERTIFICATE",
          reviewStatus = "APPROVED",
          reviewedBy = "usr_admin_1",
          reviewedAt = now - 86400000L * 40,
          createdAt = now - 86400000L * 42
        ),
        VerificationDocumentEntity(
          id = "doc_colorcraft_trade",
          businessId = "biz_colorcraft_painting",
          privateFilePath = "docs/businesses/colorcraft_municipal_license.pdf",
          documentType = "TRADE_LICENSE",
          reviewStatus = "PENDING",
          reviewedBy = null,
          reviewedAt = null,
          createdAt = now - 86400000L * 2
        )
      )
      database.documentDao().insertDocuments(docs)

      // 10. Seed Platform Settings
      database.settingsDao().insertSettings(
        PlatformSettingsEntity(
          id = "default",
          platformName = "LocalConnect",
          defaultCity = "Mumbai",
          supportedCities = "Mumbai, Bengaluru, Delhi NCR, Pune, Hyderabad, Chennai",
          defaultCommissionRate = 10.0,
          currency = "INR",
          supportPhone = "+91 80000 12345",
          supportEmail = "support@localconnect.app",
          cancellationPolicyDraft = "Free cancellation up to 2 hours prior to scheduled booking. Rescheduling allowed without penalty.",
          privacyDraft = "Customer addresses are protected and disclosed only to the confirmed service provider upon booking acceptance.",
          termsDraft = "LocalConnect is an intermediary platform connecting independent verified local service providers with customers.",
          maintenanceMode = false
        )
      )

      // 11. Seed Admin Audit Logs
      database.auditLogDao().insertLog(
        AdminAuditLogEntity(
          id = "log_init_001",
          administratorId = "usr_admin_1",
          action = "APPROVE_BUSINESS",
          targetType = "BUSINESS",
          targetId = "biz_apex_plumbing",
          reason = "Verified GSTIN and trade documentation",
          createdAt = now - 86400000L * 40
        )
      )
    }
  }
}
