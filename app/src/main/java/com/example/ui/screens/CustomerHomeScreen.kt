package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessEntity
import com.example.data.model.CategoryEntity
import com.example.ui.components.RatingPill
import com.example.ui.components.SponsoredBadge
import com.example.ui.components.StatusBadge
import com.example.ui.components.TwentyFourSevenBadge
import com.example.ui.components.VerificationBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryAmber
import com.example.ui.theme.TertiaryTeal
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun CustomerHomeScreen(
  viewModel: LocalConnectViewModel
) {
  val selectedCity by viewModel.selectedCity.collectAsState()
  val categories by viewModel.activeCategories.collectAsState()
  val approvedBusinesses by viewModel.approvedBusinesses.collectAsState()
  val currentProfile by viewModel.currentProfile.collectAsState()

  val customerBookings by viewModel.customerBookings.collectAsState()

  val cityBusinesses = remember(approvedBusinesses, selectedCity) {
    approvedBusinesses.filter { it.city.equals(selectedCity, ignoreCase = true) }
  }

  val featuredBusinesses = remember(cityBusinesses) {
    cityBusinesses.filter { it.isFeatured }
  }

  val popularBusinesses = remember(cityBusinesses) {
    cityBusinesses.sortedByDescending { it.rating }
  }

  val activeBooking = remember(customerBookings) {
    customerBookings.firstOrNull { it.status in listOf("PENDING", "ACCEPTED", "SCHEDULED", "IN_PROGRESS") }
  }
  val activeBookingBiz = remember(activeBooking, approvedBusinesses) {
    approvedBusinesses.firstOrNull { it.id == activeBooking?.businessId }
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("customer_home_list"),
    contentPadding = PaddingValues(bottom = 80.dp)
  ) {
    // 1. Search Bar Header Trigger
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 16.dp, vertical = 10.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.navigateTo(Screen.Search) }
            .testTag("home_search_trigger")
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Search,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "Search plumbing, electrical, cleaning in $selectedCity...",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 13.sp,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
          }
        }
      }
    }

    // 1b. Active Booking Live Status Tracker Card (if user has active service)
    activeBooking?.let { b ->
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("home_active_booking_card")
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "Active Booking #${b.id.takeLast(6)}",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Bold,
                  color = PrimaryBlue
                )
              }
              StatusBadge(status = b.status)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = activeBookingBiz?.name ?: "Assigned Service Provider",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
            Text(
              text = "${b.preferredDate} at ${b.preferredTime} • ${b.serviceArea}",
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.End,
              verticalAlignment = Alignment.CenterVertically
            ) {
              OutlinedButton(
                onClick = { viewModel.navigateTo(Screen.CustomerBookings) },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                modifier = Modifier.padding(end = 6.dp)
              ) {
                Text("View Details", fontSize = 11.sp)
              }

              if (b.paymentStatus != "PAID_ONLINE") {
                Button(
                  onClick = { viewModel.navigateTo(Screen.PaymentCheckout(b.id, b.agreedPrice)) },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.testTag("home_active_booking_pay_btn")
                ) {
                  Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(14.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Pay In-App 24/7", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }
    }

    // 2. 24/7 Emergency Dispatch Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        shape = RoundedCornerShape(14.dp)
      ) {
        Row(
          modifier = Modifier.padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFFEF4444).copy(alpha = 0.2f),
            modifier = Modifier.size(44.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.ElectricBolt,
                contentDescription = null,
                tint = Color(0xFFEF4444),
                modifier = Modifier.size(24.dp)
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "24/7 Emergency Dispatch",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(6.dp))
              TwentyFourSevenBadge()
            }
            Text(
              text = "Plumber, electrician, locksmith ready 24/7 in 30 mins in $selectedCity.",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
          FilledTonalButton(
            onClick = {
              viewModel.search24x7Only.value = true
              viewModel.navigateTo(Screen.Search)
            },
            colors = ButtonDefaults.filledTonalButtonColors(
              containerColor = Color(0xFFEF4444),
              contentColor = Color.White
            ),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("home_247_emergency_btn")
          ) {
            Text("Find 24/7", fontSize = 11.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // 3. Marketplace Hero Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Verified Local Experts",
              fontSize = 18.sp,
              fontWeight = FontWeight.ExtraBold,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Book background-checked home services & skilled trades in $selectedCity with upfront pricing.",
              fontSize = 12.sp,
              color = Color.White.copy(alpha = 0.9f)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
              onClick = { viewModel.navigateTo(Screen.Search) },
              colors = ButtonDefaults.buttonColors(containerColor = Color.White),
              contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
              modifier = Modifier.testTag("hero_explore_button")
            ) {
              Text(
                text = "Explore Services",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
              )
            }
          }
          Icon(
            imageVector = Icons.Default.VerifiedUser,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size(72.dp)
          )
        }
      }
    }

    // 3. Service Categories Grid
    item {
      Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Categories",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          TextButton(onClick = { viewModel.navigateTo(Screen.Search) }) {
            Text("View All", fontSize = 12.sp)
          }
        }

        LazyRow(
          contentPadding = PaddingValues(horizontal = 12.dp),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(categories) { category ->
            CategoryChipItem(
              category = category,
              onClick = {
                viewModel.searchCategory.value = category.id
                viewModel.navigateTo(Screen.Search)
              }
            )
          }
        }
      }
    }

    // 4. Featured Businesses (Sponsored Placements)
    if (featuredBusinesses.isNotEmpty()) {
      item {
        Column(modifier = Modifier.padding(top = 16.dp)) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              Icons.Default.Star,
              contentDescription = null,
              tint = SecondaryAmber,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Featured Businesses",
              fontWeight = FontWeight.Bold,
              fontSize = 17.sp
            )
          }
          Spacer(modifier = Modifier.height(10.dp))

          LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            items(featuredBusinesses) { biz ->
              FeaturedBusinessCard(
                business = biz,
                onClick = { viewModel.navigateTo(Screen.BusinessProfile(biz.id)) }
              )
            }
          }
        }
      }
    }

    // 5. Popular Verified Providers in Selected City
    item {
      Column(modifier = Modifier.padding(top = 20.dp)) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Top Rated in $selectedCity",
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
          )
          TextButton(onClick = { viewModel.navigateTo(Screen.Search) }) {
            Text("See More", fontSize = 12.sp)
          }
        }

        if (popularBusinesses.isEmpty()) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              "No verified listings currently in $selectedCity. Switch cities above to view active providers.",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              fontSize = 13.sp
            )
          }
        } else {
          Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            popularBusinesses.take(4).forEach { biz ->
              BusinessListItemCard(
                business = biz,
                onClick = { viewModel.navigateTo(Screen.BusinessProfile(biz.id)) },
                onBookClick = { viewModel.navigateTo(Screen.BookService(biz.id)) }
              )
            }
          }
        }
      }
    }

    // 6. "List Your Business" Callout Promo Banner
    item {
      Card(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(14.dp)
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              shape = CircleShape,
              color = SecondaryAmber.copy(alpha = 0.15f),
              modifier = Modifier.size(40.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Storefront,
                  contentDescription = null,
                  tint = SecondaryAmber
                )
              }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Are You a Local Business?",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
              )
              Text(
                text = "Grow your client base in $selectedCity with verified customer bookings.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Button(
            onClick = { viewModel.navigateTo(Screen.BusinessRegistration) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("list_business_button")
          ) {
            Icon(Icons.Default.AddBusiness, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Register & List Your Business Free")
          }
        }
      }
    }

    // 7. Trust, Disclaimers and Legal Footer
    item {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(16.dp)
      ) {
        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))
        Text(
          text = "My Connect Platform",
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp
        )
        Text(
          text = "My Connect is an open 24/7 marketplace connecting independent local service providers and businesses with customers. Instant in-app payments, transparent commission on completed bookings.",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "About Us",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.Legal("about")) }
          )
          Text(
            text = "Terms & Rules",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.Legal("terms")) }
          )
          Text(
            text = "Privacy Policy",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.Legal("privacy")) }
          )
          Text(
            text = "Refunds",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.Legal("refund")) }
          )
        }
      }
    }
  }
}

