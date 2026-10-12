package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BookingEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CustomerBookingsScreen(
  viewModel: LocalConnectViewModel
) {
  val context = LocalContext.current
  val bookings by viewModel.customerBookings.collectAsState()
  val allBusinesses by viewModel.allBusinessesAdmin.collectAsState()

  var selectedTab by remember { mutableStateOf(0) }
  val tabTitles = listOf("All", "Pending", "Scheduled", "Completed", "Cancelled")

  val filteredBookings = remember(bookings, selectedTab) {
    when (selectedTab) {
      1 -> bookings.filter { it.status == "PENDING" }
      2 -> bookings.filter { it.status == "SCHEDULED" || it.status == "ACCEPTED" || it.status == "IN_PROGRESS" }
      3 -> bookings.filter { it.status == "COMPLETED" }
      4 -> bookings.filter { it.status == "CANCELLED" || it.status == "REJECTED" || it.status == "DISPUTED" }
      else -> bookings
    }
  }

  // Dialog States
  var cancelBookingTarget by remember { mutableStateOf<BookingEntity?>(null) }
  var cancelReason by remember { mutableStateOf("") }

  var reviewBookingTarget by remember { mutableStateOf<BookingEntity?>(null) }
  var reviewRating by remember { mutableStateOf(5) }
  var reviewText by remember { mutableStateOf("") }

  var disputeBookingTarget by remember { mutableStateOf<BookingEntity?>(null) }
  var disputeReason by remember { mutableStateOf("") }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("customer_bookings_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      Column(modifier = Modifier.padding(top = 8.dp)) {
        Text(
          text = "My Service Bookings",
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
        )

        ScrollableTabRow(
          selectedTabIndex = selectedTab,
          edgePadding = 16.dp,
          modifier = Modifier.fillMaxWidth()
        ) {
          tabTitles.forEachIndexed { index, title ->
            Tab(
              selected = selectedTab == index,
              onClick = { selectedTab = index },
              text = { Text(title, fontSize = 13.sp) },
              modifier = Modifier.testTag("tab_$title")
            )
          }
        }
      }
    }

    if (filteredBookings.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            Icons.Default.EventBusy,
            contentDescription = null,
            modifier = Modifier.size(54.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text("No Bookings Found", fontWeight = FontWeight.Bold, fontSize = 16.sp)
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            "Book verified home services from top local providers anytime.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        items(filteredBookings) { booking ->
          val business = allBusinesses.firstOrNull { it.id == booking.businessId }

          Card(
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("booking_card_${booking.id}"),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Booking #${booking.id.takeLast(6)}",
                  fontSize = 12.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                StatusBadge(status = booking.status)
              }

              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = business?.name ?: "Service Provider",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = booking.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              Spacer(modifier = Modifier.height(10.dp))
              HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
              Spacer(modifier = Modifier.height(10.dp))

              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                  Text("Scheduled For:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text("${booking.preferredDate} • ${booking.preferredTime}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("Agreed Price:", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  Text(
                    if (booking.agreedPrice > 0) "₹${booking.agreedPrice.toInt()}" else "Inspection",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }

              if (booking.proposedTime != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                  shape = RoundedCornerShape(6.dp),
                  color = Color(0xFFFEF3C7),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Text(
                    text = "Provider proposed new time: ${booking.proposedTime}",
                    fontSize = 11.sp,
                    color = Color(0xFF78350F),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(8.dp)
                  )
                }
              }

              if (booking.cancellationReason != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Cancellation reason: ${booking.cancellationReason}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.error
                )
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Responsive Action Buttons
              Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                // Tier 1: Primary actions (Call, Payment status / Pay Now, Write Review)
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    // Contact Provider Button
                    business?.let { biz ->
                      FilledTonalIconButton(
                        onClick = {
                          val dial = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${biz.publicPhone}"))
                          try { context.startActivity(dial) } catch (_: Exception) {}
                        },
                        modifier = Modifier.size(38.dp)
                      ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call Provider", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                      }
                      Spacer(modifier = Modifier.width(6.dp))
                    }

                    if (booking.paymentStatus == "PAID_ONLINE") {
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFD1FAE5)
                      ) {
                        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                          Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(13.dp))
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Paid 24/7", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }
                      }
                    }
                  }

                  // Right Action: Pay Now (24/7) or Write Review
                  if (booking.status == "COMPLETED") {
                    Button(
                      onClick = { reviewBookingTarget = booking },
                      modifier = Modifier.testTag("review_booking_${booking.id}"),
                      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                      Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Review", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  } else if (booking.paymentStatus != "PAID_ONLINE" && booking.status != "CANCELLED" && booking.status != "REJECTED") {
                    Button(
                      onClick = { viewModel.navigateTo(Screen.PaymentCheckout(booking.id, booking.agreedPrice)) },
                      modifier = Modifier.testTag("pay_booking_${booking.id}"),
                      contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                      Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(6.dp))
                      Text("Pay Now 24/7", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                  }
                }

                // Tier 2: Secondary actions (Cancel / Report Problem)
                val canCancel = booking.status == "PENDING" || booking.status == "SCHEDULED" || booking.status == "ACCEPTED"
                val canReport = booking.status != "CANCELLED" && booking.status != "REJECTED" && booking.status != "DISPUTED"

                if (canCancel || canReport) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    if (canCancel) {
                      OutlinedButton(
                        onClick = { cancelBookingTarget = booking },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("cancel_booking_${booking.id}"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                      ) {
                        Text("Cancel Booking", fontSize = 11.sp)
                      }
                    }

                    if (canReport) {
                      Spacer(modifier = Modifier.width(8.dp))
                      TextButton(
                        onClick = { disputeBookingTarget = booking },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                      ) {
                        Text("Report Issue", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Cancel Booking Dialog
  cancelBookingTarget?.let { booking ->
    AlertDialog(
      onDismissRequest = { cancelBookingTarget = null },
      title = { Text("Cancel Booking") },
      text = {
        Column {
          Text(
            "Cancellation Policy: Free cancellation prior to provider departure. Please state why you are cancelling.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = cancelReason,
            onValueChange = { cancelReason = it },
            label = { Text("Reason for cancellation") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.updateBookingStatus(
              bookingId = booking.id,
              status = "CANCELLED",
              reason = cancelReason.ifBlank { "Customer requested cancellation" }
            )
            cancelBookingTarget = null
            cancelReason = ""
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Confirm Cancel")
        }
      },
      dismissButton = {
        TextButton(onClick = { cancelBookingTarget = null }) { Text("Keep Booking") }
      }
    )
  }

  // Review Booking Dialog (ONLY for completed bookings!)
  reviewBookingTarget?.let { booking ->
    AlertDialog(
      onDismissRequest = { reviewBookingTarget = null },
      title = { Text("Review Completed Service") },
      text = {
        Column {
          Text(
            "Rate your experience with this verified local provider.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
          ) {
            (1..5).forEach { star ->
              IconButton(onClick = { reviewRating = star }) {
                Icon(
                  imageVector = if (star <= reviewRating) Icons.Default.Star else Icons.Outlined.StarBorder,
                  contentDescription = "$star stars",
                  tint = SecondaryAmber,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = reviewText,
            onValueChange = { reviewText = it },
            label = { Text("Your Review & Feedback") },
            placeholder = { Text("Punctual, clean work, fair pricing...") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (reviewText.isNotBlank()) {
              viewModel.submitReview(
                bookingId = booking.id,
                businessId = booking.businessId,
                rating = reviewRating,
                text = reviewText,
                onSuccess = {
                  reviewBookingTarget = null
                  reviewText = ""
                }
              )
            }
          }
        ) {
          Text("Submit Review")
        }
      },
      dismissButton = {
        TextButton(onClick = { reviewBookingTarget = null }) { Text("Cancel") }
      }
    )
  }

  // Dispute / Report Problem Dialog
  disputeBookingTarget?.let { booking ->
    AlertDialog(
      onDismissRequest = { disputeBookingTarget = null },
      title = { Text("Report Problem / Dispute") },
      text = {
        Column {
          Text(
            "An administrator will review this dispute and investigate the records.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = disputeReason,
            onValueChange = { disputeReason = it },
            label = { Text("Explain Problem (e.g. No show, pricing mismatch)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (disputeReason.isNotBlank()) {
              viewModel.updateBookingStatus(
                bookingId = booking.id,
                status = "DISPUTED",
                reason = disputeReason
              )
              disputeBookingTarget = null
              disputeReason = ""
            }
          }
        ) {
          Text("Submit Dispute")
        }
      },
      dismissButton = {
        TextButton(onClick = { disputeBookingTarget = null }) { Text("Cancel") }
      }
    )
  }
}
