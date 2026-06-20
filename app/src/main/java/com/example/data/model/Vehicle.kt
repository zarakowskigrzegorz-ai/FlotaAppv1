package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customId: String,          // e.g. T1, T2, 153682b2
    val type: String,              // "Ciągnik" or "Naczepa"
    val registrationNumber: String, // e.g. PO 12345
    val brandModel: String,        // e.g. Scania R450
    val currentMileage: Int?,       // Optional mileage (km), e.g. 452000
    val inspectionDate: String     // ISO formatted date (YYYY-MM-DD), e.g. 2026-05-10
)
