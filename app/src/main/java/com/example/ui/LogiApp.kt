package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocationRecord
import com.example.data.MovementState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogiApp(viewModel: LogiViewModel) {
    val localRecords by viewModel.localRecords.collectAsState()
    val cloudRecords by viewModel.cloudRecords.collectAsState()
    val isOnline by viewModel.isOnline.collectAsState()
    val isSyncing by viewModel.isSyncing.collectAsState()
    val viewLogs by viewModel.viewLogs.collectAsState()

    val isTrackingActive by viewModel.isTrackingActive.collectAsState()
    val isOptimizationEnabled by viewModel.isOptimizationEnabled.collectAsState()
    val movementState by viewModel.movementState.collectAsState()
    val batteryPercentage by viewModel.batteryPercentage.collectAsState()

    val currentIntervalSeconds by viewModel.currentIntervalSeconds.collectAsState()
    val batterySavingsPct by viewModel.batterySavingsPct.collectAsState()

    val currentLat by viewModel.latitude.collectAsState()
    val currentLng by viewModel.longitude.collectAsState()
    val currentSpeed by viewModel.currentSpeedKmh.collectAsState()

    val userRole by viewModel.userRole.collectAsState()
    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val currentPackageId by viewModel.currentPackageId.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(userRole) {
        if (userRole == "DRIVER") {
            selectedTab = 0
        } else if (userRole == "CUSTOMER") {
            selectedTab = 1
        }
    }

    if (userRole == null) {
        LoginScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(BorderLightColor, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = "Shipping Logo",
                                tint = PolishedPrimaryGreen,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "LogiTrack",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.5).sp
                                ),
                                color = PolishedTextPrimary
                            )
                            Text(
                                text = "Real-Time Courier Tracking",
                                style = MaterialTheme.typography.bodySmall,
                                color = PolishedTextMuted,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {

                    val connectionBg = if (isOnline) PolishedAccentGreen else CrimsonError.copy(alpha = 0.12f)
                    val connectionTextCol = if (isOnline) Color(0xFF042100) else CrimsonError
                    val connectionBorder = if (isOnline) BorderOutlineColor else CrimsonError.copy(alpha = 0.3f)
                    val connectionText = if (isOnline) "ONLINE" else "OFFLINE"

                    Button(
                        onClick = { viewModel.setOnlineStatus(!isOnline) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = connectionBg,
                            contentColor = connectionTextCol
                        ),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .testTag("network_toggle_button")
                            .heightIn(min = 36.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, connectionBorder)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(connectionTextCol)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = connectionText,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }


                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (userRole == "DRIVER") PolishedAccentGreen.copy(alpha = 0.5f) 
                                else Color(0xFFD0E3F3)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = userRole ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (userRole == "DRIVER") PolishedPrimaryGreen else SkyColor
                        )
                    }


                    IconButton(
                        onClick = { viewModel.logout() },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Logout,
                            contentDescription = "Sign out of shift",
                            tint = CrimsonError,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PolishedBackground
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PolishedBackground)
                .padding(innerPadding)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SecondaryCardBackground)
                    .border(BorderStroke(1.dp, BorderOutlineColor.copy(alpha = 0.5f)))
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Storage,
                        contentDescription = "Room Database Status",
                        tint = SkyColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Room Cache: ${localRecords.size}pts",
                        color = PolishedTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudUpload,
                        contentDescription = "Firebase Sync Status",
                        tint = PolishedPrimaryGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Firebase Synced: ${cloudRecords.size}pts",
                        color = PolishedTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (localRecords.any { !it.isSynced }) {
                    Button(
                        onClick = { viewModel.forceSync() },
                        colors = ButtonDefaults.buttonColors(containerColor = PolishedPrimaryGreen),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(24.dp)
                            .testTag("sync_now_btn"),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("Sync Cache", fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Text("✓ Synchronized", color = PolishedPrimaryGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }


            AnimatedVisibility(
                visible = isSyncing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LinearProgressIndicator(
                    color = PolishedPrimaryGreen,
                    trackColor = PolishedPrimaryGreen.copy(alpha = 0.15f),
                    modifier = Modifier.fillMaxWidth()
                )
            }


            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = PolishedBackground,
                contentColor = PolishedPrimaryGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PolishedPrimaryGreen,
                        height = 3.dp
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Filled.LocalShipping else Icons.Outlined.LocalShipping,
                                contentDescription = "Driver view tag",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Driver Console", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("driver_tab"),
                    selectedContentColor = PolishedPrimaryGreen,
                    unselectedContentColor = PolishedTextSecondary
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 1) Icons.Filled.MyLocation else Icons.Outlined.MyLocation,
                                contentDescription = "Customer view tag",
                                modifier = Modifier.size(18.dp)
                            )
                            Text("Customer Tracker", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    },
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("customer_tab"),
                    selectedContentColor = PolishedPrimaryGreen,
                    unselectedContentColor = PolishedTextSecondary
                )
            }


            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                if (selectedTab == 0) {
                    DriverConsoleScreen(
                        isTrackingActive = isTrackingActive,
                        isOptimizationEnabled = isOptimizationEnabled,
                        movementState = movementState,
                        batteryPercentage = batteryPercentage,
                        currentIntervalSeconds = currentIntervalSeconds,
                        batterySavingsPct = batterySavingsPct,
                        currentLat = currentLat,
                        currentLng = currentLng,
                        currentSpeed = currentSpeed,
                        localCount = localRecords.size,
                        onToggleTracking = { viewModel.toggleTracking(it) },
                        onToggleOptimization = { viewModel.toggleOptimization(it) },
                        onSetMovementState = { viewModel.setMovementState(it) },
                        onSetBattery = { viewModel.setBatteryPercentage(it) },
                        onClearAll = { viewModel.clearAllData() }
                    )
                } else {
                    CustomerTrackerScreen(
                        cloudRecords = cloudRecords,
                        currentLat = currentLat,
                        currentLng = currentLng,
                        currentSpeed = currentSpeed,
                        batteryPercentage = batteryPercentage,
                        isOnline = isOnline,
                        currentPackageId = currentPackageId,
                        onClearAll = { viewModel.clearAllData() }
                    )
                }
            }


            LogConsolePanel(
                viewLogs = viewLogs,
                darkCardBackground = PolishedCardBackground,
                skyColor = SkyColor
            )
        }
    }
}

