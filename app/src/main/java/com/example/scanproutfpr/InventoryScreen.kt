package com.example.scanproutfpr

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.scanproutfpr.data.ItemPatrimonio
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    var abaSelecionada by remember { mutableIntStateOf(0) }
    var itemParaEditar by remember { mutableStateOf<ItemPatrimonio?>(null) }
    val listaItens by viewModel.todosItens.collectAsState()

    if (itemParaEditar != null) {
        EditItemDialog(
            item = itemParaEditar!!,
            onDismiss = { itemParaEditar = null },
            onConfirm = { itemAtualizado ->
                viewModel.atualizarItem(itemAtualizado)
                itemParaEditar = null
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Cadastro") },
                    label = { Text("Cadastro") },
                    selected = abaSelecionada == 0,
                    onClick = { abaSelecionada = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.List, contentDescription = "Consulta") },
                    label = { Text("Consulta") },
                    selected = abaSelecionada == 1,
                    onClick = { abaSelecionada = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.CheckCircle, contentDescription = "Auditoria") },
                    label = { Text("Auditoria") },
                    selected = abaSelecionada == 2,
                    onClick = { abaSelecionada = 2 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (abaSelecionada) {
                0 -> TabCadastro(viewModel)
                1 -> TabConsulta(viewModel, listaItens, onEditarItem = { itemParaEditar = it })
                2 -> TabAuditoria(listaItens, viewModel)
            }
        }
    }
}

@Composable
fun DeveloperFooter() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Developed by Luiz Henrique Magnagnagno",
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
    }
}

@Composable
fun TabCadastro(viewModel: MainViewModel) {
    var tombo by remember { mutableStateOf("") }
    var descricao by remember { mutableStateOf("") }
    var caracteristica by remember { mutableStateOf("") } // NOVO ESTADO
    var responsavel by remember { mutableStateOf("") }
    var local by remember { mutableStateOf("") }
    var tomboAntigo by remember { mutableStateOf("") }
    var numeroSerie by remember { mutableStateOf("") }

    var mostrarCamera by remember { mutableStateOf(false) }
    var mostrarDialogoConfirmacao by remember { mutableStateOf(false) }
    var mostrarErroDuplicado by remember { mutableStateOf(false) }
    var codigoLidoTemporario by remember { mutableStateOf("") }

    if (mostrarErroDuplicado) {
        AlertDialog(
            onDismissRequest = { mostrarErroDuplicado = false },
            icon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("ATENÇÃO") },
            text = { Text("O Tombo '$tombo' já existe no banco de dados.") },
            confirmButton = { TextButton(onClick = { mostrarErroDuplicado = false }) { Text("OK") } }
        )
    }

    if (mostrarDialogoConfirmacao) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoConfirmacao = false },
            icon = { Icon(Icons.Filled.QrCodeScanner, contentDescription = null) },
            title = { Text(text = "CÓDIGO LIDO") },
            text = {
                Column {
                    Text("O código foi:")
                    Text(
                        text = codigoLidoTemporario,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    tombo = codigoLidoTemporario
                    mostrarDialogoConfirmacao = false
                }) { Text("USAR ESTE CÓDIGO") }
            },
            dismissButton = {
                OutlinedButton(onClick = {
                    mostrarDialogoConfirmacao = false
                    mostrarCamera = true
                }) { Text("LER NOVAMENTE") }
            }
        )
    }

    if (mostrarCamera) {
        CameraPreviewScreen(
            onCodigoLido = { codigo ->
                // Filtra apenas números do código lido pela câmera também
                codigoLidoTemporario = codigo.filter { it.isDigit() }
                mostrarCamera = false
                mostrarDialogoConfirmacao = true
            },
            onFechar = { mostrarCamera = false }
        )
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "NOVO CADASTRO", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = tombo,
                    onValueChange = { novoValor ->
                        // REGRA: Permite apenas números (0-9)
                        if (novoValor.all { it.isDigit() }) {
                            tombo = novoValor
                        }
                    },
                    label = { Text("TOMBO ATUAL") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    isError = mostrarErroDuplicado,
                    // ABRE O TECLADO NUMÉRICO AUTOMATICAMENTE
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { mostrarCamera = true },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = "Ler Código")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descricao,
                onValueChange = { descricao = it.uppercase() },
                label = { Text("DESCRIÇÃO") },
                modifier = Modifier.fillMaxWidth()
            )

            // NOVO CAMPO: CARACTERÍSTICA
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = caracteristica,
                onValueChange = { caracteristica = it.uppercase() },
                label = { Text("CARACTERÍSTICA") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = responsavel, onValueChange = { responsavel = it.uppercase() }, label = { Text("RESPONSÁVEL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = local, onValueChange = { local = it.uppercase() }, label = { Text("SETOR / LOCAL") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(value = tomboAntigo, onValueChange = { tomboAntigo = it.uppercase() }, label = { Text("TOMBO ANTIGO") }, modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(value = numeroSerie, onValueChange = { numeroSerie = it.uppercase() }, label = { Text("Nº SÉRIE") }, modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    if (tombo.isNotEmpty()) {
                        viewModel.salvarItem(
                            tombo = tombo,
                            descricao = descricao,
                            caracteristica = caracteristica, // ENVIANDO NOVO CAMPO
                            responsavel = responsavel,
                            local = local,
                            tomboAntigo = tomboAntigo,
                            numeroSerie = numeroSerie,
                            onSucesso = {
                                tombo = ""
                                descricao = ""
                                caracteristica = "" // LIMPANDO NOVO CAMPO
                                responsavel = ""
                                local = ""
                                tomboAntigo = ""
                                numeroSerie = ""
                            },
                            onDuplicado = { mostrarErroDuplicado = true }
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("SALVAR ITEM")
            }
            DeveloperFooter()
        }
    }
}

