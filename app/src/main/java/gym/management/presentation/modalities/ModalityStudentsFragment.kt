package gym.management.presentation.modalities

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
import gym.management.ui.theme.GymManagementAppTheme

class ModalityStudentsFragment : Fragment() {

    private val modalityId: String by lazy {
        arguments?.getString(ARG_MODALITY_ID) ?: ""
    }

    private val modalityName: String by lazy {
        arguments?.getString(ARG_MODALITY_NAME) ?: ""
    }

    private val viewModel: ModalityStudentsViewModel by viewModels {
        ModalityStudentsViewModel.factory(modalityId)
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

                ModalityStudentsScreen(
                    modalityName = modalityName,
                    uiState = uiState,
                    onBackClick = { findNavController().popBackStack() }
                )
            }
        }
    }

    companion object {
        const val ARG_MODALITY_ID = "modality_id"
        const val ARG_MODALITY_NAME = "modality_name"
    }
}
