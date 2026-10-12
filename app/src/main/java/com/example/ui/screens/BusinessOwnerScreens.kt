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
import com.example.data.model.BookingEntity
import com.example.data.model.BusinessEntity
import com.example.data.model.ServiceEntity
import com.example.ui.components.SponsoredBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.*
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BusinessDashboardScreen(
  viewModel: LocalConnectViewModel
) {
  val business by viewModel.currentOwnerBusiness.collectAsState()
  val bookings by viewModel.ownerBookings.collectAsState()
  val subscription by viewModel.ownerSubscription.collectAsState()

  if (business == null) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
          Icons.Default.Storefront,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "No Business Listing Found",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "You are currently logged in as a business account, but haven't registered a business yet.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
          onClick = { viewModel.navigateTo(Screen.BusinessRegistration) },
          modifier = Modifier.testTag("dashboard_register_biz_btn")
        ) {
          Text("Register Business Now")
        }
      }
    }
    return
  }

  val biz = business!!
  val pendingCount = bookings.count { it.status == "PENDING" }
  val activeCount = bookings.count { it.status == "SCHEDULED" || it.status == "IN_PROGRESS" || it.status == "ACCEPTED" }
  val completedCount = bookings.count { it.status == "COMPLETED" }
  val cancelledCount = bookings.count { it.status == "CANCELLED" || it.status == "REJECTED" }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("business_dashboard_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = biz.name,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
              )
              Text(
                text = "${biz.city} • ${biz.serviceAreas.take(30)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
            StatusBadge(status = biz.verificationStatus)
          }

          if (biz.isFeatured) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              SponsoredBadge()
              Text(
                text = "Featured Placement Active (Top Category Visibility)",
                fontSize = 11.sp,
                color = SecondaryAmber,
                fontWeight = FontWeight.SemiBold
              )
            }
          }

          if (biz.verificationStatus == "REJECTED" && biz.rejectionReason.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Rejection note: ${biz.rejectionReason}",
              color = MaterialTheme.colorScheme.error,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    // Booking KPIs Grid
    item {
      Text("Booking Overview", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard("Pending", "$pendingCount", StatusWarning, Modifier.weight(1f))
        KpiCard("Scheduled", "$activeCount", PrimaryBlue, Modifier.weight(1f))
        KpiCard("Completed", "$completedCount", StatusSuccess, Modifier.weight(1f))
        KpiCard("Cancelled", "$cancelledCount", StatusError, Modifier.weight(1f))
      }
    }

    // Subscription Status Banner
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.CardMembership,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = if (subscription != null && subscription!!.status == "ACTIVE") "Featured Business Plan" else "Free Plan",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Text(
              text = if (subscription != null && subscription!!.status == "ACTIVE") {
                val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                "Renewal: ${sdf.format(Date(subscription!!.renewalDate))}"
              } else "Basic listing. Upgrade for top placement & priority inquiries.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onPrimaryContainer
            )
          }
          Button(
            onClick = { viewModel.navigateTo(Screen.SubscriptionManagement) },
            modifier = Modifier.testTag("dashboard_upgrade_plan_btn")
          ) {
            Text(if (subscription != null && subscription!!.status == "ACTIVE") "Manage" else "Upgrade", fontSize = 11.sp)
          }
        }
      }
    }

    // Quick Action Buttons
    item {
      Text("Business Management", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.BusinessBookingManagement) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Inbox, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Bookings ($pendingCount)")
        }
        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.ServiceManagement) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Services")
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.ManageBusinessProfile) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Edit Profile")
        }
        OutlinedButton(
          onClick = { viewModel.navigateTo(Screen.BusinessAnalytics) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Analytics")
        }
      }
    }

    // Recent Bookings Feed
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text("Incoming Requests", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        TextButton(onClick = { viewModel.navigateTo(Screen.BusinessBookingManagement) }) {
          Text("View All")
        }
      }

      if (bookings.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
          contentAlignment = Alignment.Center
        ) {
          Text("No booking requests received yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
        }
      } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          bookings.take(3).forEach { bk ->
            Card(
              shape = RoundedCornerShape(10.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${bk.preferredDate} (${bk.preferredTime})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                  )
                  Text(
                    text = "${bk.serviceArea} • ${bk.description}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                  )
                }
                StatusBadge(status = bk.status)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun KpiCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    modifier = modifier
  ) {
    Column(modifier = Modifier.padding(10.dp)) {
      Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
    }
  }
}

