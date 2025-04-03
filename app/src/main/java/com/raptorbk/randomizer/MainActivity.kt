package com.raptorbk.randomizer

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.raptorbk.randomizer.screens.CharacterListScreen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class MainActivity : ComponentActivity() {
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val viewModel by viewModels<CharacterViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val dao = database.characterDao()
                val repository = CharacterRepository(dao) // Crea el repositorio primero
                return CharacterViewModel(repository) as T
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            RandomizerTheme {
                val showList = remember { mutableStateOf(false) }
                val viewModel = viewModel

                if (showList.value) {
                    CharacterListScreen(
                        viewModel = viewModel,
                        onBack = { showList.value = false }
                    )
                } else {
                    RandomNameGenerator(
                        viewModel = viewModel,
                        onNavigateToList = { showList.value = true }
                    )
                }
            }
        }
    }
}

class CharacterViewModel(private val repository: CharacterRepository) : ViewModel() {
    private val _usedIds = mutableSetOf<Int>()
    val usedIds: Set<Int> get() = _usedIds

    val allCharacters: Flow<List<Character>> = repository.allCharacters
    val allCharactersOrdered: Flow<List<Character>> = repository.getAllCharactersOrdered()

    suspend fun resetDatabase(defaultCharacters: List<String>) {
        repository.clearAllCharacters()
        val characters = defaultCharacters.mapIndexed { index, name ->
            Character(id = index + 1, name = name)
        }
        repository.insertAll(characters)
    }

    suspend fun initializeDatabaseIfNeeded(defaultCharacters: List<String>) {
        val count = repository.getCharacterCount()
        if (count == 0) {
            val characters = defaultCharacters.mapIndexed { index, name ->
                Character(id = index + 1, name = name)
            }
            repository.insertAll(characters)
        }
    }

    suspend fun deleteCharacter(character: Character) {
        repository.deleteCharacter(character)
    }

    suspend fun getRandomUnusedCharacter(): String? {
        val allCharacters = repository.getAllCharactersOnce()
        val available = allCharacters.filterNot { it.id in _usedIds }

        return when {
            available.isNotEmpty() -> {
                val selected = available.random()
                _usedIds.add(selected.id)
                selected.name
            }
            allCharacters.isNotEmpty() -> {
                _usedIds.clear()
                allCharacters.random().name
            }
            else -> null
        }
    }

    suspend fun addNewCharacter(name: String) {
        // Obtenemos el máximo ID actual para asignar el siguiente
        val maxId = repository.getAllCharactersOnce().maxOfOrNull { it.id } ?: 0
        val newCharacter = Character(id = maxId + 1, name = name)
        repository.insertCharacter(newCharacter)
    }

    fun clearUsedIds() {
        _usedIds.clear()
    }
}


@Composable
fun rememberDoubleClickHandler(
    delayMillis: Long = 300, // Tiempo entre clics para considerarse doble clic
    onDoubleClick: () -> Unit
): () -> Unit {
    var lastClickTime by remember { mutableLongStateOf(0L) }

    return {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < delayMillis) {
            onDoubleClick()
            lastClickTime = 0 // Reset después de un doble clic exitoso
        } else {
            lastClickTime = currentTime
        }
    }
}

@Composable
fun RandomNameGenerator(
    viewModel: CharacterViewModel,
    onNavigateToList: () -> Unit
) {
    val names = listOf(
        "Odiseo", "Eurilocus", "Polites", "Atenea", "Polifemo",
        "Éolo", "Poseidón", "Circe", "Hermes", "Tiresias",
        "Anticlea", "Escila", "Cardibis", "Zeus", "Apolo",
        "Hefesto", "Ares", "Afrodita", "Hera", "Telemaco",
        "Calipso", "Penelope"
    )

    var selectedName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var showDoubleClickHint by remember { mutableStateOf(false) }
    val doubleClickHandler = rememberDoubleClickHandler {
        onNavigateToList()
    }

    LaunchedEffect(Unit) {
        viewModel.initializeDatabaseIfNeeded(names)
    }

    if (isLoading) {
        LaunchedEffect(Unit) {
            delay(2000)
            val randomName = viewModel.getRandomUnusedCharacter()
            selectedName = randomName ?: names.random()
            val sound = MediaPlayer.create(context, R.raw.test1)
            sound.start()
            isLoading = false
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = doubleClickHandler,
                modifier = Modifier.padding(16.dp)
            ) {
                Icon(Icons.Filled.List, contentDescription = "Doble clic para ver lista")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(50.dp),
                    strokeWidth = 4.dp
                )
                Text(
                    text = "Generando...",
                    fontSize = 20.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            } else {
                Text(
                    text = if (selectedName.isEmpty())
                        "Presiona el botón para generar un nombre"
                    else selectedName,
                    fontSize = 40.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 25.dp)
                )
            }

            Button(
                onClick = {
                    if (!isLoading) {
                        isLoading = true
                        selectedName = ""
                    }
                },
                modifier = Modifier.padding(0.dp),
                enabled = !isLoading
            ) {
                Text("Generar", fontSize = 30.sp)
            }
        }
    }
}