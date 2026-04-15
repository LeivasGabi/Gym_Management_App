package gym.management.presentation.payment

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val ptBR = Locale("pt", "BR")

private fun monthName(month: Int): String = when (month) {
    1 -> "Janeiro"; 2 -> "Fevereiro"; 3 -> "Março"; 4 -> "Abril"
    5 -> "Maio"; 6 -> "Junho"; 7 -> "Julho"; 8 -> "Agosto"
    9 -> "Setembro"; 10 -> "Outubro"; 11 -> "Novembro"; 12 -> "Dezembro"
    else -> ""
}

private fun formatCurrency(value: Double): String =
    NumberFormat.getCurrencyInstance(ptBR).format(value)

private fun formatDate(timestamp: Long): String =
    SimpleDateFormat("dd/MM/yyyy", ptBR).format(Date(timestamp))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    uiState: PaymentScreenState,
    onBackClick: () -> Unit,
    onYearSelected: (Int) -> Unit,
    onMonthSelected: (Int) -> Unit,
    onToggleModality: (String) -> Unit,
    onTogglePayment: (StudentPaymentItem) -> Unit
) {
    val topBarTitle = when (uiState) {
        is PaymentScreenState.YearPicker -> "Pagamentos"
        is PaymentScreenState.MonthPicker -> "${uiState.year}"
        is PaymentScreenState.Detail -> "${monthName(uiState.month)} ${uiState.year}"
        is PaymentScreenState.DetailError -> "${monthName(uiState.month)} ${uiState.year}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(topBarTitle, fontWeight = FontWeight.Bold) },
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
        }
    ) { innerPadding ->
        when (uiState) {
            is PaymentScreenState.YearPicker -> YearPickerView(
                years = uiState.years,
                onYearSelected = onYearSelected,
                modifier = Modifier.padding(innerPadding)
            )

            is PaymentScreenState.MonthPicker -> MonthPickerView(
                months = uiState.months,
                onMonthSelected = onMonthSelected,
                modifier = Modifier.padding(innerPadding)
            )

            is PaymentScreenState.Detail -> DetailView(
                uiState = uiState,
                onToggleModality = onToggleModality,
                onTogglePayment = onTogglePayment,
                modifier = Modifier.padding(innerPadding)
            )

            is PaymentScreenState.DetailError -> {
                Text(
                    text = uiState.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(24.dp)
                )
            }
        }
    }
}

// ── Year Picker ──────────────────────────────────────────────────────────────

@Composable
private fun YearPickerView(
    years: List<Int>,
    onYearSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Selecione o ano",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(years) { year ->
            FilledTonalButton(
                onClick = { onYearSelected(year) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Month Picker ─────────────────────────────────────────────────────────────

@Composable
private fun MonthPickerView(
    months: List<Int>,
    onMonthSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Selecione o mês",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        items(months) { month ->
            FilledTonalButton(
                onClick = { onMonthSelected(month) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = monthName(month),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

// ── Detail View ──────────────────────────────────────────────────────────────

@Composable
private fun DetailView(
    uiState: PaymentScreenState.Detail,
    onToggleModality: (String) -> Unit,
    onTogglePayment: (StudentPaymentItem) -> Unit,
    modifier: Modifier = Modifier
) {
    if (uiState.groups.isEmpty()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhum aluno ativo cadastrado.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        uiState.groups.forEach { group ->
            item(key = "header_${group.modality.id}") {
                ModalitySectionHeader(
                    group = group,
                    onToggle = { onToggleModality(group.modality.id) }
                )
            }

            if (group.isExpanded) {
                items(
                    items = group.students,
                    key = { "${it.student.id}_${it.modality.id}" }
                ) { item ->
                    AnimatedVisibility(
                        visible = true,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        StudentPaymentCard(
                            item = item,
                            onToggle = { onTogglePayment(item) }
                        )
                    }
                }
            }

            item(key = "divider_${group.modality.id}") {
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        item(key = "footer") {
            PaymentFooter(totalPaid = uiState.totalPaid)
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun ModalitySectionHeader(
    group: ModalityPaymentGroup,
    onToggle: () -> Unit
) {
    val rotation = animateFloatAsState(
        targetValue = if (group.isExpanded) 0f else -90f,
        label = "chevron"
    )
    val paid = group.students.count { it.isPaid }
    val total = group.students.size

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = group.modality.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "${formatCurrency(group.modality.price)}/mês  ·  $paid/$total pagos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )
            }
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = if (group.isExpanded) "Recolher" else "Expandir",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier
                    .size(24.dp)
                    .rotate(rotation.value)
            )
        }
    }
}

@Composable
private fun StudentPaymentCard(
    item: StudentPaymentItem,
    onToggle: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.student.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.student.paymentDay > 0) {
                    Text(
                        text = "Vencimento: todo dia ${item.student.paymentDay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (item.isPaid && item.payment?.paidAt != null) {
                    Text(
                        text = "Pago em: ${formatDate(item.payment.paidAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Switch(
                checked = item.isPaid,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun PaymentFooter(totalPaid: Double) {
    Spacer(modifier = Modifier.height(8.dp))
    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Total recebido",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = formatCurrency(totalPaid),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
