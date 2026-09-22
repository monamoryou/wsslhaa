package com.example.wasselha

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.MainActivity
import com.example.ui.account.AccountSelectionScreen
import com.example.ui.theme.MyApplicationTheme

/**
 * RoleSelectionActivity matching com.example.wasselha.RoleSelectionActivity
 * Handles:
 * - cardBuyer click -> navigate to registration/app with role "buyer"
 * - cardPartners click -> bottom sheet with layoutSeller & layoutDriver -> navigate with "seller" or "driver"
 */
class RoleSelectionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                AccountSelectionScreen(
                    onTypeSelected = { type ->
                        val role = when (type) {
                            "BUYER" -> "buyer"
                            "MERCHANT" -> "seller"
                            "DELIVERY" -> "driver"
                            else -> "buyer"
                        }
                        navigateToRegister(role)
                    },
                    onGoogleSignInClick = {
                        navigateToRegister("buyer")
                    }
                )
            }
        }
    }

    private fun navigateToRegister(role: String) {
        val intent = Intent(this, RegisterActivity::class.java).apply {
            putExtra("USER_ROLE", role)
        }
        startActivity(intent)
    }
}
