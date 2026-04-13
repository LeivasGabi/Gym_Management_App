package gym.management.presentation.students.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import gym.management.R
import gym.management.ui.theme.GymManagementAppTheme

class StudentDetailFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val studentId = arguments?.getString(ARG_STUDENT_ID).orEmpty()
            val studentName = arguments?.getString(ARG_STUDENT_NAME).orEmpty()

            GymManagementAppTheme {
                StudentDetailScreen(
                    studentName = studentName,
                    onNotesClick = {
                        // TODO: navegar para tela de notas
                    },
                    onProfileClick = {
                        findNavController().navigate(
                            R.id.action_studentDetail_to_studentProfile,
                            bundleOf(
                                StudentProfileFragment.ARG_STUDENT_ID to studentId,
                                StudentProfileFragment.ARG_STUDENT_NAME to studentName
                            )
                        )
                    },
                    onBackClick = { findNavController().popBackStack() }
                )
            }
        }
    }

    companion object {
        const val ARG_STUDENT_ID = "studentId"
        const val ARG_STUDENT_NAME = "studentName"
    }
}
