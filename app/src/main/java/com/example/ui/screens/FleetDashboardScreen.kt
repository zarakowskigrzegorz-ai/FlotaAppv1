package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.InspectionStatus
import com.example.data.model.Vehicle
import com.example.ui.theme.*
import com.example.ui.viewmodel.FleetStats
import com.example.ui.viewmodel.VehicleViewModel
import com.example.util.DateUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FleetDashboardScreen(
    viewModel: VehicleViewModel,
    modifier: Modifier = Modifier
) {
    val vehicles by viewModel.filteredVehicles.collectAsStateWithLifecycle()
    val stats by viewModel.fleetStats.collectAsStateWithLifecycle()
    val refDate by viewModel.referenceDate.collectAsStateWithLifecycle()
    
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedType by viewModel.selectedTypeFilter.collectAsStateWithLifecycle()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }
    var showSimulationMenu by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Flota i Przeglądy",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = HardText
                        )
                        Text(
                            text = "Monitoring terminów pr. rejestracyjnych",
                            fontSize = 12.sp,
                            color = InactiveText
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showSimulationMenu = !showSimulationMenu },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Opcje",
                            tint = HardText
                        )
                    }

                    DropdownMenu(
                        expanded = showSimulationMenu,
                        onDismissRequest = { showSimulationMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Resetuj do danych MVP (CSV)") },
                            leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                            onClick = {
                                viewModel.resetToDefaults()
                                showSimulationMenu = false
                            },
                            modifier = Modifier.testTag("reset_to_mvp")
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Symulacja: Maj 2026") },
                            onClick = {
                                viewModel.updateReferenceDate(LocalDate.of(2026, 5, 10))
                                showSimulationMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Symulacja: Czerwiec 2026 (MVP)") },
                            onClick = {
                                viewModel.updateReferenceDate(LocalDate.of(2026, 6, 18))
                                showSimulationMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Symulacja: Dzisiejsza data") },
                            onClick = {
                                viewModel.updateReferenceDate(LocalDate.now())
                                showSimulationMenu = false
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ProfessionalBg
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = LavenderActive,
                contentColor = LavenderText,
                modifier = Modifier
                    .testTag("add_vehicle_fab")
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Dodaj Pojazd")
            }
        },
        containerColor = ProfessionalBg,
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .navigationBarsPadding() // Handled correctly in Compose
        ) {
            // Simulation Reference Date Indicator Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Symulacja stanu na dzień: ${refDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "Zmień",
                        color = LavenderActive,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showSimulationMenu = true }
                            .padding(4.dp)
                    )
                }
            }

            // Stats row cards
            StatsSelectorRow(
                stats = stats,
                selectedStatus = selectedStatus,
                onStatusSelect = { status ->
                    viewModel.selectedStatusFilter.value = if (viewModel.selectedStatusFilter.value == status) null else status
                }
            )

            // Search and Filters layout
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Szukaj pojazdu (nr rej, model, ID)...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = InactiveText) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                Icon(Icons.Default.Delete, contentDescription = "Wyczyść tekst", tint = InactiveText)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_field"),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentPurple,
                        unfocusedBorderColor = ProfessionalBorder,
                        focusedContainerColor = NeutralBg,
                        unfocusedContainerColor = NeutralBg,
                        focusedLabelColor = AccentPurple
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Vehicle Type Segmented Control
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        null to "Wszystkie",
                        "Ciągnik" to "Ciągniki 🚛",
                        "Naczepa" to "Naczepy 🛞"
                    ).forEach { (typeVal, label) ->
                        val isSelected = selectedType == typeVal
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectedTypeFilter.value = typeVal },
                            label = { Text(label, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentPurpleBg,
                                selectedLabelColor = AccentPurpleText,
                                containerColor = NeutralBg,
                                labelColor = InactiveText
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = if (isSelected) Color.Transparent else ProfessionalBorder,
                                selectedBorderColor = Color.Transparent,
                                borderWidth = 1.dp,
                                selectedBorderWidth = 1.dp
                            ),
                            modifier = Modifier.testTag("type_filter_${typeVal ?: "all"}")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Active Filters Status Info
            if (selectedType != null || selectedStatus != null || searchQuery.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Wyniki wyszukiwania (${vehicles.size}):",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = InactiveText
                    )
                    Text(
                        text = "Wyczyść filtry",
                        fontSize = 12.sp,
                        color = AccentPurple,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable {
                                viewModel.selectedTypeFilter.value = null
                                viewModel.selectedStatusFilter.value = null
                                viewModel.searchQuery.value = ""
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Vehicles list or Empty State
            if (vehicles.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = InactiveText,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Brak pasujących pojazdów",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = HardText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Wyczyść filtry wyszukiwania lub kliknij przycisk +, aby dodać nowy pojazd do floty.",
                            fontSize = 13.sp,
                            color = InactiveText,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("vehicles_list"),
                    contentPadding = PaddingValues(bottom = 80.dp) // Leave safety block for FAB
                ) {
                    items(vehicles, key = { it.id }) { vehicle ->
                        VehicleCard(
                            vehicle = vehicle,
                            referenceDate = refDate,
                            onRenewOneYear = { viewModel.renewInspectionOneYear(vehicle) },
                            onEdit = { vehicleToEdit = vehicle },
                            onDelete = { viewModel.deleteVehicle(vehicle) }
                        )
                    }
                }
            }
        }
    }

    // Add Vehicle Dialog Window
    if (showAddDialog) {
        AddEditVehicleDialog(
            vehicleToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { cid, type, reg, model, mileage, date ->
                viewModel.addVehicle(cid, type, reg, model, mileage, date)
                showAddDialog = false
            }
        )
    }

    // Edit Vehicle Dialog Window
    vehicleToEdit?.let { vehicle ->
        AddEditVehicleDialog(
            vehicleToEdit = vehicle,
            onDismiss = { vehicleToEdit = null },
            onSave = { cid, type, reg, model, mileage, date ->
                viewModel.updateVehicle(
                    vehicle.copy(
                        customId = cid,
                        type = type,
                        registrationNumber = reg,
                        brandModel = model,
                        currentMileage = mileage,
                        inspectionDate = date
                    )
                )
                vehicleToEdit = null
            }
        )
    }
}

