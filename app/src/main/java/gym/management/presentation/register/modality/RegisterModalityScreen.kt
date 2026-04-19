package gym.management.presentation.register.modality

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import gym.management.presentation.common.WheelTimePickerDialog

private val DAYS_OF_WEEK = listOf("Segunda", "Terça", "Quarta", "Quinta", "Sexta", "Sábado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterModalityScreen(
    uiState: RegisterModalityUiState,
    onSaveClick: (name: String, schedules: List<String>, price: String, frequency: String) -> Unit,
    onSuccess: () -> Unit,
    onErrorShown: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var schedules by remember { mutableStateOf(listOf("08:00")) }
    var editingIndex by remember { mutableStateOf<Int?>(null) }
    var price by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState) {
        when (uiState) {
            is RegisterModalityUiState.Success -> onSuccess()
            is RegisterModalityUiState.Error -> {
                snackbarHostState.showSnackbar(uiState.message)
                onErrorShown()
            }
            else -> Unit
        }
    }

    if (uiState is RegisterModalityUiState.Conflict) {
        val names = uiState.conflictingNames
        AlertDialog(
            onDismissRequest = { onErrorShown() },
            title = { Text("Conflito de horário") },
            text = {
                val listText = names.joinToString("\n") { "• $it" }
                Text("Já existe${if (names.size > 1) "m" else ""} modalidade${if (names.size > 1) "s" else ""} cadastrada${if (names.size > 1) "s" else ""} neste mesmo dia e horário:\n\n$listText")
            },
            confirmButton = {
                Button(onClick = { onErrorShown() }) { Text("Entendi") }
            }
        )
    }

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

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Modalidade") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
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
                label = { Text("Nome da Modalidade") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = "Frequência",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
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
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
                            IconButton(onClick = { editingIndex = index }) {
                                Icon(
                                    imageVector = Icons.Default.AccessTime,
                                    contentDescription = "Editar horário",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            if (schedules.size > 1) {
                                IconButton(onClick = {
                                    schedules = schedules.filterIndexed { i, _ -> i != index }
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remover horário",
                                        tint = MaterialTheme.colorScheme.error
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
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Adicionar horário")
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

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    val frequency = selectedDays
                        .sortedBy { DAYS_OF_WEEK.indexOf(it) }
                        .joinToString(", ")
                    onSaveClick(name, schedules, price, frequency)
                },
                enabled = uiState !is RegisterModalityUiState.Loading
                        && name.isNotBlank()
                        && selectedDays.isNotEmpty(),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 50.dp)
            ) {
                if (uiState is RegisterModalityUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar Modalidade")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
