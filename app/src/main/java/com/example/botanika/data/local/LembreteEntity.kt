package com.example.botanika.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "lembretes")
data class LembreteEntity(
    @PrimaryKey(autoGenerate = true)
    val idLembrete: Int,
    val plantaId: Int,
    val nomePlanta: String,
    val mensagem: String,
    val tempoEmMillis: Long
)
