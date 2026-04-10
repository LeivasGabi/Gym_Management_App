package gym.management.presentation.register

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
import gym.management.R
import gym.management.data.repository.AuthRepositoryImpl
import gym.management.ui.theme.GymManagementAppTheme

class RegisterFragment : Fragment() {

    private val viewModel: RegisterViewModel by viewModels {
        RegisterViewModel.factory(AuthRepositoryImpl())
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

                RegisterScreen(
                    uiState = uiState,
                    onRegisterClick = { email, password, confirmPassword ->
                        viewModel.register(email, password, confirmPassword)
                    },
                    onRegisterSuccess = {
                        findNavController().navigate(R.id.action_register_to_mainMenu)
                    },
                    onErrorShown = { viewModel.resetState() },
                    onNavigateBack = { findNavController().popBackStack() }
                )
            }
        }
    }
}