@Composable
fun StatsSelectorRow(
    stats: FleetStats,
    selectedStatus: InspectionStatus?,
    onStatusSelect: (InspectionStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp)
            .height(78.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Total stats
        StatBadgeCard(
            title = "Wszystkie",
            count = stats.totalCount.toString(),
            colorAccent = AccentPurple,
            isSelected = selectedStatus == null,
            onClick = { onStatusSelect(null) },
            modifier = Modifier
                .weight(1f)
                .testTag("stat_filter_all")
        )

        // Overdue status
        StatBadgeCard(
            title = "Po terminie",
            count = stats.overdueCount.toString(),
            colorAccent = RedOverdue,
            isSelected = selectedStatus == InspectionStatus.OVERDUE,
            onClick = { onStatusSelect(InspectionStatus.OVERDUE) },
            modifier = Modifier
                .weight(1f)
                .testTag("stat_filter_overdue")
        )

        // Ending soon status
        StatBadgeCard(
            title = "Kończy się",
            count = stats.endingSoonCount.toString(),
            colorAccent = AmberSoon,
            isSelected = selectedStatus == InspectionStatus.ENDING_SOON,
            onClick = { onStatusSelect(InspectionStatus.ENDING_SOON) },
            modifier = Modifier
                .weight(1f)
                .testTag("stat_filter_soon")
        )

        // OK status
        StatBadgeCard(
            title = "W normie",
            count = stats.okCount.toString(),
            colorAccent = GreenOk,
            isSelected = selectedStatus == InspectionStatus.OK,
            onClick = { onStatusSelect(InspectionStatus.OK) },
            modifier = Modifier
                .weight(1f)
                .testTag("stat_filter_ok")
        )
    }
}

