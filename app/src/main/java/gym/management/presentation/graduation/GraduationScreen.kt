package gym.management.presentation.graduation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import gym.management.domain.model.Graduation
import gym.management.domain.model.Modality
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale("pt", "BR")).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraduationScreen(
    uiState: GraduationUiState,
    saveState: GraduationSaveState,
    studentId: String,
    onBackClick: () -> Unit,
    onAddGraduation: (modalityId: String, modalityName: String, belt: String, generalGrade: String, observation: String, date: Long) -> Unit,
    onUpdateGraduation: (Graduation) -> Unit,
    onSaveHandled: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by rememberSaveable { mutableStateOf(false) }
    var editingGraduation by remember { mutableStateOf<Graduation?>(null) }

    LaunchedEffect(saveState) {
        when (saveState) {
            is GraduationSaveState.Success -> {
                showAddDialog = false
                editingGraduation = null
                snackbarHostState.showSnackbar("Graduação salva com sucesso!")
                onSaveHandled()
            }
            is GraduationSaveState.Error -> {
                snackbarHostState.showSnackbar(saveState.message)
                onSaveHandled()
            }
            else -> Unit
        }
    }

    val title = if (uiState is GraduationUiState.Success) uiState.studentName else "Graduação"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = title, fontWeight = FontWeight.Bold) },
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
        floatingActionButton = {
            if (uiState is GraduationUiState.Success) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Adicionar graduação",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        }
    ) { innerPadding ->
        when (uiState) {
            is GraduationUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is GraduationUiState.Error -> {
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

            is GraduationUiState.Success -> {
                GraduationContent(
                    groups = uiState.groups,
                    onAddClick = { showAddDialog = true },
                    onEditClick = { editingGraduation = it },
                    modifier = Modifier.padding(innerPadding)
                )

                if (showAddDialog) {
                    GraduationDialog(
                        title = "Adicionar Graduação",
                        modalities = uiState.studentModalities,
                        isSaving = saveState is GraduationSaveState.Loading,
                        onDismiss = { showAddDialog = false },
                        onConfirm = { modalityId, modalityName, belt, grade, observation, date ->
                            onAddGraduation(modalityId, modalityName, belt, grade, observation, date)
                        }
                    )
                }

                editingGraduation?.let { graduation ->
                    GraduationDialog(
                        title = "Editar Graduação",
                        modalities = uiState.studentModalities,
                        initial = graduation,
                        isSaving = saveState is GraduationSaveState.Loading,
                        onDismiss = { editingGraduation = null },
                        onConfirm = { modalityId, modalityName, belt, grade, observation, date ->
                            onUpdateGraduation(
                                graduation.copy(
                                    modalityId = modalityId,
                                    modalityName = modalityName,
                                    belt = belt,
                                    generalGrade = grade,
                                    observation = observation,
                                    date = date
                                )
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GraduationContent(
    groups: List<GraduationGroup>,
    onAddClick: () -> Unit,
    onEditClick: (Graduation) -> Unit,
    modifier: Modifier = Modifier
) {
    if (groups.all { it.graduations.isEmpty() }) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Nenhuma graduação registrada.",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onAddClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Adicionar graduação")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        groups.forEach { group ->
            if (group.graduations.isEmpty()) return@forEach

            item(key = "header_${group.modality.id}") {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = group.modality.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(4.dp))
            }

            items(items = group.graduations, key = { it.id }) { graduation ->
                GraduationCard(
                    graduation = graduation,
                    onEditClick = { onEditClick(graduation) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(88.dp)) }
    }
}

@Composable
private fun GraduationCard(
    graduation: Graduation,
    onEditClick: () -> Unit
) {
    val dateFormatted = remember(graduation.date) {
        dateFormatter.format(Date(graduation.date))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = graduation.belt,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar graduação",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (graduation.generalGrade.isNotBlank()) {
                Text(
                    text = "Nota geral: ${graduation.generalGrade}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (graduation.observation.isNotBlank()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp))
                Text(
                    text = graduation.observation,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GraduationDialog(
    title: String,
    modalities: List<Modality>,
    initial: Graduation? = null,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (modalityId: String, modalityName: String, belt: String, generalGrade: String, observation: String, date: Long) -> Unit
) {
    val initialModality = modalities.find { it.id == initial?.modalityId } ?: modalities.firstOrNull()
    var selectedModality by remember { mutableStateOf(initialModality) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var belt by rememberSaveable { mutableStateOf(initial?.belt ?: "") }
    var generalGrade by rememberSaveable { mutableStateOf(initial?.generalGrade ?: "") }
    var observation by rememberSaveable { mutableStateOf(initial?.observation ?: "") }

    val initialDateMillis = initial?.date ?: System.currentTimeMillis()
    var selectedDateMillis by rememberSaveable { mutableStateOf(initialDateMillis) }
    var showDatePicker by remember { mutableStateOf(false) }

    val selectedDateFormatted = remember(selectedDateMillis) {
        dateFormatter.format(Date(selectedDateMillis))
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = utcMidnight(selectedDateMillis)
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedModality?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Modalidade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        modalities.forEach { modality ->
                            DropdownMenuItem(
                                text = { Text(modality.name) },
                                onClick = {
                                    selectedModality = modality
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = belt,
                    onValueChange = { belt = it },
                    label = { Text("Faixa") },
                    placeholder = { Text("Ex: Faixa Amarela 8 gub") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = generalGrade,
                    onValueChange = { generalGrade = it },
                    label = { Text("Nota geral") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = selectedDateFormatted,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Data da graduação") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Selecionar data"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = observation,
                    onValueChange = { observation = it },
                    label = { Text("Observação") },
                    placeholder = { Text("Resumo sobre o desempenho do aluno...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    maxLines = 6
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val modality = selectedModality ?: return@Button
                    onConfirm(modality.id, modality.name, belt, generalGrade, observation, selectedDateMillis)
                },
                enabled = !isSaving && selectedModality != null && belt.isNotBlank()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text("Cancelar")
            }
        }
    )
}

private fun utcMidnight(millis: Long): Long = (millis / 86_400_000L) * 86_400_000L
