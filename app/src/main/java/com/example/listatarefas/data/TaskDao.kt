package com.example.listatarefas.data

import androidx.room.*

@Dao
interface TaskDao {

    @Query("SELECT * FROM tarefas WHERE userId = :userId ORDER BY id DESC")
    suspend fun listarTarefas(userId: Int): List<Task>

    @Insert
    suspend fun inserir(task: Task)

    @Update
    suspend fun atualizar(task: Task)

    @Delete
    suspend fun deletar(task: Task)

    @Query("SELECT * FROM tarefas WHERE concluida = 1 AND userId = :userId ORDER BY id DESC")
    suspend fun listarConcluidas(userId: Int): List<Task>

    @Query("SELECT * FROM tarefas WHERE concluida = 0 AND userId = :userId ORDER BY id DESC")
    suspend fun listarPendentes(userId: Int): List<Task>
}