@Composable
fun CategoryChipItem(
  category: CategoryEntity,
  onClick: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surfaceVariant,
    modifier = Modifier
      .clickable(onClick = onClick)
      .width(100.dp)
      .testTag("category_chip_${category.id}")
  ) {
    Column(
      modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = getCategoryIcon(category.id),
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = category.name,
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}

@Composable
fun FeaturedBusinessCard(
  business: BusinessEntity,
  onClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    modifier = Modifier
      .width(260.dp)
      .clickable(onClick = onClick)
      .testTag("featured_biz_${business.id}"),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        if (business.is24x7) TwentyFourSevenBadge()
        SponsoredBadge()
        VerificationBadge(isVerified = business.verificationStatus == "APPROVED")
        Spacer(modifier = Modifier.weight(1f))
        RatingPill(rating = business.rating, reviewCount = business.reviewCount)
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = business.name,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = business.description,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.LocationOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(13.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
          text = "${business.serviceAreas.take(28)}...",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }
    }
  }
}

@Composable
fun BusinessListItemCard(
  business: BusinessEntity,
  onClick: () -> Unit,
  onBookClick: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .testTag("biz_item_${business.id}"),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            if (business.is24x7) TwentyFourSevenBadge()
            if (business.isFeatured) SponsoredBadge()
            VerificationBadge(isVerified = business.verificationStatus == "APPROVED")
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = business.name,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        RatingPill(rating = business.rating, reviewCount = business.reviewCount)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = business.description,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Place,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${business.city} • ${business.serviceAreas}",
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        OutlinedButton(
          onClick = onClick,
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Text("Details", fontSize = 12.sp)
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(
          onClick = onBookClick,
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
          modifier = Modifier
            .height(34.dp)
            .testTag("book_btn_${business.id}")
        ) {
          Text("Book Service", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

fun getCategoryIcon(catId: String): ImageVector {
  return when {
    catId.contains("plumb") -> Icons.Default.Plumbing
    catId.contains("elect") -> Icons.Default.ElectricalServices
    catId.contains("clean") -> Icons.Default.CleaningServices
    catId.contains("paint") -> Icons.Default.FormatPaint
    catId.contains("appliance") -> Icons.Default.Kitchen
    catId.contains("mobile") -> Icons.Default.PhoneAndroid
    catId.contains("comp") -> Icons.Default.Computer
    catId.contains("mech") -> Icons.Default.CarRepair
    catId.contains("rest") -> Icons.Default.Restaurant
    catId.contains("salon") -> Icons.Default.ContentCut
    catId.contains("tutor") -> Icons.Default.School
    catId.contains("tailor") -> Icons.Default.DryCleaning
    else -> Icons.Default.Handyman
  }
}
