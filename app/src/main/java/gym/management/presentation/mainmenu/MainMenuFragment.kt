package gym.management.presentation.mainmenu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import gym.management.ui.theme.GymManagementAppTheme

class MainMenuFragment : Fragment() {

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
                        // TODO: navegar para tela de alunos
                    },
                    onRegisterClick = {
                        // TODO: navegar para tela de registro
                    },
                    onPaymentsClick = {
                        // TODO: navegar para tela de pagamentos
                    }
                )
            }
        }
    }
}
