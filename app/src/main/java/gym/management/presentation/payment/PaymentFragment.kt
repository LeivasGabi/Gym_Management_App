package gym.management.presentation.payment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
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

class PaymentFragment : Fragment() {

    private val viewModel: PaymentViewModel by viewModels { PaymentViewModel.Factory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (!viewModel.goBack()) {
                        findNavController().popBackStack()
                    }
                }
            }
        )

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                GymManagementAppTheme {
                    val uiState by viewModel.uiState.collectAsState()

                    PaymentScreen(
                        uiState = uiState,
                        onBackClick = {
                            if (!viewModel.goBack()) findNavController().popBackStack()
                        },
                        onYearSelected = viewModel::selectYear,
                        onMonthSelected = viewModel::selectMonth,
                        onToggleModality = viewModel::toggleModality,
                        onTogglePayment = viewModel::togglePayment,
                        onStudentClick = { id, name ->
                            findNavController().navigate(
                                R.id.action_payment_to_studentDetail,
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
}