@Composable
fun TabConsulta(
    viewModel: MainViewModel,
    listaItens: List<ItemPatrimonio>,
    onEditarItem: (ItemPatrimonio) -> Unit
) {
    val context = LocalContext.current
    var textoPesquisa by remember { mutableStateOf("") }
    var mostrarCamera by remember { mutableStateOf(false) }

    // Launcher para importar novo JSON
    val launcherImportacao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.importarNovoJson(context, it) }
    }

    // LÓGICA DE FILTRAGEM
    val listaFiltrada = listaItens.filter { item ->
        val termo = textoPesquisa.uppercase().trim()
        if (termo.isEmpty()) {
            !item.localUltimaAuditoria.isNullOrBlank()
        } else {
            item.tombo.contains(termo, ignoreCase = true)
        }
    }

    if (mostrarCamera) {
        // Reutiliza o componente de câmera para ler o tombo
        CameraPreviewScreen(
            onCodigoLido = { codigo ->
                textoPesquisa = codigo.uppercase().trim()
                mostrarCamera = false
            },
            onFechar = { mostrarCamera = false }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            // Barra de Ações com Pesquisa, Câmera e Importação
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = textoPesquisa,
                    onValueChange = { textoPesquisa = it.uppercase() },
                    placeholder = { Text("PESQUISAR POR TOMBO") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                    trailingIcon = {
                        Row {
                            if (textoPesquisa.isNotEmpty()) {
                                IconButton(onClick = { textoPesquisa = "" }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Limpar")
                                }
                            }
                            // ÍCONE DA CÂMERA PARA PESQUISA
                            IconButton(onClick = { mostrarCamera = true }) {
                                Icon(Icons.Filled.CameraAlt, contentDescription = "Pesquisar com Câmera")
                            }
                        }
                    }
                )

                IconButton(onClick = { launcherImportacao.launch("application/json") }) {
                    Icon(Icons.Filled.FileDownload, contentDescription = "Importar JSON")
                }

                IconButton(onClick = { exportarECompartilharCsv(context, listaFiltrada) }) {
                    Icon(Icons.Filled.Share, contentDescription = "Exportar CSV")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (textoPesquisa.isEmpty())
                    "EXIBINDO: ITENS RASTREADOS"
                else "BUSCANDO TOMBO: $textoPesquisa",
                style = MaterialTheme.typography.labelMedium,
                color = if (textoPesquisa.isEmpty()) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                if (listaFiltrada.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (textoPesquisa.isEmpty())
                                "Nenhum item rastreado ainda."
                            else "Tombo não encontrado no banco.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    ItensList(
                        itens = listaFiltrada,
                        onDelete = { viewModel.apagarItem(it) },
                        onEdit = onEditarItem
                    )
                }
            }

            DeveloperFooter()
        }
    }
}

