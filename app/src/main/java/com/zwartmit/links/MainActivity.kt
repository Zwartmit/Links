package com.zwartmit.links

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

class MainActivity : ComponentActivity() {
    private var detectedLinks by mutableStateOf<ArrayList<String>?>(null)
    
    companion object {
        const val DETECTED_LINKS_EXTRA = "DETECTED_LINKS"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
        setContent {
            LinkScannerApp(detectedLinks)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        setContent {
            LinkScannerApp(detectedLinks)
        }
    }

    private fun handleIntent(intent: Intent?) {
        intent?.let {
            val links = it.getStringArrayListExtra(DETECTED_LINKS_EXTRA)
            if (links != null && links.isNotEmpty()) {
                detectedLinks = links
                
                // Condición A: Si hay exactamente 1 enlace, abrir directamente el navegador
                if (links.size == 1) {
                    val url = links[0]
                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    startActivity(browserIntent)
                    finish()
                }
                // Condición B: Si hay múltiples enlaces, mostrar lista en la UI
                // La UI se actualizará automáticamente a través del estado detectedLinks
            }
        }
    }
}

@Composable
fun LinkScannerApp(detectedLinks: ArrayList<String>? = null) {
    val context = LocalContext.current
    var isServiceEnabled by remember { mutableStateOf(false) }
    
    // Verificar el estado del servicio
    LaunchedEffect(Unit) {
        val am = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        isServiceEnabled = enabledServices.any { it.resolveInfo.serviceInfo.name == LinkScannerService::class.java.name }
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A)),
            contentAlignment = Alignment.Center
        ) {
            if (detectedLinks != null && detectedLinks.size > 1) {
                // Mostrar lista de enlaces detectados
                LinkListScreen(detectedLinks)
            } else {
                // Mostrar pantalla principal
                MainScreen(isServiceEnabled)
            }
        }
    }
}

@Composable
fun MainScreen(isServiceEnabled: Boolean) {
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

@Composable
fun LinkListScreen(links: ArrayList<String>) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Enlaces Detectados",
            color = Color(0xFF22d3ee),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(links) { url ->
                LinkItem(url) {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            }
        }
    }
}

@Composable
fun LinkItem(url: String, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1a1a1a)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = url,
                color = Color(0xFF22d3ee),
                fontSize = 14.sp,
                fontFamily = FontFamily.Monospace,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Pulsa para abrir",
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}