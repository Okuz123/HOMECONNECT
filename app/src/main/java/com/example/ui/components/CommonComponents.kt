package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NotificationEntity
import com.example.data.model.ProfileEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalConnectTopBar(
  viewModel: LocalConnectViewModel,
  title: String? = null,
  canNavigateBack: Boolean = false,
  onNavigateBack: () -> Unit = {}
) {
  val currentProfile by viewModel.currentProfile.collectAsState()
  val selectedCity by viewModel.selectedCity.collectAsState()
  val notifications by viewModel.userNotifications.collectAsState()
  val unreadCount = notifications.count { it.readAt == null }

  var showRoleDialog by remember { mutableStateOf(false) }
  var showCityDialog by remember { mutableStateOf(false) }
  var showNotifDialog by remember { mutableStateOf(false) }

  TopAppBar(
    title = {
      if (title != null) {
        Text(
          text = title,
          fontWeight = FontWeight.Bold,
          fontSize = 17.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      } else {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(30.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Handyman,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(6.dp))
          Column {
            Text(
              text = "My Project: My Connect",
              fontWeight = FontWeight.ExtraBold,
              fontSize = 13.sp,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            // City selector button
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .clickable { showCityDialog = true }
                .testTag("top_city_selector")
            ) {
              Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(11.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = selectedCity,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
              )
              Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
            }
          }
        }
      }
    },
    navigationIcon = {
      if (canNavigateBack) {
        IconButton(
          onClick = onNavigateBack,
          modifier = Modifier.testTag("top_back_button")
        ) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
      }
    },
    actions = {
      // Notification icon
      IconButton(
        onClick = { showNotifDialog = true },
        modifier = Modifier.testTag("top_notifications_button")
      ) {
        BadgedBox(
          badge = {
            if (unreadCount > 0) {
              Badge { Text("$unreadCount") }
            }
          }
        ) {
          Icon(Icons.Outlined.Notifications, contentDescription = "Notifications", modifier = Modifier.size(20.dp))
        }
      }

      // Role switcher pill button
      currentProfile?.let { profile ->
        AssistChip(
          onClick = { showRoleDialog = true },
          label = {
            Text(
              text = when (profile.role) {
                "ADMINISTRATOR" -> "Admin"
                "BUSINESS_OWNER" -> "Owner"
                "SERVICE_PROVIDER" -> "Provider"
                else -> "User"
              },
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          },
          leadingIcon = {
            Icon(
              imageVector = when (profile.role) {
                "ADMINISTRATOR" -> Icons.Default.AdminPanelSettings
                "BUSINESS_OWNER" -> Icons.Default.Storefront
                "SERVICE_PROVIDER" -> Icons.Default.Engineering
                else -> Icons.Default.Person
              },
              contentDescription = null,
              modifier = Modifier.size(13.dp),
              tint = when (profile.role) {
                "ADMINISTRATOR" -> StatusPurple
                "BUSINESS_OWNER" -> SecondaryAmber
                "SERVICE_PROVIDER" -> TertiaryTeal
                else -> PrimaryBlue
              }
            )
          },
          modifier = Modifier
            .padding(end = 4.dp)
            .testTag("role_switcher_chip"),
          colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
          )
        )
      }
    },
    colors = TopAppBarDefaults.topAppBarColors(
      containerColor = MaterialTheme.colorScheme.surface
    )
  )

  if (showCityDialog) {
    CitySelectorDialog(
      currentCity = selectedCity,
      onSelectCity = { city ->
        viewModel.setCity(city)
        showCityDialog = false
      },
      onDismiss = { showCityDialog = false }
    )
  }

  if (showRoleDialog) {
    RoleSwitcherDialog(
      viewModel = viewModel,
      onDismiss = { showRoleDialog = false }
    )
  }

  if (showNotifDialog) {
    NotificationsDialog(
      notifications = notifications,
      onMarkRead = { id -> viewModel.markNotificationRead(id) },
      onDismiss = { showNotifDialog = false }
    )
  }
}

