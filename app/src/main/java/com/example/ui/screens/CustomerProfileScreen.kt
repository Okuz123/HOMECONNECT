package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CustomerProfileScreen(
  viewModel: LocalConnectViewModel
) {
  val profile by viewModel.currentProfile.collectAsState()
  val bookings by viewModel.customerBookings.collectAsState()
  val selectedCity by viewModel.selectedCity.collectAsState()

  var showDeleteDialog by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("customer_profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Profile Header
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(60.dp)
              .clip(CircleShape)
              .background(PrimaryBlue),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = profile?.fullName?.take(1) ?: "U",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 24.sp
            )
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column {
            Text(
              text = profile?.fullName ?: "Customer",
              fontWeight = FontWeight.Bold,
              fontSize = 18.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
              text = profile?.email ?: "",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                Icons.Default.Verified,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "${profile?.phone} (Phone Verified)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = StatusSuccess
              )
            }
          }
        }
      }
    }

    // Booking Summary Metrics
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Total Bookings", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${bookings.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold)
          }
        }
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Completed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${bookings.count { it.status == "COMPLETED" }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
          }
        }
        Card(
          modifier = Modifier.weight(1f),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          shape = RoundedCornerShape(12.dp)
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text("Active / Sched", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("${bookings.count { it.status == "SCHEDULED" || it.status == "PENDING" }}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
          }
        }
      }
    }

    // Account Preferences
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Account & Location", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Preferred City", fontSize = 13.sp, fontWeight = FontWeight.Medium)
              Text("Browse and book providers in this region", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(selectedCity, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
          }

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))

          Text("Privacy & Security", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "LocalConnect enforces strict zero-knowledge address protection. Unconfirmed businesses cannot see your street address or phone number.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(12.dp))
          OutlinedButton(
            onClick = { viewModel.navigateTo(Screen.Legal("privacy")) },
            modifier = Modifier.fillMaxWidth()
          ) {
            Text("Read Privacy Policy")
          }
        }
      }
    }

    // Switch Role / Portal Links
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Business & Admin Portals", fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            "Use the top-right profile chip or buttons below to switch role contexts.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))

          Button(
            onClick = { viewModel.navigateTo(Screen.BusinessRegistration) },
            modifier = Modifier.fillMaxWidth().testTag("profile_list_biz_btn")
          ) {
            Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register a Business")
          }

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = { viewModel.switchRoleDestination(Screen.AdminOverview) },
            modifier = Modifier.fillMaxWidth().testTag("profile_admin_btn")
          ) {
            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Open Administrator Panel")
          }

          Spacer(modifier = Modifier.height(8.dp))
          OutlinedButton(
            onClick = { viewModel.navigateTo(Screen.Login) },
            modifier = Modifier.fillMaxWidth().testTag("profile_login_btn")
          ) {
            Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Switch Account / Sign In")
          }
        }
      }
    }

    // Danger Zone - Account Deletion
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Account Management", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.error)
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            "Request account deletion and removal of personal identifiable data, subject to legal compliance and completed transaction records.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedButton(
            onClick = { showDeleteDialog = true },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.testTag("delete_account_button")
          ) {
            Text("Request Account Deletion")
          }
        }
      }
    }
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Account Confirmation") },
      text = {
        Text(
          "Are you sure you want to request account deletion? All personal details will be expunged. Active bookings will be cancelled."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.requestAccountDeletion {
              showDeleteDialog = false
              viewModel.navigateTo(Screen.CustomerHome)
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Yes, Delete My Account")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
      }
    )
  }
}
