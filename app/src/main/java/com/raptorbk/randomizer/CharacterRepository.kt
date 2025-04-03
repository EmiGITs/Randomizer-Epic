package com.raptorbk.randomizer

import kotlinx.coroutines.flow.Flow

class CharacterRepository(private val characterDao: CharacterDao) {
    val allCharacters: Flow<List<Character>> = characterDao.getAllCharacters()

    suspend fun insertAll(characters: List<Character>) {
        characterDao.insertAll(characters)
    }

    suspend fun getRandomCharacter(): String? {
        return characterDao.getRandomCharacter()?.name
    }

    suspend fun getRandomCharacterWithId(): Character? {
        return characterDao.getRandomCharacter()
    }

    fun getAllCharactersOrdered(): Flow<List<Character>> {
        return characterDao.getAllCharactersOrdered()
    }

    suspend fun insertCharacter(character: Character) {
        characterDao.insert(character)
    }

    suspend fun getCharacterCount(): Int {
        return characterDao.getCharacterCount()
    }

    suspend fun getAllCharactersOnce(): List<Character> {
        return characterDao.getAllCharactersOnce()
    }

    suspend fun deleteCharacter(character: Character) {
        characterDao.deleteCharacter(character)
    }

    suspend fun clearAllCharacters() {
        characterDao.clearAll()
    }
}