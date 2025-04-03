package com.raptorbk.randomizer

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CharacterDao {
    // Para operaciones de inicialización
    @Query("SELECT COUNT(*) FROM characters_random")
    suspend fun getCharacterCount(): Int

    // Para obtener todos los personajes (Flow observable)
    @Query("SELECT * FROM characters_random")
    fun getAllCharacters(): Flow<List<Character>>

    // Para obtener todos los personajes como lista simple (suspend)
    @Query("SELECT * FROM characters_random")
    suspend fun getAllCharactersOnce(): List<Character>

    // Para obtener personajes ordenados
    @Query("SELECT * FROM characters_random ORDER BY id ASC")
    fun getAllCharactersOrdered(): Flow<List<Character>>

    // Para obtener un personaje aleatorio
    @Query("SELECT * FROM characters_random ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomCharacter(): Character?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(characters: List<Character>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(character: Character)

    @Delete
    suspend fun deleteCharacter(character: Character)

    @Query("DELETE FROM characters_random")
    suspend fun clearAll()
}