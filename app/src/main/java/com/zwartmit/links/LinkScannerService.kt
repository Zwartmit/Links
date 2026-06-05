package com.zwartmit.links

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.os.Build
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import androidx.core.app.NotificationCompat
import java.util.regex.Pattern
import java.util.HashSet

class LinkScannerService : AccessibilityService() {

    companion object {
        private const val TAG = "LinkScannerService"
        private const val CHANNEL_ID = "link_scanner_alerts"
        private const val NOTIFICATION_ID = 1
        private const val DETECTED_LINKS_EXTRA = "DETECTED_LINKS"
        private val URL_PATTERN = Pattern.compile(
            "https?://[a-zA-Z0-9./?=_-]+"
        )
    }

    private var isServiceActive = false
    private val detectedUrls = HashSet<String>()
    private val newUrlsBuffer = ArrayList<String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event ?: return
            
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (isServiceActive) {
                        // Clear buffer before scanning
                        newUrlsBuffer.clear()
                        scanForUrls(event.source)
                        
                        // Show notification if new URLs were detected
                        if (newUrlsBuffer.isNotEmpty()) {
                            showLinkNotification(newUrlsBuffer)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Scanner", "Error in onAccessibilityEvent", e)
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        try {
            super.onServiceConnected()
            Log.d(TAG, "Accessibility service connected")
            isServiceActive = true
            createNotificationChannel()
        } catch (e: Exception) {
            Log.e("Scanner", "Error in onServiceConnected", e)
        }
    }

    override fun onDestroy() {
        try {
            super.onDestroy()
            Log.d(TAG, "Accessibility service destroyed")
            isServiceActive = false
            detectedUrls.clear()
            newUrlsBuffer.clear()
        } catch (e: Exception) {
            Log.e("Scanner", "Error in onDestroy", e)
        }
    }

    private fun scanForUrls(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo ?: return
        
        traverseNode(nodeInfo)
    }

    private fun traverseNode(node: AccessibilityNodeInfo?) {
        node ?: return
        
        // Extract text and contentDescription from current node
        val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
        
        if (text.isNotBlank()) {
            val urls = extractUrls(text)
            for (url in urls) {
                // Anti-spam: only add URL if it hasn't been detected recently
                if (!detectedUrls.contains(url)) {
                    detectedUrls.add(url)
                    newUrlsBuffer.add(url)
                    Log.d(TAG, "Found URL: $url")
                    
                    // Remove URL from set after 10 seconds to allow detection again
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        detectedUrls.remove(url)
                    }, 10000)
                }
            }
        }
        
        // Recursively traverse child nodes
        for (i in 0 until node.childCount) {
            val child = node.getChild(i)
            traverseNode(child)
        }
    }

    private fun extractUrls(text: String): List<String> {
        val urls = mutableListOf<String>()
        val matcher = URL_PATTERN.matcher(text)
        
        while (matcher.find()) {
            urls.add(matcher.group())
        }
        
        return urls
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Link Scanner Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alertas cuando se detectan enlaces en pantalla"
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showLinkNotification(urls: ArrayList<String>) {
        val intent = Intent(this, MainActivity::class.java).apply {
            putStringArrayListExtra(DETECTED_LINKS_EXTRA, urls)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Enlace(s) detectado(s)")
            .setContentText("Pulsa aquí para acceder")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun setServiceActive(active: Boolean) {
        isServiceActive = active
        Log.d(TAG, "Service active: $active")
    }
}