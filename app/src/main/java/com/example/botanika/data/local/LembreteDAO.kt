package com.example.botanika.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LembreteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun salvarLembrete(lembrete: LembreteEntity): Long

    @Query("SELECT * FROM lembretes")
    suspend fun listarTodosLembretes(): List<LembreteEntity>

    @Query("DELETE FROM lembretes WHERE id = :id")
    suspend fun deletarLembretePorId(id: String)

}
