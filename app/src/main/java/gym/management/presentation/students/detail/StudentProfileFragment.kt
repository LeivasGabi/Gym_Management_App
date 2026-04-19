package gym.management.presentation.students.detail

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

class StudentProfileFragment : Fragment() {

    private val viewModel: StudentProfileViewModel by viewModels {
        StudentProfileViewModel.factory()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val saveState by viewModel.saveState.collectAsState()

            GymManagementAppTheme {
                StudentProfileScreen(
                    uiState = uiState,
                    saveState = saveState,
                    onBackClick = { findNavController().popBackStack() },
                    onToggleActive = { viewModel.toggleActive(it) },
                    onSave = { name, phone, address, emergencyContactName, emergencyContact, paymentDay, modalityIds, notes, birthDate, registrationDate ->
                        viewModel.saveContactInfo(name, phone, address, emergencyContactName, emergencyContact, paymentDay, modalityIds, notes, birthDate, registrationDate)
                    },
                    onSaveHandled = { viewModel.resetSaveState() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val studentId = arguments?.getString(ARG_STUDENT_ID).orEmpty()
        viewModel.loadStudent(studentId)
    }

    companion object {
        const val ARG_STUDENT_ID = "studentId"
        const val ARG_STUDENT_NAME = "studentName"
    }
}
