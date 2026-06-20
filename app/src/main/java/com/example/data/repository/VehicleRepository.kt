package com.example.data.repository

import com.example.data.dao.VehicleDao
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

class VehicleRepository(private val vehicleDao: VehicleDao) {

    val allVehiclesFlow: Flow<List<Vehicle>> = vehicleDao.getAllVehiclesFlow()

    suspend fun insertVehicle(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle)
    }

    // Support updating/editing a vehicle completely
    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
    }

    suspend fun deleteVehicleById(id: Int) {
        vehicleDao.deleteVehicleById(id)
    }

    suspend fun resetToDefault(initialVehicles: List<Vehicle>) {
        vehicleDao.clearAll()
        vehicleDao.insertVehicles(initialVehicles)
    }
}
