package com.example.colonpath_ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.colonpath_ai.navigation.ColonPathNavigation
import com.example.colonpath_ai.ui.theme.ColonPathAITheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        com.example.colonpath_ai.data.SampleDataRepository.initialize(applicationContext)
        setContent {
            ColonPathAITheme {
                ColonPathNavigation()
            }
        }
    }
}