package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.dao.VehicleDao
import com.example.data.model.Vehicle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [Vehicle::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fleet_database"
                )
                .addCallback(DatabaseCallback(context))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    prepopulateDatabase(database.vehicleDao())
                }
            }
        }

        private suspend fun prepopulateDatabase(vehicleDao: VehicleDao) {
            val initialVehicles = listOf(
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
            vehicleDao.insertVehicles(initialVehicles)
        }
    }
}
