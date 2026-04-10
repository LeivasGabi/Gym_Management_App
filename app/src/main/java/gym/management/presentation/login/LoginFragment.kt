package gym.management.presentation.login

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

class LoginFragment : Fragment() {

    private val viewModel: LoginViewModel by viewModels {
        LoginViewModel.factory(AuthRepositoryImpl())
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

                LoginScreen(
                    uiState = uiState,
                    onLoginClick = { email, password -> viewModel.login(email, password) },
                    onRegisterClick = {
                        findNavController().navigate(R.id.action_login_to_register)
                    },
                    onLoginSuccess = {
                        findNavController().navigate(R.id.action_login_to_mainMenu)
                    },
                    onErrorShown = { viewModel.resetState() }
                )
            }
        }
    }
}
