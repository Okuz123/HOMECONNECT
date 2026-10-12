package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessEntity
import com.example.data.model.CategoryEntity
import com.example.data.model.PlatformSettingsEntity
import com.example.data.model.ReportEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminOverviewScreen(
  viewModel: LocalConnectViewModel
) {
  val currentProfile by viewModel.currentProfile.collectAsState()

  // RBAC Enforcement Check
  if (currentProfile?.role != "ADMINISTRATOR") {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Default.Lock, contentDescription = null, tint = StatusError, modifier = Modifier.size(56.dp))
        Spacer(modifier = Modifier.height(12.dp))
        Text("Access Denied", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(
          "This area requires verified administrator privileges. Use the role switcher top-right to switch to the Administrator profile.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
    return
  }

  val users by viewModel.allProfiles.collectAsState()
  val businesses by viewModel.allBusinessesAdmin.collectAsState()
  val bookings by viewModel.allBookingsAdmin.collectAsState()
  val auditLogs by viewModel.allAuditLogs.collectAsState()
  val reports by viewModel.allReportsAdmin.collectAsState()

  val approvedBusinessesCount = businesses.count { it.verificationStatus == "APPROVED" }
  val pendingBusinessesCount = businesses.count { it.verificationStatus == "UNDER_REVIEW" || it.verificationStatus == "SUBMITTED" }
  val completedBookings = bookings.filter { it.status == "COMPLETED" }
  val totalGrossVolume = completedBookings.sumOf { it.agreedPrice }
  val totalCommissionRevenue = totalGrossVolume * 0.10
  val activeDisputes = bookings.count { it.status == "DISPUTED" }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_overview_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Platform Administration", fontSize = 20.sp, fontWeight = FontWeight.Bold)
          Text("LocalConnect Central Command & Governance", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        StatusBadge(status = "ADMIN")
      }
    }

    // Platform Metrics Grid
    item {
      Text("Platform Statistics", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard("Registered Users", "${users.size}", PrimaryBlue, Modifier.weight(1f))
        KpiCard("Approved Businesses", "$approvedBusinessesCount", StatusSuccess, Modifier.weight(1f))
        KpiCard("Awaiting Approval", "$pendingBusinessesCount", StatusWarning, Modifier.weight(1f))
      }
      Spacer(modifier = Modifier.height(8.dp))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        KpiCard("Total Bookings", "${bookings.size}", PrimaryBlue, Modifier.weight(1f))
        KpiCard("Gross Volume", "₹${totalGrossVolume.toInt()}", TertiaryTeal, Modifier.weight(1f))
        KpiCard("Commission (10%)", "₹${totalCommissionRevenue.toInt()}", StatusSuccess, Modifier.weight(1f))
      }
      if (activeDisputes > 0 || reports.count { it.status == "PENDING" } > 0) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          KpiCard("Open Disputes", "$activeDisputes", StatusError, Modifier.weight(1f))
          KpiCard("Pending Reports", "${reports.count { it.status == "PENDING" }}", StatusWarning, Modifier.weight(1f))
        }
      }
    }

    // Quick Admin Management Portals
    item {
      Text("Administrative Sections", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AdminNavCard("Business Verification Queue", "$pendingBusinessesCount businesses awaiting inspection", Icons.Default.Approval) {
          viewModel.navigateTo(Screen.AdminApprovals)
        }
        AdminNavCard("Category & Hierarchy Management", "Add, edit, reorder marketplace service categories", Icons.Default.Category) {
          viewModel.navigateTo(Screen.AdminCategories)
        }
        AdminNavCard("Bookings & Dispute Governance", "Global audit of booking requests and dispute resolutions", Icons.Default.Assignment) {
          viewModel.navigateTo(Screen.AdminBookings)
        }
        AdminNavCard("Subscription & Pricing Controls", "Manage plan tiers (₹299, ₹599) and commission rate", Icons.Default.Paid) {
          viewModel.navigateTo(Screen.AdminSubscriptions)
        }
        AdminNavCard("User Accounts & Access Control", "Manage user roles, suspension, and deletion requests", Icons.Default.Group) {
          viewModel.navigateTo(Screen.AdminUsers)
        }
        AdminNavCard("Reviews & Fraud Reports", "Moderate customer feedback and investigate abuse reports", Icons.Default.RateReview) {
          viewModel.navigateTo(Screen.AdminReviewsReports)
        }
        AdminNavCard("System Audit Log", "${auditLogs.size} recorded administrative governance events", Icons.Default.History) {
          viewModel.navigateTo(Screen.AdminAuditLogs)
        }
      }
    }
  }
}

