package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntity
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookServiceScreen(
  businessId: String,
  selectedServiceId: String?,
  viewModel: LocalConnectViewModel
) {
  val allBusinesses by viewModel.allBusinessesAdmin.collectAsState()
  val business = remember(allBusinesses, businessId) {
    allBusinesses.firstOrNull { it.id == businessId }
  }

  val services by viewModel.getServicesForBusiness(businessId).collectAsState()
  val currentProfile by viewModel.currentProfile.collectAsState()

  var chosenServiceId by remember { mutableStateOf(selectedServiceId ?: "") }
  var problemDescription by remember { mutableStateOf("") }
  var preferredDate by remember {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.add(Calendar.DAY_OF_YEAR, 1)
    mutableStateOf(sdf.format(cal.time))
  }
  var preferredTimeSlot by remember { mutableStateOf("10:00 AM - 12:00 PM") }
  var serviceArea by remember { mutableStateOf(business?.serviceAreas?.split(",")?.firstOrNull()?.trim() ?: "Main Area") }
  var protectedAddress by remember { mutableStateOf("Flat 302, Palm Residency, Mumbai") }
  var customerPhone by remember { mutableStateOf(currentProfile?.phone ?: "+91 98201 12345") }
  var privacyConsent by remember { mutableStateOf(false) }
  var photoAttached by remember { mutableStateOf(false) }

  var validationError by remember { mutableStateOf<String?>(null) }
  var bookingSuccess by remember { mutableStateOf(false) }

  // Set default service if available
  LaunchedEffect(services) {
    if (chosenServiceId.isEmpty() && services.isNotEmpty()) {
      chosenServiceId = services.first().id
    }
  }

  val chosenService = services.firstOrNull { it.id == chosenServiceId }
  val agreedPrice = when (chosenService?.priceType) {
    "FIXED" -> chosenService.fixedPrice
    "STARTING_AT" -> chosenService.startingPrice
    else -> 0.0
  }

  val timeSlots = listOf(
    "09:00 AM - 11:00 AM",
    "11:00 AM - 01:00 PM",
    "02:00 PM - 04:00 PM",
    "04:00 PM - 06:00 PM",
    "06:00 PM - 08:00 PM"
  )

  Scaffold(
    bottomBar = {
      Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Estimated Price",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = if (agreedPrice > 0) "₹${agreedPrice.toInt()}" else "Inspection Quote",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }

            Button(
              onClick = {
                // Validation checks
                if (problemDescription.isBlank()) {
                  validationError = "Please describe the problem or service requirements."
                  return@Button
                }
                if (protectedAddress.isBlank()) {
                  validationError = "Please enter your service address."
                  return@Button
                }
                if (customerPhone.isBlank()) {
                  validationError = "Please provide your contact phone number."
                  return@Button
                }
                if (!privacyConsent) {
                  validationError = "Please consent to share address with the service provider."
                  return@Button
                }

                // Check date validity (cannot be in the past)
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                try {
                  val chosenCal = Calendar.getInstance().apply { time = sdf.parse(preferredDate)!! }
                  val todayCal = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                  }
                  if (chosenCal.before(todayCal)) {
                    validationError = "Booking date cannot be in the past."
                    return@Button
                  }
                } catch (_: Exception) {
                  validationError = "Please enter date formatted as YYYY-MM-DD."
                  return@Button
                }

                viewModel.createBooking(
                  businessId = businessId,
                  serviceId = chosenServiceId.ifEmpty { "general_service" },
                  description = problemDescription,
                  date = preferredDate,
                  time = preferredTimeSlot,
                  serviceArea = serviceArea,
                  protectedAddress = protectedAddress,
                  phone = customerPhone,
                  agreedPrice = agreedPrice,
                  onSuccess = {
                    bookingSuccess = true
                  }
                )
              },
              modifier = Modifier.testTag("submit_booking_button")
            ) {
              Text("Submit Booking Request")
            }
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .testTag("book_service_form"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Text(
          text = "Request a Service Booking",
          fontSize = 20.sp,
          fontWeight = FontWeight.Bold
        )
        Text(
          text = "With ${business?.name ?: "Business"}",
          fontSize = 14.sp,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.SemiBold
        )
      }

      // Validation error alert
      validationError?.let { err ->
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = err,
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // 1. Select Service Offering
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "1. Select Service",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            services.forEach { srv ->
              val isSelected = srv.id == chosenServiceId
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { chosenServiceId = srv.id }
                  .padding(vertical = 3.dp)
                  .testTag("select_service_${srv.id}")
              ) {
                Row(
                  modifier = Modifier.padding(10.dp),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  RadioButton(
                    selected = isSelected,
                    onClick = { chosenServiceId = srv.id }
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Column(modifier = Modifier.weight(1f)) {
                    Text(text = srv.name, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                      text = when (srv.priceType) {
                        "FIXED" -> "Fixed Price: ₹${srv.fixedPrice.toInt()}"
                        "STARTING_AT" -> "Starting At: ₹${srv.startingPrice.toInt()}"
                        else -> "Requires Inspection"
                      },
                      fontSize = 11.sp,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }
                }
              }
            }
          }
        }
      }

      // 2. Problem Description
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "2. Describe Problem / Scope",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
              value = problemDescription,
              onValueChange = { problemDescription = it },
              placeholder = { Text("e.g. Bathroom sink pipe leaking heavily, needs replacement washer.") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_desc_input"),
              maxLines = 4,
              shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Photo attachment simulator
            OutlinedButton(
              onClick = { photoAttached = !photoAttached },
              modifier = Modifier.fillMaxWidth().testTag("attach_photo_button")
            ) {
              Icon(
                imageVector = if (photoAttached) Icons.Default.CheckCircle else Icons.Default.AddPhotoAlternate,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                if (photoAttached) "Photo Attached (pipe_defect_01.jpg)" else "Attach Problem Photo (Optional)"
              )
            }
          }
        }
      }

      // 3. Preferred Date & Time
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "3. Preferred Date & Time Slot",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = preferredDate,
              onValueChange = { preferredDate = it },
              label = { Text("Date (YYYY-MM-DD)") },
              leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_date_input"),
              singleLine = true,
              shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text("Select Preferred Time Slot:", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              timeSlots.forEach { slot ->
                val isSelected = slot == preferredTimeSlot
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { preferredTimeSlot = slot }
                    .padding(vertical = 2.dp)
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    RadioButton(
                      selected = isSelected,
                      onClick = { preferredTimeSlot = slot }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = slot, fontSize = 13.sp)
                  }
                }
              }
            }
          }
        }
      }

      // 4. Protected Service Address & Contact
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "4. Protected Location & Contact",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
            Text(
              text = "Privacy Protected: Full street address is never published publicly and disclosed only to the confirmed provider upon booking acceptance.",
              fontSize = 11.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
              value = serviceArea,
              onValueChange = { serviceArea = it },
              label = { Text("Neighbourhood / Locality") },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = protectedAddress,
              onValueChange = { protectedAddress = it },
              label = { Text("Complete Service Address & Landmark") },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_address_input"),
              maxLines = 2,
              shape = RoundedCornerShape(8.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
              value = customerPhone,
              onValueChange = { customerPhone = it },
              label = { Text("Customer Phone Number") },
              leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
              modifier = Modifier
                .fillMaxWidth()
                .testTag("booking_phone_input"),
              singleLine = true,
              shape = RoundedCornerShape(8.dp)
            )
          }
        }
      }

      // 5. Consent & Marketplace Disclosure
      item {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clickable { privacyConsent = !privacyConsent }
            .padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Checkbox(
            checked = privacyConsent,
            onCheckedChange = { privacyConsent = it },
            modifier = Modifier.testTag("privacy_consent_checkbox")
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "I consent to the LocalConnect Privacy Policy and authorize sharing my contact details with the assigned service provider.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface
          )
        }
      }
    }
  }

  if (bookingSuccess) {
    AlertDialog(
      onDismissRequest = {
        bookingSuccess = false
        viewModel.navigateTo(Screen.CustomerBookings)
      },
      title = { Text("Booking Request Submitted!", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "Your service request has been sent to ${business?.name}. Status is PENDING. You will receive an in-app notification when the provider accepts or confirms the appointment."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            bookingSuccess = false
            viewModel.navigateTo(Screen.CustomerBookings)
          },
          modifier = Modifier.testTag("booking_success_ok_button")
        ) {
          Text("View My Bookings")
        }
      }
    )
  }
}
