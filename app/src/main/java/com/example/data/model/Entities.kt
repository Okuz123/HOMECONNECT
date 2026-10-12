package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
  @PrimaryKey val id: String,
  val fullName: String,
  val email: String,
  val phone: String,
  val role: String, // "CUSTOMER", "BUSINESS_OWNER", "SERVICE_PROVIDER", "ADMINISTRATOR"
  val accountStatus: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "PENDING_DELETION"
  val preferredCity: String = "Mumbai",
  val password: String = "pass123",
  val isGoogleAuth: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "businesses")
data class BusinessEntity(
  @PrimaryKey val id: String,
  val ownerId: String,
  val name: String,
  val slug: String,
  val categoryId: String,
  val description: String,
  val publicPhone: String,
  val publicEmail: String,
  val city: String,
  val serviceAreas: String,
  val publicAddress: String,
  val openingHours: String,
  val logoUrl: String = "",
  val verificationStatus: String = "UNDER_REVIEW", // "DRAFT", "SUBMITTED", "UNDER_REVIEW", "APPROVED", "REJECTED", "SUSPENDED"
  val rejectionReason: String = "",
  val listingStatus: String = "ACTIVE", // "ACTIVE", "INACTIVE"
  val isFeatured: Boolean = false,
  val is24x7: Boolean = true, // 24/7 Service Available
  val rating: Float = 4.8f,
  val reviewCount: Int = 0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "categories")
data class CategoryEntity(
  @PrimaryKey val id: String,
  val name: String,
  val slug: String,
  val parentCategoryId: String? = null,
  val icon: String,
  val displayOrder: Int,
  val active: Boolean = true,
)

@Entity(tableName = "services")
data class ServiceEntity(
  @PrimaryKey val id: String,
  val businessId: String,
  val name: String,
  val description: String,
  val priceType: String = "FIXED", // "FIXED", "STARTING_AT", "INSPECTION_REQUIRED"
  val startingPrice: Double = 0.0,
  val fixedPrice: Double = 0.0,
  val estimatedDuration: String = "1-2 Hours",
  val active: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "bookings")
data class BookingEntity(
  @PrimaryKey val id: String,
  val customerId: String,
  val businessId: String,
  val serviceId: String,
  val description: String,
  val preferredDate: String,
  val preferredTime: String,
  val serviceArea: String,
  val protectedServiceAddress: String,
  val customerPhone: String,
  val agreedPrice: Double,
  val status: String = "PENDING", // "PENDING", "ACCEPTED", "REJECTED", "SCHEDULED", "IN_PROGRESS", "COMPLETED", "CANCELLED", "DISPUTED"
  val paymentStatus: String = "UNPAID", // "UNPAID", "PAID_ONLINE", "REFUNDED"
  val paymentId: String? = null,
  val paymentMethod: String? = null, // "UPI", "CARD", "NET_BANKING", "CASH"
  val cancellationReason: String? = null,
  val disputeReason: String? = null,
  val proposedTime: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null,
)

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
  @PrimaryKey val id: String,
  val businessId: String,
  val planId: String,
  val paymentProviderSubscriptionId: String,
  val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "CANCELLED", "TRIAL"
  val startDate: Long = System.currentTimeMillis(),
  val renewalDate: Long = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
  val cancellationDate: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "subscription_plans")
data class SubscriptionPlanEntity(
  @PrimaryKey val id: String,
  val name: String,
  val monthlyPrice: Double,
  val currency: String = "INR",
  val features: String, // Comma separated
  val active: Boolean = true,
)

@Entity(tableName = "payments")
data class PaymentEntity(
  @PrimaryKey val id: String,
  val payerId: String,
  val businessId: String? = null,
  val bookingId: String? = null,
  val subscriptionId: String? = null,
  val providerPaymentId: String,
  val amount: Double,
  val currency: String = "INR",
  val paymentType: String, // "SUBSCRIPTION", "COMMISSION", "FEATURED_LISTING", "DIRECT_SERVICE"
  val paymentStatus: String = "COMPLETED", // "COMPLETED", "PENDING", "FAILED", "REFUNDED"
  val refundAmount: Double = 0.0,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "commissions")
data class CommissionEntity(
  @PrimaryKey val id: String,
  val bookingId: String,
  val businessId: String,
  val commissionRate: Double = 10.0, // 10%
  val commissionableAmount: Double,
  val commissionAmount: Double,
  val status: String = "PENDING", // "PENDING", "SETTLED", "WAIVED"
  val createdAt: Long = System.currentTimeMillis(),
  val settledAt: Long? = null,
)

@Entity(tableName = "reviews")
data class ReviewEntity(
  @PrimaryKey val id: String,
  val bookingId: String,
  val customerId: String,
  val customerName: String,
  val businessId: String,
  val rating: Int,
  val reviewText: String,
  val moderationStatus: String = "APPROVED", // "APPROVED", "FLAGGED", "REJECTED"
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "favorites")
data class FavoriteEntity(
  @PrimaryKey val id: String,
  val customerId: String,
  val businessId: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "notifications")
data class NotificationEntity(
  @PrimaryKey val id: String,
  val userId: String,
  val title: String,
  val message: String,
  val notificationType: String, // "BOOKING", "APPROVAL", "SUBSCRIPTION", "ALERT", "DISPUTE"
  val relatedRecordId: String? = null,
  val readAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "reports")
data class ReportEntity(
  @PrimaryKey val id: String,
  val reporterId: String,
  val reportedBusinessId: String? = null,
  val reportedUserId: String? = null,
  val bookingId: String? = null,
  val reason: String,
  val description: String,
  val status: String = "PENDING", // "PENDING", "INVESTIGATING", "RESOLVED", "DISMISSED"
  val resolution: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val resolvedAt: Long? = null,
)

@Entity(tableName = "business_verification_documents")
data class VerificationDocumentEntity(
  @PrimaryKey val id: String,
  val businessId: String,
  val privateFilePath: String,
  val documentType: String, // "GST_CERTIFICATE", "TRADE_LICENSE", "ID_PROOF", "SHOP_ESTABLISHMENT"
  val reviewStatus: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
  val reviewedBy: String? = null,
  val reviewedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
  @PrimaryKey val id: String,
  val administratorId: String,
  val action: String,
  val targetType: String,
  val targetId: String,
  val reason: String,
  val createdAt: Long = System.currentTimeMillis(),
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
  @PrimaryKey val id: String = "default",
  val platformName: String = "LocalConnect",
  val defaultCity: String = "Mumbai",
  val supportedCities: String = "Mumbai, Bengaluru, Delhi NCR, Pune, Hyderabad, Chennai",
  val defaultCommissionRate: Double = 10.0,
  val currency: String = "INR",
  val supportPhone: String = "+91 80000 12345",
  val supportEmail: String = "support@localconnect.app",
  val cancellationPolicyDraft: String = "Free cancellation up to 2 hours before scheduled time. Late cancellations subject to 50% fee.",
  val privacyDraft: String = "Customer addresses are protected and disclosed only to the confirmed service provider upon booking acceptance.",
  val termsDraft: String = "LocalConnect is an intermediary platform connecting independent verified local service providers with customers.",
  val maintenanceMode: Boolean = false,
)
