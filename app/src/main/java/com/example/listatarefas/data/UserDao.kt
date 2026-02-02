package com.example.listatarefas.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface UserDao {
    @Insert
    suspend fun inserir(user: User)

    @Query("SELECT * FROM users WHERE email = :email AND senha = :senha LIMIT 1")
    suspend fun autenticar(email: String, senha: String): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun verificarEmailExiste(email: String): User?
}