@Composable
fun AdminNavCard(
  title: String,
  subtitle: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
      Spacer(modifier = Modifier.width(12.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}

@Composable
fun AdminBusinessApprovalsScreen(
  viewModel: LocalConnectViewModel
) {
  val businesses by viewModel.allBusinessesAdmin.collectAsState()

  var rejectTarget by remember { mutableStateOf<BusinessEntity?>(null) }
  var rejectReason by remember { mutableStateOf("") }
  var selectedBizForDocs by remember { mutableStateOf<BusinessEntity?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_approvals_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Business Approvals & Verification", fontSize = 18.sp, fontWeight = FontWeight.Bold)
      Text("Inspect submitted business registrations and government documents before granting verified public status.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    items(businesses) { biz ->
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("admin_biz_card_${biz.id}")
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(biz.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Text("${biz.city} • ${biz.serviceAreas}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            StatusBadge(status = biz.verificationStatus)
          }

          Spacer(modifier = Modifier.height(6.dp))
          Text(biz.description, fontSize = 12.sp, maxLines = 2)
          Spacer(modifier = Modifier.height(4.dp))
          Text("Public Contact: ${biz.publicPhone} • ${biz.publicEmail}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)

          if (biz.rejectionReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text("Rejection Reason: ${biz.rejectionReason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
          ) {
            TextButton(onClick = { selectedBizForDocs = biz }) {
              Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text("Inspect Docs", fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.width(8.dp))

            if (biz.verificationStatus != "APPROVED") {
              OutlinedButton(
                onClick = { rejectTarget = biz },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.testTag("admin_reject_btn_${biz.id}")
              ) {
                Text("Reject", fontSize = 11.sp)
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = { viewModel.adminApproveBusiness(biz.id) },
                modifier = Modifier.testTag("admin_approve_btn_${biz.id}")
              ) {
                Text("Approve & Verify", fontSize = 11.sp)
              }
            } else {
              OutlinedButton(
                onClick = { viewModel.adminToggleFeatured(biz.id, !biz.isFeatured) }
              ) {
                Text(if (biz.isFeatured) "Remove Featured" else "Make Featured", fontSize = 11.sp)
              }
            }
          }
        }
      }
    }
  }

  // Reject Reason Dialog
  rejectTarget?.let { biz ->
    AlertDialog(
      onDismissRequest = { rejectTarget = null },
      title = { Text("Reject Business Listing") },
      text = {
        Column {
          Text("Provide a clear reason for the rejection (e.g. Invalid trade license, mismatch in address):", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = rejectReason,
            onValueChange = { rejectReason = it },
            label = { Text("Rejection Reason *") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (rejectReason.isNotBlank()) {
              viewModel.adminRejectBusiness(biz.id, rejectReason)
              rejectTarget = null
              rejectReason = ""
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Confirm Rejection")
        }
      },
      dismissButton = {
        TextButton(onClick = { rejectTarget = null }) { Text("Cancel") }
      }
    )
  }

  // Inspect Documents Dialog
  selectedBizForDocs?.let { biz ->
    val docs by viewModel.getDocumentsForBusiness(biz.id).collectAsState()

    AlertDialog(
      onDismissRequest = { selectedBizForDocs = null },
      title = { Text("Verification Documents: ${biz.name}") },
      text = {
        Column {
          if (docs.isEmpty()) {
            Text("No digital verification documents uploaded yet.", fontSize = 12.sp)
          } else {
            docs.forEach { doc ->
              Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
              ) {
                Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(8.dp))
                  Column {
                    Text(doc.documentType.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("Secure storage path: ${doc.privateFilePath}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
                }
              }
            }
          }
        }
      },
      confirmButton = {
        Button(onClick = { selectedBizForDocs = null }) { Text("Done") }
      }
    )
  }
}

@Composable
fun AdminCategoriesScreen(
  viewModel: LocalConnectViewModel
) {
  val categories by viewModel.allCategoriesAdmin.collectAsState()

  var showAddCategory by remember { mutableStateOf(false) }
  var newCatName by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_categories_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text("Category Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text("${categories.size} categories configured", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
          onClick = { showAddCategory = true },
          modifier = Modifier.testTag("admin_add_category_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Category")
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(categories) { cat ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp),
              contentAlignment = Alignment.Center
            ) {
              Icon(getCategoryIcon(cat.id), contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(cat.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("Slug: ${cat.slug} • Order: ${cat.displayOrder}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = cat.active,
              onCheckedChange = { viewModel.adminToggleCategoryActive(cat.id, it) }
            )
          }
        }
      }
    }
  }

  if (showAddCategory) {
    AlertDialog(
      onDismissRequest = { showAddCategory = false },
      title = { Text("Add Category") },
      text = {
        OutlinedTextField(
          value = newCatName,
          onValueChange = { newCatName = it },
          label = { Text("Category Name (e.g. Roofing, Gardening)") },
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        Button(
          onClick = {
            if (newCatName.isNotBlank()) {
              viewModel.adminAddCategory(newCatName, "ic_service")
              showAddCategory = false
              newCatName = ""
            }
          }
        ) {
          Text("Create")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddCategory = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun AdminSettingsScreen(
  viewModel: LocalConnectViewModel
) {
  val settings by viewModel.platformSettings.collectAsState()

  if (settings == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    return
  }

  var platformName by remember { mutableStateOf(settings!!.platformName) }
  var commissionRate by remember { mutableStateOf(settings!!.defaultCommissionRate.toString()) }
  var supportedCities by remember { mutableStateOf(settings!!.supportedCities) }
  var supportPhone by remember { mutableStateOf(settings!!.supportPhone) }
  var supportEmail by remember { mutableStateOf(settings!!.supportEmail) }
  var savedAlert by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Platform Settings & Governance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text("Configure marketplace commission rates, supported cities, and compliance parameters.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    item {
      OutlinedTextField(
        value = platformName,
        onValueChange = { platformName = it },
        label = { Text("Platform Brand Name") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = commissionRate,
        onValueChange = { commissionRate = it },
        label = { Text("Default Commission Rate (%) on Completed Bookings") },
        modifier = Modifier.fillMaxWidth().testTag("admin_commission_input")
      )
    }

    item {
      OutlinedTextField(
        value = supportedCities,
        onValueChange = { supportedCities = it },
        label = { Text("Supported Cities (Comma-separated)") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = supportPhone,
        onValueChange = { supportPhone = it },
        label = { Text("Customer Support Phone") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = supportEmail,
        onValueChange = { supportEmail = it },
        label = { Text("Customer Support Email") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Button(
        onClick = {
          val rateVal = commissionRate.toDoubleOrNull() ?: 10.0
          viewModel.adminUpdateSettings(
            settings!!.copy(
              platformName = platformName,
              defaultCommissionRate = rateVal,
              supportedCities = supportedCities,
              supportPhone = supportPhone,
              supportEmail = supportEmail
            )
          )
          savedAlert = true
        },
        modifier = Modifier.fillMaxWidth().testTag("save_admin_settings_btn")
      ) {
        Text("Save Platform Settings")
      }
    }
  }

  if (savedAlert) {
    AlertDialog(
      onDismissRequest = { savedAlert = false },
      title = { Text("Settings Saved") },
      text = { Text("Platform configuration and commission rates have been updated and logged.") },
      confirmButton = {
        Button(onClick = { savedAlert = false }) { Text("OK") }
      }
    )
  }
}
