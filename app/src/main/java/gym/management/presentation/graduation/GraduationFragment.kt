package gym.management.presentation.graduation

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

class GraduationFragment : Fragment() {

    private val viewModel: GraduationViewModel by viewModels {
        GraduationViewModel.factory()
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
            val studentId = arguments?.getString(ARG_STUDENT_ID).orEmpty()

            GymManagementAppTheme {
                GraduationScreen(
                    uiState = uiState,
                    saveState = saveState,
                    studentId = studentId,
                    onBackClick = { findNavController().popBackStack() },
                    onAddGraduation = { modalityId, modalityName, belt, generalGrade, observation, date ->
                        viewModel.addGraduation(
                            studentId = studentId,
                            modalityId = modalityId,
                            modalityName = modalityName,
                            belt = belt,
                            generalGrade = generalGrade,
                            observation = observation,
                            date = date
                        )
                    },
                    onUpdateGraduation = { viewModel.updateGraduation(it) },
                    onSaveHandled = { viewModel.resetSaveState() }
                )
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val studentId = arguments?.getString(ARG_STUDENT_ID).orEmpty()
        viewModel.load(studentId)
    }

    companion object {
        const val ARG_STUDENT_ID = "studentId"
        const val ARG_STUDENT_NAME = "studentName"
    }
}
