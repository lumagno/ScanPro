package com.example.scanproutfpr.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabela_itens")
data class ItemPatrimonio(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // O banco gera este ID automaticamente
    val tombo: String,
    val descricao: String,
    val caracteristica: String = "",
    val responsavel: String,
    val local: String,
    val tomboAntigo: String = "",
    val numeroSerie: String = "",
    val localUltimaAuditoria: String? = null
)
