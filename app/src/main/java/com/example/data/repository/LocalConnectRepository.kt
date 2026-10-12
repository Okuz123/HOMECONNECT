package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

class LocalConnectRepository(private val database: AppDatabase) {

  // Profiles
  fun getAllProfiles(): Flow<List<ProfileEntity>> = database.profileDao().getAllProfiles()
  fun getProfileById(id: String): Flow<ProfileEntity?> = database.profileDao().getProfileById(id)
  suspend fun getProfileByIdSync(id: String): ProfileEntity? = database.profileDao().getProfileByIdSync(id)
  suspend fun getProfileByEmail(email: String): ProfileEntity? = database.profileDao().getProfileByEmail(email)
  suspend fun insertProfile(profile: ProfileEntity) = database.profileDao().insertProfile(profile)
  suspend fun updateProfile(profile: ProfileEntity) = database.profileDao().updateProfile(profile)
  suspend fun updatePreferredCity(id: String, city: String) = database.profileDao().updatePreferredCity(id, city)
  suspend fun deleteProfile(id: String) = database.profileDao().deleteProfile(id)

  // Categories
  fun getActiveCategories(): Flow<List<CategoryEntity>> = database.categoryDao().getActiveCategories()
  fun getAllCategoriesAdmin(): Flow<List<CategoryEntity>> = database.categoryDao().getAllCategories()
  suspend fun insertCategory(category: CategoryEntity) = database.categoryDao().insertCategory(category)
  suspend fun updateCategory(category: CategoryEntity) = database.categoryDao().updateCategory(category)
  suspend fun setCategoryActive(id: String, active: Boolean) = database.categoryDao().setCategoryActive(id, active)

  // Businesses
  fun getApprovedBusinesses(): Flow<List<BusinessEntity>> = database.businessDao().getApprovedBusinesses()
  fun getAllBusinessesAdmin(): Flow<List<BusinessEntity>> = database.businessDao().getAllBusinessesAdmin()
  fun getBusinessById(id: String): Flow<BusinessEntity?> = database.businessDao().getBusinessById(id)
  suspend fun getBusinessByIdSync(id: String): BusinessEntity? = database.businessDao().getBusinessByIdSync(id)
  fun getBusinessByOwnerId(ownerId: String): Flow<BusinessEntity?> = database.businessDao().getBusinessByOwnerId(ownerId)
  suspend fun getBusinessByOwnerIdSync(ownerId: String): BusinessEntity? = database.businessDao().getBusinessByOwnerIdSync(ownerId)
  suspend fun insertBusiness(business: BusinessEntity) = database.businessDao().insertBusiness(business)
  suspend fun updateBusiness(business: BusinessEntity) = database.businessDao().updateBusiness(business)
  suspend fun updateVerificationStatus(id: String, status: String, reason: String = "", adminId: String = "") {
    database.businessDao().updateVerificationStatus(id, status, reason)
    if (adminId.isNotEmpty()) {
      database.auditLogDao().insertLog(
        AdminAuditLogEntity(
          id = "log_${UUID.randomUUID().toString().take(8)}",
          administratorId = adminId,
          action = if (status == "APPROVED") "APPROVE_BUSINESS" else "REJECT_BUSINESS",
          targetType = "BUSINESS",
          targetId = id,
          reason = reason.ifEmpty { "Admin status change to $status" },
          createdAt = System.currentTimeMillis()
        )
      )
    }
  }
  suspend fun updateFeaturedStatus(id: String, isFeatured: Boolean) = database.businessDao().updateFeaturedStatus(id, isFeatured)

  // Services
  fun getServicesForBusiness(businessId: String): Flow<List<ServiceEntity>> = database.serviceDao().getServicesForBusiness(businessId)
  suspend fun insertService(service: ServiceEntity) = database.serviceDao().insertService(service)
  suspend fun updateService(service: ServiceEntity) = database.serviceDao().updateService(service)
  suspend fun deleteService(id: String) = database.serviceDao().deleteService(id)

