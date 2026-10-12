package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.SecondaryAmber
import com.example.ui.viewmodel.LocalConnectViewModel
import com.example.ui.viewmodel.Screen

@Composable
fun LoginScreen(
  viewModel: LocalConnectViewModel
) {
  var email by remember { mutableStateOf("aarav.sharma@example.in") }
  var password by remember { mutableStateOf("pass123") }
  var passwordVisible by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }
  var showGoogleDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .testTag("login_screen"),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(10.dp))
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(64.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.Handyman,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(36.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
          text = "My Connect",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 24.sp,
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Local Business & 24/7 Home Services Marketplace",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      errorMessage?.let { err ->
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = err,
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }

      item {
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("Email Address") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("login_email_input")
        )
      }

      item {
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Password") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          trailingIcon = {
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
              Icon(
                imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                contentDescription = null
              )
            }
          },
          visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("login_password_input")
        )
      }

      item {
        Button(
          onClick = {
            if (email.isBlank() || password.isBlank()) {
              errorMessage = "Please enter both email and password."
              return@Button
            }
            viewModel.login(
              email = email,
              pass = password,
              onSuccess = {
                viewModel.navigateTo(Screen.CustomerHome)
              },
              onError = { err -> errorMessage = err }
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("login_submit_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Sign In", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          HorizontalDivider(modifier = Modifier.weight(1f))
          Text(
            text = "  OR  ",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          HorizontalDivider(modifier = Modifier.weight(1f))
        }
      }

      // Continue with Google Button
      item {
        OutlinedButton(
          onClick = { showGoogleDialog = true },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("google_login_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = "Continue with Google",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
          )
        }
      }

      // Quick Demo Profiles
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              text = "Quick Demo Accounts:",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              OutlinedButton(
                onClick = {
                  email = "aarav.sharma@example.in"
                  password = "pass123"
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Text("Customer", fontSize = 10.sp)
              }
              OutlinedButton(
                onClick = {
                  email = "rajesh@apexplumbing.in"
                  password = "pass123"
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Text("Owner", fontSize = 10.sp)
              }
              OutlinedButton(
                onClick = {
                  email = "admin@localconnect.app"
                  password = "pass123"
                },
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp)
              ) {
                Text("Admin", fontSize = 10.sp)
              }
            }
          }
        }
      }

      item {
        Row(
          modifier = Modifier.padding(top = 8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Don't have an account? ",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "Register",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .clickable { viewModel.navigateTo(Screen.Register) }
              .testTag("go_to_register_btn")
          )
        }
      }
    }
  }

  // Simulated Google One-Tap Account Dialog
  if (showGoogleDialog) {
    AlertDialog(
      onDismissRequest = { showGoogleDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.AccountCircle, contentDescription = null, tint = PrimaryBlue)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign in with Google", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
      },
      text = {
        Column {
          Text(
            "Choose a Google account to continue to My Connect:",
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(12.dp))

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.loginWithGoogle(
                  email = "okhanday891@gmail.com",
                  name = "OK Handay",
                  role = "CUSTOMER",
                  onSuccess = {
                    showGoogleDialog = false
                    viewModel.navigateTo(Screen.CustomerHome)
                  }
                )
              }
              .padding(vertical = 4.dp)
              .testTag("google_account_okhanday")
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(PrimaryBlue),
                contentAlignment = Alignment.Center
              ) {
                Text("O", color = Color.White, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("OK Handay", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("okhanday891@gmail.com", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }

          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                viewModel.loginWithGoogle(
                  email = "aarav.sharma@example.in",
                  name = "Aarav Sharma",
                  role = "CUSTOMER",
                  onSuccess = {
                    showGoogleDialog = false
                    viewModel.navigateTo(Screen.CustomerHome)
                  }
                )
              }
              .padding(vertical = 4.dp)
          ) {
            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(32.dp)
                  .clip(CircleShape)
                  .background(SecondaryAmber),
                contentAlignment = Alignment.Center
              ) {
                Text("A", color = Color.White, fontWeight = FontWeight.Bold)
              }
              Spacer(modifier = Modifier.width(10.dp))
              Column {
                Text("Aarav Sharma", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text("aarav.sharma@example.in", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        TextButton(onClick = { showGoogleDialog = false }) { Text("Cancel") }
      }
    )
  }
}

@Composable
fun RegisterScreen(
  viewModel: LocalConnectViewModel
) {
  var fullName by remember { mutableStateOf("") }
  var email by remember { mutableStateOf("") }
  var phone by remember { mutableStateOf("+91 ") }
  var password by remember { mutableStateOf("") }
  var selectedRole by remember { mutableStateOf("CUSTOMER") }
  var selectedCity by remember { mutableStateOf("Mumbai") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val cities = listOf("Mumbai", "Bengaluru", "Delhi NCR", "Pune", "Hyderabad", "Chennai")

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background),
    contentAlignment = Alignment.Center
  ) {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .testTag("register_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        Text(
          text = "Create Account",
          fontWeight = FontWeight.ExtraBold,
          fontSize = 22.sp
        )
        Text(
          text = "Join My Connect as a customer, business owner, or independent technician.",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      errorMessage?.let { err ->
        item {
          Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = err,
              color = MaterialTheme.colorScheme.onErrorContainer,
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(10.dp)
            )
          }
        }
      }

      // Role Selection
      item {
        Text("I want to register as:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          listOf("CUSTOMER", "BUSINESS_OWNER", "SERVICE_PROVIDER").forEach { r ->
            FilterChip(
              selected = selectedRole == r,
              onClick = { selectedRole = r },
              label = {
                Text(
                  text = when (r) {
                    "BUSINESS_OWNER" -> "Business"
                    "SERVICE_PROVIDER" -> "Provider"
                    else -> "Customer"
                  },
                  fontSize = 11.sp
                )
              },
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      item {
        OutlinedTextField(
          value = fullName,
          onValueChange = { fullName = it; errorMessage = null },
          label = { Text("Full Name *") },
          leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("reg_fullname_input")
        )
      }

      item {
        OutlinedTextField(
          value = email,
          onValueChange = { email = it; errorMessage = null },
          label = { Text("Email Address *") },
          leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("reg_email_input")
        )
      }

      item {
        OutlinedTextField(
          value = phone,
          onValueChange = { phone = it; errorMessage = null },
          label = { Text("Mobile Phone Number *") },
          leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("reg_phone_input")
        )
      }

      item {
        OutlinedTextField(
          value = password,
          onValueChange = { password = it; errorMessage = null },
          label = { Text("Password (Min 6 chars) *") },
          leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
          visualTransformation = PasswordVisualTransformation(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth().testTag("reg_password_input")
        )
      }

      item {
        Text("Primary City *", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
          cities.take(3).forEach { c ->
            FilterChip(
              selected = selectedCity == c,
              onClick = { selectedCity = c },
              label = { Text(c, fontSize = 10.sp) },
              modifier = Modifier.padding(end = 4.dp)
            )
          }
        }
      }

      item {
        Button(
          onClick = {
            if (fullName.trim().length < 2) {
              errorMessage = "Please enter your valid full name."
              return@Button
            }
            if (!email.contains("@") || !email.contains(".")) {
              errorMessage = "Please enter a valid email address."
              return@Button
            }
            if (phone.trim().length < 10) {
              errorMessage = "Please enter a valid 10-digit mobile number."
              return@Button
            }
            if (password.length < 6) {
              errorMessage = "Password must be at least 6 characters."
              return@Button
            }

            viewModel.registerUser(
              fullName = fullName,
              email = email,
              phone = phone,
              pass = password,
              role = selectedRole,
              city = selectedCity,
              onSuccess = {
                viewModel.navigateTo(Screen.CustomerHome)
              },
              onError = { err -> errorMessage = err }
            )
          },
          modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .testTag("reg_submit_btn"),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text("Complete Registration", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center
        ) {
          Text("Already have an account? ", fontSize = 13.sp)
          Text(
            text = "Sign In",
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            modifier = Modifier.clickable { viewModel.navigateTo(Screen.Login) }
          )
        }
      }
    }
  }
}
