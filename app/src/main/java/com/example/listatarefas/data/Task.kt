package com.example.listatarefas.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tarefas")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var titulo: String,
    var descricao: String = "",
    var concluida: Boolean = false,
    var data: String? = null,   // opcional
    var hora: String? = null    // opcional
)
