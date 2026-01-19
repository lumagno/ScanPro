package com.example.scanproutfpr.data

import android.content.Context
import android.util.JsonReader
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.scanproutfpr.R
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@Database(entities = [ItemPatrimonio::class], version = 4, exportSchema = false)
abstract class InventarioDatabase : RoomDatabase() {

    abstract fun itemPatrimonioDao(): ItemPatrimonioDao

    companion object {
        @Volatile
        private var Instance: InventarioDatabase? = null

        fun getDatabase(context: Context): InventarioDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    InventarioDatabase::class.java,
                    "inventario_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { Instance = it }
            }
        }

        // --- MÉTODO OTIMIZADO PARA GRANDES ARQUIVOS (STREAMING) ---
        suspend fun popularBancoSeVazio(context: Context, dao: ItemPatrimonioDao) {
            try {
                // 1. Verifica se o banco já tem itens. Se tiver, não faz nada.
                if (dao.contarItens() > 0) {
                    Log.d("DB_LOAD", "O banco já contém dados. Importação ignorada.")
                    return
                }

                Log.d("DB_LOAD", "Iniciando leitura de arquivo GIGANTE via Stream...")

                // 2. Abre o arquivo 'dados_iniciais.json' da pasta res/raw
                val inputStream = context.resources.openRawResource(R.raw.dados_iniciais)
                val reader = JsonReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))

                val loteItens = mutableListOf<ItemPatrimonio>()
                var totalProcessado = 0
                val tamanhoLote = 2000 // Quantidade de itens para salvar por vez

                // 3. Inicia a leitura do Array JSON "["
                reader.beginArray()

                while (reader.hasNext()) {
                    // Lê um item individualmente sem carregar o arquivo todo na memória
                    val item = lerItem(reader)

                    if (item != null) {
                        loteItens.add(item)
                    }

                    // 4. Salva em lotes para liberar memória
                    if (loteItens.size >= tamanhoLote) {
                        dao.inserirTudo(loteItens)
                        totalProcessado += loteItens.size
                        Log.d("DB_LOAD", "Salvou parcial: $totalProcessado itens...")
                        loteItens.clear() // LIMPA A LISTA DA MEMÓRIA RAM
                    }
                }

                // 5. Salva o restante (se sobrou algo no último lote)
                if (loteItens.isNotEmpty()) {
                    dao.inserirTudo(loteItens)
                    totalProcessado += loteItens.size
                }

                reader.endArray()
                reader.close()

                Log.d("DB_LOAD", "✅ SUCESSO! Total de $totalProcessado itens importados.")

            } catch (e: Exception) {
                Log.e("DB_LOAD", "❌ Erro crítico ao importar JSON: ${e.message}")
                e.printStackTrace()
            }
        }

        // --- FUNÇÃO AUXILIAR PARA LER UM ÚNICO ITEM ---
        // Resolve o problema de maiúsculas/minúsculas e acentos
        private fun lerItem(reader: JsonReader): ItemPatrimonio? {
            var tombo = ""
            var descricao = ""
            var caracteristica = ""
            var responsavel = ""
            var local = ""
            var tomboAntigo = ""
            var numeroSerie = ""
            // Se tiver outros campos no JSON, adicione variáveis aqui

            reader.beginObject()
            while (reader.hasNext()) {
                val nomeOriginal = reader.nextName()
                val nomeChave = nomeOriginal.lowercase().trim() // Converte para minúsculo para comparar

                // Verifica se o valor é nulo antes de ler
                if (reader.peek() == android.util.JsonToken.NULL) {
                    reader.skipValue()
                    continue
                }

                when (nomeChave) {
                    "tombo" -> tombo = reader.nextString()
                    "descricao", "descrição", "description" -> descricao = reader.nextString()
                    "caracteristica", "característica", "detalhes" -> caracteristica = reader.nextString()
                    "responsavel", "responsável", "usuario" -> responsavel = reader.nextString()
                    "local", "setor", "localizacao" -> local = reader.nextString()
                    "tomboantigo", "tombo_antigo", "antigo" -> tomboAntigo = reader.nextString()
                    "numeroserie", "numero_serie", "n_serie", "serial" -> numeroSerie = reader.nextString()
                    "localultimaauditoria", "auditoria" -> reader.skipValue() // Se existir mas não formos usar agora
                    else -> reader.skipValue() // Ignora campos extras desconhecidos
                }
            }
            reader.endObject()

            // Validação mínima: só retorna se tiver Tombo
            if (tombo.isNotBlank()) {
                return ItemPatrimonio(
                    tombo = tombo,
                    descricao = descricao,
                    caracteristica = caracteristica,
                    responsavel = responsavel,
                    local = local,
                    tomboAntigo = tomboAntigo,
                    numeroSerie = numeroSerie
                )
            }
            return null
        }
    }
}