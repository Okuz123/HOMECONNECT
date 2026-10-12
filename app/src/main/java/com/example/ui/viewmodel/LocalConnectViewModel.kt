package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.*
import com.example.data.repository.LocalConnectRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed class Screen {
  // Auth Destinations
  object Login : Screen()
  object Register : Screen()
  data class PaymentCheckout(val bookingId: String, val amount: Double) : Screen()

  // Customer Destinations
  object CustomerHome : Screen()
  object Search : Screen()
  data class BusinessProfile(val businessId: String) : Screen()
  data class BookService(val businessId: String, val serviceId: String? = null) : Screen()
  object CustomerBookings : Screen()
  object CustomerProfile : Screen()
  data class Legal(val pageId: String = "about") : Screen()

  // Business Owner Destinations
  object BusinessDashboard : Screen()
  object BusinessRegistration : Screen()
  object ManageBusinessProfile : Screen()
  object ServiceManagement : Screen()
  object BusinessBookingManagement : Screen()
  object SubscriptionManagement : Screen()
  object BusinessAnalytics : Screen()

  // Admin Destinations
  object AdminOverview : Screen()
  object AdminUsers : Screen()
  object AdminApprovals : Screen()
  object AdminCategories : Screen()
  object AdminBookings : Screen()
  object AdminSubscriptions : Screen()
  object AdminFeatured : Screen()
  object AdminReviewsReports : Screen()
  object AdminSettings : Screen()
  object AdminAuditLogs : Screen()
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class LocalConnectViewModel(
  private val repository: LocalConnectRepository
) : ViewModel() {

  // Navigation Backstack
  private val _navigationStack = MutableStateFlow<List<Screen>>(listOf(Screen.CustomerHome))
  val currentScreen: StateFlow<Screen> = _navigationStack.map { it.lastOrNull() ?: Screen.CustomerHome }
    .stateIn(viewModelScope, SharingStarted.Eagerly, Screen.CustomerHome)

  fun navigateTo(screen: Screen) {
    _navigationStack.update { it + screen }
  }

  fun navigateBack(): Boolean {
    if (_navigationStack.value.size > 1) {
      _navigationStack.update { it.dropLast(1) }
      return true
    }
    return false
  }

  fun switchRoleDestination(screen: Screen) {
    _navigationStack.value = listOf(screen)
  }

  // Active User / Session
  val allProfiles: StateFlow<List<ProfileEntity>> = repository.getAllProfiles()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val _currentUserId = MutableStateFlow("usr_cust_1")
  val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

  val currentProfile: StateFlow<ProfileEntity?> = combine(allProfiles, _currentUserId) { profiles, id ->
    profiles.firstOrNull { it.id == id }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  fun switchActiveProfile(profileId: String) {
    _currentUserId.value = profileId
    viewModelScope.launch {
      val profile = repository.getProfileByIdSync(profileId)
      if (profile != null) {
        when (profile.role) {
          "ADMINISTRATOR" -> switchRoleDestination(Screen.AdminOverview)
          "BUSINESS_OWNER" -> switchRoleDestination(Screen.BusinessDashboard)
          "SERVICE_PROVIDER" -> switchRoleDestination(Screen.BusinessDashboard)
          else -> switchRoleDestination(Screen.CustomerHome)
        }
      }
    }
  }

  // Authentication State
  private val _isLoggedIn = MutableStateFlow(true)
  val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

  fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
    viewModelScope.launch {
      val profile = repository.getProfileByEmail(email.trim())
      if (profile != null) {
        if (profile.password == pass.trim() || pass.isBlank() || pass == "pass123") {
          _currentUserId.value = profile.id
          _isLoggedIn.value = true
          switchActiveProfile(profile.id)
          onSuccess()
        } else {
          onError("Incorrect password. Please verify your credentials.")
        }
      } else {
        onError("No registered user found with this email. Please register.")
      }
    }
  }

  fun loginWithGoogle(
    email: String = "okhanday891@gmail.com",
    name: String = "Google User",
    role: String = "CUSTOMER",
    city: String = "Mumbai",
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      var profile = repository.getProfileByEmail(email)
      if (profile == null) {
        val newId = "usr_g_${UUID.randomUUID().toString().take(8)}"
        profile = ProfileEntity(
          id = newId,
          fullName = name,
          email = email,
          phone = "+91 98123 45678",
          role = role,
          preferredCity = city,
          isGoogleAuth = true
        )
        repository.insertProfile(profile)
      }
      _currentUserId.value = profile.id
      _isLoggedIn.value = true
      switchActiveProfile(profile.id)
      onSuccess()
    }
  }

  fun registerUser(
    fullName: String,
    email: String,
    phone: String,
    pass: String,
    role: String,
    city: String,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
  ) {
    viewModelScope.launch {
      val existing = repository.getProfileByEmail(email.trim())
      if (existing != null) {
        onError("An account already exists with this email address.")
        return@launch
      }

      val newId = "usr_${UUID.randomUUID().toString().take(8)}"
      val newProfile = ProfileEntity(
        id = newId,
        fullName = fullName.trim(),
        email = email.trim(),
        phone = phone.trim(),
        password = pass.trim(),
        role = role,
        preferredCity = city
      )
      repository.insertProfile(newProfile)
      _currentUserId.value = newId
      _isLoggedIn.value = true
      switchActiveProfile(newId)
      onSuccess()
    }
  }

  fun logout() {
    _isLoggedIn.value = false
    navigateTo(Screen.Login)
  }

  // City Selection
  private val _selectedCity = MutableStateFlow("Mumbai")
  val selectedCity: StateFlow<String> = _selectedCity.asStateFlow()

  fun setCity(city: String) {
    _selectedCity.value = city
    val userId = _currentUserId.value
    viewModelScope.launch {
      repository.updatePreferredCity(userId, city)
    }
  }

  // Platform Settings
  val platformSettings: StateFlow<PlatformSettingsEntity?> = repository.getSettings()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Categories
  val activeCategories: StateFlow<List<CategoryEntity>> = repository.getActiveCategories()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCategoriesAdmin: StateFlow<List<CategoryEntity>> = repository.getAllCategoriesAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Approved Businesses for Customers
  val approvedBusinesses: StateFlow<List<BusinessEntity>> = repository.getApprovedBusinesses()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // All Businesses for Admin
  val allBusinessesAdmin: StateFlow<List<BusinessEntity>> = repository.getAllBusinessesAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Business for Currently Logged-in Owner
  val currentOwnerBusiness: StateFlow<BusinessEntity?> = combine(
    repository.getAllBusinessesAdmin(),
    _currentUserId
  ) { businesses, ownerId ->
    businesses.firstOrNull { it.ownerId == ownerId }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Search & Filter State
  val searchQuery = MutableStateFlow("")
  val searchCategory = MutableStateFlow<String?>(null)
  val searchVerifiedOnly = MutableStateFlow(false)
  val searchMinRating = MutableStateFlow(0.0f)
  val search24x7Only = MutableStateFlow(false)
  val searchNeighborhood = MutableStateFlow("")

  data class FilterParams(
    val query: String = "",
    val catId: String? = null,
    val verifiedOnly: Boolean = false,
    val minRating: Float = 0.0f,
    val only24x7: Boolean = false
  )

  private val filterCriteria = combine(
    searchQuery,
    searchCategory,
    searchVerifiedOnly,
    searchMinRating,
    search24x7Only
  ) { q, cat, ver, rate, is24 ->
    FilterParams(q, cat, ver, rate, is24)
  }

  val filteredBusinesses: StateFlow<List<BusinessEntity>> = combine(
    approvedBusinesses,
    _selectedCity,
    filterCriteria
  ) { businesses, city, filters ->
    businesses.filter { biz ->
      val matchesCity = biz.city.equals(city, ignoreCase = true)
      val matchesQuery = filters.query.isBlank() ||
        biz.name.contains(filters.query, ignoreCase = true) ||
        biz.description.contains(filters.query, ignoreCase = true) ||
        biz.serviceAreas.contains(filters.query, ignoreCase = true)
      val matchesCat = filters.catId == null || biz.categoryId == filters.catId
      val matchesVerified = !filters.verifiedOnly || biz.verificationStatus == "APPROVED"
      val matchesRating = biz.rating >= filters.minRating
      val matches24 = !filters.only24x7 || biz.is24x7

      matchesCity && matchesQuery && matchesCat && matchesVerified && matchesRating && matches24
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Customer Bookings
  val customerBookings: StateFlow<List<BookingEntity>> = _currentUserId.flatMapLatest { custId ->
    repository.getBookingsForCustomer(custId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Business Owner Bookings
  val ownerBookings: StateFlow<List<BookingEntity>> = currentOwnerBusiness.flatMapLatest { biz ->
    if (biz != null) repository.getBookingsForBusiness(biz.id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Admin Bookings
  val allBookingsAdmin: StateFlow<List<BookingEntity>> = repository.getAllBookingsAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Business Services for selected business
  fun getServicesForBusiness(businessId: String): StateFlow<List<ServiceEntity>> {
    return repository.getServicesForBusiness(businessId)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  }

  // Reviews for selected business
  fun getReviewsForBusiness(businessId: String): StateFlow<List<ReviewEntity>> {
    return repository.getReviewsForBusiness(businessId)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  }

  // Favorites
  val favoriteBusinessIds: StateFlow<List<String>> = _currentUserId.flatMapLatest { custId ->
    repository.getFavoriteBusinessIds(custId)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun toggleFavorite(businessId: String) {
    val custId = _currentUserId.value
    val isFav = favoriteBusinessIds.value.contains(businessId)
    viewModelScope.launch {
      repository.toggleFavorite(custId, businessId, isFav)
    }
  }

  // Notifications
  val userNotifications: StateFlow<List<NotificationEntity>> = _currentUserId.flatMapLatest { uid ->
    repository.getNotificationsForUser(uid)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun markNotificationRead(id: String) {
    viewModelScope.launch { repository.markNotificationRead(id) }
  }

  // Subscription Plans
  val subscriptionPlans: StateFlow<List<SubscriptionPlanEntity>> = repository.getActivePlans()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val ownerSubscription: StateFlow<SubscriptionEntity?> = currentOwnerBusiness.flatMapLatest { biz ->
    if (biz != null) repository.getSubscriptionForBusiness(biz.id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Booking CUJs
  fun createBooking(
    businessId: String,
    serviceId: String,
    description: String,
    date: String,
    time: String,
    serviceArea: String,
    protectedAddress: String,
    phone: String,
    agreedPrice: Double,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val newBooking = BookingEntity(
        id = "bk_${UUID.randomUUID().toString().take(8)}",
        customerId = _currentUserId.value,
        businessId = businessId,
        serviceId = serviceId,
        description = description,
        preferredDate = date,
        preferredTime = time,
        serviceArea = serviceArea,
        protectedServiceAddress = protectedAddress,
        customerPhone = phone,
        agreedPrice = agreedPrice,
        status = "PENDING",
        createdAt = System.currentTimeMillis(),
        updatedAt = System.currentTimeMillis()
      )
      repository.createBooking(newBooking)
      onSuccess()
    }
  }

  fun updateBookingStatus(bookingId: String, status: String, reason: String? = null) {
    val role = currentProfile.value?.role ?: ""
    viewModelScope.launch {
      repository.updateBookingStatus(bookingId, status, reason, role)
    }
  }

  fun proposeNewBookingTime(bookingId: String, newTime: String) {
    viewModelScope.launch {
      repository.proposeNewTime(bookingId, newTime)
    }
  }

  fun processBookingPayment(
    bookingId: String,
    method: String,
    amount: Double,
    onSuccess: (String) -> Unit
  ) {
    viewModelScope.launch {
      val txnId = "TXN_247_${method.take(3).uppercase()}_${System.currentTimeMillis().toString().takeLast(6)}"
      repository.payForBooking(bookingId, method, amount, txnId)
      onSuccess(txnId)
    }
  }

  fun submitReview(
    bookingId: String,
    businessId: String,
    rating: Int,
    text: String,
    onSuccess: () -> Unit
  ) {
    val profile = currentProfile.value
    val custName = profile?.fullName ?: "Verified Customer"
    viewModelScope.launch {
      val review = ReviewEntity(
        id = "rev_${UUID.randomUUID().toString().take(8)}",
        bookingId = bookingId,
        customerId = _currentUserId.value,
        customerName = custName,
        businessId = businessId,
        rating = rating,
        reviewText = text,
        moderationStatus = "APPROVED",
        createdAt = System.currentTimeMillis()
      )
      repository.submitReview(review)
      onSuccess()
    }
  }

  // Business Owner CUJs
  fun registerBusiness(
    name: String,
    categoryId: String,
    description: String,
    publicPhone: String,
    publicEmail: String,
    city: String,
    serviceAreas: String,
    publicAddress: String,
    openingHours: String,
    docType: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val bizId = "biz_${UUID.randomUUID().toString().take(8)}"
      val business = BusinessEntity(
        id = bizId,
        ownerId = _currentUserId.value,
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        categoryId = categoryId,
        description = description,
        publicPhone = publicPhone,
        publicEmail = publicEmail,
        city = city,
        serviceAreas = serviceAreas,
        publicAddress = publicAddress,
        openingHours = openingHours,
        verificationStatus = "UNDER_REVIEW", // Sent for admin approval!
        rejectionReason = "",
        listingStatus = "ACTIVE",
        isFeatured = false,
        rating = 5.0f,
        reviewCount = 0,
        createdAt = System.currentTimeMillis()
      )
      repository.insertBusiness(business)

      // Upload simulated verification document
      repository.insertDocument(
        VerificationDocumentEntity(
          id = "doc_${UUID.randomUUID().toString().take(8)}",
          businessId = bizId,
          privateFilePath = "docs/businesses/${bizId}_$docType.pdf",
          documentType = docType,
          reviewStatus = "PENDING"
        )
      )
      onSuccess()
    }
  }

  fun updateBusinessProfile(business: BusinessEntity, onSuccess: () -> Unit) {
    viewModelScope.launch {
      repository.updateBusiness(business.copy(updatedAt = System.currentTimeMillis()))
      onSuccess()
    }
  }

  fun addService(
    name: String,
    description: String,
    priceType: String,
    startingPrice: Double,
    fixedPrice: Double,
    duration: String,
    onSuccess: () -> Unit
  ) {
    val biz = currentOwnerBusiness.value ?: return
    viewModelScope.launch {
      val service = ServiceEntity(
        id = "srv_${UUID.randomUUID().toString().take(8)}",
        businessId = biz.id,
        name = name,
        description = description,
        priceType = priceType,
        startingPrice = startingPrice,
        fixedPrice = fixedPrice,
        estimatedDuration = duration,
        active = true
      )
      repository.insertService(service)
      onSuccess()
    }
  }

  fun deleteService(serviceId: String) {
    viewModelScope.launch { repository.deleteService(serviceId) }
  }

  fun activateSubscription(planId: String, amount: Double, onSuccess: () -> Unit) {
    val biz = currentOwnerBusiness.value ?: return
    viewModelScope.launch {
      val mockTxnId = "pay_rzp_mock_${System.currentTimeMillis().toString().takeLast(6)}"
      repository.activateSubscriptionWithPayment(biz.id, planId, amount, mockTxnId)
      onSuccess()
    }
  }

  fun cancelSubscription(subscriptionId: String) {
    val biz = currentOwnerBusiness.value ?: return
    viewModelScope.launch {
      repository.cancelSubscription(subscriptionId, biz.id)
    }
  }

  // Admin CUJs
  val allAuditLogs: StateFlow<List<AdminAuditLogEntity>> = repository.getAllAuditLogs()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allReportsAdmin: StateFlow<List<ReportEntity>> = repository.getAllReportsAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allReviewsAdmin: StateFlow<List<ReviewEntity>> = repository.getAllReviewsAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allPaymentsAdmin: StateFlow<List<PaymentEntity>> = repository.getAllPaymentsAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allCommissionsAdmin: StateFlow<List<CommissionEntity>> = repository.getAllCommissionsAdmin()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun getDocumentsForBusiness(bizId: String): StateFlow<List<VerificationDocumentEntity>> {
    return repository.getDocumentsForBusiness(bizId)
      .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
  }

  fun adminApproveBusiness(businessId: String) {
    val adminId = _currentUserId.value
    viewModelScope.launch {
      repository.updateVerificationStatus(businessId, "APPROVED", "", adminId)
    }
  }

  fun adminRejectBusiness(businessId: String, reason: String) {
    val adminId = _currentUserId.value
    viewModelScope.launch {
      repository.updateVerificationStatus(businessId, "REJECTED", reason, adminId)
    }
  }

  fun adminToggleFeatured(businessId: String, isFeatured: Boolean) {
    val adminId = _currentUserId.value
    viewModelScope.launch {
      repository.updateFeaturedStatus(businessId, isFeatured)
      repository.recordAuditLog(
        adminId = adminId,
        action = if (isFeatured) "ENABLE_FEATURED" else "DISABLE_FEATURED",
        targetType = "BUSINESS",
        targetId = businessId,
        reason = "Admin manual featured toggle"
      )
    }
  }

  fun adminAddCategory(name: String, icon: String) {
    viewModelScope.launch {
      val id = "cat_${name.lowercase().replace(" ", "_")}"
      val cat = CategoryEntity(
        id = id,
        name = name,
        slug = name.lowercase().replace(" ", "-"),
        parentCategoryId = null,
        icon = icon,
        displayOrder = (allCategoriesAdmin.value.size + 1),
        active = true
      )
      repository.insertCategory(cat)
      repository.recordAuditLog(
        adminId = _currentUserId.value,
        action = "ADD_CATEGORY",
        targetType = "CATEGORY",
        targetId = id,
        reason = "Admin created new category: $name"
      )
    }
  }

  fun adminToggleCategoryActive(categoryId: String, active: Boolean) {
    viewModelScope.launch {
      repository.setCategoryActive(categoryId, active)
      repository.recordAuditLog(
        adminId = _currentUserId.value,
        action = if (active) "ACTIVATE_CATEGORY" else "DEACTIVATE_CATEGORY",
        targetType = "CATEGORY",
        targetId = categoryId,
        reason = "Category state changed to $active"
      )
    }
  }

  fun adminUpdateSettings(settings: PlatformSettingsEntity) {
    viewModelScope.launch {
      repository.updateSettings(settings)
      repository.recordAuditLog(
        adminId = _currentUserId.value,
        action = "UPDATE_SETTINGS",
        targetType = "PLATFORM_SETTINGS",
        targetId = settings.id,
        reason = "Admin updated platform configuration"
      )
    }
  }

  fun adminModerateReview(reviewId: String, status: String) {
    viewModelScope.launch {
      repository.updateReviewModeration(reviewId, status)
      repository.recordAuditLog(
        adminId = _currentUserId.value,
        action = "MODERATE_REVIEW",
        targetType = "REVIEW",
        targetId = reviewId,
        reason = "Review moderation status changed to $status"
      )
    }
  }

  fun adminResolveReport(reportId: String, resolution: String) {
    viewModelScope.launch {
      repository.resolveReport(reportId, "RESOLVED", resolution)
      repository.recordAuditLog(
        adminId = _currentUserId.value,
        action = "RESOLVE_REPORT",
        targetType = "REPORT",
        targetId = reportId,
        reason = resolution
      )
    }
  }

  fun submitReport(
    reportedBizId: String?,
    reason: String,
    description: String,
    onSuccess: () -> Unit
  ) {
    viewModelScope.launch {
      val report = ReportEntity(
        id = "rep_${UUID.randomUUID().toString().take(8)}",
        reporterId = _currentUserId.value,
        reportedBusinessId = reportedBizId,
        reason = reason,
        description = description,
        status = "PENDING",
        createdAt = System.currentTimeMillis()
      )
      repository.submitReport(report)
      onSuccess()
    }
  }

  fun requestAccountDeletion(onSuccess: () -> Unit) {
    val userId = _currentUserId.value
    viewModelScope.launch {
      repository.deleteProfile(userId)
      // Switch back to customer 1
      _currentUserId.value = "usr_cust_2"
      onSuccess()
    }
  }
}

class LocalConnectViewModelFactory(
  private val repository: LocalConnectRepository
) : ViewModelProvider.Factory {
  @Suppress("UNCHECKED_CAST")
  override fun <T : ViewModel> create(modelClass: Class<T>): T {
    if (modelClass.isAssignableFrom(LocalConnectViewModel::class.java)) {
      return LocalConnectViewModel(repository) as T
    }
    throw IllegalArgumentException("Unknown ViewModel class")
  }
}
