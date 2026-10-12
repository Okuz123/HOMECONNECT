package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PaymentCheckoutScreen(
  bookingId: String,
  amount: Double,
  viewModel: LocalConnectViewModel
) {
  val allBookings by viewModel.allBookingsAdmin.collectAsState()
  val allBusinesses by viewModel.allBusinessesAdmin.collectAsState()
  val booking = allBookings.firstOrNull { it.id == bookingId }
  val business = allBusinesses.firstOrNull { it.id == booking?.businessId }

  var selectedMethod by remember { mutableStateOf("UPI") }
  var upiId by remember { mutableStateOf("okhanday@okhdfcbank") }
  var cardNumber by remember { mutableStateOf("4532 •••• •••• 8912") }
  var cardExpiry by remember { mutableStateOf("12/28") }
  var cardCvv by remember { mutableStateOf("382") }
  var selectedBank by remember { mutableStateOf("HDFC Bank") }

  var isProcessing by remember { mutableStateOf(false) }
  var completedTxnId by remember { mutableStateOf<String?>(null) }
  var receiptModal by remember { mutableStateOf(false) }

  val paymentAmount = if (amount > 0) amount else (booking?.agreedPrice ?: 299.0)

  if (completedTxnId != null) {
    // Payment Success & Digital Receipt View
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      contentAlignment = Alignment.Center
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
          .fillMaxWidth()
          .widthIn(max = 500.dp)
          .testTag("payment_receipt_card")
      ) {
        Column(
          modifier = Modifier.padding(20.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFD1FAE5),
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                tint = StatusSuccess,
                modifier = Modifier.size(36.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "Payment Completed 24/7",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = StatusSuccess
          )
          Text(
            text = "₹${paymentAmount.toInt()}",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Transaction ID: $completedTxnId",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Spacer(modifier = Modifier.height(14.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(14.dp))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Service Provider:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(business?.name ?: "Verified Provider", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Booking Reference:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("#${bookingId.takeLast(6)}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Payment Method:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(selectedMethod, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            Text("Timestamp:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(sdf.format(Date()), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
          }

          Spacer(modifier = Modifier.height(20.dp))
          Button(
            onClick = {
              viewModel.navigateTo(Screen.CustomerBookings)
            },
            modifier = Modifier.fillMaxWidth().testTag("receipt_done_btn")
          ) {
            Text("View Booking Details")
          }
        }
      }
    }
    return
  }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("payment_checkout_screen"),
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
          Text("24/7 Secure In-App Payment", fontSize = 20.sp, fontWeight = FontWeight.Bold)
          Text("Instant settlement & buyer protection guarantee", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = Color(0xFFD1FAE5)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.ElectricBolt, contentDescription = null, tint = StatusSuccess, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("24/7 LIVE", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = StatusSuccess)
          }
        }
      }
    }

    // Bill Details
    item {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text("Order Summary", fontWeight = FontWeight.Bold, fontSize = 14.sp)
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Payable to:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(business?.name ?: "Provider", fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Scheduled Service:", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(booking?.description?.take(25) ?: "Home Service", fontSize = 12.sp)
          }
          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Total Amount to Pay:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("₹${paymentAmount.toInt()}", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
          }
        }
      }
    }

    // Payment Methods Selection
    item {
      Text("Select Payment Method", fontWeight = FontWeight.Bold, fontSize = 15.sp)
      Spacer(modifier = Modifier.height(6.dp))

      listOf("UPI", "CARD", "NET_BANKING", "CASH").forEach { method ->
        val isSelected = selectedMethod == method
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
          modifier = Modifier
            .fillMaxWidth()
            .clickable { selectedMethod = method }
            .padding(vertical = 4.dp)
            .testTag("method_option_$method")
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = isSelected, onClick = { selectedMethod = method })
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = when (method) {
                "UPI" -> Icons.Default.QrCode
                "CARD" -> Icons.Default.CreditCard
                "NET_BANKING" -> Icons.Default.AccountBalance
                else -> Icons.Default.Payments
              },
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = when (method) {
                  "UPI" -> "UPI (Google Pay / PhonePe / Paytm / BHIM)"
                  "CARD" -> "Credit or Debit Card"
                  "NET_BANKING" -> "Net Banking (All Major Indian Banks)"
                  else -> "Cash on Service Completion"
                },
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = when (method) {
                  "UPI" -> "Instant 24/7 VPA settlement"
                  "CARD" -> "Visa, MasterCard, RuPay"
                  "NET_BANKING" -> "Direct bank debit"
                  else -> "Pay technician directly after work"
                },
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Input fields for chosen method
    item {
      when (selectedMethod) {
        "UPI" -> {
          OutlinedTextField(
            value = upiId,
            onValueChange = { upiId = it },
            label = { Text("Virtual Payment Address (UPI ID)") },
            placeholder = { Text("username@okhdfcbank") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("upi_input_field")
          )
        }
        "CARD" -> {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = cardNumber,
              onValueChange = { cardNumber = it },
              label = { Text("Card Number") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              OutlinedTextField(
                value = cardExpiry,
                onValueChange = { cardExpiry = it },
                label = { Text("MM/YY") },
                modifier = Modifier.weight(1f)
              )
              OutlinedTextField(
                value = cardCvv,
                onValueChange = { cardCvv = it },
                label = { Text("CVV") },
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
        "NET_BANKING" -> {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf("HDFC Bank", "SBI", "ICICI", "Axis Bank").forEach { bank ->
              FilterChip(
                selected = selectedBank == bank,
                onClick = { selectedBank = bank },
                label = { Text(bank, fontSize = 11.sp) }
              )
            }
          }
        }
        else -> {
          Text(
            "Pay the technician directly upon job completion. The 10% platform commission remains protected.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    }

    // Pay Button
    item {
      Spacer(modifier = Modifier.height(10.dp))
      Button(
        onClick = {
          isProcessing = true
          viewModel.processBookingPayment(
            bookingId = bookingId,
            method = selectedMethod,
            amount = paymentAmount,
            onSuccess = { txnId ->
              isProcessing = false
              completedTxnId = txnId
            }
          )
        },
        enabled = !isProcessing,
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("confirm_pay_button"),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isProcessing) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
          Text("Pay ₹${paymentAmount.toInt()} Now (24/7 Secure)", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      }
    }
  }
}
