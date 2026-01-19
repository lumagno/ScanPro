package com.example.scanproutfpr

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.scanproutfpr.data.ItemPatrimonio
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportarECompartilharCsv(context: Context, lista: List<ItemPatrimonio>) {
    try {
        // Gera um nome de arquivo único com data e hora
        val sdf = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault())
        val dataAtual = sdf.format(Date())
        val nomeArquivo = "inventario_$dataAtual.csv"

        // CABEÇALHO CORRIGIDO:
        // Adicionada a coluna "Característica" para totalizar 8 colunas,
        // batendo com a quantidade de dados gerados abaixo.
        val csvHeader = "Tombo,Descrição,Característica,Responsável,Setor/Local,Tombo Antigo,Nº Série,Última Auditoria\n"
        val csvBody = StringBuilder()

        lista.forEach { item ->
            // Removemos vírgulas do texto interno para não quebrar as colunas do CSV
            // Se uma descrição tiver vírgula (ex: "Cadeira, giratória"), o Excel entenderia como duas colunas diferentes.
            val desc = item.descricao.replace(",", " ")
            val carac = item.caracteristica.replace(",", " ")
            val resp = item.responsavel.replace(",", " ")
            val local = item.local.replace(",", " ")
            val tAntigo = item.tomboAntigo.replace(",", " ")
            val nSerie = item.numeroSerie.replace(",", " ")

            // Se não tiver auditoria (null), deixa vazio ("")
            val auditoria = item.localUltimaAuditoria?.replace(",", " ") ?: ""

            // Monta a linha com os 8 campos na ordem exata do cabeçalho
            csvBody.append("${item.tombo},$desc,$carac,$resp,$local,$tAntigo,$nSerie,$auditoria\n")
        }

        val conteudo = csvHeader + csvBody.toString()

        // Salva na pasta de cache (autorizada pelo file_paths.xml)
        // Usamos cacheDir para não precisar de permissão de escrita no armazenamento externo
        val arquivo = File(context.cacheDir, nomeArquivo)
        arquivo.writeText(conteudo)

        // Gera a URI segura para compartilhamento (FileProvider)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            arquivo
        )

        // Cria a intenção de compartilhamento
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_SUBJECT, "Inventário ScanPro - $dataAtual")
            putExtra(Intent.EXTRA_TEXT, "Segue em anexo o arquivo de inventário gerado.")
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        // Abre o menu de compartilhamento do Android (WhatsApp, Email, Drive, etc)
        context.startActivity(Intent.createChooser(intent, "Compartilhar Inventário via:"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}