package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
  @Query("SELECT * FROM profiles ORDER BY fullName ASC")
  fun getAllProfiles(): Flow<List<ProfileEntity>>

  @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
  fun getProfileById(id: String): Flow<ProfileEntity?>

  @Query("SELECT * FROM profiles WHERE id = :id LIMIT 1")
  suspend fun getProfileByIdSync(id: String): ProfileEntity?

  @Query("SELECT * FROM profiles WHERE email = :email LIMIT 1")
  suspend fun getProfileByEmail(email: String): ProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfile(profile: ProfileEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProfiles(profiles: List<ProfileEntity>)

  @Update
  suspend fun updateProfile(profile: ProfileEntity)

  @Query("UPDATE profiles SET accountStatus = :status WHERE id = :id")
  suspend fun updateAccountStatus(id: String, status: String)

  @Query("UPDATE profiles SET preferredCity = :city WHERE id = :id")
  suspend fun updatePreferredCity(id: String, city: String)

  @Query("DELETE FROM profiles WHERE id = :id")
  suspend fun deleteProfile(id: String)
}

@Dao
interface BusinessDao {
  @Query("SELECT * FROM businesses WHERE listingStatus = 'ACTIVE' ORDER BY isFeatured DESC, rating DESC")
  fun getAllActiveBusinesses(): Flow<List<BusinessEntity>>

  @Query("SELECT * FROM businesses ORDER BY createdAt DESC")
  fun getAllBusinessesAdmin(): Flow<List<BusinessEntity>>

  @Query("SELECT * FROM businesses WHERE verificationStatus = 'APPROVED' AND listingStatus = 'ACTIVE' ORDER BY isFeatured DESC, rating DESC")
  fun getApprovedBusinesses(): Flow<List<BusinessEntity>>

  @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
  fun getBusinessById(id: String): Flow<BusinessEntity?>

  @Query("SELECT * FROM businesses WHERE id = :id LIMIT 1")
  suspend fun getBusinessByIdSync(id: String): BusinessEntity?

  @Query("SELECT * FROM businesses WHERE ownerId = :ownerId LIMIT 1")
  fun getBusinessByOwnerId(ownerId: String): Flow<BusinessEntity?>

