package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LogiViewModel


private val PolishedBackground = Color(0xFFFBFDF9)
private val PolishedCardBackground = Color(0xFFFFFFFF)
private val SecondaryCardBackground = Color(0xFFF0F5EB)
private val PolishedPrimaryGreen = Color(0xFF386A20)
private val PolishedAccentGreen = Color(0xFFB9F397)
private val PolishedTextPrimary = Color(0xFF191C17)
private val PolishedTextSecondary = Color(0xFF42493F)
private val PolishedTextMuted = Color(0xFF55624C)
private val BorderLightColor = Color(0xFFDDE6D3)
private val BorderOutlineColor = Color(0xFFC3C8BB)
private val BatteryGolden = Color(0xFFD97706)
private val CrimsonError = Color(0xFFDC2626)
private val SkyColor = Color(0xFF0369A1)

enum class LoginView {
    ROLE_SELECT,
    DRIVER_LOGIN,
    CUSTOMER_LOGIN
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(viewModel: LogiViewModel) {
    val authError by viewModel.authError.collectAsState()
    var currentView by remember { mutableStateOf(LoginView.ROLE_SELECT) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PolishedBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            PolishedAccentGreen.copy(alpha = 0.4f),
                            PolishedBackground
                        )
                    )
                )
        )

        AnimatedContent(
            targetState = currentView,
            transitionSpec = {
                if (targetState == LoginView.ROLE_SELECT) {
                    slideInHorizontally(animationSpec = tween(300)) { -it } + fadeIn() with
                            slideOutHorizontally(animationSpec = tween(300)) { it } + fadeOut()
                } else {
                    slideInHorizontally(animationSpec = tween(300)) { it } + fadeIn() with
                            slideOutHorizontally(animationSpec = tween(300)) { -it } + fadeOut()
                }
            },
            label = "LoginNavigation"
        ) { state ->
            when (state) {
                LoginView.ROLE_SELECT -> {
                    RoleSelectView(
                        onSelectDriver = {
                            viewModel.login("", "", "")
                            currentView = LoginView.DRIVER_LOGIN
                        },
                        onSelectCustomer = {
                            viewModel.login("", "", "")
                            currentView = LoginView.CUSTOMER_LOGIN
                        }
                    )
                }
                LoginView.DRIVER_LOGIN -> {
                    DriverLoginView(
                        viewModel = viewModel,
                        authError = authError,
                        onBack = { currentView = LoginView.ROLE_SELECT }
                    )
                }
                LoginView.CUSTOMER_LOGIN -> {
                    CustomerLoginView(
                        viewModel = viewModel,
                        authError = authError,
                        onBack = { currentView = LoginView.ROLE_SELECT }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleSelectView(
    onSelectDriver: () -> Unit,
    onSelectCustomer: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Box(
            modifier = Modifier
                .size(64.dp)
                .background(PolishedAccentGreen, RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocalShipping,
                contentDescription = "LogiTrack Icon",
                tint = PolishedPrimaryGreen,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "LogiTrack Portal",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            ),
            color = PolishedTextPrimary,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Choose your role below to access the logistics dashboard, live telemetry console, or real-time delivery tracing.",
            style = MaterialTheme.typography.bodyMedium,
            color = PolishedTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))


        Card(
            onClick = onSelectDriver,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("entry_driver_panel_btn"),
            colors = CardDefaults.cardColors(containerColor = PolishedCardBackground),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderOutlineColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(SecondaryCardBackground, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.EditLocationAlt,
                        contentDescription = "Driver Entry Icon",
                        tint = PolishedPrimaryGreen,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Driver Terminal",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PolishedTextPrimary
                    )
                    Text(
                        text = "Broadcast live coordinates, control telemetry optimization, and manage off-line sync caches.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishedTextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Go to driver login",
                    tint = PolishedTextMuted
                )
            }
        }


        Card(
            onClick = onSelectCustomer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("entry_customer_panel_btn"),
            colors = CardDefaults.cardColors(containerColor = PolishedCardBackground),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.dp, BorderOutlineColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE2F0FD), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "Customer Entry Icon",
                        tint = SkyColor,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Customer Client Tracker",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = PolishedTextPrimary
                    )
                    Text(
                        text = "Track your parcel status on our interactive city grid, view live ETA times, and query courier speed metrics.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PolishedTextMuted
                    )
                }

                Icon(
                    imageVector = Icons.Filled.ChevronRight,
                    contentDescription = "Go to customer login",
                    tint = PolishedTextMuted
                )
            }
        }


    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DriverLoginView(
    viewModel: LogiViewModel,
    authError: String?,
    onBack: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var shiftCode by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .border(1.dp, BorderOutlineColor, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PolishedTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Back to Portals",
                style = MaterialTheme.typography.titleSmall,
                color = PolishedTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Driver Authentication",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PolishedTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = "Access your active transit console to coordinate streaming positions with dispatched systems.",
            style = MaterialTheme.typography.bodyMedium,
            color = PolishedTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )


        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Driver Email ID") },
            placeholder = { Text("courier@logitrack.com") },
            leadingIcon = {
                Icon(Icons.Filled.Email, contentDescription = "Email icon", tint = PolishedTextMuted)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("driver_email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolishedPrimaryGreen,
                unfocusedBorderColor = BorderOutlineColor,
                cursorColor = PolishedPrimaryGreen
            )
        )


        OutlinedTextField(
            value = passwordInput,
            onValueChange = { passwordInput = it },
            label = { Text("Security Access Key") },
            placeholder = { Text("••••••••") },
            leadingIcon = {
                Icon(Icons.Filled.Lock, contentDescription = "Password icon", tint = PolishedTextMuted)
            },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Toggle password visibility")
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("driver_password_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolishedPrimaryGreen,
                unfocusedBorderColor = BorderOutlineColor,
                cursorColor = PolishedPrimaryGreen
            )
        )


        OutlinedTextField(
            value = shiftCode,
            onValueChange = { shiftCode = it },
            label = { Text("Shift Dispatch Code (Optional)") },
            placeholder = { Text("SF-EAST-A9") },
            leadingIcon = {
                Icon(Icons.Filled.AccessTime, contentDescription = "Shift icon", tint = PolishedTextMuted)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("driver_shift_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PolishedPrimaryGreen,
                unfocusedBorderColor = BorderOutlineColor,
                cursorColor = PolishedPrimaryGreen
            )
        )


        if (authError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f)),
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error detail",
                        tint = CrimsonError,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = authError,
                        color = CrimsonError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                viewModel.login("DRIVER", emailInput, passwordInput)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PolishedPrimaryGreen,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("driver_submit_login_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Login,
                contentDescription = "Login action icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("AUTHORIZED SIGN IN", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(24.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = SecondaryCardBackground),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, BorderLightColor),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Credentials badge",
                        tint = PolishedPrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "CREDENTIALS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = PolishedPrimaryGreen,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().clickable {
                        emailInput = "driver@logitrack.com"
                        passwordInput = "driver123"
                        shiftCode = "SF-EAST-A9"
                    }
                    .background(PolishedCardBackground, RoundedCornerShape(8.dp))
                    .border(1.dp, BorderLightColor, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Email: driver@logitrack.com",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                        Text(
                            text = "Password: driver123",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                    }
                    Text(
                        text = "AUTOFIL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishedPrimaryGreen,
                        modifier = Modifier
                            .background(PolishedAccentGreen, RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerLoginView(
    viewModel: LogiViewModel,
    authError: String?,
    onBack: () -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var trackingCode by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .border(1.dp, BorderOutlineColor, CircleShape)
                    .size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = PolishedTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Back to Portals",
                style = MaterialTheme.typography.titleSmall,
                color = PolishedTextSecondary,
                fontWeight = FontWeight.Bold
            )
        }

        Text(
            text = "Customer Hub",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = PolishedTextPrimary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Text(
            text = "Access your parcel telemetry. Provide your tracking ID and associated email address to monitor current transit status.",
            style = MaterialTheme.typography.bodyMedium,
            color = PolishedTextSecondary,
            modifier = Modifier.padding(bottom = 24.dp)
        )


        OutlinedTextField(
            value = emailInput,
            onValueChange = { emailInput = it },
            label = { Text("Customer Email Address") },
            placeholder = { Text("customer@gmail.com") },
            leadingIcon = {
                Icon(Icons.Filled.AccountCircle, contentDescription = "Email icon", tint = PolishedTextMuted)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("customer_email_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkyColor,
                unfocusedBorderColor = BorderOutlineColor,
                cursorColor = SkyColor
            )
        )


        OutlinedTextField(
            value = trackingCode,
            onValueChange = { trackingCode = it },
            label = { Text("Package Tracking ID") },
            placeholder = { Text("LT-4938-MH") },
            leadingIcon = {
                Icon(Icons.Filled.QrCodeScanner, contentDescription = "Package icon", tint = PolishedTextMuted)
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .testTag("customer_tracking_code_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SkyColor,
                unfocusedBorderColor = BorderOutlineColor,
                cursorColor = SkyColor
            )
        )


        if (authError != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = CrimsonError.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, CrimsonError.copy(alpha = 0.3f)),
                modifier = Modifier
                    .padding(vertical = 10.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Error,
                        contentDescription = "Error detail",
                        tint = CrimsonError,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = authError,
                        color = CrimsonError,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))


        Button(
            onClick = {
                viewModel.login("CUSTOMER", emailInput, trackingCode)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = SkyColor,
                contentColor = Color.White
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("customer_submit_login_btn"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Track action icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("QUERY SHIPMENT STATUS", fontWeight = FontWeight.ExtraBold)
        }

        Spacer(modifier = Modifier.height(24.dp))


        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F6FC)),
            modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, Color(0xFFD0E3F3)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Credentials badge",
                        tint = SkyColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "CUSTOMER CREDENTIALS",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 11.sp,
                        color = SkyColor,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            emailInput = "customer@gmail.com"
                            trackingCode = "LT-4938-MH"
                        }
                        .background(PolishedCardBackground, RoundedCornerShape(8.dp))
                        .border(1.dp, Color(0xFFD0E3F3), RoundedCornerShape(8.dp))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Email: customer@gmail.com",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                        Text(
                            text = "Tracking ID: LT-4938-MH",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                    }
                    Text(
                        text = "AUTOFIL",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = SkyColor,
                        modifier = Modifier
                            .background(Color(0xFFE2F0FD), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}
