package com.example.listatarefas.data

import androidx.room.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM tarefas ORDER BY id DESC")
    suspend fun listarTarefas(): List<Task>

    @Insert
    suspend fun inserir(task: Task)

    @Update
    suspend fun atualizar(task: Task)

    @Delete
    suspend fun deletar(task: Task)

    @Query("SELECT * FROM tarefas WHERE concluida = 1 ORDER BY id DESC")
    suspend fun listarConcluidas(): List<Task>

    @Query("SELECT * FROM tarefas WHERE concluida = 0 ORDER BY id DESC")
    suspend fun listarPendentes(): List<Task>
}
