package com.example.resqlink.ui.feature_sos.compose

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.resqlink.ui.feature_sos.compose.model.SosComposeUiState
import com.example.resqlink.ui.feature_sos.compose.model.SosSituationUi
import com.example.resqlink.ui.feature_sos.compose.model.SosUrgencyUi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SosComposeScreen(
    state: SosComposeUiState,
    onSelectUrgency: (SosUrgencyUi) -> Unit,
    onSelectSituation: (SosSituationUi) -> Unit,
    onIncreasePeople: () -> Unit,
    onDecreasePeople: () -> Unit,
    onHintChange: (String) -> Unit,
    onToggleLocation: (Boolean) -> Unit,
    onClickSend: () -> Unit,
    onClickBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SOS 보내기") },
                navigationIcon = {
                    IconButton(onClick = onClickBack) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "닫기"
                        )
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onClickSend,
                enabled = state.canSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text("지금 SOS 전송")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {

            /* ---------- 긴급도 ---------- */
            Text("긴급도 (선택)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SosUrgencyUi.values().forEach {
                    FilterChip(
                        selected = state.urgency == it,
                        onClick = { onSelectUrgency(it) },
                        label = { Text(it.label) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- 상황 ---------- */
            Text("상황 (선택)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SosSituationUi.values().forEach {
                    FilterChip(
                        selected = state.situation == it,
                        onClick = { onSelectSituation(it) },
                        label = { Text(it.label) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- 인원 ---------- */
            Text("대략 인원 (선택)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(onClick = onDecreasePeople) {
                    Text("−", style = MaterialTheme.typography.headlineMedium)
                }
                Text(
                    text = state.peopleCount.toString(),
                    style = MaterialTheme.typography.headlineMedium
                )
                IconButton(onClick = onIncreasePeople) {
                    Text("+", style = MaterialTheme.typography.headlineMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            /* ---------- 힌트 ---------- */
            Text("추가 힌트 (선택, 짧게)", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = state.hint,
                onValueChange = onHintChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("예: B2 주차장 기둥 옆") },
                supportingText = {
                    Text("${state.hint.length}/40자")
                },
                singleLine = true
            )

            Spacer(Modifier.height(24.dp))

            /* ---------- 위치 ---------- */
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "현재 위치 포함",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = state.includeLocation,
                    onCheckedChange = onToggleLocation
                )
            }

            Spacer(Modifier.height(4.dp))
            Text(
                text = "* GPS 가능할 때만 거친 위치 사용",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