@Composable
fun CitySelectorDialog(
  currentCity: String,
  onSelectCity: (String) -> Unit,
  onDismiss: () -> Unit
) {
  val cities = listOf("Mumbai", "Bengaluru", "Delhi NCR", "Pune", "Hyderabad", "Chennai")

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select Your City", fontWeight = FontWeight.Bold) },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 380.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          "My Connect connects local businesses & 24/7 service providers in your city.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(10.dp))
        cities.forEach { city ->
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (city == currentCity) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            modifier = Modifier
              .fillMaxWidth()
              .clickable { onSelectCity(city) }
              .padding(vertical = 3.dp)
              .testTag("city_option_$city")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.LocationCity,
                contentDescription = null,
                tint = if (city == currentCity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              Text(
                text = city,
                fontSize = 13.sp,
                fontWeight = if (city == currentCity) FontWeight.Bold else FontWeight.Normal,
                color = if (city == currentCity) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
              )
              if (city == currentCity) {
                Spacer(modifier = Modifier.weight(1f))
                Icon(
                  Icons.Default.Check,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}

@Composable
fun RoleSwitcherDialog(
  viewModel: LocalConnectViewModel,
  onDismiss: () -> Unit
) {
  val profiles by viewModel.allProfiles.collectAsState()
  val currentUserId by viewModel.currentUserId.collectAsState()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Column {
        Text("Account & Role Switcher", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(
          "Switch active test user or access Login/Registration",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
          .verticalScroll(rememberScrollState())
      ) {
        profiles.forEach { profile ->
          val isSelected = profile.id == currentUserId
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.switchActiveProfile(profile.id)
                onDismiss()
              }
              .padding(vertical = 3.dp)
              .testTag("profile_option_${profile.id}")
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(
                    when (profile.role) {
                      "ADMINISTRATOR" -> StatusPurple
                      "BUSINESS_OWNER" -> SecondaryAmber
                      "SERVICE_PROVIDER" -> TertiaryTeal
                      else -> PrimaryBlue
                    }
                  ),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = profile.fullName.take(1),
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp
                )
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = profile.fullName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp
                )
                Text(
                  text = "${profile.role.replace("_", " ")} • ${profile.preferredCity}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (isSelected) {
                Icon(
                  Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        // Direct Login / Register actions
        OutlinedButton(
          onClick = {
            onDismiss()
            viewModel.navigateTo(Screen.Login)
          },
          modifier = Modifier.fillMaxWidth().testTag("switch_to_login_btn")
        ) {
          Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Open Login Screen", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Button(
          onClick = {
            onDismiss()
            viewModel.navigateTo(Screen.Register)
          },
          modifier = Modifier.fillMaxWidth().testTag("switch_to_register_btn")
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Register New Account", fontSize = 12.sp)
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}

@Composable
fun NotificationsDialog(
  notifications: List<NotificationEntity>,
  onMarkRead: (String) -> Unit,
  onDismiss: () -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("In-App Notifications", fontWeight = FontWeight.Bold) },
    text = {
      if (notifications.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            "No notifications yet.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 13.sp
          )
        }
      } else {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
            .verticalScroll(rememberScrollState())
        ) {
          notifications.take(8).forEach { notif ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = if (notif.readAt == null) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
              modifier = Modifier
                .fillMaxWidth()
                .clickable { onMarkRead(notif.id) }
                .padding(vertical = 3.dp)
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = when (notif.notificationType) {
                      "BOOKING" -> Icons.Default.Event
                      "APPROVAL" -> Icons.Default.Verified
                      "SUBSCRIPTION" -> Icons.Default.Star
                      else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = notif.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                  )
                  if (notif.readAt == null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Box(
                      modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(StatusInfo)
                    )
                  }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = notif.message,
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) { Text("Done") }
    }
  )
}

@Composable
fun StatusBadge(status: String) {
  val (color, bgColor) = when (status) {
    "APPROVED", "COMPLETED", "ACTIVE", "VERIFIED", "PAID_ONLINE" -> StatusSuccess to Color(0xFFD1FAE5)
    "PENDING", "UNDER_REVIEW", "SCHEDULED" -> StatusWarning to Color(0xFFFEF3C7)
    "IN_PROGRESS" -> StatusInfo to Color(0xFFDBEAFE)
    "REJECTED", "CANCELLED", "FAILED", "UNPAID" -> StatusError to Color(0xFFFEE2E2)
    "DISPUTED" -> StatusPurple to Color(0xFFEDE9FE)
    else -> Color.Gray to Color(0xFFF3F4F6)
  }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor,
    modifier = Modifier.padding(horizontal = 2.dp, vertical = 2.dp)
  ) {
    Text(
      text = status.replace("_", " "),
      color = color,
      fontSize = 10.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    )
  }
}

@Composable
fun TwentyFourSevenBadge() {
  Surface(
    shape = RoundedCornerShape(4.dp),
    color = Color(0xFFD1FAE5),
    modifier = Modifier.padding(end = 4.dp)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(10.dp))
      Spacer(modifier = Modifier.width(2.dp))
      Text("24/7", color = StatusSuccess, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
    }
  }
}

@Composable
fun VerificationBadge(isVerified: Boolean) {
  if (isVerified) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Icon(
        Icons.Default.Verified,
        contentDescription = "Verified Business",
        tint = PrimaryBlue,
        modifier = Modifier.size(13.dp)
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
        text = "Verified",
        color = PrimaryBlue,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold
      )
    }
  }
}

@Composable
fun SponsoredBadge() {
  Surface(
    shape = RoundedCornerShape(4.dp),
    color = SecondaryContainerAmber,
    modifier = Modifier.padding(end = 4.dp)
  ) {
    Text(
      text = "FEATURED",
      color = OnSecondaryContainerAmber,
      fontSize = 9.sp,
      fontWeight = FontWeight.ExtraBold,
      modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    )
  }
}

