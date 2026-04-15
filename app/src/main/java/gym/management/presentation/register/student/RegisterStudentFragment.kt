package gym.management.presentation.register.student

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
import gym.management.data.repository.ModalityRepositoryImpl
import gym.management.data.repository.StudentRepositoryImpl
import gym.management.ui.theme.GymManagementAppTheme

class RegisterStudentFragment : Fragment() {

    private val viewModel: RegisterStudentViewModel by viewModels {
        RegisterStudentViewModel.factory(StudentRepositoryImpl(), ModalityRepositoryImpl())
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
                val modalities by viewModel.modalities.collectAsState()
                val modalitiesLoaded by viewModel.modalitiesLoaded.collectAsState()

                RegisterStudentScreen(
                    uiState = uiState,
                    modalities = modalities,
                    modalitiesLoaded = modalitiesLoaded,
                    onSaveClick = { name, phone, address, birthDate, emergencyContactName, emergencyContact, paymentDay, modalityIds ->
                        viewModel.saveStudent(name, phone, address, birthDate, emergencyContactName, emergencyContact, paymentDay, modalityIds)
                    },
                    onSuccess = { findNavController().popBackStack() },
                    onErrorShown = { viewModel.resetState() },
                    onNavigateBack = { findNavController().popBackStack() },
                    onAddModalityClick = {
                        findNavController().navigate(R.id.registerModalityFragment)
                    }
                )
            }
        }
    }
}