@Composable
fun StatBadgeCard(
    title: String,
    count: String,
    colorAccent: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) colorAccent else ProfessionalBorder
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorAccent.copy(alpha = 0.12f) else NeutralBg
        ),
        modifier = modifier
            .fillMaxHeight()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 8.dp, horizontal = 4.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = colorAccent,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = InactiveText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    referenceDate: LocalDate,
    onRenewOneYear: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val status = InspectionStatus.calculate(vehicle.inspectionDate, referenceDate)

    val (badgeBg, badgeText, statusLabel, cardBorderColor) = when (status) {
        InspectionStatus.OVERDUE -> Quadruple(RedOverdueBg, RedOverdue, "🔴 PO TERMINIE!", RedOverdue.copy(alpha = 0.3f))
        InspectionStatus.ENDING_SOON -> Quadruple(AmberSoonBg, AmberSoon, "🟡 KOŃCZY SIĘ", AmberSoon.copy(alpha = 0.3f))
        InspectionStatus.OK -> Quadruple(GreenOkBg, GreenOk, "🟢 OK", ProfessionalBorder)
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, if (expanded) AccentPurple else cardBorderColor),
        colors = CardDefaults.cardColors(containerColor = NeutralBg),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { expanded = !expanded }
            .testTag("vehicle_card_${vehicle.customId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Vehicle custom avatar badge: Left indicator box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (vehicle.type == "Ciągnik") "🚛" else "🛞",
                            fontSize = 18.sp
                        )
                        Text(
                            text = vehicle.customId,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Mid detail column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = vehicle.brandModel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = HardText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(AmethystBg)
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                               text = "#${vehicle.customId}",
                                fontSize = 10.sp,
                                color = InactiveText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Rej: ${vehicle.registrationNumber}",
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Mileage description
                    val mileageText = if (vehicle.currentMileage != null) {
                        "${String.format("%,d", vehicle.currentMileage).replace(',', ' ')} km"
                    } else {
                        "---"
                    }
                    Text(
                        text = "Przebieg: $mileageText",
                        fontSize = 12.sp,
                        color = InactiveText
                    )
                }

                // Right status + date column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(badgeBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeText
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Termin:",
                        fontSize = 10.sp,
                        color = InactiveText
                    )
                    Text(
                        text = vehicle.inspectionDate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (status == InspectionStatus.OVERDUE) RedOverdue else HardText
                    )
                }
            }

            // Expanded Collapsible view showing Operations Actions
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = ProfessionalBorder, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Action: RENEW REVIEW FOR 1 YEAR (extremely useful feature!)
                        Button(
                            onClick = onRenewOneYear,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = GreenOkBg,
                                contentColor = GreenOk
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GreenOk.copy(alpha = 0.4f)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("renew_button_${vehicle.customId}")
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Przedłuż o Rok",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Action: EDIT & DELETE
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onEdit,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, ProfessionalBorder),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("edit_button_${vehicle.customId}")
                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edytuj",
                                    tint = InactiveText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Edytuj", fontSize = 12.sp, color = InactiveText)
                            }

                            Button(
                                onClick = onDelete,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = RedOverdueBg,
                                    contentColor = RedOverdue
                                ),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, RedOverdue.copy(alpha = 0.4f)),
                                contentPadding = PaddingValues(horizontal = 10.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("delete_button_${vehicle.customId}")
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Usuń",
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Dialog helper component
data class Quadruple<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditVehicleDialog(
    vehicleToEdit: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (customId: String, type: String, reg: String, model: String, mileage: Int?, date: String) -> Unit
) {
    var customId by remember { mutableStateOf(vehicleToEdit?.customId ?: "") }
    var type by remember { mutableStateOf(vehicleToEdit?.type ?: "Ciągnik") }
    var registrationNumber by remember { mutableStateOf(vehicleToEdit?.registrationNumber ?: "") }
    var brandModel by remember { mutableStateOf(vehicleToEdit?.brandModel ?: "") }
    var currentMileageStr by remember { mutableStateOf(vehicleToEdit?.currentMileage?.toString() ?: "") }
    var inspectionDate by remember { mutableStateOf(vehicleToEdit?.inspectionDate ?: LocalDate.now().toString()) }

    var errorText by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            tonalElevation = 6.dp,
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = if (vehicleToEdit == null) "🚛 Nowy Pojazd we Flocie" else "✏️ Edycja Pojazdu",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = HardText
                )
                Spacer(modifier = Modifier.height(14.dp))

                // Standard Toggle buttons for vehicle type selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Ciągnik" to "🚛 Ciągnik", "Naczepa" to "🛞 Naczepa").forEach { (typeVal, label) ->
                        val isSelected = type == typeVal
                        Button(
                            onClick = { type = typeVal },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) AccentPurple else AmethystBg,
                                contentColor = if (isSelected) Color.White else HardText
                            ),
                            border = strokeBorderForToggle(isSelected),
                            modifier = Modifier
                                .weight(1f)
                                .height(42.dp)
                                .testTag("dialog_type_toggle_$typeVal")
                        ) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Custom Unique ID field
                OutlinedTextField(
                    value = customId,
                    onValueChange = { customId = it },
                    label = { Text("Identyfikator (np. T3, N3)", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_custom_id")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Registration License plate number
                OutlinedTextField(
                    value = registrationNumber,
                    onValueChange = { registrationNumber = it.uppercase() },
                    label = { Text("Numer Rejestracyjny PO XXXXX", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_reg")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Brand & Model spec
                OutlinedTextField(
                    value = brandModel,
                    onValueChange = { brandModel = it },
                    label = { Text("Marka i Model (np. Scania R450)", fontSize = 13.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_model")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Optional mileage (km)
                OutlinedTextField(
                    value = currentMileageStr,
                    onValueChange = { currentMileageStr = it },
                    label = { Text("Przebieg aktualny (km) - opcjonalnie", fontSize = 13.sp) },
                    placeholder = { Text("np. 450000") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_mileage")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Inspection date YYYY-MM-DD
                OutlinedTextField(
                    value = inspectionDate,
                    onValueChange = { inspectionDate = it },
                    label = { Text("Data Przeglądu (RRRR-MM-DD)", fontSize = 13.sp) },
                    placeholder = { Text("np. 2026-06-18") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dialog_input_date")
                )

                // Error alerts
                errorText?.let { err ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = err,
                        color = RedOverdue,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Action Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .testTag("dialog_cancel")
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("Anuluj", color = InactiveText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (customId.isBlank() || registrationNumber.isBlank() || brandModel.isBlank() || inspectionDate.isBlank()) {
                                errorText = "Wypełnij wymagane pola (id, rejestracja, model, data)"
                                return@Button
                             }
                            val parsedMileage = currentMileageStr.trim().toIntOrNull()
                            if (currentMileageStr.isNotBlank() && parsedMileage == null) {
                                errorText = "Przebieg musi być poprawną liczbą całkowitą"
                                return@Button
                            }

                            val parsedDate = DateUtils.parseDate(inspectionDate)
                            if (parsedDate == null) {
                                errorText = "Błędny format daty! Wpisz RRRR-MM-DD lub DD.MM.RRRR"
                                return@Button
                            }

                            // Let's pass validation
                            onSave(
                                customId,
                                type,
                                registrationNumber,
                                brandModel,
                                parsedMileage,
                                DateUtils.formatDateToIso(parsedDate)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                        modifier = Modifier
                            .testTag("dialog_save")
                            .minimumInteractiveComponentSize()
                    ) {
                        Text("Zapisz", color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun strokeBorderForToggle(isSelected: Boolean): BorderStroke? {
    return if (isSelected) null else BorderStroke(1.dp, ProfessionalBorder)
}
