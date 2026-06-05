package com.zwartmit.links

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast
import java.util.regex.Pattern
import java.util.HashSet

class LinkScannerService : AccessibilityService() {

    companion object {
        private const val TAG = "LinkScannerService"
        private val URL_PATTERN = Pattern.compile(
            "https?://[a-zA-Z0-9./?=_-]+"
        )
    }

    private var isServiceActive = false
    private val detectedUrls = HashSet<String>()

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        try {
            event ?: return
            
            when (event.eventType) {
                AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
                AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                    if (isServiceActive) {
                        scanForUrls(event.source)
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
                // Anti-spam: only show toast if URL hasn't been detected recently
                if (!detectedUrls.contains(url)) {
                    detectedUrls.add(url)
                    Log.d(TAG, "Found URL: $url")
                    
                    // Show visual feedback
                    Toast.makeText(applicationContext, "Enlace detectado: $url", Toast.LENGTH_SHORT).show()
                    
                    // Remove URL from set after 5 seconds to allow detection again
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        detectedUrls.remove(url)
                    }, 5000)
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

    fun setServiceActive(active: Boolean) {
        isServiceActive = active
        Log.d(TAG, "Service active: $active")
    }
}