@Composable
fun DriverConsoleScreen(
    isTrackingActive: Boolean,
    isOptimizationEnabled: Boolean,
    movementState: MovementState,
    batteryPercentage: Int,
    currentIntervalSeconds: Int,
    batterySavingsPct: Double,
    currentLat: Double,
    currentLng: Double,
    currentSpeed: Double,
    localCount: Int,
    onToggleTracking: (Boolean) -> Unit,
    onToggleOptimization: (Boolean) -> Unit,
    onSetMovementState: (MovementState) -> Unit,
    onSetBattery: (Int) -> Unit,
    onClearAll: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SecondaryCardBackground),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderOutlineColor)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isTrackingActive) "BROADCAST ACTIVE" else "BROADCAST INACTIVE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTrackingActive) PolishedPrimaryGreen else PolishedTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = if (isTrackingActive) "Streaming high-precision locations of courier shift to cache" 
                                   else "Locations frozen. Activate to stream driver courier shift",
                            style = MaterialTheme.typography.bodySmall,
                            color = PolishedTextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { onToggleTracking(!isTrackingActive) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTrackingActive) CrimsonError else PolishedPrimaryGreen,
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .testTag("broadcast_toggle")
                            .heightIn(min = 44.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (isTrackingActive) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                            contentDescription = if (isTrackingActive) "Stop broadcast icon" else "Start broadcast icon",
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isTrackingActive) "STOP" else "START", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }


        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishedCardBackground),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderLightColor),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MyLocation,
                            contentDescription = "Telemetry icon",
                            tint = PolishedPrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Live Telemetry Node",
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary,
                            fontSize = 14.sp
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(SecondaryCardBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderLightColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("LATITUDE", fontSize = 9.sp, color = PolishedTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(
                                text = String.format("%.6f", currentLat),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PolishedPrimaryGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .background(SecondaryCardBackground, RoundedCornerShape(12.dp))
                                .border(1.dp, BorderLightColor, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("LONGITUDE", fontSize = 9.sp, color = PolishedTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(
                                text = String.format("%.6f", currentLng),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Monospace,
                                color = PolishedPrimaryGreen,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }


        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = PolishedCardBackground),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, BorderLightColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.BatteryChargingFull,
                                contentDescription = "Active optimization indicator icon",
                                tint = PolishedPrimaryGreen,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "Battery & Adaptive Loop",
                                    fontWeight = FontWeight.Bold,
                                    color = PolishedTextPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Adapts updates under device power constraints",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PolishedTextMuted
                                )
                            }
                        }

                        Switch(
                            checked = isOptimizationEnabled,
                            onCheckedChange = { onToggleOptimization(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = PolishedPrimaryGreen,
                                checkedTrackColor = PolishedAccentGreen.copy(alpha = 0.6f),
                                uncheckedThumbColor = PolishedTextSecondary,
                                uncheckedTrackColor = BorderLightColor
                            ),
                            modifier = Modifier.testTag("optimization_switch")
                        )
                    }

                    HorizontalDivider(color = BorderLightColor)


                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Calculated Interval", fontSize = 10.sp, color = PolishedTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(
                                text = "${currentIntervalSeconds} seconds",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOptimizationEnabled) PolishedPrimaryGreen else PolishedTextSecondary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("Power & Data Saved", fontSize = 10.sp, color = PolishedTextMuted, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                            Text(
                                text = String.format("%.1f%% Saved", batterySavingsPct),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (batterySavingsPct > 0) BatteryGolden else PolishedTextSecondary
                            )
                        }
                    }


                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Battery Level Control:", fontSize = 11.sp, color = PolishedTextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = if (batteryPercentage > 50) Icons.Filled.BatteryFull
                                              else if (batteryPercentage > 20) Icons.Filled.BatteryChargingFull
                                              else Icons.Filled.BatteryAlert,
                                contentDescription = "Battery charge icon",
                                tint = if (batteryPercentage < 20) CrimsonError else if (batteryPercentage < 50) BatteryGolden else PolishedPrimaryGreen
                            )
                            Slider(
                                value = batteryPercentage.toFloat(),
                                onValueChange = { onSetBattery(it.toInt()) },
                                valueRange = 5f..100f,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("battery_slider"),
                                colors = SliderDefaults.colors(
                                    thumbColor = if (batteryPercentage < 20) CrimsonError else PolishedPrimaryGreen,
                                    activeTrackColor = if (batteryPercentage < 20) CrimsonError else PolishedPrimaryGreen,
                                    inactiveTrackColor = BorderLightColor
                                )
                            )
                            Text(
                                text = "${batteryPercentage}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PolishedTextPrimary,
                                modifier = Modifier.width(36.dp)
                            )
                        }
                    }


                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Active Movement Engine Profile:", fontSize = 11.sp, color = PolishedTextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            PresetButton(
                                text = "Stationary",
                                isSelected = movementState is MovementState.Stationary,
                                onClick = { onSetMovementState(MovementState.Stationary) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_stationary")
                            )
                            PresetButton(
                                text = "Walking",
                                isSelected = movementState is MovementState.Walking,
                                onClick = { onSetMovementState(MovementState.Walking) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_walking")
                            )
                            PresetButton(
                                text = "Driving",
                                isSelected = movementState is MovementState.Driving,
                                onClick = { onSetMovementState(MovementState.Driving) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_driving")
                            )
                        }
                    }
                }
            }
        }


        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClearAll,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrimsonError),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("clear_history_btn")
                        .heightIn(min = 46.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderOutlineColor)
                ) {
                    Icon(
                        imageVector = Icons.Filled.DeleteSweep, 
                        contentDescription = "Clear cache icon",
                        tint = CrimsonError,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Clear Local Database Cache", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun CustomerTrackerScreen(
    cloudRecords: List<LocationRecord>,
    currentLat: Double,
    currentLng: Double,
    currentSpeed: Double,
    batteryPercentage: Int,
    isOnline: Boolean,
    currentPackageId: String,
    onClearAll: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Card(
            modifier = Modifier
                .weight(1.3f)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE1E4D9)),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderOutlineColor)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {

                LiveDeliveryMapCanvas(
                    cloudRecords = cloudRecords,
                    currentLat = currentLat,
                    currentLng = currentLng
                )


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = PolishedCardBackground.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderLightColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Badge(
                                containerColor = if (isOnline) PolishedPrimaryGreen else BatteryGolden,
                                modifier = Modifier.size(6.dp)
                            )
                            Text(
                                text = if (isOnline) "Live Streaming Active" else "Reconnecting/Offline",
                                fontSize = 11.sp,
                                color = PolishedTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(
                        onClick = onClearAll,
                        modifier = Modifier
                            .size(36.dp)
                            .background(PolishedCardBackground.copy(alpha = 0.9f), CircleShape)
                            .border(1.dp, BorderLightColor, CircleShape)
                            .testTag("reset_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Reset Location map",
                            tint = PolishedPrimaryGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }


            }
        }


        Card(
            modifier = Modifier.weight(1f),
            colors = CardDefaults.cardColors(containerColor = SecondaryCardBackground),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, BorderOutlineColor)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Text(
                            text = "PACKAGE ID: $currentPackageId",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextMuted,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (currentSpeed > 0) "Arriving in 14m" else "Delayed (Paused)",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PolishedAccentGreen)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "On Time",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF042100)
                        )
                    }
                }

                HorizontalDivider(color = BorderLightColor)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BorderLightColor)
                            .border(1.dp, BorderOutlineColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "HS",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishedPrimaryGreen
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hitesh Shewale",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = PolishedTextPrimary
                        )
                        Text(
                            text = "Professional Driver • +91 8261987733",
                            fontSize = 11.sp,
                            color = PolishedTextMuted
                        )
                    }


                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(PolishedPrimaryGreen)
                            .clickable { /* Active call simulation */ },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Call,
                            contentDescription = "Call Hitesh",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                HorizontalDivider(color = BorderLightColor)


                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val step1 = true
                    val step2 = cloudRecords.isNotEmpty()
                    val step3 = cloudRecords.size > 8

                    checkpointIndicator("Picked Up", checked = step1, color = PolishedPrimaryGreen)
                    checkpointLine(checked = step2, color = PolishedPrimaryGreen)
                    checkpointIndicator("In Transit", checked = step2, color = PolishedPrimaryGreen)
                    checkpointLine(checked = step3, color = PolishedPrimaryGreen)
                    checkpointIndicator("Near You", checked = step3, color = PolishedPrimaryGreen)
                }


                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(PolishedCardBackground, RoundedCornerShape(12.dp))
                        .border(1.dp, BorderLightColor, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Telemetry Speed:",
                        fontSize = 11.sp,
                        color = PolishedTextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = String.format("%.1f km/h", currentSpeed),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PolishedPrimaryGreen
                    )
                }
            }
        }
    }
}

