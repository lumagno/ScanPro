package com.example.scanproutfpr

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanproutfpr.data.ItemPatrimonio
import com.example.scanproutfpr.data.ItemPatrimonioDao
import com.example.scanproutfpr.data.InventarioDatabase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val dao: ItemPatrimonioDao) : ViewModel() {

    val todosItens = dao.listarTodosItens()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun importarNovoJson(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    // Chama a importação
                    InventarioDatabase.importarJsonExterno(dao, inputStream)

                    // Log para confirmar que o processo terminou no código
                    Log.d("IMPORT_VM", "Importação finalizada. O Flow deve atualizar a lista.")
                }
            } catch (e: Exception) {
                Log.e("IMPORT_VM", "Erro crítico: ${e.message}")
            }
        }
    }

    fun apagarItem(item: ItemPatrimonio) {
        viewModelScope.launch {
            dao.deletarItem(item)
        }
    }

    fun salvarItem(
        tombo: String, descricao: String, caracteristica: String,
        responsavel: String, local: String, tomboAntigo: String,
        numeroSerie: String, onSucesso: () -> Unit, onDuplicado: () -> Unit
    ) {
        viewModelScope.launch {
            val tomboFormatado = tombo.uppercase().trim()
            if (dao.verificarTombo(tomboFormatado) > 0) {
                onDuplicado()
            } else {
                val novoItem = ItemPatrimonio(
                    tombo = tomboFormatado,
                    descricao = descricao.uppercase().trim(),
                    caracteristica = caracteristica.uppercase().trim(),
                    responsavel = responsavel.uppercase().trim(),
                    local = local.uppercase().trim(),
                    tomboAntigo = tomboAntigo.uppercase().trim(),
                    numeroSerie = numeroSerie.uppercase().trim()
                )
                dao.inserirItem(novoItem)
                onSucesso()
            }
        }
    }

    fun atualizarItem(item: ItemPatrimonio) {
        viewModelScope.launch {
            dao.inserirItem(item.copy(
                tombo = item.tombo.uppercase().trim(),
                descricao = item.descricao.uppercase().trim(),
                caracteristica = item.caracteristica.uppercase().trim(),
                responsavel = item.responsavel.uppercase().trim(),
                local = item.local.uppercase().trim(),
                tomboAntigo = item.tomboAntigo.uppercase().trim(),
                numeroSerie = item.numeroSerie.uppercase().trim()
            ))
        }
    }
}