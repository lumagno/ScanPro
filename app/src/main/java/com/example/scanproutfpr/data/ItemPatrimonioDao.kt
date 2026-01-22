package com.example.scanproutfpr.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemPatrimonioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirItem(item: ItemPatrimonio)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun inserirTudo(itens: List<ItemPatrimonio>)

    @Query("SELECT COUNT(*) FROM tabela_itens")
    suspend fun contarItens(): Int

    @Query("DELETE FROM tabela_itens")
    suspend fun deletarTodosItens()

    @Delete
    suspend fun deletarItem(item: ItemPatrimonio)

    @Query("SELECT * from tabela_itens ORDER BY tombo ASC")
    fun listarTodosItens(): Flow<List<ItemPatrimonio>>

    @Query("SELECT COUNT(*) FROM tabela_itens WHERE tombo = :tombo")
    suspend fun verificarTombo(tombo: String): Int
}