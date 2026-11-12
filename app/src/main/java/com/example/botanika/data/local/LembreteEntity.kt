package com.example.botanika.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lembretes")
data class LembreteEntity(
    @PrimaryKey
    val id: String,
    val plantId: String,
    val plantName: String,
    val type: String,
    val dateUtc: Long,
    val hour: Int,
    val minute: Int,
    val repeatDays: Int,
    val note: String,
    val notify: Boolean,
    val plantaId: Int = 0,
    val nomePlanta: String = "",
    val mensagem: String = "",
    val tempoEmMillis: Long = 0L
)
