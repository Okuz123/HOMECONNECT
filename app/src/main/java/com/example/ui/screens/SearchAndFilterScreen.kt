package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun SearchAndFilterScreen(
  viewModel: LocalConnectViewModel
) {
  val query by viewModel.searchQuery.collectAsState()
  val selectedCat by viewModel.searchCategory.collectAsState()
  val verifiedOnly by viewModel.searchVerifiedOnly.collectAsState()
  val minRating by viewModel.searchMinRating.collectAsState()
  val selectedCity by viewModel.selectedCity.collectAsState()
  val categories by viewModel.activeCategories.collectAsState()
  val results by viewModel.filteredBusinesses.collectAsState()

  var showFilterSheet by remember { mutableStateOf(false) }

  Column(
    modifier = Modifier
      .fillMaxSize()
      .testTag("search_filter_screen")
  ) {
    // Search input bar
    Surface(
      color = MaterialTheme.colorScheme.surface,
      shadowElevation = 2.dp
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        OutlinedTextField(
          value = query,
          onValueChange = { viewModel.searchQuery.value = it },
          placeholder = { Text("Search services, trades or business name...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (query.isNotEmpty()) {
              IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear")
              }
            }
          },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_text_field")
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Category filter chips
        LazyRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          item {
            FilterChip(
              selected = selectedCat == null,
              onClick = { viewModel.searchCategory.value = null },
              label = { Text("All Categories") }
            )
          }
          items(categories) { cat ->
            FilterChip(
              selected = selectedCat == cat.id,
              onClick = {
                viewModel.searchCategory.value = if (selectedCat == cat.id) null else cat.id
              },
              label = { Text(cat.name) },
              modifier = Modifier.testTag("filter_chip_${cat.id}")
            )
          }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Secondary filter controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "${results.size} businesses in $selectedCity",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          Row(verticalAlignment = Alignment.CenterVertically) {
            InputChip(
              selected = verifiedOnly,
              onClick = { viewModel.searchVerifiedOnly.value = !verifiedOnly },
              label = { Text("Verified Only", fontSize = 11.sp) },
              leadingIcon = {
                if (verifiedOnly) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                }
              }
            )
            Spacer(modifier = Modifier.width(6.dp))
            InputChip(
              selected = minRating >= 4.5f,
              onClick = {
                viewModel.searchMinRating.value = if (minRating >= 4.5f) 0.0f else 4.5f
              },
              label = { Text("4.5★+", fontSize = 11.sp) }
            )
            val is247 by viewModel.search24x7Only.collectAsState()
            Spacer(modifier = Modifier.width(6.dp))
            InputChip(
              selected = is247,
              onClick = { viewModel.search24x7Only.value = !is247 },
              label = { Text("24/7", fontSize = 11.sp) },
              leadingIcon = {
                Icon(Icons.Default.ElectricBolt, contentDescription = null, modifier = Modifier.size(12.dp))
              }
            )
          }
        }
      }
    }

    // Results List
    if (results.isEmpty()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(32.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(54.dp)
          )
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "No matching businesses found",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "Try adjusting your search terms, removing filters, or choosing a different city.",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
          )
        }
      }
    } else {
      LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(results) { biz ->
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
