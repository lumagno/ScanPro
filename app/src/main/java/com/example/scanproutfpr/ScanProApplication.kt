package com.example.scanproutfpr

import android.app.Application
import com.example.scanproutfpr.data.InventarioDatabase

class ScanProApplication : Application() {
    val database by lazy { InventarioDatabase.getDatabase(this) }
}