package com.zwartmit.links

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import java.util.regex.Pattern

class LinkScannerService : AccessibilityService() {

    companion object {
        private const val TAG = "LinkScannerService"
        private val URL_PATTERN = Pattern.compile(
            "(https?://[\\w\\-]+(\\.[\\w\\-]+)+[\\w\\-\\.,@?^=%&amp;:/~\\+#]*[\\w\\-\\@?^=%&amp;/~\\+#])"
        )
    }

    private var isServiceActive = false

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        
        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                if (isServiceActive) {
                    scanForUrls(event.source)
                }
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.d(TAG, "Accessibility service connected")
        isServiceActive = true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "Accessibility service destroyed")
        isServiceActive = false
    }

    private fun scanForUrls(nodeInfo: AccessibilityNodeInfo?) {
        nodeInfo ?: return
        
        val nodes = nodeInfo?.let { getAllDescendants(it) } ?: return
        
        for (node in nodes) {
            val text = node.text?.toString() ?: node.contentDescription?.toString() ?: ""
            
            if (text.isNotBlank()) {
                val urls = extractUrls(text)
                if (urls.isNotEmpty()) {
                    Log.d(TAG, "Found URLs: $urls")
                    // TODO: Handle found URLs (e.g., display in UI, copy to clipboard, etc.)
                }
            }
        }
    }

    private fun getAllDescendants(root: AccessibilityNodeInfo): List<AccessibilityNodeInfo> {
        val nodes = mutableListOf<AccessibilityNodeInfo>()
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)

        while (queue.isNotEmpty()) {
            val node = queue.removeFirst()
            nodes.add(node)

            for (i in 0 until node.childCount) {
                node.getChild(i)?.let { queue.add(it) }
            }
        }

        return nodes
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