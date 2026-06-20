package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.InspectionStatus
import com.example.data.model.Vehicle
import com.example.data.repository.VehicleRepository
import com.example.util.DateUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class FleetStats(
    val totalCount: Int = 0,
    val overdueCount: Int = 0,
    val endingSoonCount: Int = 0,
    val okCount: Int = 0
)

class VehicleViewModel(private val repository: VehicleRepository) : ViewModel() {

    // Reference date for inspection alerts
    // Default to 2026-06-18 to match the user's dataset timeframe perfectly
    private val _referenceDate = MutableStateFlow(LocalDate.of(2026, 6, 18))
    val referenceDate: StateFlow<LocalDate> = _referenceDate.asStateFlow()

    // Filters UI State
    val searchQuery = MutableStateFlow("")
    val selectedTypeFilter = MutableStateFlow<String?> (null) // "Ciągnik", "Naczepa" or null
    val selectedStatusFilter = MutableStateFlow<InspectionStatus?> (null)

    // Raw vehicles flow from Room
    val allVehicles: StateFlow<List<Vehicle>> = repository.allVehiclesFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Derived filtered list of vehicles
    val filteredVehicles: StateFlow<List<Vehicle>> = combine(
        allVehicles,
        searchQuery,
        selectedTypeFilter,
        selectedStatusFilter,
        referenceDate
    ) { vehicles, query, typeFilter, statusFilter, refDate ->
        vehicles.filter { vehicle ->
            // Search filter (number plates or model)
            val matchesQuery = query.isBlank() ||
                    vehicle.registrationNumber.contains(query, ignoreCase = true) ||
                    vehicle.brandModel.contains(query, ignoreCase = true) ||
                    vehicle.customId.contains(query, ignoreCase = true)

            // Type filter
            val matchesType = typeFilter == null || vehicle.type == typeFilter

            // Status filter
            val matchesStatus = statusFilter == null || 
                    InspectionStatus.calculate(vehicle.inspectionDate, refDate) == statusFilter

            matchesQuery && matchesType && matchesStatus
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Derived fleet stats
    val fleetStats: StateFlow<FleetStats> = combine(
        allVehicles,
        referenceDate
    ) { vehicles, refDate ->
        var overdue = 0
        var endingSoon = 0
        var ok = 0
        vehicles.forEach { vehicle ->
            when (InspectionStatus.calculate(vehicle.inspectionDate, refDate)) {
                InspectionStatus.OVERDUE -> overdue++
                InspectionStatus.ENDING_SOON -> endingSoon++
                InspectionStatus.OK -> ok++
            }
        }
        FleetStats(
            totalCount = vehicles.size,
            overdueCount = overdue,
            endingSoonCount = endingSoon,
            okCount = ok
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FleetStats())

    fun updateReferenceDate(newDate: LocalDate) {
        _referenceDate.value = newDate
    }

    fun addVehicle(
        customId: String,
        type: String,
        registrationNumber: String,
        brandModel: String,
        currentMileage: Int?,
        inspectionDate: String
    ) {
        viewModelScope.launch {
            // Ensure date is saved as ISO
            val parsed = DateUtils.parseDate(inspectionDate)
            val isoDate = if (parsed != null) DateUtils.formatDateToIso(parsed) else inspectionDate
            
            val newVehicle = Vehicle(
                customId = customId.trim(),
                type = type,
                registrationNumber = registrationNumber.trim().uppercase(),
                brandModel = brandModel.trim(),
                currentMileage = currentMileage,
                inspectionDate = isoDate
            )
            repository.insertVehicle(newVehicle)
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
        }
    }

    fun renewInspectionOneYear(vehicle: Vehicle) {
        viewModelScope.launch {
            val parsedDate = DateUtils.parseDate(vehicle.inspectionDate) ?: LocalDate.now()
            val renewedDate = parsedDate.plusYears(1)
            val updatedVehicle = vehicle.copy(
                inspectionDate = DateUtils.formatDateToIso(renewedDate)
            )
            repository.updateVehicle(updatedVehicle)
        }
    }

    fun resetToDefaults() {
        viewModelScope.launch {
            val defaults = listOf(
                Vehicle(
                    customId = "T1",
                    type = "Ciągnik",
                    registrationNumber = "PO 12345",
                    brandModel = "Scania R450",
                    currentMileage = 452000,
                    inspectionDate = "2026-05-10"
                ),
                Vehicle(
                    customId = "T2",
                    type = "Ciągnik",
                    registrationNumber = "PO 67890",
                    brandModel = "DAF XF 480",
                    currentMileage = 210500,
                    inspectionDate = "2026-06-25"
                ),
                Vehicle(
                    customId = "N1",
                    type = "Naczepa",
                    registrationNumber = "PO 999AA",
                    brandModel = "Schmitz Cargobull",
                    currentMileage = null,
                    inspectionDate = "2026-09-12"
                ),
                Vehicle(
                    customId = "N2",
                    type = "Naczepa",
                    registrationNumber = "PO 888BB",
                    brandModel = "Krone Firanka",
                    currentMileage = null,
                    inspectionDate = "2026-04-01"
                ),
                Vehicle(
                    customId = "153682b2",
                    type = "Naczepa",
                    registrationNumber = "PO4545",
                    brandModel = "MAN",
                    currentMileage = 890000,
                    inspectionDate = "2026-06-28"
                ),
                Vehicle(
                    customId = "da4b2865",
                    type = "Naczepa",
                    registrationNumber = "PO9898",
                    brandModel = "Krone",
                    currentMileage = 12234,
                    inspectionDate = "2026-06-17"
                )
            )
            repository.resetToDefault(defaults)
        }
    }
}

class VehicleViewModelFactory(private val repository: VehicleRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(VehicleViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return VehicleViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
