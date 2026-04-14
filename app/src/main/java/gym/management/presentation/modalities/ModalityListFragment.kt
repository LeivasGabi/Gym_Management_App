package gym.management.presentation.modalities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import gym.management.R
import gym.management.ui.theme.GymManagementAppTheme

class ModalityListFragment : Fragment() {

    private val viewModel: ModalityListViewModel by viewModels {
        ModalityListViewModel.factory()
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

                ModalityListScreen(
                    uiState = uiState,
                    onBackClick = { findNavController().popBackStack() },
                    onModalityClick = { id, name ->
                        findNavController().navigate(
                            R.id.action_modalityList_to_modalityStudents,
                            bundleOf(
                                ModalityStudentsFragment.ARG_MODALITY_ID to id,
                                ModalityStudentsFragment.ARG_MODALITY_NAME to name
                            )
                        )
                    }
                )
            }
        }
    }
}
