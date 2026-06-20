package com.example.data.dao

import androidx.room.*
import com.example.data.model.Vehicle
import kotlinx.coroutines.flow.Flow

@Dao
interface VehicleDao {
    @Query("SELECT * FROM vehicles ORDER BY inspectionDate ASC")
    fun getAllVehiclesFlow(): Flow<List<Vehicle>>

    @Query("SELECT * FROM vehicles ORDER BY inspectionDate ASC")
    suspend fun getAllVehicles(): List<Vehicle>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicle(vehicle: Vehicle)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVehicles(vehicles: List<Vehicle>)

    @Update
    suspend fun updateVehicle(vehicle: Vehicle)

    @Delete
    suspend fun deleteVehicle(vehicle: Vehicle)

    @Query("DELETE FROM vehicles WHERE id = :id")
    suspend fun deleteVehicleById(id: Int)

    @Query("DELETE FROM vehicles")
    suspend fun clearAll()
}