  // Bookings
  fun getBookingsForCustomer(customerId: String): Flow<List<BookingEntity>> = database.bookingDao().getBookingsForCustomer(customerId)
  fun getBookingsForBusiness(businessId: String): Flow<List<BookingEntity>> = database.bookingDao().getBookingsForBusiness(businessId)
  fun getAllBookingsAdmin(): Flow<List<BookingEntity>> = database.bookingDao().getAllBookingsAdmin()
  fun getBookingById(id: String): Flow<BookingEntity?> = database.bookingDao().getBookingById(id)
  suspend fun getBookingByIdSync(id: String): BookingEntity? = database.bookingDao().getBookingByIdSync(id)

  suspend fun createBooking(booking: BookingEntity) {
    database.bookingDao().insertBooking(booking)
    // Send in-app notification to business owner
    database.notificationDao().insertNotification(
      NotificationEntity(
        id = "notif_${UUID.randomUUID().toString().take(8)}",
        userId = booking.businessId,
        title = "New Booking Request",
        message = "New booking received for ${booking.preferredDate} at ${booking.preferredTime}.",
        notificationType = "BOOKING",
        relatedRecordId = booking.id
      )
    )
  }

  suspend fun updateBookingStatus(
    bookingId: String,
    status: String,
    reason: String? = null,
    updaterRole: String = ""
  ) {
    val booking = database.bookingDao().getBookingByIdSync(bookingId) ?: return

    when (status) {
      "COMPLETED" -> {
        database.bookingDao().markCompleted(bookingId)
        // Calculate and record platform commission
        val settings = database.settingsDao().getSettingsSync()
        val commissionRate = settings?.defaultCommissionRate ?: 10.0
        val commissionAmount = (booking.agreedPrice * commissionRate) / 100.0

        database.commissionDao().insertCommission(
          CommissionEntity(
            id = "comm_${UUID.randomUUID().toString().take(8)}",
            bookingId = bookingId,
            businessId = booking.businessId,
            commissionRate = commissionRate,
            commissionableAmount = booking.agreedPrice,
            commissionAmount = commissionAmount,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
          )
        )
      }
      else -> {
        database.bookingDao().updateBookingStatus(bookingId, status, reason)
      }
    }

    // Send notification to customer
    database.notificationDao().insertNotification(
      NotificationEntity(
        id = "notif_${UUID.randomUUID().toString().take(8)}",
        userId = booking.customerId,
        title = "Booking Status Updated",
        message = "Your booking #$bookingId is now $status.${if (!reason.isNullOrBlank()) " Note: $reason" else ""}",
        notificationType = "BOOKING",
        relatedRecordId = bookingId
      )
    )
  }

  suspend fun proposeNewTime(bookingId: String, proposedTime: String) {
    database.bookingDao().proposeNewTime(bookingId, proposedTime)
    val booking = database.bookingDao().getBookingByIdSync(bookingId) ?: return
    database.notificationDao().insertNotification(
      NotificationEntity(
        id = "notif_${UUID.randomUUID().toString().take(8)}",
        userId = booking.customerId,
        title = "Alternative Time Proposed",
        message = "Provider proposed a new time for your booking: $proposedTime.",
        notificationType = "BOOKING",
        relatedRecordId = bookingId
      )
    )
  }

