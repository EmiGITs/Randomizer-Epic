package com.raptorbk.randomizer.screens

import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import com.raptorbk.randomizer.CharacterViewModel
import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raptorbk.randomizer.ui.theme.RandomizerTheme
import kotlinx.coroutines.delay
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.raptorbk.randomizer.Character
import com.raptorbk.randomizer.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

@Composable
fun CharacterListScreen(
    viewModel: CharacterViewModel,
    onBack: () -> Unit
) {
    val characters by viewModel.allCharactersOrdered.collectAsState(initial = emptyList())
    var showDialog by remember { mutableStateOf(false) }
    var newCharacterName by remember { mutableStateOf("") }
    val context = LocalContext.current
    var showSuccess by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var characterToDelete by remember { mutableStateOf<Character?>(null) }
    var deleteConfirmed by remember { mutableStateOf(false) }


    val defaultCharacters = listOf(
        "Odiseo", "Eurilocus", "Polites", "Atenea", "Polifemo",
        "Éolo", "Poseidón", "Circe", "Hermes", "Tiresias",
        "Anticlea", "Escila", "Cardibis", "Zeus", "Apolo",
        "Hefesto", "Ares", "Afrodita", "Hera", "Telemaco",
        "Calipso", "Penelope"
    )

    var clickCount by remember { mutableIntStateOf(0) }
    var lastClickTime by remember { mutableLongStateOf(0L) }
    var showResetMessage by remember { mutableStateOf(false) }

    LaunchedEffect(clickCount) {
        if (clickCount >= 5) {
            viewModel.resetDatabase(defaultCharacters)
            clickCount = 0
            showResetMessage = true
            delay(2000) // Muestra el mensaje por 2 segundos
            showResetMessage = false
        }
    }

    // Efecto para el sonido
    if (showSuccess) {
        LaunchedEffect(Unit) {
            val sound = MediaPlayer.create(context, R.raw.test1)
            sound.start()
            showSuccess = false
        }
    }

    // Efecto para manejar la adición de personajes
    var addCharacterTrigger by remember { mutableStateOf(false) }
    if (addCharacterTrigger) {
        LaunchedEffect(Unit) {
            viewModel.addNewCharacter(newCharacterName)
            newCharacterName = ""
            showDialog = false
            showSuccess = true
            addCharacterTrigger = false
        }
    }

    if (deleteConfirmed) {
        LaunchedEffect(Unit) {
            characterToDelete?.let { viewModel.deleteCharacter(it) }
            characterToDelete = null
            showDeleteConfirmation = false
            deleteConfirmed = false
        }
    }


    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Todos los Personajes",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = { showDialog = true }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir personaje")
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                IconButton(
                    onClick = {
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastClickTime < 500) {
                            clickCount++
                        } else {
                            clickCount = 1
                        }
                        lastClickTime = currentTime
                    },
                    modifier = Modifier
                        .background(
                            color = if (clickCount > 0)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Cinco clics para reiniciar",
                        tint = if (clickCount > 0)
                            MaterialTheme.colorScheme.onSecondaryContainer
                        else
                            MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            LazyColumn {
                items(characters) { character ->
                    CharacterListItem(
                        character = character,
                        onDeleteRequest = { char ->
                            characterToDelete = char
                            showDeleteConfirmation = true
                        }
                    )
                }
            }


            if (showDeleteConfirmation && characterToDelete != null) {
                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirmation = false
                        characterToDelete = null
                    },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de que quieres eliminar a ${characterToDelete?.name}?") },
                    confirmButton = {
                        Button(
                            onClick = { deleteConfirmed = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                characterToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }



            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Añadir nuevo personaje") },
                    text = {
                        OutlinedTextField(
                            value = newCharacterName,
                            onValueChange = { newCharacterName = it },
                            label = { Text("Nombre del personaje") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = { addCharacterTrigger = true },
                            enabled = newCharacterName.isNotBlank()
                        ) {
                            Text("Añadir")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }

            if (showDeleteConfirmation && characterToDelete != null) {
                var deleteConfirmed by remember { mutableStateOf(false) }

                if (deleteConfirmed) {
                    LaunchedEffect(Unit) {
                        characterToDelete?.let { viewModel.deleteCharacter(it) }
                        showDeleteConfirmation = false
                        characterToDelete = null
                    }
                }

                AlertDialog(
                    onDismissRequest = {
                        showDeleteConfirmation = false
                        characterToDelete = null
                    },
                    title = { Text("Confirmar eliminación") },
                    text = { Text("¿Estás seguro de que quieres eliminar a ${characterToDelete?.name}?") },
                    confirmButton = {
                        Button(
                            onClick = { deleteConfirmed = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text("Eliminar")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = {
                                showDeleteConfirmation = false
                                characterToDelete = null
                            }
                        ) {
                            Text("Cancelar")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CharacterListItem(
    character: Character,
    onDeleteRequest: (Character) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "#${character.id}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.width(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = character.name,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            IconButton(
                onClick = { onDeleteRequest(character) }, // Ahora solicita la eliminación
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar personaje",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}