package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.data.model.ReportEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusError
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.LocalConnectViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AdminUsersScreen(
  viewModel: LocalConnectViewModel
) {
  val users by viewModel.allProfiles.collectAsState()
  var searchQuery by remember { mutableStateOf("") }

  val filteredUsers = remember(users, searchQuery) {
    if (searchQuery.isBlank()) users else users.filter {
      it.fullName.contains(searchQuery, ignoreCase = true) ||
      it.email.contains(searchQuery, ignoreCase = true) ||
      it.role.contains(searchQuery, ignoreCase = true)
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_users_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("User Account Management", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Inspect user records, verify roles, and monitor account integrity.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = { Text("Search users by name, email, or role...") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filteredUsers) { user ->
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
              Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
              Text("${user.email} • ${user.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Spacer(modifier = Modifier.height(2.dp))
              Text("Preferred City: ${user.preferredCity}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            }
            StatusBadge(status = user.role)
          }
        }
      }
    }
  }
}

@Composable
fun AdminBookingsScreen(
  viewModel: LocalConnectViewModel
) {
  val bookings by viewModel.allBookingsAdmin.collectAsState()
  val allBusinesses by viewModel.allBusinessesAdmin.collectAsState()

  var selectedFilter by remember { mutableStateOf("ALL") }

  val filtered = remember(bookings, selectedFilter) {
    when (selectedFilter) {
      "DISPUTED" -> bookings.filter { it.status == "DISPUTED" }
      "COMPLETED" -> bookings.filter { it.status == "COMPLETED" }
      "PENDING" -> bookings.filter { it.status == "PENDING" }
      else -> bookings
    }
  }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_bookings_screen")
  ) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text("Global Bookings & Disputes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Inspect lifecycle state transitions, payments, and disputes.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
          listOf("ALL", "DISPUTED", "COMPLETED", "PENDING").forEach { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = { Text(filter, fontSize = 11.sp) },
              modifier = Modifier.padding(end = 6.dp)
            )
          }
        }
      }
    }

    LazyColumn(
      modifier = Modifier.fillMaxSize(),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      items(filtered) { bk ->
        val biz = allBusinesses.firstOrNull { it.id == bk.businessId }

        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text("Booking #${bk.id.takeLast(6)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              StatusBadge(status = bk.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Business: ${biz?.name ?: bk.businessId}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            Text("Customer Phone: ${bk.customerPhone}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
            Text("Protected Service Address: ${bk.protectedServiceAddress}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text("Scheduled Date: ${bk.preferredDate} (${bk.preferredTime})", fontSize = 11.sp)
            if (bk.agreedPrice > 0) {
              Text("Agreed Price: ₹${bk.agreedPrice.toInt()} (10% Commission: ₹${(bk.agreedPrice * 0.10).toInt()})", fontSize = 11.sp, color = StatusSuccess, fontWeight = FontWeight.Bold)
            }

            if (bk.status == "DISPUTED") {
              Spacer(modifier = Modifier.height(6.dp))
              Text("Dispute Reason: ${bk.cancellationReason ?: "Customer reported issue"}", color = MaterialTheme.colorScheme.error, fontSize = 11.sp)
              Spacer(modifier = Modifier.height(6.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                  onClick = { viewModel.updateBookingStatus(bk.id, "COMPLETED", "Admin resolved dispute in favor of provider") },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Text("Resolve as Completed", fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedButton(
                  onClick = { viewModel.updateBookingStatus(bk.id, "CANCELLED", "Admin refunded/cancelled booking") },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Text("Resolve as Cancelled", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun AdminAuditLogsScreen(
  viewModel: LocalConnectViewModel
) {
  val logs by viewModel.allAuditLogs.collectAsState()
  val sdf = remember { SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_audit_logs_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    item {
      Text("System Audit Logs", fontSize = 18.sp, fontWeight = FontWeight.Bold)
      Text("Immutable record of administrative governance events and approvals.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    if (logs.isEmpty()) {
      item {
        Text("No audit log events recorded yet.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      items(logs) { log ->
        Card(
          shape = RoundedCornerShape(8.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryBlue)
              Text(sdf.format(Date(log.createdAt)), fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text("Admin: ${log.administratorId} • Target: [${log.targetType}] ${log.targetId}", fontSize = 11.sp)
            Text("Reason: ${log.reason}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
    }
  }
}

@Composable
fun AdminReviewsReportsScreen(
  viewModel: LocalConnectViewModel
) {
  val reviews by viewModel.allReviewsAdmin.collectAsState()
  val reports by viewModel.allReportsAdmin.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .testTag("admin_reviews_reports_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      Text("Reviews Moderation & Customer Reports", fontSize = 18.sp, fontWeight = FontWeight.Bold)
      Text("Investigate flagged reviews and user-submitted incident complaints.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }

    item {
      Text("User Reports & Complaints (${reports.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    if (reports.isEmpty()) {
      item {
        Text("No outstanding reports or disputes filed.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      items(reports) { rep ->
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text("Report #${rep.id.takeLast(6)}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              StatusBadge(status = rep.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("Reason: ${rep.reason}", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text("Details: ${rep.description}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

            if (rep.status == "PENDING") {
              Spacer(modifier = Modifier.height(8.dp))
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(
                  onClick = { viewModel.adminResolveReport(rep.id, "Issue investigated & warning issued to provider") },
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                  modifier = Modifier.height(30.dp)
                ) {
                  Text("Resolve Report", fontSize = 10.sp)
                }
              }
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text("Platform Reviews (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }

    items(reviews) { rev ->
      Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("${rev.customerName} (${rev.rating}★)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            StatusBadge(status = rev.moderationStatus)
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(rev.reviewText, fontSize = 12.sp)

          Spacer(modifier = Modifier.height(8.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            if (rev.moderationStatus != "APPROVED") {
              Button(
                onClick = { viewModel.adminModerateReview(rev.id, "APPROVED") },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Approve", fontSize = 10.sp)
              }
              Spacer(modifier = Modifier.width(6.dp))
            }
            if (rev.moderationStatus != "FLAGGED") {
              OutlinedButton(
                onClick = { viewModel.adminModerateReview(rev.id, "FLAGGED") },
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
              ) {
                Text("Flag", fontSize = 10.sp)
              }
            }
          }
        }
      }
    }
  }
}
