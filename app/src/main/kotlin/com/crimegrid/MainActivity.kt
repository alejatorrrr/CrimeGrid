package com.crimegrid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.createSavedStateHandle
import com.crimegrid.ui.CrimeGridApp
import com.crimegrid.ui.InvestigationViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val model: InvestigationViewModel = viewModel(factory = viewModelFactory {
                initializer { InvestigationViewModel(applicationContext, createSavedStateHandle()) }
            })
            CrimeGridApp(model)
        }
    }
}
