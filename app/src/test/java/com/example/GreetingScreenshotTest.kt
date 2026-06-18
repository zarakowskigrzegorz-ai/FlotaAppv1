package com.example

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.database.AppDatabase
import com.example.data.repository.VehicleRepository
import com.example.ui.screens.FleetDashboardScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.VehicleViewModel
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

    @get:Rule val composeTestRule = createComposeRule()

    private lateinit var database: AppDatabase
    private lateinit var repository: VehicleRepository
    private lateinit var viewModel: VehicleViewModel

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = VehicleRepository(database.vehicleDao())
        viewModel = VehicleViewModel(repository)
        
        // Let's prepopulate immediately in JVM test thread for predictable screenshot
        viewModel.resetToDefaults()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun fleet_dashboard_screenshot() {
        composeTestRule.setContent {
            MyApplicationTheme {
                FleetDashboardScreen(
                    viewModel = viewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
    }
}