  @Query("SELECT * FROM businesses WHERE ownerId = :ownerId LIMIT 1")
  suspend fun getBusinessByOwnerIdSync(ownerId: String): BusinessEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBusiness(business: BusinessEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBusinesses(businesses: List<BusinessEntity>)

  @Update
  suspend fun updateBusiness(business: BusinessEntity)

  @Query("UPDATE businesses SET verificationStatus = :status, rejectionReason = :reason, updatedAt = :timestamp WHERE id = :id")
  suspend fun updateVerificationStatus(id: String, status: String, reason: String = "", timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE businesses SET isFeatured = :isFeatured WHERE id = :id")
  suspend fun updateFeaturedStatus(id: String, isFeatured: Boolean)

  @Query("UPDATE businesses SET listingStatus = :status WHERE id = :id")
  suspend fun updateListingStatus(id: String, status: String)

  @Query("UPDATE businesses SET rating = :rating, reviewCount = :count WHERE id = :id")
  suspend fun updateRating(id: String, rating: Float, count: Int)
}

@Dao
interface CategoryDao {
  @Query("SELECT * FROM categories ORDER BY displayOrder ASC")
  fun getAllCategories(): Flow<List<CategoryEntity>>

  @Query("SELECT * FROM categories WHERE active = 1 ORDER BY displayOrder ASC")
  fun getActiveCategories(): Flow<List<CategoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategory(category: CategoryEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCategories(categories: List<CategoryEntity>)

  @Update
  suspend fun updateCategory(category: CategoryEntity)

  @Query("UPDATE categories SET active = :active WHERE id = :id")
  suspend fun setCategoryActive(id: String, active: Boolean)
}

@Dao
interface ServiceDao {
  @Query("SELECT * FROM services WHERE businessId = :businessId ORDER BY name ASC")
  fun getServicesForBusiness(businessId: String): Flow<List<ServiceEntity>>

  @Query("SELECT * FROM services WHERE id = :id LIMIT 1")
  suspend fun getServiceById(id: String): ServiceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertService(service: ServiceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertServices(services: List<ServiceEntity>)

  @Update
  suspend fun updateService(service: ServiceEntity)

  @Query("DELETE FROM services WHERE id = :id")
  suspend fun deleteService(id: String)
}

@Dao
interface BookingDao {
  @Query("SELECT * FROM bookings ORDER BY createdAt DESC")
  fun getAllBookingsAdmin(): Flow<List<BookingEntity>>

  @Query("SELECT * FROM bookings WHERE customerId = :customerId ORDER BY createdAt DESC")
  fun getBookingsForCustomer(customerId: String): Flow<List<BookingEntity>>

  @Query("SELECT * FROM bookings WHERE businessId = :businessId ORDER BY createdAt DESC")
  fun getBookingsForBusiness(businessId: String): Flow<List<BookingEntity>>

  @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
  fun getBookingById(id: String): Flow<BookingEntity?>

  @Query("SELECT * FROM bookings WHERE id = :id LIMIT 1")
  suspend fun getBookingByIdSync(id: String): BookingEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBooking(booking: BookingEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertBookings(bookings: List<BookingEntity>)

  @Update
  suspend fun updateBooking(booking: BookingEntity)

  @Query("UPDATE bookings SET status = :status, cancellationReason = :reason, updatedAt = :timestamp WHERE id = :id")
  suspend fun updateBookingStatus(id: String, status: String, reason: String? = null, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE bookings SET proposedTime = :proposedTime, updatedAt = :timestamp WHERE id = :id")
  suspend fun proposeNewTime(id: String, proposedTime: String, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE bookings SET status = 'COMPLETED', completedAt = :timestamp, updatedAt = :timestamp WHERE id = :id")
  suspend fun markCompleted(id: String, timestamp: Long = System.currentTimeMillis())

  @Query("UPDATE bookings SET paymentStatus = :paymentStatus, paymentId = :paymentId, paymentMethod = :method, updatedAt = :timestamp WHERE id = :id")
  suspend fun updateBookingPayment(id: String, paymentStatus: String, paymentId: String, method: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface SubscriptionDao {
  @Query("SELECT * FROM subscription_plans WHERE active = 1 ORDER BY monthlyPrice ASC")
  fun getActivePlans(): Flow<List<SubscriptionPlanEntity>>

  @Query("SELECT * FROM subscription_plans ORDER BY monthlyPrice ASC")
  fun getAllPlansAdmin(): Flow<List<SubscriptionPlanEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlan(plan: SubscriptionPlanEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPlans(plans: List<SubscriptionPlanEntity>)

  @Update
  suspend fun updatePlan(plan: SubscriptionPlanEntity)

  @Query("SELECT * FROM subscriptions WHERE businessId = :businessId ORDER BY createdAt DESC LIMIT 1")
  fun getSubscriptionForBusiness(businessId: String): Flow<SubscriptionEntity?>

  @Query("SELECT * FROM subscriptions ORDER BY createdAt DESC")
  fun getAllSubscriptionsAdmin(): Flow<List<SubscriptionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSubscription(subscription: SubscriptionEntity)

  @Update
  suspend fun updateSubscription(subscription: SubscriptionEntity)

  @Query("UPDATE subscriptions SET status = 'CANCELLED', cancellationDate = :timestamp WHERE id = :id")
  suspend fun cancelSubscription(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface PaymentDao {
  @Query("SELECT * FROM payments ORDER BY createdAt DESC")
  fun getAllPayments(): Flow<List<PaymentEntity>>

  @Query("SELECT * FROM payments WHERE businessId = :businessId ORDER BY createdAt DESC")
  fun getPaymentsForBusiness(businessId: String): Flow<List<PaymentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayment(payment: PaymentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertPayments(payments: List<PaymentEntity>)

  @Update
  suspend fun updatePayment(payment: PaymentEntity)
}

@Dao
interface CommissionDao {
  @Query("SELECT * FROM commissions ORDER BY createdAt DESC")
  fun getAllCommissions(): Flow<List<CommissionEntity>>

  @Query("SELECT * FROM commissions WHERE businessId = :businessId ORDER BY createdAt DESC")
  fun getCommissionsForBusiness(businessId: String): Flow<List<CommissionEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertCommission(commission: CommissionEntity)

  @Update
  suspend fun updateCommission(commission: CommissionEntity)
}

@Dao
interface ReviewDao {
  @Query("SELECT * FROM reviews WHERE businessId = :businessId AND moderationStatus = 'APPROVED' ORDER BY createdAt DESC")
  fun getReviewsForBusiness(businessId: String): Flow<List<ReviewEntity>>

  @Query("SELECT * FROM reviews ORDER BY createdAt DESC")
  fun getAllReviewsAdmin(): Flow<List<ReviewEntity>>

  @Query("SELECT * FROM reviews WHERE bookingId = :bookingId LIMIT 1")
  suspend fun getReviewByBookingId(bookingId: String): ReviewEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReview(review: ReviewEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReviews(reviews: List<ReviewEntity>)

  @Query("UPDATE reviews SET moderationStatus = :status WHERE id = :id")
  suspend fun updateModerationStatus(id: String, status: String)
}

@Dao
interface FavoriteDao {
  @Query("SELECT businessId FROM favorites WHERE customerId = :customerId")
  fun getFavoriteBusinessIds(customerId: String): Flow<List<String>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun addFavorite(favorite: FavoriteEntity)

  @Query("DELETE FROM favorites WHERE customerId = :customerId AND businessId = :businessId")
  suspend fun removeFavorite(customerId: String, businessId: String)
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
  fun getNotificationsForUser(userId: String): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertNotification(notification: NotificationEntity)

  @Query("UPDATE notifications SET readAt = :timestamp WHERE id = :id")
  suspend fun markAsRead(id: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface ReportDao {
  @Query("SELECT * FROM reports ORDER BY createdAt DESC")
  fun getAllReportsAdmin(): Flow<List<ReportEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertReport(report: ReportEntity)

  @Query("UPDATE reports SET status = :status, resolution = :resolution, resolvedAt = :timestamp WHERE id = :id")
  suspend fun resolveReport(id: String, status: String, resolution: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface DocumentDao {
  @Query("SELECT * FROM business_verification_documents WHERE businessId = :businessId")
  fun getDocumentsForBusiness(businessId: String): Flow<List<VerificationDocumentEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocument(doc: VerificationDocumentEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDocuments(docs: List<VerificationDocumentEntity>)

  @Query("UPDATE business_verification_documents SET reviewStatus = :status, reviewedBy = :adminId, reviewedAt = :timestamp WHERE id = :id")
  suspend fun updateDocumentStatus(id: String, status: String, adminId: String, timestamp: Long = System.currentTimeMillis())
}

@Dao
interface AuditLogDao {
  @Query("SELECT * FROM admin_audit_logs ORDER BY createdAt DESC")
  fun getAllAuditLogs(): Flow<List<AdminAuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertLog(log: AdminAuditLogEntity)
}

@Dao
interface SettingsDao {
  @Query("SELECT * FROM platform_settings WHERE id = 'default' LIMIT 1")
  fun getSettings(): Flow<PlatformSettingsEntity?>

  @Query("SELECT * FROM platform_settings WHERE id = 'default' LIMIT 1")
  suspend fun getSettingsSync(): PlatformSettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSettings(settings: PlatformSettingsEntity)

  @Update
  suspend fun updateSettings(settings: PlatformSettingsEntity)
}
