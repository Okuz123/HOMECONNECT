package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.LocalConnectViewModel

@Composable
fun LegalPagesScreen(
  pageId: String,
  viewModel: LocalConnectViewModel
) {
  var selectedTab by remember {
    mutableStateOf(
      when (pageId) {
        "terms" -> 1
        "privacy" -> 2
        "cancellation" -> 3
        "refund" -> 4
        else -> 0
      }
    )
  }

  val tabs = listOf("About Us", "Terms of Service", "Privacy Policy", "Cancellation Policy", "Refunds", "Contact Us")

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("legal_pages_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        tabs.forEachIndexed { idx, title ->
          Tab(
            selected = selectedTab == idx,
            onClick = { selectedTab = idx },
            text = { Text(title, fontSize = 12.sp) }
          )
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            when (selectedTab) {
              0 -> {
                Text("About LocalConnect", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "LocalConnect is an open digital marketplace connecting residents with verified independent local trade specialists and neighbourhood service providers across Indian metropolitan cities.\n\n" +
                  "Our mission is to eliminate unverified middlemen and empower skilled trades—such as plumbers, electricians, appliance technicians, and home cleaners—with transparent booking management and sustainable growth tools.\n\n" +
                  "Marketplace Role: LocalConnect acts solely as an intermediary technology platform. Service providers operate as independent commercial entities responsible for service delivery, workmanship warranties, and direct customer satisfaction.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              1 -> {
                Text("Terms & Conditions", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "1. Scope of Service: Users access verified directory listings and submit service requests subject to business acceptance.\n\n" +
                  "2. Independent Contractor Model: All businesses registered on LocalConnect are independent trade professionals. LocalConnect does not employ service providers.\n\n" +
                  "3. Commission Structure: A platform commission of 10% applies to eligible completed bookings to fund fraud monitoring, verified customer support, and system operations.\n\n" +
                  "4. User Conduct: Defamatory reviews, fraudulent accounts, or price circumvention will result in immediate account suspension.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              2 -> {
                Text("Privacy & Customer Protection Policy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "1. Protected Street Addresses: Customer physical street addresses are strictly hidden from search engines and non-confirmed businesses. Only when an owner accepts a booking is the location disclosed.\n\n" +
                  "2. Contact Information: Your verified phone number is disclosed only for appointment logistics.\n\n" +
                  "3. Account Deletion: In compliance with applicable data privacy laws, customers can request complete deletion of their profile and personal identifiable information anytime in the Account settings.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              3 -> {
                Text("Booking & Cancellation Policy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "1. Free Cancellations: Customers may cancel any service booking free of charge up to 2 hours prior to the scheduled appointment.\n\n" +
                  "2. Rescheduling: Customers and providers can propose alternate appointment time slots without cancellation penalties.\n\n" +
                  "3. Provider Cancellations: In the rare event a provider cannot fulfill a confirmed booking, the platform will notify the customer immediately to rebook with an alternate verified provider.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              4 -> {
                Text("Refund & Dispute Policy", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "1. Direct Payments: For services settled directly between customers and providers, pricing disputes must be reported through the 'Report Issue' button on the booking details card.\n\n" +
                  "2. Platform Subscriptions: Business subscriptions cancelled during an active cycle remain active until the renewal date, after which recurring billing terminates without further charge.\n\n" +
                  "3. Dispute Investigation: Administrators review service records, photos, and chat logs before making binding decisions on disputes.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
              else -> {
                Text("Customer & Merchant Support", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  "Customer Help Desk: +91 80000 12345\n" +
                  "Email: support@localconnect.app\n\n" +
                  "Operating Hours: Monday – Saturday, 8:00 AM – 9:00 PM IST\n\n" +
                  "For urgent escalation of in-progress bookings, please contact the dedicated helpline with your 6-digit Booking ID.",
                  fontSize = 13.sp,
                  lineHeight = 18.sp
                )
              }
            }
          }
        }
      }
    }
  }
}
