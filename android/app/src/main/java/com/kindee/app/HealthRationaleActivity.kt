package com.kindee.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView

class HealthRationaleActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(TextView(this).apply {
            text = "KinDee reads the selected source’s active energy and steps only after your consent. Values are sent to your KinDee account when you tap Sync. No background collection. Disconnect deletes KinDee health imports; revoke device access in Health Connect. Activity estimates do not automatically increase your food goal."
            setPadding(32, 64, 32, 32)
        })
    }
}
