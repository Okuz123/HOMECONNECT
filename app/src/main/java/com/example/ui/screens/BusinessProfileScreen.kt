package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ServiceEntity
import com.example.ui.components.RatingPill
import com.example.ui.components.SponsoredBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryAmber
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
  businessId: String,
  viewModel: LocalConnectViewModel
) {
  val context = LocalContext.current
  val allBusinesses by viewModel.allBusinessesAdmin.collectAsState()
  val business = remember(allBusinesses, businessId) {
    allBusinesses.firstOrNull { it.id == businessId }
  }

  val services by viewModel.getServicesForBusiness(businessId).collectAsState()
  val reviews by viewModel.getReviewsForBusiness(businessId).collectAsState()
  val favoriteIds by viewModel.favoriteBusinessIds.collectAsState()
  val isFavorite = favoriteIds.contains(businessId)

  var showReportDialog by remember { mutableStateOf(false) }
  var reportReason by remember { mutableStateOf("") }
  var reportDesc by remember { mutableStateOf("") }
  var reportSubmittedMessage by remember { mutableStateOf<String?>(null) }

  if (business == null) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
      CircularProgressIndicator()
    }
    return
  }

  Scaffold(
    bottomBar = {
      Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          OutlinedButton(
            onClick = {
              val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${business.publicPhone}"))
              try {
                context.startActivity(dialIntent)
              } catch (_: Exception) {}
            },
            modifier = Modifier.weight(1f).testTag("call_business_button")
          ) {
            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Call")
          }
          Spacer(modifier = Modifier.width(12.dp))
          Button(
            onClick = { viewModel.navigateTo(Screen.BookService(businessId)) },
            modifier = Modifier.weight(2f).testTag("request_booking_button")
          ) {
            Icon(Icons.Default.Event, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Request Booking", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .testTag("business_profile_view"),
      contentPadding = PaddingValues(bottom = 16.dp)
    ) {
      // Header Banner
      item {
        Card(
          shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(20.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Top
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  if (business.isFeatured) SponsoredBadge()
                  VerificationBadge(isVerified = business.verificationStatus == "APPROVED")
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                  text = business.name,
                  fontSize = 20.sp,
                  fontWeight = FontWeight.ExtraBold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
              IconButton(onClick = { viewModel.toggleFavorite(businessId) }) {
                Icon(
                  imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                  contentDescription = "Favorite",
                  tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              RatingPill(rating = business.rating, reviewCount = business.reviewCount)
              Spacer(modifier = Modifier.width(12.dp))
              Text(
                text = "${business.city} • Verified Provider",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }
        }
      }

      // About & Highlights
      item {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "About the Business",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = business.description,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            color = MaterialTheme.colorScheme.onSurface
          )

          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))

          // Business Details Grid
          DetailRow(Icons.Default.Place, "Service Areas", business.serviceAreas)
          DetailRow(Icons.Default.AccessTime, "Opening Hours", business.openingHours)
          DetailRow(Icons.Default.Store, "Public Address", business.publicAddress)
          DetailRow(Icons.Default.Phone, "Public Phone", business.publicPhone)
          DetailRow(Icons.Default.Email, "Public Email", business.publicEmail)

          Spacer(modifier = Modifier.height(8.dp))
          TextButton(
            onClick = { showReportDialog = true },
            modifier = Modifier.testTag("report_listing_button")
          ) {
            Icon(Icons.Outlined.Flag, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Report this Listing", color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
          }
        }
      }

      // Services & Pricing List
      item {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Available Services",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            Text(
              text = "${services.size} offered",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          Spacer(modifier = Modifier.height(10.dp))
        }
      }

      if (services.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "No specific services listed yet. Contact business for direct estimates.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 12.sp
            )
          }
        }
      } else {
        items(services) { service ->
          ServiceCardItem(
            service = service,
            onBookClick = { viewModel.navigateTo(Screen.BookService(businessId, service.id)) }
          )
        }
      }

      // Customer Reviews Section
      item {
        Column(modifier = Modifier.padding(16.dp)) {
          Spacer(modifier = Modifier.height(12.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Verified Customer Reviews",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp
            )
            RatingPill(rating = business.rating, reviewCount = business.reviewCount)
          }

          Text(
            text = "Reviews can only be submitted by customers with completed bookings.",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
          )

          if (reviews.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "No customer reviews posted yet.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp
              )
            }
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              reviews.forEach { review ->
                Card(
                  shape = RoundedCornerShape(10.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(
                        text = review.customerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      Row {
                        repeat(review.rating) {
                          Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = SecondaryAmber,
                            modifier = Modifier.size(14.dp)
                          )
                        }
                      }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = review.reviewText,
                      fontSize = 12.sp,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Report Listing Dialog
  if (showReportDialog) {
    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = { Text("Report Listing") },
      text = {
        Column {
          Text(
            "Help us maintain community integrity. Describe the concern regarding ${business.name}.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = reportReason,
            onValueChange = { reportReason = it },
            label = { Text("Reason (e.g. Incorrect info, fraud)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          Spacer(modifier = Modifier.height(8.dp))
          OutlinedTextField(
            value = reportDesc,
            onValueChange = { reportDesc = it },
            label = { Text("Details") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (reportReason.isNotBlank()) {
              viewModel.submitReport(
                reportedBizId = businessId,
                reason = reportReason,
                description = reportDesc,
                onSuccess = {
                  showReportDialog = false
                  reportSubmittedMessage = "Report submitted for admin review."
                }
              )
            }
          }
        ) {
          Text("Submit Report")
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) { Text("Cancel") }
      }
    )
  }

  reportSubmittedMessage?.let { msg ->
    AlertDialog(
      onDismissRequest = { reportSubmittedMessage = null },
      title = { Text("Thank You") },
      text = { Text(msg) },
      confirmButton = {
        Button(onClick = { reportSubmittedMessage = null }) { Text("OK") }
      }
    )
  }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    verticalAlignment = Alignment.Top
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier
        .size(16.dp)
        .padding(top = 2.dp)
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = label,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Text(
        text = value.ifEmpty { "Not provided" },
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium
      )
    }
  }
}

@Composable
fun ServiceCardItem(
  service: ServiceEntity,
  onBookClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(10.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .padding(horizontal = 16.dp, vertical = 4.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Row(
      modifier = Modifier.padding(14.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = service.name,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = service.description,
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = when (service.priceType) {
              "FIXED" -> "₹${service.fixedPrice.toInt()}"
              "STARTING_AT" -> "Starts at ₹${service.startingPrice.toInt()}"
              else -> "Price on Inspection"
            },
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "• Est: ${service.estimatedDuration}",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }

      Button(
        onClick = onBookClick,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
        modifier = Modifier.height(34.dp)
      ) {
        Text("Book", fontSize = 12.sp)
      }
    }
  }
}
