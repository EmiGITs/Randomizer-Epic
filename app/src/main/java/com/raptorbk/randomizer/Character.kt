package com.raptorbk.randomizer

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "characters_random")
data class Character(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)