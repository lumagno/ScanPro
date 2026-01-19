package com.example.scanproutfpr

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.scanproutfpr.data.ItemPatrimonio
import com.example.scanproutfpr.data.ItemPatrimonioDao
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val dao: ItemPatrimonioDao) : ViewModel() {

    val todosItens = dao.listarTodosItens()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun salvarItem(
        tombo: String,
        descricao: String,
        caracteristica: String,
        responsavel: String,
        local: String,
        tomboAntigo: String,
        numeroSerie: String,
        onSucesso: () -> Unit,
        onDuplicado: () -> Unit
    ) {
        viewModelScope.launch {
            val tomboFormatado = tombo.uppercase().trim()

            // Verifica duplicidade apenas se for um novo cadastro
            // (Assumindo que esta função é chamada para create, não update via ID)
            val qtd = dao.verificarTombo(tomboFormatado)

            if (qtd > 0) {
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
        val itemMaiusculo = item.copy(
            tombo = item.tombo.uppercase().trim(),
            descricao = item.descricao.uppercase().trim(),
            caracteristica = item.caracteristica.uppercase().trim(),
            responsavel = item.responsavel.uppercase().trim(),
            local = item.local.uppercase().trim(),
            tomboAntigo = item.tomboAntigo.uppercase().trim(),
            numeroSerie = item.numeroSerie.uppercase().trim(),
            localUltimaAuditoria = item.localUltimaAuditoria?.uppercase()?.trim()
        )
        viewModelScope.launch {
            dao.inserirItem(itemMaiusculo)
        }
    }

    fun apagarItem(item: ItemPatrimonio) {
        viewModelScope.launch { dao.deletarItem(item) }
    }
}