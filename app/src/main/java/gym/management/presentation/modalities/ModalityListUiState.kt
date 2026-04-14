package gym.management.presentation.modalities

import gym.management.domain.model.Modality

sealed class ModalityListUiState {
    object Loading : ModalityListUiState()
    data class Success(val modalities: List<Modality>) : ModalityListUiState()
    data class Error(val message: String) : ModalityListUiState()
}
