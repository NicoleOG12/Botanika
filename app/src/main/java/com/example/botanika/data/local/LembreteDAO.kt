package com.example.botanika.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface LembreteDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun criarLembrete(lembreteEntity: LembreteEntity): Long

    @Query("SELECT * FROM lembretes")
    suspend fun listarTodosLembretes(): List<LembreteEntity>

}
