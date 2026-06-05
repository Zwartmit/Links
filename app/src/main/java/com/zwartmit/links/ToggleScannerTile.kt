package com.zwartmit.links

import android.content.Intent
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo

class ToggleScannerTile : TileService() {

    override fun onClick() {
        super.onClick()
        
        // Check if accessibility service is enabled
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val isServiceEnabled = enabledServices.any { 
            it.resolveInfo.serviceInfo.name == LinkScannerService::class.java.name 
        }
        
        if (isServiceEnabled) {
            // Service is enabled, open MainActivity
            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        } else {
            // Service is disabled, navigate to accessibility settings
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityAndCollapse(intent)
        }
    }

    override fun onStartListening() {
        super.onStartListening()
        
        // Check if accessibility service is enabled using AccessibilityManager
        val am = getSystemService(ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC)
        val isServiceEnabled = enabledServices.any { 
            it.resolveInfo.serviceInfo.name == LinkScannerService::class.java.name 
        }
        
        qsTile.state = if (isServiceEnabled) {
            Tile.STATE_ACTIVE
        } else {
            Tile.STATE_INACTIVE
        }
        qsTile.updateTile()
    }
}