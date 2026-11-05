package com.danidev.apprickmorty.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danidev.apprickmorty.data.model.RickCharacter
import com.danidev.apprickmorty.data.repository.CharacterRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.jvm.JvmOverloads

/**
 * Estado de la pantalla de personajes.
 */
sealed interface CharacterUiState {
    data object Loading : CharacterUiState
    data class Success(val characters: List<RickCharacter>) : CharacterUiState
    data class Error(val message: String) : CharacterUiState
}

class CharacterViewModel @JvmOverloads constructor(
    private val repository: CharacterRepository = CharacterRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters(name: String? = null) {
        viewModelScope.launch {
            _uiState.value = CharacterUiState.Loading
            repository.getCharacters(name)
                .onSuccess { characters ->
                    _uiState.value = CharacterUiState.Success(characters)
                }
                .onFailure { error ->
                    _uiState.value = CharacterUiState.Error(
                        error.localizedMessage ?: "No se pudieron cargar los personajes"
                    )
                }
        }
    }
}
