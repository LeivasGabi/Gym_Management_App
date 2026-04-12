package gym.management.presentation.register.selection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import gym.management.R
import gym.management.ui.theme.GymManagementAppTheme

class RegisterSelectionFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            GymManagementAppTheme {
                RegisterSelectionScreen(
                    onStudentClick = {
                        findNavController().navigate(R.id.action_registerSelection_to_registerStudent)
                    },
                    onModalityClick = {
                        findNavController().navigate(R.id.action_registerSelection_to_registerModality)
                    },
                    onNavigateBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}
