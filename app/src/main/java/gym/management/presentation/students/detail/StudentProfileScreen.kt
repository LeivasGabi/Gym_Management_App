package gym.management.presentation.students.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gym.management.domain.model.Modality
import gym.management.domain.model.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentProfileScreen(
    uiState: StudentProfileUiState,
    saveState: SaveState,
    onBackClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    onSave: (phone: String, address: String, emergencyContactName: String, emergencyContact: String, paymentDay: Int, modalityIds: List<String>) -> Unit,
    onSaveHandled: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(saveState) {
        when (saveState) {
            is SaveState.Success -> {
                snackbarHostState.showSnackbar("Dados salvos com sucesso!")
                onSaveHandled()
            }
            is SaveState.Error -> {
                snackbarHostState.showSnackbar(saveState.message)
                onSaveHandled()
            }
            else -> Unit
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val title = if (uiState is StudentProfileUiState.Success)
                        uiState.student.name else "Perfil"
                    Text(text = title, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { innerPadding ->
        when (uiState) {
            is StudentProfileUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is StudentProfileUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.message,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }
            }

            is StudentProfileUiState.Success -> {
                StudentProfileContent(
                    student = uiState.student,
                    availableModalities = uiState.availableModalities,
                    isSaving = saveState is SaveState.Loading,
                    onToggleActive = onToggleActive,
                    onSave = onSave,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StudentProfileContent(
    student: Student,
    availableModalities: List<Modality>,
    isSaving: Boolean,
    onToggleActive: (Boolean) -> Unit,
    onSave: (phone: String, address: String, emergencyContactName: String, emergencyContact: String, paymentDay: Int, modalityIds: List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var phone by rememberSaveable(student.phone) { mutableStateOf(student.phone) }
    var address by rememberSaveable(student.address) { mutableStateOf(student.address) }
    var emergencyContactName by rememberSaveable(student.emergencyContactName) { mutableStateOf(student.emergencyContactName) }
    var emergencyContact by rememberSaveable(student.emergencyContact) { mutableStateOf(student.emergencyContact) }
    var paymentDay by rememberSaveable(student.paymentDay) { mutableStateOf(student.paymentDay) }
    var paymentDayExpanded by remember { mutableStateOf(false) }
    var selectedModalityIds by rememberSaveable(student.modalityIds) { mutableStateOf(student.modalityIds.toSet()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ProfileField(label = "Nome", value = student.name)
        HorizontalDivider()
        ProfileField(label = "Data de nascimento", value = student.birthDate.ifBlank { "—" })

        Spacer(modifier = Modifier.height(4.dp))

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Telefone") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Endereço") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        Text(
            text = "Contato de Emergência",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        OutlinedTextField(
            value = emergencyContactName,
            onValueChange = { emergencyContactName = it },
            label = { Text("Nome do contato") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = emergencyContact,
            onValueChange = { emergencyContact = it },
            label = { Text("Telefone do contato") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        HorizontalDivider()

        ExposedDropdownMenuBox(
            expanded = paymentDayExpanded,
            onExpandedChange = { paymentDayExpanded = it }
        ) {
            OutlinedTextField(
                value = if (paymentDay == 0) "" else "Todo dia $paymentDay",
                onValueChange = {},
                readOnly = true,
                label = { Text("Dia de pagamento") },
                placeholder = { Text("Selecione o dia") },
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
                        text = { Text("Todo dia $day") },
                        onClick = {
                            paymentDay = day
                            paymentDayExpanded = false
                        }
                    )
                }
            }
        }

        if (availableModalities.isNotEmpty()) {
            HorizontalDivider()

            Text(
                text = "Modalidades",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            availableModalities.forEach { modality ->
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
                    Text(
                        text = modality.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Button(
            onClick = {
                onSave(phone, address, emergencyContactName, emergencyContact, paymentDay, selectedModalityIds.toList())
            },
            enabled = !isSaving,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Salvar alterações")
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Aluno inativo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Switch(
                checked = !student.active,
                onCheckedChange = { inactive -> onToggleActive(!inactive) }
            )
        }
    }
}

@Composable
private fun ProfileField(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
