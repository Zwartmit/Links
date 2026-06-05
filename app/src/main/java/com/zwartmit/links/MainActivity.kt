package com.zwartmit.links

import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LinkScannerApp()
        }
    }

    override fun onResume() {
        super.onResume()
        // Verificar el estado del servicio usando AccessibilityManager
        val accessibilityManager = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val serviceEnabled = accessibilityManager.isEnabled && 
            isAccessibilityServiceEnabled(this)
        
        // Actualizar el estado de la UI
        setContent {
            LinkScannerApp(serviceEnabled)
        }
    }
}

@Composable
fun LinkScannerApp(initialServiceEnabled: Boolean? = null) {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(initialServiceEnabled ?: isAccessibilityServiceEnabled(context)) }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Link Scanner",
                    color = Color(0xFF22d3ee),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isServiceEnabled) 
                            Color(0xFF22d3ee).copy(alpha = 0.1f) 
                        else 
                            Color(0xFF333333)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Estado del Servicio",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        Text(
                            text = if (isServiceEnabled) "Activo" else "Inactivo",
                            color = if (isServiceEnabled) Color(0xFF22d3ee) else Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "Activa el servicio desde Configuración Rápida",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
    }
}

private fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val expectedComponentName = "${context.packageName}/.LinkScannerService"
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
    )
    return enabledServices?.contains(expectedComponentName) == true
}