package gym.management.presentation.register.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gym.management.R
import gym.management.domain.model.Modality
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStudentScreen(
    uiState: RegisterStudentUiState,
    modalities: List<Modality>,
    modalitiesLoaded: Boolean,
    onSaveClick: (name: String, phone: String, address: String, birthDate: String, emergencyContactName: String, emergencyContact: String, paymentDay: Int, modalityIds: List<String>, registrationDate: Long) -> Unit,
    onSuccess: () -> Unit,
    onErrorShown: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddModalityClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showRegistrationDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
    }
    val todayMillis = remember { System.currentTimeMillis() }
    val datePickerState = rememberDatePickerState()
    val registrationDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = (todayMillis / 86_400_000L) * 86_400_000L
    )
    var registrationDate by remember {
        mutableStateOf((todayMillis / 86_400_000L) * 86_400_000L)
    }
    var registrationDateText by remember {
        mutableStateOf(dateFormatter.format(Date((todayMillis / 86_400_000L) * 86_400_000L)))
    }
    var emergencyContactName by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var paymentDay by remember { mutableStateOf(0) }
    var paymentDayExpanded by remember { mutableStateOf(false) }
    var selectedModalityIds by remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterStudentUiState.Success -> onSuccess()
            is RegisterStudentUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
                onErrorShown()
            }
            else -> Unit
        }
    }

    if (modalitiesLoaded && modalities.isEmpty()) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(R.string.dialog_modality_required_title)) },
            text = { Text(stringResource(R.string.dialog_modality_required_msg)) },
            confirmButton = {
                Button(onClick = onAddModalityClick) {
                    Text(stringResource(R.string.btn_add_modality))
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text(stringResource(R.string.btn_back))
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.screen_register_student)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.btn_back),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.label_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text(stringResource(R.string.label_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text(stringResource(R.string.label_address)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (showDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val normalized = (millis / 86_400_000L) * 86_400_000L
                                birthDate = dateFormatter.format(Date(normalized))
                            }
                            showDatePicker = false
                        }) { Text(stringResource(R.string.btn_ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                ) {
                    DatePicker(state = datePickerState)
                }
            }

            if (showRegistrationDatePicker) {
                DatePickerDialog(
                    onDismissRequest = { showRegistrationDatePicker = false },
                    confirmButton = {
                        TextButton(onClick = {
                            registrationDatePickerState.selectedDateMillis?.let { millis ->
                                val normalized = (millis / 86_400_000L) * 86_400_000L
                                registrationDate = normalized
                                registrationDateText = dateFormatter.format(Date(normalized))
                            }
                            showRegistrationDatePicker = false
                        }) { Text(stringResource(R.string.btn_ok)) }
                    },
                    dismissButton = {
                        TextButton(onClick = { showRegistrationDatePicker = false }) {
                            Text(stringResource(R.string.btn_cancel))
                        }
                    }
                ) {
                    DatePicker(state = registrationDatePickerState)
                }
            }

            OutlinedTextField(
                value = registrationDateText,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_registration_date)) },
                placeholder = { Text(stringResource(R.string.placeholder_date)) },
                trailingIcon = {
                    IconButton(onClick = { showRegistrationDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.cd_select_registration_date)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.label_birth_date)) },
                placeholder = { Text(stringResource(R.string.placeholder_date)) },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = stringResource(R.string.cd_select_date)
                        )
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyContactName,
                onValueChange = { emergencyContactName = it },
                label = { Text(stringResource(R.string.label_emergency_contact_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text(stringResource(R.string.label_emergency_contact_phone)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            ExposedDropdownMenuBox(
                expanded = paymentDayExpanded,
                onExpandedChange = { paymentDayExpanded = it }
            ) {
                OutlinedTextField(
                    value = if (paymentDay == 0) "" else stringResource(R.string.every_day, paymentDay),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.label_payment_day)) },
                    placeholder = { Text(stringResource(R.string.placeholder_select_day)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = paymentDayExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = paymentDayExpanded,
                    onDismissRequest = { paymentDayExpanded = false }
                ) {
                    (1..31).forEach { day ->
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.every_day, day)) },
                            onClick = {
                                paymentDay = day
                                paymentDayExpanded = false
                            }
                        )
                    }
                }
            }

            if (modalities.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.label_modalities_section),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 4.dp)
                )
                modalities.forEach { modality ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = modality.id in selectedModalityIds,
                            onCheckedChange = { checked ->
                                selectedModalityIds = if (checked) {
                                    selectedModalityIds + modality.id
                                } else {
                                    selectedModalityIds - modality.id
                                }
                            }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = modality.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            val schedule = modality.schedules.joinToString(" · ").ifBlank { modality.schedule }
                            if (schedule.isNotBlank()) {
                                Text(
                                    text = schedule,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (modality.frequency.isNotBlank()) {
                                Text(
                                    text = modality.frequency,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSaveClick(name, phone, address, birthDate, emergencyContactName, emergencyContact, paymentDay, selectedModalityIds.toList(), registrationDate)
                },
                enabled = uiState !is RegisterStudentUiState.Loading && name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                if (uiState is RegisterStudentUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(stringResource(R.string.btn_save_student))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
