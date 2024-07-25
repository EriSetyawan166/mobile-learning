package com.example.mobilelearning

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activty_login)
    }
}
