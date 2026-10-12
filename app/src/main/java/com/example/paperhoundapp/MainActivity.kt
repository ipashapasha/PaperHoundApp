package com.example.paperhoundapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.paperhoundapp.ui.navigation.PaperHoundNavHost
import com.example.paperhoundapp.ui.theme.PaperHoundAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PaperHoundAppTheme {
                PaperHoundNavHost((application as PaperHoundApplication).bookRepository)
            }
        }
    }
}