  suspend fun payForBooking(
    bookingId: String,
    paymentMethod: String,
    amount: Double,
    transactionId: String
  ) {
    val booking = database.bookingDao().getBookingByIdSync(bookingId) ?: return
    val now = System.currentTimeMillis()

    // 1. Update Booking payment status
    database.bookingDao().updateBookingPayment(
      id = bookingId,
      paymentStatus = "PAID_ONLINE",
      paymentId = transactionId,
      method = paymentMethod,
      timestamp = now
    )

    // 2. Insert Payment Record
    database.paymentDao().insertPayment(
      PaymentEntity(
        id = "pay_${UUID.randomUUID().toString().take(8)}",
        payerId = booking.customerId,
        businessId = booking.businessId,
        bookingId = bookingId,
        providerPaymentId = transactionId,
        amount = amount,
        currency = "INR",
        paymentType = "DIRECT_SERVICE",
        paymentStatus = "COMPLETED",
        createdAt = now,
        updatedAt = now
      )
    )

    // 3. Record commission
    val settings = database.settingsDao().getSettingsSync()
    val rate = settings?.defaultCommissionRate ?: 10.0
    val commAmt = (amount * rate) / 100.0
    database.commissionDao().insertCommission(
      CommissionEntity(
        id = "comm_${UUID.randomUUID().toString().take(8)}",
        bookingId = bookingId,
        businessId = booking.businessId,
        commissionRate = rate,
        commissionableAmount = amount,
        commissionAmount = commAmt,
        status = "SETTLED",
        createdAt = now,
        settledAt = now
      )
    )

    // 4. Send notification to Customer
    database.notificationDao().insertNotification(
      NotificationEntity(
        id = "notif_${UUID.randomUUID().toString().take(8)}",
        userId = booking.customerId,
        title = "Payment Confirmed (24/7 Gateway)",
        message = "Payment of ₹${amount.toInt()} for booking #${bookingId.takeLast(6)} was successful via $paymentMethod. Ref: $transactionId",
        notificationType = "BOOKING",
        relatedRecordId = bookingId
      )
    )

    // 5. Send notification to Business Owner
    database.notificationDao().insertNotification(
      NotificationEntity(
        id = "notif_${UUID.randomUUID().toString().take(8)}",
        userId = booking.businessId,
        title = "In-App Payment Received",
        message = "Customer paid ₹${amount.toInt()} for booking #${bookingId.takeLast(6)} via $paymentMethod.",
        notificationType = "BOOKING",
        relatedRecordId = bookingId
      )
    )
  }

  // Reviews
  fun getReviewsForBusiness(businessId: String): Flow<List<ReviewEntity>> = database.reviewDao().getReviewsForBusiness(businessId)
  fun getAllReviewsAdmin(): Flow<List<ReviewEntity>> = database.reviewDao().getAllReviewsAdmin()
  suspend fun submitReview(review: ReviewEntity) {
    database.reviewDao().insertReview(review)
    // Update business average rating and review count
    val reviews = database.reviewDao().getReviewsForBusiness(review.businessId).firstOrNull() ?: emptyList()
    val allReviews = reviews + review
    val avg = allReviews.map { it.rating }.average().toFloat()
    database.businessDao().updateRating(review.businessId, avg, allReviews.size)
  }
  suspend fun updateReviewModeration(id: String, status: String) = database.reviewDao().updateModerationStatus(id, status)

  // Subscriptions & Plans
  fun getActivePlans(): Flow<List<SubscriptionPlanEntity>> = database.subscriptionDao().getActivePlans()
  fun getAllPlansAdmin(): Flow<List<SubscriptionPlanEntity>> = database.subscriptionDao().getAllPlansAdmin()
  fun getSubscriptionForBusiness(businessId: String): Flow<SubscriptionEntity?> = database.subscriptionDao().getSubscriptionForBusiness(businessId)
  fun getAllSubscriptionsAdmin(): Flow<List<SubscriptionEntity>> = database.subscriptionDao().getAllSubscriptionsAdmin()
  suspend fun updatePlan(plan: SubscriptionPlanEntity) = database.subscriptionDao().updatePlan(plan)

