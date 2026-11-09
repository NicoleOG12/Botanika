package com.example.botanika.data.local

import androidx.room3.Entity
import androidx.room3.PrimaryKey


@Entity(tableName = "lembretes")
    data class LembreteEntity(
    @PrimaryKey(autoGenerate = true)
    val idLembrete: Int,
    val plantaId: Int,
    val nomePlanta: String,
    val mensagem: String,
    val tempoEmMillis: Long
    )