@Composable
fun checkpointIndicator(label: String, checked: Boolean, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(if (checked) color else BorderOutlineColor.copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check, 
                    contentDescription = "Check icon representing completed step", 
                    tint = Color.White, 
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Box(modifier = Modifier.size(4.dp).background(PolishedTextSecondary, CircleShape))
            }
        }
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (checked) PolishedTextPrimary else PolishedTextMuted
        )
    }
}

@Composable
fun checkpointLine(checked: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .width(36.dp)
            .height(2.dp)
            .background(if (checked) color else BorderOutlineColor.copy(alpha = 0.4f))
    )
}

@Composable
fun LiveDeliveryMapCanvas(
    cloudRecords: List<LocationRecord>,
    currentLat: Double,
    currentLng: Double
) {

    val transition = rememberInfiniteTransition(label = "pulse")
    val pulseSize by transition.animateFloat(
        initialValue = 12f,
        targetValue = 40f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        if (width == 0f || height == 0f) return@Canvas



        val latMin = 18.300000
        val latMax = 19.300000
        val lngMin = 72.600000
        val lngMax = 74.000000

        fun getCanvasCoord(lat: Double, lng: Double): Offset {
            val pctX = ((lng - lngMin) / (lngMax - lngMin)).coerceIn(0.0, 1.0)
            val pctY = (1.0 - ((lat - latMin) / (latMax - latMin))).coerceIn(0.0, 1.0)
            return Offset((pctX * width).toFloat(), (pctY * height).toFloat())
        }


        val coastPath = Path().apply {
            moveTo(0f, 0f)
            cubicTo(
                width * 0.16f, height * 0.25f,
                width * 0.19f, height * 0.50f,
                width * 0.14f, height * 0.75f
            )
            lineTo(width * 0.10f, height)
            lineTo(0f, height)
            close()
        }
        drawPath(path = coastPath, color = Color(0xFFD4EBF7))


        val waveColor = Color(0xFFA2CEDF)
        drawLine(color = waveColor, start = Offset(width * 0.04f, height * 0.25f), end = Offset(width * 0.07f, height * 0.27f), strokeWidth = 3f)
        drawLine(color = waveColor, start = Offset(width * 0.03f, height * 0.65f), end = Offset(width * 0.06f, height * 0.67f), strokeWidth = 3f)


        val mountainColor = Color(0xFFCAD8C5)
        for (i in 0..4) {
            val peakY = height * (0.18f + i * 0.18f)
            val peakX = width * 0.56f + (if (i % 2 == 0) -10f else 10f)
            val mountainPath = Path().apply {
                moveTo(peakX, peakY - 16f)
                lineTo(peakX - 20f, peakY + 16f)
                lineTo(peakX + 20f, peakY + 16f)
                close()
            }
            drawPath(path = mountainPath, color = mountainColor)
        }


        val mub = getCanvasCoord(19.0760, 72.8777)
        val navi = getCanvasCoord(19.0330, 73.0297)
        val lonav = getCanvasCoord(18.7541, 73.4024)
        val pune = getCanvasCoord(18.5204, 73.8567)

        val pointsList = listOf(mub, navi, lonav, pune)


        for (i in 0 until pointsList.size - 1) {
            drawLine(
                color = Color(0xFFC3C8BB),
                start = pointsList[i],
                end = pointsList[i+1],
                strokeWidth = 14f
            )
        }

        for (i in 0 until pointsList.size - 1) {
            drawLine(
                color = Color(0xFFFCD34D),
                start = pointsList[i],
                end = pointsList[i+1],
                strokeWidth = 3f
            )
        }


        for (i in -2..2) {
            drawLine(Color.White, Offset(mub.x - 30, mub.y + i * 12), Offset(mub.x + 30, mub.y + i * 12), 3f)
            drawLine(Color.White, Offset(mub.x + i * 12, mub.y - 30), Offset(mub.x + i * 12, mub.y + 30), 3f)
        }
        for (i in -2..2) {
            drawLine(Color.White, Offset(pune.x - 30, pune.y + i * 12), Offset(pune.x + 30, pune.y + i * 12), 3f)
            drawLine(Color.White, Offset(pune.x + i * 12, pune.y - 30), Offset(pune.x + i * 12, pune.y + 30), 3f)
        }


        drawCircle(
            color = Color(0xFF0369A1),
            radius = 12f,
            center = mub
        )

        drawCircle(
            color = Color(0xFF386A20),
            radius = 12f,
            center = pune
        )


        drawCircle(
            color = Color(0xFFD97706),
            radius = 6f,
            center = lonav
        )


        if (cloudRecords.size > 1) {
            val pathPoints = cloudRecords.map { getCanvasCoord(it.latitude, it.longitude) }
            for (i in 0 until pathPoints.size - 1) {
                drawLine(
                    color = Color(0xFF386A20),
                    start = pathPoints[i],
                    end = pathPoints[i+1],
                    strokeWidth = 6f
                )
            }

            pathPoints.forEach { pt ->
                drawCircle(
                    color = Color(0xFF386A20),
                    radius = 4f,
                    center = pt
                )
            }
        }


        val courierPos = getCanvasCoord(currentLat, currentLng)
        

        drawCircle(
            color = Color(0xFF0369A1).copy(alpha = pulseAlpha),
            radius = pulseSize,
            center = courierPos
        )


        drawCircle(
            color = Color(0xFF0369A1),
            radius = 8f,
            center = courierPos
        )


        val textPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#191C17")
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        val rightAlignTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#191C17")
            textSize = 24f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
            textAlign = android.graphics.Paint.Align.RIGHT
        }

        val labelPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#42493F")
            textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.NORMAL)
        }

        val seaTextPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.parseColor("#0284C7")
            textSize = 20f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }


        drawContext.canvas.nativeCanvas.drawText("ARABIAN SEA", width * 0.02f, height * 0.45f, seaTextPaint)
        drawContext.canvas.nativeCanvas.drawText("WESTERN GHATS", width * 0.52f, height * 0.95f, labelPaint)


        drawContext.canvas.nativeCanvas.drawText("MUMBAI WAREHOUSE (HQ)", mub.x + 18f, mub.y - 12f, textPaint)
        drawContext.canvas.nativeCanvas.drawText("PUNE TERMINAL (DEST)", pune.x - 18f, pune.y + 32f, rightAlignTextPaint)
        drawContext.canvas.nativeCanvas.drawText("Lonavala", lonav.x + 12f, lonav.y - 8f, labelPaint)
        drawContext.canvas.nativeCanvas.drawText("Navi Mumbai", navi.x + 12f, navi.y + 14f, labelPaint)
    }
}

