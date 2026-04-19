package gym.management.presentation.modalities

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import gym.management.domain.model.Modality
import gym.management.presentation.common.WheelTimePickerDialog

private val DAYS_OF_WEEK = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalityStudentsScreen(
    modalityName: String,
    uiState: ModalityStudentsUiState,
    editSaveState: ModalityEditSaveState,
    deleteState: ModalityDeleteState,
    onBackClick: () -> Unit,
    onUpdateModality: (name: String, schedules: List<String>, price: String, frequency: String, active: Boolean) -> Unit,
    onEditSaveHandled: () -> Unit,
    onDeleteModality: () -> Unit,
    onDeleteHandled: () -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    var showEditDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(deleteState) {
        when (deleteState) {
            is ModalityDeleteState.Success -> {
                onDeleteHandled()
                onBackClick()
            }
            is ModalityDeleteState.Error -> {
                snackbarHostState.showSnackbar(deleteState.message)
                onDeleteHandled()
            }
            else -> Unit
        }
    }

    val currentModality = (uiState as? ModalityStudentsUiState.Success)?.modality
    val title = currentModality?.name ?: modalityName

    LaunchedEffect(editSaveState) {
        when (editSaveState) {
            is ModalityEditSaveState.Success -> {
                showEditDialog = false
                snackbarHostState.showSnackbar("Modalidade atualizada!")
                onEditSaveHandled()
            }
            is ModalityEditSaveState.Error -> {
                snackbarHostState.showSnackbar(editSaveState.message)
                onEditSaveHandled()
            }
            else -> Unit
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (deleteState !is ModalityDeleteState.Loading) showDeleteDialog = false },
            title = { Text("Excluir modalidade") },
            text = { Text("Tem certeza que deseja excluir a modalidade \"${currentModality?.name ?: modalityName}\"? Esta ação não pode ser desfeita.") },
            confirmButton = {
                Button(
                    onClick = onDeleteModality,
                    enabled = deleteState !is ModalityDeleteState.Loading,
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (deleteState is ModalityDeleteState.Loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Excluir")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = false },
                    enabled = deleteState !is ModalityDeleteState.Loading
                ) { Text("Cancelar") }
            }
        )
    }

    if (showEditDialog && currentModality != null) {
        EditModalityDialog(
            modality = currentModality,
            isSaving = editSaveState is ModalityEditSaveState.Loading,
            onDismiss = { showEditDialog = false },
            onConfirm = { name, schedules, price, frequency, active ->
                onUpdateModality(name, schedules, price, frequency, active)
            }
        )
    }

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
                actions = {
                    if (currentModality != null) {
                        IconButton(onClick = { showEditDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Editar modalidade",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Excluir modalidade",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data -> Snackbar(snackbarData = data) }
        }
    ) { innerPadding ->
        when (uiState) {
            is ModalityStudentsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ModalityStudentsUiState.Error -> {
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

            is ModalityStudentsUiState.Success -> {
                if (uiState.students.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Nenhum aluno ativo nesta modalidade.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.students, key = { it.student.id }) { item ->
                            StudentItem(item = item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentItem(item: StudentWithLatestBelt) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = item.student.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (item.latestBelt != null) {
                Text(
                    text = item.latestBelt,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun EditModalityDialog(
    modality: Modality,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (name: String, schedules: List<String>, price: String, frequency: String, active: Boolean) -> Unit
) {
    val initialSchedules = modality.schedules.ifEmpty { listOf("08:00") }
    val initialDays = modality.frequency.split(", ").filter { it.isNotBlank() }.toSet()

    var name by rememberSaveable { mutableStateOf(modality.name) }
    var schedules by remember { mutableStateOf(initialSchedules) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var price by rememberSaveable { mutableStateOf("%.2f".format(modality.price)) }
    var selectedDays by rememberSaveable { mutableStateOf(initialDays) }
    var active by rememberSaveable { mutableStateOf(modality.active) }

    if (editingIndex != null) {
        val idx = editingIndex!!
        val parts = schedules.getOrElse(idx) { "08:00" }.split(":")
        WheelTimePickerDialog(
            initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 8,
            initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0,
            onDismiss = {
                if (idx >= schedules.size) schedules = schedules.dropLast(1)
                editingIndex = null
            },
            onConfirm = { hour, minute ->
                val newTime = "%02d:%02d".format(hour, minute)
                schedules = schedules.toMutableList().also { list ->
                    if (idx < list.size) list[idx] = newTime else list.add(newTime)
                }
                editingIndex = null
            }
        )
    }

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        title = { Text("Editar Modalidade") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Frequência",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
                    DAYS_OF_WEEK.forEach { day ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Checkbox(
                                checked = day in selectedDays,
                                onCheckedChange = { checked ->
                                    selectedDays = if (checked) selectedDays + day else selectedDays - day
                                }
                            )
                            Text(text = day, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Text(
                    text = "Horários",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        schedules.forEachIndexed { index, schedule ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = schedule,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { editingIndex = index },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Editar",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                if (schedules.size > 1) {
                                    IconButton(
                                        onClick = {
                                            schedules = schedules.filterIndexed { i, _ -> i != index }
                                        },
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remover",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }
                        }
                        TextButton(
                            onClick = {
                                val newIndex = schedules.size
                                schedules = schedules + "08:00"
                                editingIndex = newIndex
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adicionar horário", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Valor (R$)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Modalidade ativa",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Switch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val frequency = selectedDays
                        .sortedBy { DAYS_OF_WEEK.indexOf(it) }
                        .joinToString(", ")
                    onConfirm(name, schedules, price, frequency, active)
                },
                enabled = !isSaving && name.isNotBlank() && selectedDays.isNotEmpty()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Salvar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) { Text("Cancelar") }
        }
    )
}
