package gym.management.presentation.register.modality

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.ui.theme.GymManagementAppTheme

class RegisterModalityFragment : Fragment() {

    private val viewModel: RegisterModalityViewModel by viewModels {
        RegisterModalityViewModel.factory(ModalityRepositoryImpl())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            GymManagementAppTheme {
                val uiState by viewModel.uiState.collectAsState()

                RegisterModalityScreen(
                    uiState = uiState,
                    onSaveClick = { name, schedule, price, frequency ->
                        viewModel.saveModality(name, schedule, price, frequency)
                    },
                    onSuccess = { findNavController().popBackStack() },
                    onErrorShown = { viewModel.resetState() },
                    onNavigateBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}