@Composable
fun TabAuditoria(listaItens: List<ItemPatrimonio>, viewModel: MainViewModel) {
    var mostrarCamera by remember { mutableStateOf(false) }
    var localAuditoria by remember { mutableStateOf("") }
    var itemEncontrado by remember { mutableStateOf<ItemPatrimonio?>(null) }
    var codigoLidoSemSucesso by remember { mutableStateOf<String?>(null) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    fun auditarCodigo(codigo: String) {
        val item = listaItens.find { it.tombo == codigo }
        if (item != null) {
            if (localAuditoria.isNotBlank()) {
                val itemAtualizado = item.copy(localUltimaAuditoria = localAuditoria)
                viewModel.atualizarItem(itemAtualizado)
                itemEncontrado = itemAtualizado
            } else {
                itemEncontrado = item
            }
            codigoLidoSemSucesso = null
        } else {
            itemEncontrado = null
            codigoLidoSemSucesso = codigo
        }
        mostrarDialogo = true
        mostrarCamera = false
    }

    if (mostrarCamera) {
        CameraPreviewScreen(onCodigoLido = { auditarCodigo(it) }, onFechar = { mostrarCamera = false })
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Filled.QrCodeScanner, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))
            Text("MODO AUDITORIA", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Insira o local abaixo para atualizar o rastreamento.", textAlign = TextAlign.Center, color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedTextField(
                value = localAuditoria,
                onValueChange = { localAuditoria = it.uppercase() },
                label = { Text("LOCAL ATUAL") },
                leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = { mostrarCamera = true }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("ESCANEAR")
            }
            Spacer(modifier = Modifier.weight(1f))
            DeveloperFooter()
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                icon = { Icon(if (itemEncontrado != null) Icons.Filled.CheckCircle else Icons.Filled.Warning, contentDescription = null) },
                title = { Text(if (itemEncontrado != null) "ENCONTRADO" else "NÃO ENCONTRADO") },
                text = {
                    if (itemEncontrado != null) {
                        Column {
                            Text("TOMBO: ${itemEncontrado!!.tombo}", style = MaterialTheme.typography.titleMedium)
                            Text("LOCAL: ${itemEncontrado!!.local}")
                            Text("DESC: ${itemEncontrado!!.descricao}")
                            // MOSTRAR CARACTERÍSTICA NA AUDITORIA
                            if (itemEncontrado!!.caracteristica.isNotBlank()) {
                                Text("DETALHE: ${itemEncontrado!!.caracteristica}", style = MaterialTheme.typography.bodySmall)
                            }
                            if (localAuditoria.isNotEmpty()) {
                                Text("RASTREADO: $localAuditoria", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Text("O código ($codigoLidoSemSucesso) não está no banco.")
                    }
                },
                confirmButton = { TextButton(onClick = { mostrarDialogo = false }) { Text("OK") } },
                dismissButton = { TextButton(onClick = { mostrarDialogo = false; mostrarCamera = true }) { Text("LER OUTRO") } }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItensList(
    itens: List<ItemPatrimonio>,
    onDelete: (ItemPatrimonio) -> Unit,
    onEdit: (ItemPatrimonio) -> Unit
) {
    // REMOVIDO: SwipeToDismissBox e a lógica de arrastar
    // AGORA: Apenas lista os cards, passando a ação de deletar para dentro do card
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = itens, key = { it.id }) { item ->
            ItemCard(
                item = item,
                onEditClick = { onEdit(item) },
                onDeleteClick = { onDelete(item) }
            )
        }
    }
}

@Composable
fun ItemCard(
    item: ItemPatrimonio,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    // Estado para controlar a visibilidade do diálogo de confirmação
    var mostrarConfirmacaoExclusao by remember { mutableStateOf(false) }

    if (mostrarConfirmacaoExclusao) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacaoExclusao = false },
            title = { Text(text = "Excluir Item?") },
            text = { Text(text = "Tem certeza que deseja apagar o item '${item.tombo}'? Essa ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteClick() // Executa a exclusão real
                        mostrarConfirmacaoExclusao = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacaoExclusao = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onEditClick() }, // Clicar no card ainda edita
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "TOMBO: ${item.tombo}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(text = item.descricao, style = MaterialTheme.typography.bodyMedium)

                if (item.caracteristica.isNotBlank()) {
                    Text(text = item.caracteristica, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Text(text = "${item.local} | ${item.responsavel}", style = MaterialTheme.typography.bodySmall)

                if (item.localUltimaAuditoria != null) {
                    Text(
                        text = "RASTREADO EM: ${item.localUltimaAuditoria}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2E7D32),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Coluna para os ícones de ação (Editar e Excluir)
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Ícone de Editar (Visual, já que o card todo é clicável, mas ajuda na affordance)
                IconButton(onClick = { onEditClick() }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray)
                }

                // Ícone de Excluir (Novo)
                IconButton(onClick = { mostrarConfirmacaoExclusao = true }) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Excluir",
                        tint = MaterialTheme.colorScheme.error // Cor vermelha para indicar perigo
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCard(item: ItemPatrimonio, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onClick() }, elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "TOMBO: ${item.tombo}", style = MaterialTheme.typography.titleMedium)
                Text(text = item.descricao, style = MaterialTheme.typography.bodyMedium)

                // EXIBINDO CARACTERÍSTICA NO CARD SE EXISTIR
                if (item.caracteristica.isNotBlank()) {
                    Text(text = item.caracteristica, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }

                Text(text = "${item.local} | ${item.responsavel}", style = MaterialTheme.typography.bodySmall)
                if (item.localUltimaAuditoria != null) {
                    Text(text = "RASTREADO EM: ${item.localUltimaAuditoria}", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                }
            }
            Icon(Icons.Filled.Edit, contentDescription = "Editar", tint = Color.Gray)
        }
    }
}