  suspend fun activateSubscriptionWithPayment(
    businessId: String,
    planId: String,
    amount: Double,
    providerPaymentId: String
  ) {
    val now = System.currentTimeMillis()
    val subscriptionId = "sub_${UUID.randomUUID().toString().take(8)}"

    // Record verified mock payment
    database.paymentDao().insertPayment(
      PaymentEntity(
        id = "pay_${UUID.randomUUID().toString().take(8)}",
        payerId = businessId,
        businessId = businessId,
        subscriptionId = subscriptionId,
        providerPaymentId = providerPaymentId,
        amount = amount,
        currency = "INR",
        paymentType = "SUBSCRIPTION",
        paymentStatus = "COMPLETED",
        createdAt = now,
        updatedAt = now
      )
    )

    // Save Subscription
    database.subscriptionDao().insertSubscription(
      SubscriptionEntity(
        id = subscriptionId,
        businessId = businessId,
        planId = planId,
        paymentProviderSubscriptionId = providerPaymentId,
        status = "ACTIVE",
        startDate = now,
        renewalDate = now + 30L * 24 * 60 * 60 * 1000,
        createdAt = now
      )
    )

    // Update business featured flag if Plan is featured or premium
    if (planId == "plan_featured" || planId == "plan_premium") {
      database.businessDao().updateFeaturedStatus(businessId, true)
    }
  }

  suspend fun cancelSubscription(subscriptionId: String, businessId: String) {
    database.subscriptionDao().cancelSubscription(subscriptionId)
    database.businessDao().updateFeaturedStatus(businessId, false)
  }

  // Favorites
  fun getFavoriteBusinessIds(customerId: String): Flow<List<String>> = database.favoriteDao().getFavoriteBusinessIds(customerId)
  suspend fun toggleFavorite(customerId: String, businessId: String, isFav: Boolean) {
    if (isFav) {
      database.favoriteDao().removeFavorite(customerId, businessId)
    } else {
      database.favoriteDao().addFavorite(FavoriteEntity("fav_${customerId}_$businessId", customerId, businessId))
    }
  }

  // Notifications
  fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>> = database.notificationDao().getNotificationsForUser(userId)
  suspend fun markNotificationRead(id: String) = database.notificationDao().markAsRead(id)

  // Reports
  fun getAllReportsAdmin(): Flow<List<ReportEntity>> = database.reportDao().getAllReportsAdmin()
  suspend fun submitReport(report: ReportEntity) = database.reportDao().insertReport(report)
  suspend fun resolveReport(id: String, status: String, resolution: String) = database.reportDao().resolveReport(id, status, resolution)

  // Documents
  fun getDocumentsForBusiness(businessId: String): Flow<List<VerificationDocumentEntity>> = database.documentDao().getDocumentsForBusiness(businessId)
  suspend fun insertDocument(doc: VerificationDocumentEntity) = database.documentDao().insertDocument(doc)
  suspend fun updateDocumentStatus(id: String, status: String, adminId: String) = database.documentDao().updateDocumentStatus(id, status, adminId)

  // Audit Logs
  fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>> = database.auditLogDao().getAllAuditLogs()
  suspend fun recordAuditLog(adminId: String, action: String, targetType: String, targetId: String, reason: String) {
    database.auditLogDao().insertLog(
      AdminAuditLogEntity(
        id = "log_${UUID.randomUUID().toString().take(8)}",
        administratorId = adminId,
        action = action,
        targetType = targetType,
        targetId = targetId,
        reason = reason,
        createdAt = System.currentTimeMillis()
      )
    )
  }

  // Platform Settings
  fun getSettings(): Flow<PlatformSettingsEntity?> = database.settingsDao().getSettings()
  suspend fun getSettingsSync(): PlatformSettingsEntity? = database.settingsDao().getSettingsSync()
  suspend fun updateSettings(settings: PlatformSettingsEntity) = database.settingsDao().updateSettings(settings)

  // Payments & Commissions for Admin and Business Owner
  fun getAllPaymentsAdmin(): Flow<List<PaymentEntity>> = database.paymentDao().getAllPayments()
  fun getPaymentsForBusiness(businessId: String): Flow<List<PaymentEntity>> = database.paymentDao().getPaymentsForBusiness(businessId)
  fun getAllCommissionsAdmin(): Flow<List<CommissionEntity>> = database.commissionDao().getAllCommissions()
  fun getCommissionsForBusiness(businessId: String): Flow<List<CommissionEntity>> = database.commissionDao().getCommissionsForBusiness(businessId)
}