@Composable
fun RatingPill(rating: Float, reviewCount: Int) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFFEF3C7)
  ) {
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
      Icon(
        Icons.Default.Star,
        contentDescription = null,
        tint = Color(0xFFD97706),
        modifier = Modifier.size(12.dp)
      )
      Spacer(modifier = Modifier.width(2.dp))
      Text(
        text = String.format("%.1f", rating),
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        color = Color(0xFF78350F)
      )
      if (reviewCount > 0) {
        Text(
          text = " ($reviewCount)",
          fontSize = 10.sp,
          color = Color(0xFF92400E)
        )
      }
    }
  }
}

@Composable
fun CustomerBottomNav(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit
) {
  NavigationBar(
    windowInsets = WindowInsets.navigationBars,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    NavigationBarItem(
      selected = currentScreen is Screen.CustomerHome,
      onClick = { onNavigate(Screen.CustomerHome) },
      icon = { Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(20.dp)) },
      label = { Text("Home", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_customer_home")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.Search,
      onClick = { onNavigate(Screen.Search) },
      icon = { Icon(Icons.Default.Search, contentDescription = "Search", modifier = Modifier.size(20.dp)) },
      label = { Text("Explore", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_customer_search")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.CustomerBookings,
      onClick = { onNavigate(Screen.CustomerBookings) },
      icon = { Icon(Icons.Default.CalendarMonth, contentDescription = "Bookings", modifier = Modifier.size(20.dp)) },
      label = { Text("Bookings", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_customer_bookings")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.CustomerProfile,
      onClick = { onNavigate(Screen.CustomerProfile) },
      icon = { Icon(Icons.Default.AccountCircle, contentDescription = "Profile", modifier = Modifier.size(20.dp)) },
      label = { Text("Account", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_customer_profile")
    )
  }
}

@Composable
fun OwnerBottomNav(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit
) {
  NavigationBar(
    windowInsets = WindowInsets.navigationBars,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    NavigationBarItem(
      selected = currentScreen is Screen.BusinessDashboard,
      onClick = { onNavigate(Screen.BusinessDashboard) },
      icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard", modifier = Modifier.size(20.dp)) },
      label = { Text("Overview", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_owner_dashboard")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.BusinessBookingManagement,
      onClick = { onNavigate(Screen.BusinessBookingManagement) },
      icon = { Icon(Icons.Default.Inbox, contentDescription = "Bookings", modifier = Modifier.size(20.dp)) },
      label = { Text("Bookings", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_owner_bookings")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.ServiceManagement,
      onClick = { onNavigate(Screen.ServiceManagement) },
      icon = { Icon(Icons.Default.DesignServices, contentDescription = "Services", modifier = Modifier.size(20.dp)) },
      label = { Text("Services", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_owner_services")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.SubscriptionManagement,
      onClick = { onNavigate(Screen.SubscriptionManagement) },
      icon = { Icon(Icons.Default.CardMembership, contentDescription = "Plans", modifier = Modifier.size(20.dp)) },
      label = { Text("Plans", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_owner_subscription")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.BusinessAnalytics,
      onClick = { onNavigate(Screen.BusinessAnalytics) },
      icon = { Icon(Icons.Default.Analytics, contentDescription = "Analytics", modifier = Modifier.size(20.dp)) },
      label = { Text("Stats", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_owner_analytics")
    )
  }
}

@Composable
fun AdminBottomNav(
  currentScreen: Screen,
  onNavigate: (Screen) -> Unit
) {
  NavigationBar(
    windowInsets = WindowInsets.navigationBars,
    containerColor = MaterialTheme.colorScheme.surface
  ) {
    NavigationBarItem(
      selected = currentScreen is Screen.AdminOverview,
      onClick = { onNavigate(Screen.AdminOverview) },
      icon = { Icon(Icons.Default.AdminPanelSettings, contentDescription = "Overview", modifier = Modifier.size(20.dp)) },
      label = { Text("Overview", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_admin_overview")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.AdminApprovals,
      onClick = { onNavigate(Screen.AdminApprovals) },
      icon = { Icon(Icons.Default.Approval, contentDescription = "Approvals", modifier = Modifier.size(20.dp)) },
      label = { Text("Approve", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_admin_approvals")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.AdminBookings,
      onClick = { onNavigate(Screen.AdminBookings) },
      icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Bookings", modifier = Modifier.size(20.dp)) },
      label = { Text("Bookings", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_admin_bookings")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.AdminUsers,
      onClick = { onNavigate(Screen.AdminUsers) },
      icon = { Icon(Icons.Default.Group, contentDescription = "Users", modifier = Modifier.size(20.dp)) },
      label = { Text("Users", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_admin_users")
    )
    NavigationBarItem(
      selected = currentScreen is Screen.AdminSettings,
      onClick = { onNavigate(Screen.AdminSettings) },
      icon = { Icon(Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(20.dp)) },
      label = { Text("Settings", fontSize = 10.sp) },
      modifier = Modifier.testTag("nav_admin_settings")
    )
  }
}
