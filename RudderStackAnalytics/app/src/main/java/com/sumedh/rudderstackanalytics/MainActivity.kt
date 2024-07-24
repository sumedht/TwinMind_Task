package com.sumedh.rudderstackanalytics

import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import com.rudderstack.analytics.AnalyticsClient

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        val button: Button = findViewById(R.id.button)
        val analyticsClient: AnalyticsClient = AnalyticsClient.getInstance(this)
        button.setOnClickListener {
            val map:Map<String, Any> = mapOf("username" to "rudderstack", "id" to 100, "balance" to 125.50)
            analyticsClient.sendEvent("Test Event",map)
        }
    }
}