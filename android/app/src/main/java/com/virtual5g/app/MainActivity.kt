package com.virtual5g.app

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.virtual5g.presentation.navigation.Destination
import com.virtual5g.presentation.navigation.Virtual5GNavGraph
import com.virtual5g.presentation.theme.Virtual5GTheme

class MainActivity : ComponentActivity() {

    private val requestPhoneStatePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Granted or denied - DeviceRepository re-checks on next read either way. */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val container = (application as Virtual5GApplication).container
        container.onRequestTelephonyPermission = {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                "android.permission.READ_BASIC_PHONE_STATE"
            } else {
                Manifest.permission.READ_PHONE_STATE
            }
            requestPhoneStatePermission.launch(permission)
        }

        val startDestination = if (container.hasTelephonyPermission()) {
            Destination.Dashboard.route
        } else {
            Destination.Onboarding.route
        }

        setContent {
            Virtual5GTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    Virtual5GNavGraph(
                        navController = navController,
                        dependencies = container,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