@Composable
fun LogConsolePanel(
    viewLogs: List<String>,
    darkCardBackground: Color,
    skyColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(horizontal = 12.dp)
            .testTag("log_panel"),
        colors = CardDefaults.cardColors(containerColor = PolishedCardBackground),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        border = BorderStroke(1.dp, BorderLightColor)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(modifier = Modifier.size(6.dp).background(PolishedPrimaryGreen, CircleShape))
                    Text(
                        text = "RECENT ACTIVITY LOGS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold, 
                            letterSpacing = 1.sp
                        ),
                        color = PolishedTextMuted
                    )
                }
                Text(
                    text = "LIVE FEED", 
                    fontSize = 8.sp, 
                    color = PolishedPrimaryGreen, 
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
            }

            HorizontalDivider(color = BorderLightColor)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(viewLogs) { log ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        val barColor = if (log.contains("Sync")) PolishedPrimaryGreen 
                                       else if (log.contains("Error")) CrimsonError
                                       else if (log.contains("Firebase")) PolishedPrimaryGreen
                                       else BorderOutlineColor

                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(22.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(barColor)
                        )

                        Text(
                            text = log,
                            color = PolishedTextPrimary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PresetButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isSelected) PolishedPrimaryGreen else SecondaryCardBackground
    val labelColor = if (isSelected) Color.White else PolishedTextSecondary
    val borderStroke = if (isSelected) null else BorderStroke(1.dp, BorderLightColor)

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .then(if (borderStroke != null) Modifier.border(borderStroke, RoundedCornerShape(12.dp)) else Modifier)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = labelColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
