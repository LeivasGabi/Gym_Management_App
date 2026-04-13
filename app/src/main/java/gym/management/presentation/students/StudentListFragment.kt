package gym.management.presentation.students

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
import gym.management.presentation.students.detail.StudentDetailFragment
import gym.management.ui.theme.GymManagementAppTheme

class StudentListFragment : Fragment() {

    private val viewModel: StudentListViewModel by viewModels {
        StudentListViewModel.factory()
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

                StudentListScreen(
                    uiState = uiState,
                    onBackClick = { findNavController().popBackStack() },
                    onStudentClick = { id, name ->
                        findNavController().navigate(
                            R.id.action_studentList_to_studentDetail,
                            bundleOf(
                                StudentDetailFragment.ARG_STUDENT_ID to id,
                                StudentDetailFragment.ARG_STUDENT_NAME to name
                            )
                        )
                    }
                )
            }
        }
    }
}
