package com.example.scanproutfpr.data

import android.content.Context
import android.util.JsonReader
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.scanproutfpr.R
import java.io.InputStream
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

        suspend fun popularBancoSeVazio(context: Context, dao: ItemPatrimonioDao) {
            try {
                if (dao.contarItens() > 0) return
                val inputStream = context.resources.openRawResource(R.raw.dados_iniciais)
                importarJsonSessao(dao, inputStream)
            } catch (e: Exception) {
                Log.e("DB_LOAD", "Erro ao popular inicial: ${e.message}")
            }
        }

        // Método centralizado para processar o Stream
        suspend fun importarJsonSessao(dao: ItemPatrimonioDao, inputStream: InputStream) {
            val reader = JsonReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val loteItens = mutableListOf<ItemPatrimonio>()
            val tamanhoLote = 2000

            reader.beginArray()
            while (reader.hasNext()) {
                val item = lerItem(reader)
                if (item != null) loteItens.add(item)
                if (loteItens.size >= tamanhoLote) {
                    dao.inserirTudo(loteItens)
                    loteItens.clear()
                }
            }
            if (loteItens.isNotEmpty()) dao.inserirTudo(loteItens)
            reader.endArray()
            reader.close()
        }

        // Método chamado pelo ViewModel para substituição total
        suspend fun importarJsonExterno(dao: ItemPatrimonioDao, inputStream: InputStream) {
            try {
                dao.deletarTodosItens() // Limpa o banco atual
                importarJsonSessao(dao, inputStream)
                Log.d("DB_LOAD", "Substituição de banco concluída com sucesso.")
            } catch (e: Exception) {
                Log.e("DB_LOAD", "Erro na importação externa: ${e.message}")
            }
        }

        private fun lerItem(reader: JsonReader): ItemPatrimonio? {
            var tombo = ""; var descricao = ""; var caracteristica = ""
            var responsavel = ""; var local = ""; var tomboAntigo = ""; var numeroSerie = ""

            reader.beginObject()
            while (reader.hasNext()) {
                val nomeOriginal = reader.nextName()
                val nomeChave = nomeOriginal.lowercase().trim()

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
                    else -> reader.skipValue()
                }
            }
            reader.endObject()

            return if (tombo.isNotBlank()) {
                ItemPatrimonio(
                    tombo = tombo, descricao = descricao, caracteristica = caracteristica,
                    responsavel = responsavel, local = local, tomboAntigo = tomboAntigo, numeroSerie = numeroSerie
                )
            } else null
        }
    }
}