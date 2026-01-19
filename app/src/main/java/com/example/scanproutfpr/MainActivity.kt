package com.example.scanproutfpr

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope // IMPORTANTE
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.scanproutfpr.data.InventarioDatabase // IMPORTANTE
import com.example.scanproutfpr.ui.theme.ScanProUTFPRTheme
import kotlinx.coroutines.launch // IMPORTANTE

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- BLOCO DE CARREGAMENTO SEGURO ---
        val app = application as ScanProApplication
        val dao = app.database.itemPatrimonioDao()

        // Executa em segundo plano assim que a tela abre
        lifecycleScope.launch {
            InventarioDatabase.popularBancoSeVazio(applicationContext, dao)
        }
        // ------------------------------------

        setContent {
            ScanProUTFPRTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val viewModel = viewModel<MainViewModel>(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return MainViewModel(dao) as T
                            }
                        }
                    )
                    InventoryScreen(viewModel = viewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}