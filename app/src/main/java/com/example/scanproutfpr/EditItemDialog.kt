package com.example.scanproutfpr

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.scanproutfpr.data.ItemPatrimonio

@Composable
fun EditItemDialog(
    item: ItemPatrimonio,
    onDismiss: () -> Unit,
    onConfirm: (ItemPatrimonio) -> Unit
) {
    var tombo by remember { mutableStateOf(item.tombo) }
    var descricao by remember { mutableStateOf(item.descricao) }
    var responsavel by remember { mutableStateOf(item.responsavel) }
    var local by remember { mutableStateOf(item.local) }

    // Campos novos
    var tomboAntigo by remember { mutableStateOf(item.tomboAntigo) }
    var numeroSerie by remember { mutableStateOf(item.numeroSerie) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "EDITAR ITEM") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = tombo,
                    onValueChange = { tombo = it.uppercase() },
                    label = { Text("TOMBO") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = descricao,
                    onValueChange = { descricao = it.uppercase() },
                    label = { Text("DESCRIÇÃO") }
                )
                OutlinedTextField(
                    value = responsavel,
                    onValueChange = { responsavel = it.uppercase() },
                    label = { Text("RESPONSÁVEL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = local,
                    onValueChange = { local = it.uppercase() },
                    label = { Text("LOCAL") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = tomboAntigo,
                    onValueChange = { tomboAntigo = it.uppercase() },
                    label = { Text("TOMBO ANTIGO") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = numeroSerie,
                    onValueChange = { numeroSerie = it.uppercase() },
                    label = { Text("Nº SÉRIE") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val itemAtualizado = item.copy(
                        tombo = tombo,
                        descricao = descricao,
                        responsavel = responsavel,
                        local = local,
                        tomboAntigo = tomboAntigo,
                        numeroSerie = numeroSerie
                    )
                    onConfirm(itemAtualizado)
                }
            ) {
                Text("SALVAR")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}