package gym.management.presentation.mainmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import gym.management.R
import gym.management.data.repository.AuthRepositoryImpl
import gym.management.ui.theme.GymManagementAppTheme
import kotlinx.coroutines.launch

class MainMenuFragment : Fragment() {

    private val authRepository = AuthRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            GymManagementAppTheme {
                MainMenuScreen(
                    onStudentsClick = {
                        findNavController().navigate(R.id.action_mainMenu_to_studentList)
                    },
                    onRegisterClick = {
                        findNavController().navigate(R.id.action_mainMenu_to_registerSelection)
                    },
                    onPaymentsClick = {
                        findNavController().navigate(R.id.action_mainMenu_to_payments)
                    },
                    onModalitiesClick = {
                        findNavController().navigate(R.id.action_mainMenu_to_modalityList)
                    },
                    onLogoutClick = {
                        lifecycleScope.launch {
                            authRepository.logout()
                            findNavController().navigate(R.id.action_mainMenu_to_login)
                        }
                    }
                )
            }
        }
    }
}
