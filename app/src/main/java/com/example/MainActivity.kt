package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.repository.LocalConnectRepository
import com.example.ui.components.AdminBottomNav
import com.example.ui.components.CustomerBottomNav
import com.example.ui.components.LocalConnectTopBar
import com.example.ui.components.OwnerBottomNav
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.LocalConnectViewModelFactory
import com.example.ui.viewmodel.Screen

class MainActivity : ComponentActivity() {

  private val viewModel: LocalConnectViewModel by viewModels {
    val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
    val repository = LocalConnectRepository(database)
    LocalConnectViewModelFactory(repository)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      MyApplicationTheme {
        LocalConnectApp(viewModel = viewModel)
      }
    }
  }
}

@Composable
fun LocalConnectApp(viewModel: LocalConnectViewModel) {
  val currentScreen by viewModel.currentScreen.collectAsState()
  val currentProfile by viewModel.currentProfile.collectAsState()

  // Handle hardware / system back navigation
  BackHandler(enabled = true) {
    if (!viewModel.navigateBack()) {
      // At root, let system handle exit
    }
  }

  val isCustomerTabScreen = currentScreen is Screen.CustomerHome || currentScreen is Screen.Search || currentScreen is Screen.CustomerBookings || currentScreen is Screen.CustomerProfile
  val isOwnerTabScreen = currentScreen is Screen.BusinessDashboard || currentScreen is Screen.BusinessBookingManagement || currentScreen is Screen.ServiceManagement || currentScreen is Screen.SubscriptionManagement || currentScreen is Screen.BusinessAnalytics
  val isAdminTabScreen = currentScreen is Screen.AdminOverview || currentScreen is Screen.AdminApprovals || currentScreen is Screen.AdminBookings || currentScreen is Screen.AdminUsers || currentScreen is Screen.AdminSettings
  val isAuthScreen = currentScreen is Screen.Login || currentScreen is Screen.Register
  val canGoBack = !(currentScreen is Screen.CustomerHome || currentScreen is Screen.BusinessDashboard || currentScreen is Screen.AdminOverview || isAuthScreen)

  val screenTitle = when (currentScreen) {
    is Screen.Login -> "Sign In"
    is Screen.Register -> "Create Account"
    is Screen.PaymentCheckout -> "24/7 In-App Checkout"
    is Screen.CustomerHome -> null
    is Screen.Search -> "Explore Businesses"
    is Screen.BusinessProfile -> "Business Profile"
    is Screen.BookService -> "Book Service"
    is Screen.CustomerBookings -> "My Bookings"
    is Screen.CustomerProfile -> "Customer Account"
    is Screen.Legal -> "Legal & Policies"
    is Screen.BusinessDashboard -> "Owner Dashboard"
    is Screen.BusinessRegistration -> "Business Registration"
    is Screen.ManageBusinessProfile -> "Edit Business"
    is Screen.ServiceManagement -> "Services"
    is Screen.BusinessBookingManagement -> "Manage Bookings"
    is Screen.SubscriptionManagement -> "Subscription Plans"
    is Screen.BusinessAnalytics -> "Analytics"
    is Screen.AdminOverview -> "Admin Command Center"
    is Screen.AdminUsers -> "User Management"
    is Screen.AdminApprovals -> "Approvals Queue"
    is Screen.AdminCategories -> "Categories"
    is Screen.AdminBookings -> "Global Bookings"
    is Screen.AdminSubscriptions -> "Subscriptions & Pricing"
    is Screen.AdminFeatured -> "Featured Placements"
    is Screen.AdminReviewsReports -> "Reviews & Moderation"
    is Screen.AdminSettings -> "Platform Settings"
    is Screen.AdminAuditLogs -> "Audit Logs"
  }

  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      if (!isAuthScreen) {
        LocalConnectTopBar(
          viewModel = viewModel,
          title = screenTitle,
          canNavigateBack = canGoBack,
          onNavigateBack = { viewModel.navigateBack() }
        )
      }
    },
    bottomBar = {
      if (!isAuthScreen) {
        val role = currentProfile?.role ?: "CUSTOMER"
        when (role) {
          "ADMINISTRATOR" -> {
            if (isAdminTabScreen) {
              AdminBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
              )
            }
          }
          "BUSINESS_OWNER", "SERVICE_PROVIDER" -> {
            if (isOwnerTabScreen) {
              OwnerBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
              )
            }
          }
          else -> {
            if (isCustomerTabScreen) {
              CustomerBottomNav(
                currentScreen = currentScreen,
                onNavigate = { viewModel.navigateTo(it) }
              )
            }
          }
        }
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding),
      contentAlignment = Alignment.TopCenter
    ) {
      Box(
        modifier = Modifier
          .fillMaxHeight()
          .fillMaxWidth()
          .widthIn(max = 680.dp)
      ) {
        when (val screen = currentScreen) {
          // Auth Destinations
          is Screen.Login -> LoginScreen(viewModel = viewModel)
          is Screen.Register -> RegisterScreen(viewModel = viewModel)
          is Screen.PaymentCheckout -> PaymentCheckoutScreen(
            bookingId = screen.bookingId,
            amount = screen.amount,
            viewModel = viewModel
          )

          // Customer Destinations
          is Screen.CustomerHome -> CustomerHomeScreen(viewModel = viewModel)
          is Screen.Search -> SearchAndFilterScreen(viewModel = viewModel)
          is Screen.BusinessProfile -> BusinessProfileScreen(businessId = screen.businessId, viewModel = viewModel)
          is Screen.BookService -> BookServiceScreen(
            businessId = screen.businessId,
            selectedServiceId = screen.serviceId,
            viewModel = viewModel
          )
          is Screen.CustomerBookings -> CustomerBookingsScreen(viewModel = viewModel)
          is Screen.CustomerProfile -> CustomerProfileScreen(viewModel = viewModel)
          is Screen.Legal -> LegalPagesScreen(pageId = screen.pageId, viewModel = viewModel)

          // Business Owner Destinations
          is Screen.BusinessDashboard -> BusinessDashboardScreen(viewModel = viewModel)
          is Screen.BusinessRegistration -> BusinessRegistrationScreen(viewModel = viewModel)
          is Screen.ManageBusinessProfile -> ManageBusinessProfileScreen(viewModel = viewModel)
          is Screen.ServiceManagement -> ServiceManagementScreen(viewModel = viewModel)
          is Screen.BusinessBookingManagement -> BusinessBookingManagementScreen(viewModel = viewModel)
          is Screen.SubscriptionManagement -> SubscriptionManagementScreen(viewModel = viewModel)
          is Screen.BusinessAnalytics -> BusinessAnalyticsScreen(viewModel = viewModel)

          // Admin Destinations
          is Screen.AdminOverview -> AdminOverviewScreen(viewModel = viewModel)
          is Screen.AdminUsers -> AdminUsersScreen(viewModel = viewModel)
          is Screen.AdminApprovals -> AdminBusinessApprovalsScreen(viewModel = viewModel)
          is Screen.AdminCategories -> AdminCategoriesScreen(viewModel = viewModel)
          is Screen.AdminBookings -> AdminBookingsScreen(viewModel = viewModel)
          is Screen.AdminSubscriptions -> SubscriptionManagementScreen(viewModel = viewModel)
          is Screen.AdminFeatured -> AdminBusinessApprovalsScreen(viewModel = viewModel)
          is Screen.AdminReviewsReports -> AdminReviewsReportsScreen(viewModel = viewModel)
          is Screen.AdminSettings -> AdminSettingsScreen(viewModel = viewModel)
          is Screen.AdminAuditLogs -> AdminAuditLogsScreen(viewModel = viewModel)
        }
      }
    }
  }
}