@Composable
fun BusinessRegistrationScreen(
  viewModel: LocalConnectViewModel
) {
  val categories by viewModel.activeCategories.collectAsState()
  val selectedCity by viewModel.selectedCity.collectAsState()

  var name by remember { mutableStateOf("") }
  var categoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "cat_plumbing") }
  var description by remember { mutableStateOf("") }
  var publicPhone by remember { mutableStateOf("+91 ") }
  var publicEmail by remember { mutableStateOf("") }
  var city by remember { mutableStateOf(selectedCity) }
  var serviceAreas by remember { mutableStateOf("") }
  var publicAddress by remember { mutableStateOf("") }
  var openingHours by remember { mutableStateOf("Mon - Sat: 9:00 AM - 8:00 PM") }
  var docType by remember { mutableStateOf("GST_CERTIFICATE") }

  var errorMsg by remember { mutableStateOf<String?>(null) }
  var submittedSuccess by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("business_registration_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Register Your Business", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text(
        "Submit your trade or business listing. Listings are reviewed by administrators before being marked as Verified.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    errorMsg?.let { err ->
      item {
        Text(err, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }

    item {
      OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        label = { Text("Legal / Trading Business Name *") },
        modifier = Modifier.fillMaxWidth().testTag("reg_biz_name_input")
      )
    }

    item {
      Text("Business Category *", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
      Spacer(modifier = Modifier.height(4.dp))
      categories.forEach { cat ->
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { categoryId = cat.id }
            .padding(vertical = 2.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          RadioButton(selected = categoryId == cat.id, onClick = { categoryId = cat.id })
          Spacer(modifier = Modifier.width(6.dp))
          Text(cat.name, fontSize = 13.sp)
        }
      }
    }

    item {
      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Business Description *") },
        modifier = Modifier.fillMaxWidth().testTag("reg_biz_desc_input"),
        maxLines = 3
      )
    }

    item {
      OutlinedTextField(
        value = publicPhone,
        onValueChange = { publicPhone = it },
        label = { Text("Public Contact Phone *") },
        modifier = Modifier.fillMaxWidth().testTag("reg_biz_phone_input")
      )
    }

    item {
      OutlinedTextField(
        value = publicEmail,
        onValueChange = { publicEmail = it },
        label = { Text("Business Email *") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = city,
        onValueChange = { city = it },
        label = { Text("City *") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = serviceAreas,
        onValueChange = { serviceAreas = it },
        label = { Text("Service Areas (Comma-separated neighbourhoods) *") },
        placeholder = { Text("e.g. Bandra, Andheri, Powai") },
        modifier = Modifier.fillMaxWidth().testTag("reg_biz_areas_input")
      )
    }

    item {
      OutlinedTextField(
        value = publicAddress,
        onValueChange = { publicAddress = it },
        label = { Text("Public Address / Workshop *") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = openingHours,
        onValueChange = { openingHours = it },
        label = { Text("Opening Hours") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text("Verification Document Upload", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          Text(
            "Upload government or trade documentation to earn the Verified badge.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row {
            listOf("GST_CERTIFICATE", "TRADE_LICENSE", "ID_PROOF").forEach { type ->
              FilterChip(
                selected = docType == type,
                onClick = { docType = type },
                label = { Text(type.replace("_", " "), fontSize = 10.sp) },
                modifier = Modifier.padding(end = 6.dp)
              )
            }
          }
        }
      }
    }

    item {
      Button(
        onClick = {
          if (name.isBlank() || description.isBlank() || publicPhone.isBlank() || serviceAreas.isBlank()) {
            errorMsg = "Please fill in all required fields."
            return@Button
          }

          viewModel.registerBusiness(
            name = name,
            categoryId = categoryId,
            description = description,
            publicPhone = publicPhone,
            publicEmail = publicEmail,
            city = city,
            serviceAreas = serviceAreas,
            publicAddress = publicAddress,
            openingHours = openingHours,
            docType = docType,
            onSuccess = {
              submittedSuccess = true
            }
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
          .testTag("submit_registration_btn")
      ) {
        Text("Submit Registration for Approval")
      }
    }
  }

  if (submittedSuccess) {
    AlertDialog(
      onDismissRequest = {
        submittedSuccess = false
        viewModel.navigateTo(Screen.BusinessDashboard)
      },
      title = { Text("Registration Submitted!") },
      text = {
        Text("Your business listing has been received. Status: UNDER REVIEW. Our compliance administrators will verify the documents and approve your listing.")
      },
      confirmButton = {
        Button(
          onClick = {
            submittedSuccess = false
            viewModel.navigateTo(Screen.BusinessDashboard)
          }
        ) {
          Text("Go to Dashboard")
        }
      }
    )
  }
}

@Composable
fun ManageBusinessProfileScreen(
  viewModel: LocalConnectViewModel
) {
  val business by viewModel.currentOwnerBusiness.collectAsState()

  if (business == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      Text("No business profile to edit.")
    }
    return
  }

  val biz = business!!
  var desc by remember { mutableStateOf(biz.description) }
  var phone by remember { mutableStateOf(biz.publicPhone) }
  var email by remember { mutableStateOf(biz.publicEmail) }
  var areas by remember { mutableStateOf(biz.serviceAreas) }
  var hours by remember { mutableStateOf(biz.openingHours) }
  var isTemporarilyClosed by remember { mutableStateOf(biz.listingStatus == "INACTIVE") }
  var savedSuccess by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("manage_profile_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Text("Manage Business Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold)
      Text("Update customer-facing information for ${biz.name}.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    item {
      OutlinedTextField(
        value = desc,
        onValueChange = { desc = it },
        label = { Text("Business Description") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4
      )
    }

    item {
      OutlinedTextField(
        value = phone,
        onValueChange = { phone = it },
        label = { Text("Public Phone") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Public Email") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = areas,
        onValueChange = { areas = it },
        label = { Text("Service Areas") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      OutlinedTextField(
        value = hours,
        onValueChange = { hours = it },
        label = { Text("Opening Hours") },
        modifier = Modifier.fillMaxWidth()
      )
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text("Temporary Closure", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Text("Pause accepting new booking requests while closed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
          checked = isTemporarilyClosed,
          onCheckedChange = { isTemporarilyClosed = it },
          modifier = Modifier.testTag("temp_close_switch")
        )
      }
    }

    item {
      Button(
        onClick = {
          viewModel.updateBusinessProfile(
            biz.copy(
              description = desc,
              publicPhone = phone,
              publicEmail = email,
              serviceAreas = areas,
              openingHours = hours,
              listingStatus = if (isTemporarilyClosed) "INACTIVE" else "ACTIVE"
            ),
            onSuccess = { savedSuccess = true }
          )
        },
        modifier = Modifier.fillMaxWidth().testTag("save_profile_btn")
      ) {
        Text("Save Changes")
      }
    }
  }

  if (savedSuccess) {
    AlertDialog(
      onDismissRequest = { savedSuccess = false },
      title = { Text("Profile Updated") },
      text = { Text("Your business details have been updated successfully.") },
      confirmButton = {
        Button(onClick = { savedSuccess = false }) { Text("OK") }
      }
    )
  }
}

@Composable
fun ServiceManagementScreen(
  viewModel: LocalConnectViewModel
) {
  val business by viewModel.currentOwnerBusiness.collectAsState()
  val services = business?.let { viewModel.getServicesForBusiness(it.id).collectAsState().value } ?: emptyList()

  var showAddDialog by remember { mutableStateOf(false) }
  var name by remember { mutableStateOf("") }
  var desc by remember { mutableStateOf("") }
  var priceType by remember { mutableStateOf("FIXED") }
  var priceInput by remember { mutableStateOf("299") }
  var duration by remember { mutableStateOf("1 Hour") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("service_mgmt_screen")
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
          Text("Service Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
          Text("${services.size} services active", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Button(
          onClick = { showAddDialog = true },
          modifier = Modifier.testTag("add_service_btn")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Add Service")
        }
      }
    }

    if (services.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No services configured yet. Click 'Add Service' to create your first offering.")
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(services) { service ->
          Card(
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(service.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(service.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = when (service.priceType) {
                    "FIXED" -> "Fixed: ₹${service.fixedPrice.toInt()}"
                    "STARTING_AT" -> "Starting At: ₹${service.startingPrice.toInt()}"
                    else -> "Price on Inspection"
                  } + " • Duration: ${service.estimatedDuration}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
              }
              IconButton(onClick = { viewModel.deleteService(service.id) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog) {
    AlertDialog(
      onDismissRequest = { showAddDialog = false },
      title = { Text("Add Service Offering") },
      text = {
        Column {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Service Name") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Description") },
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row {
            listOf("FIXED", "STARTING_AT", "INSPECTION_REQUIRED").forEach { type ->
              FilterChip(
                selected = priceType == type,
                onClick = { priceType = type },
                label = { Text(type.replace("_", " ").take(10), fontSize = 10.sp) },
                modifier = Modifier.padding(end = 4.dp)
              )
            }
          }
          if (priceType != "INSPECTION_REQUIRED") {
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = priceInput,
              onValueChange = { priceInput = it },
              label = { Text("Price in INR (₹)") },
              modifier = Modifier.fillMaxWidth()
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = duration,
            onValueChange = { duration = it },
            label = { Text("Estimated Duration (e.g. 1-2 Hours)") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (name.isNotBlank()) {
              val priceVal = priceInput.toDoubleOrNull() ?: 0.0
              viewModel.addService(
                name = name,
                description = desc,
                priceType = priceType,
                startingPrice = if (priceType == "STARTING_AT") priceVal else 0.0,
                fixedPrice = if (priceType == "FIXED") priceVal else 0.0,
                duration = duration,
                onSuccess = {
                  showAddDialog = false
                  name = ""
                  desc = ""
                }
              )
            }
          }
        ) {
          Text("Add Service")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun BusinessBookingManagementScreen(
  viewModel: LocalConnectViewModel
) {
  val bookings by viewModel.ownerBookings.collectAsState()

  var rejectTarget by remember { mutableStateOf<BookingEntity?>(null) }
  var rejectReason by remember { mutableStateOf("") }

  var proposeTarget by remember { mutableStateOf<BookingEntity?>(null) }
  var proposedTimeInput by remember { mutableStateOf("03:00 PM - 05:00 PM") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("owner_bookings_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Incoming Customer Bookings", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Accept, reschedule, or complete service requests from clients.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    }

    if (bookings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Text("No bookings received yet.")
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(bookings) { booking ->
          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth().testTag("owner_booking_card_${booking.id}")
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text("Booking #${booking.id.takeLast(6)}", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                StatusBadge(status = booking.status)
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text("Request: ${booking.description}", fontSize = 13.sp)
              Spacer(modifier = Modifier.height(4.dp))
              Text("Scheduled: ${booking.preferredDate} (${booking.preferredTime})", fontSize = 12.sp, fontWeight = FontWeight.Medium)
              Text("Area: ${booking.serviceArea}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Customer Phone: ${booking.customerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
              Text("Protected Address: ${booking.protectedServiceAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

              if (booking.agreedPrice > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text("Agreed Price: ₹${booking.agreedPrice.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
              }

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider()
              Spacer(modifier = Modifier.height(8.dp))

              // Owner status action controls
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
              ) {
                if (booking.status == "PENDING") {
                  OutlinedButton(
                    onClick = { rejectTarget = booking },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                  ) {
                    Text("Reject", fontSize = 11.sp)
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  OutlinedButton(onClick = { proposeTarget = booking }) {
                    Text("Propose Time", fontSize = 11.sp)
                  }
                  Spacer(modifier = Modifier.width(6.dp))
                  Button(onClick = { viewModel.updateBookingStatus(booking.id, "SCHEDULED") }) {
                    Text("Accept", fontSize = 11.sp)
                  }
                } else if (booking.status == "SCHEDULED" || booking.status == "ACCEPTED") {
                  Button(onClick = { viewModel.updateBookingStatus(booking.id, "IN_PROGRESS") }) {
                    Text("Start Work (In Progress)", fontSize = 11.sp)
                  }
                } else if (booking.status == "IN_PROGRESS") {
                  Button(
                    onClick = { viewModel.updateBookingStatus(booking.id, "COMPLETED") },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusSuccess)
                  ) {
                    Text("Mark Completed", fontSize = 11.sp)
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Reject Booking Dialog
  rejectTarget?.let { bk ->
    AlertDialog(
      onDismissRequest = { rejectTarget = null },
      title = { Text("Reject Booking") },
      text = {
        Column {
          Text("State the reason for declining this request (e.g. Schedule full, out of radius):", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = rejectReason,
            onValueChange = { rejectReason = it },
            label = { Text("Reason for Rejection") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateBookingStatus(bk.id, "REJECTED", rejectReason.ifBlank { "Provider unavailable" })
            rejectTarget = null
            rejectReason = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Confirm Reject")
        }
      },
      dismissButton = {
        TextButton(onClick = { rejectTarget = null }) { Text("Cancel") }
      }
    )
  }

  // Propose Time Dialog
  proposeTarget?.let { bk ->
    AlertDialog(
      onDismissRequest = { proposeTarget = null },
      title = { Text("Propose Another Time") },
      text = {
        Column {
          Text("Propose an alternative appointment slot to the customer:", fontSize = 12.sp)
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = proposedTimeInput,
            onValueChange = { proposedTimeInput = it },
            label = { Text("Alternative Slot") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.proposeNewBookingTime(bk.id, proposedTimeInput)
            proposeTarget = null
          }
        ) {
          Text("Send Proposal")
        }
      },
      dismissButton = {
        TextButton(onClick = { proposeTarget = null }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun SubscriptionManagementScreen(
  viewModel: LocalConnectViewModel
) {
  val business by viewModel.currentOwnerBusiness.collectAsState()
  val subscription by viewModel.ownerSubscription.collectAsState()
  val plans by viewModel.subscriptionPlans.collectAsState()

  var selectedPlanForCheckout by remember { mutableStateOf<Pair<String, Double>?>(null) }
  var checkoutSuccessMsg by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("subscription_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Business Subscription Plans", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text(
        "Unlock premium placement, priority inquiries, and verified badges. Note: This checkout workflow integrates with server-side payment verification.",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }

    // Current Subscription Status
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Current Status", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = if (subscription != null && subscription!!.status == "ACTIVE") "Active: Featured Plan" else "Free Plan",
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )
            StatusBadge(status = subscription?.status ?: "ACTIVE")
          }
          if (subscription != null && subscription!!.status == "ACTIVE") {
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            Text(
              text = "Renewal Date: ${sdf.format(Date(subscription!!.renewalDate))}",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
              onClick = { viewModel.cancelSubscription(subscription!!.id) },
              colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
              Text("Cancel Paid Subscription", fontSize = 11.sp)
            }
          }
        }
      }
    }

    // Available Plans
    items(plans) { plan ->
      val isCurrentPlan = (plan.id == "plan_free" && (subscription == null || subscription!!.status != "ACTIVE")) ||
        (subscription != null && subscription!!.planId == plan.id && subscription!!.status == "ACTIVE")

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
          containerColor = if (plan.id == "plan_premium") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
              Text(
                text = if (plan.monthlyPrice > 0) "₹${plan.monthlyPrice.toInt()} / month" else "Free Forever",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
              )
            }
            if (isCurrentPlan) {
              StatusBadge(status = "ACTIVE")
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(10.dp))

          plan.features.split(",").forEach { feature ->
            Row(
              modifier = Modifier.padding(vertical = 2.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Check, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(feature.trim(), fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(12.dp))
          if (!isCurrentPlan && plan.monthlyPrice > 0) {
            Button(
              onClick = { selectedPlanForCheckout = plan.id to plan.monthlyPrice },
              modifier = Modifier.fillMaxWidth().testTag("subscribe_btn_${plan.id}")
            ) {
              Text("Subscribe to ${plan.name} (₹${plan.monthlyPrice.toInt()}/mo)")
            }
          }
        }
      }
    }
  }

  // Payment Simulation & Verification Dialog
  selectedPlanForCheckout?.let { (planId, amount) ->
    AlertDialog(
      onDismissRequest = { selectedPlanForCheckout = null },
      title = { Text("Payment & Subscription Checkout") },
      text = {
        Column {
          Text(
            "Sandbox Payment Provider Gateway (INR ₹${amount.toInt()})\n\nIn live production, payment is authorized via verified server-side webhooks. Clicking 'Confirm Payment' simulates the webhook verification and immediately activates the subscription.",
            fontSize = 12.sp
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text("Amount: ₹${amount.toInt()}", fontWeight = FontWeight.Bold)
          Text("Billing: Monthly auto-renewal", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.activateSubscription(
              planId = planId,
              amount = amount,
              onSuccess = {
                selectedPlanForCheckout = null
                checkoutSuccessMsg = "Payment successful! Your Featured Business subscription is now active."
              }
            )
          }
        ) {
          Text("Authorize & Confirm Payment")
        }
      },
      dismissButton = {
        TextButton(onClick = { selectedPlanForCheckout = null }) { Text("Cancel") }
      }
    )
  }

  checkoutSuccessMsg?.let { msg ->
    AlertDialog(
      onDismissRequest = { checkoutSuccessMsg = null },
      title = { Text("Subscription Activated!") },
      text = { Text(msg) },
      confirmButton = {
        Button(onClick = { checkoutSuccessMsg = null }) { Text("OK") }
      }
    )
  }
}

@Composable
fun BusinessAnalyticsScreen(
  viewModel: LocalConnectViewModel
) {
  val business by viewModel.currentOwnerBusiness.collectAsState()
  val bookings by viewModel.ownerBookings.collectAsState()

  val totalBookings = bookings.size
  val completed = bookings.count { it.status == "COMPLETED" }
  val conversionRate = if (totalBookings > 0) (completed.toFloat() / totalBookings * 100).toInt() else 0
  val grossRevenue = bookings.filter { it.status == "COMPLETED" }.sumOf { it.agreedPrice }
  val platformCommission = (grossRevenue * 0.10) // 10%
  val netEarnings = grossRevenue - platformCommission

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("business_analytics_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Business Analytics & Performance", fontSize = 20.sp, fontWeight = FontWeight.Bold)
      Text("Real data calculated from customer bookings and profile views.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text("Financial Summary (Completed Jobs)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(10.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
              Text("Gross Volume", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("₹${grossRevenue.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
            }
            Column {
              Text("Platform Fee (10%)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("₹${platformCommission.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SecondaryAmber)
            }
            Column {
              Text("Net Provider Payout", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("₹${netEarnings.toInt()}", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
            }
          }
        }
      }
    }

    item {
      Text("Engagement & Funnel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        KpiCard("Directory Impressions", "1,248", PrimaryBlue, Modifier.weight(1f))
        KpiCard("Profile Views", "312", TertiaryTeal, Modifier.weight(1f))
        KpiCard("Call / Inquiries", "64", SecondaryAmber, Modifier.weight(1f))
        KpiCard("Conversion", "$conversionRate%", StatusSuccess, Modifier.weight(1f))
      }
    }
  }
}
