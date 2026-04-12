package gym.management.presentation.register.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import gym.management.domain.model.Modality

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStudentScreen(
    uiState: RegisterStudentUiState,
    modalities: List<Modality>,
    modalitiesLoaded: Boolean,
    onSaveClick: (name: String, phone: String, address: String, birthDate: String, emergencyContact: String, modalityIds: List<String>) -> Unit,
    onSuccess: () -> Unit,
    onErrorShown: () -> Unit,
    onNavigateBack: () -> Unit,
    onAddModalityClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf(TextFieldValue("")) }
    var emergencyContact by remember { mutableStateOf("") }
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
            title = { Text("Modalidade necessária") },
            text = { Text("Você precisa cadastrar pelo menos uma modalidade antes de cadastrar um aluno.") },
            confirmButton = {
                Button(onClick = onAddModalityClick) {
                    Text("Adicionar Modalidade")
                }
            },
            dismissButton = {
                TextButton(onClick = onNavigateBack) {
                    Text("Voltar")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Cadastrar Aluno") },
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
                label = { Text("Nome") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Telefone") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Endereço") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = birthDate,
                onValueChange = { input ->
                    val digits = input.text.filter { it.isDigit() }.take(8)
                    val formatted = buildString {
                        digits.forEachIndexed { index, char ->
                            if (index == 2 || index == 4) append('/')
                            append(char)
                        }
                    }
                    birthDate = TextFieldValue(
                        text = formatted,
                        selection = TextRange(formatted.length)
                    )
                },
                label = { Text("Data de Nascimento") },
                placeholder = { Text("dd/mm/aaaa") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = emergencyContact,
                onValueChange = { emergencyContact = it },
                label = { Text("Contato de Emergência") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (modalities.isNotEmpty()) {
                Text(
                    text = "Modalidades",
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
                        Text(
                            text = "${modality.name} — ${modality.frequency} ${modality.schedule}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onSaveClick(name, phone, address, birthDate.text, emergencyContact, selectedModalityIds.toList())
                },
                enabled = uiState !is RegisterStudentUiState.Loading && name.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState is RegisterStudentUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salvar Aluno")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
