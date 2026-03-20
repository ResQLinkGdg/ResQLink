package com.example.resqlink.ui.feature_guide

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.resqlink.rag.GuideChatMessage
import com.example.resqlink.rag.ManualState
import com.example.resqlink.rag.RagViewModel
import com.example.resqlink.rag.StructuredGuideAnswer

@Composable
fun GuideScreen(viewModel: RagViewModel, onSosClick: () -> Unit) {
    val manualState by viewModel.manualState.collectAsState()

    when (manualState) {
        is ManualState.NotInstalled -> ManualInstallScreen(onInstall = { viewModel.installManual() })
        is ManualState.Installing -> ManualInstallingScreen(
            progress = (manualState as ManualState.Installing).progress
        )
        is ManualState.Error -> ManualInstallErrorScreen(
            message = (manualState as ManualState.Error).message,
            onRetry = { viewModel.installManual() }
        )
        is ManualState.Ready -> GuideContent(viewModel = viewModel, onSosClick = onSosClick)
    }
}

@Composable
private fun ManualInstallScreen(onInstall: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE3F2FD)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MenuBook,
                contentDescription = null,
                tint = Color(0xFF1565C0),
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "오프라인 매뉴얼 설치",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "인터넷 없이 응급 가이드를 사용하려면\nAI 매뉴얼을 설치해주세요",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "약 1GB의 저장 공간이 필요합니다",
            fontSize = 14.sp,
            color = Color(0xFFBDBDBD)
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onInstall,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("설치하기", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ManualInstallingScreen(progress: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFFD32F2F),
            modifier = Modifier.size(64.dp),
            strokeWidth = 5.dp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "매뉴얼 설치 중...",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (progress.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = progress,
                fontSize = 14.sp,
                color = Color.Gray
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "앱을 종료하지 마세요",
            fontSize = 14.sp,
            color = Color(0xFFD32F2F)
        )
    }
}

@Composable
private fun ManualInstallErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(64.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "설치 중 오류가 발생했습니다",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("다시 시도", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private data class QuickChip(val label: String, val query: String)

private val quickChips = listOf(
    QuickChip("심폐소생", "심폐소생술 방법을 알려주세요"),
    QuickChip("출혈", "출혈 응급처치 방법을 알려주세요"),
    QuickChip("기도폐쇄", "기도폐쇄 응급처치 방법을 알려주세요"),
    QuickChip("저체온", "저체온증 응급처치 방법을 알려주세요"),
    QuickChip("골절/고정", "골절 응급처치 및 고정 방법을 알려주세요"),
    QuickChip("화상", "화상 응급처치 방법을 알려주세요")
)

@Composable
private fun GuideContent(viewModel: RagViewModel, onSosClick: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isModelReady by viewModel.isModelReady.collectAsState()

    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size, isLoading) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Header
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)) {
            Text(
                text = "응급가이드",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = if (isModelReady) "오프라인 매뉴얼 준비 완료" else "모델 로딩 중...",
                fontSize = 14.sp,
                color = if (isModelReady) Color(0xFF4CAF50) else Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        // SOS Button
        Button(
            onClick = onSosClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 20.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("SOS 긴급 전송", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            if (messages.isEmpty() && !isLoading) {
                item {
                    EmptyStatePlaceholder()
                }
            }

            items(messages, key = { it.id }) { message ->
                ChatBubble(message = message)
            }

            if (isLoading) {
                item {
                    LoadingBubble()
                }
            }
        }

        // Quick chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickChips) { chip ->
                SuggestionChip(
                    onClick = { viewModel.ask(chip.query) },
                    label = { Text(chip.label, fontSize = 13.sp) },
                    enabled = isModelReady && !isLoading
                )
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                enabled = isModelReady && !isLoading,
                placeholder = {
                    Text(
                        if (isModelReady) "질문을 입력해주세요" else "모델 로딩 중...",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedBorderColor = Color(0xFFDDDDDD),
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = Color.Black
                ),
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        viewModel.ask(inputText)
                        inputText = ""
                    }
                },
                enabled = isModelReady && !isLoading && inputText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isModelReady && !isLoading && inputText.isNotBlank())
                            Color(0xFF424242)
                        else
                            Color(0xFFE0E0E0)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "전송",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyStatePlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.QuestionAnswer,
            contentDescription = null,
            tint = Color(0xFFBDBDBD),
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "질문을 입력해주세요",
            fontSize = 16.sp,
            color = Color(0xFFBDBDBD)
        )
        Text(
            text = "응급 상황에 대한 가이드를 제공합니다",
            fontSize = 14.sp,
            color = Color(0xFFE0E0E0),
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
private fun ChatBubble(message: GuideChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start
    ) {
        if (message.isUser) {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(16.dp, 4.dp, 16.dp, 16.dp))
                    .background(Color(0xFFE8E8E8))
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF212121)
                )
            }
        } else if (message.structuredAnswer != null) {
            Box(
                modifier = Modifier
                    .widthIn(max = 320.dp)
                    .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                StructuredGuideCard(answer = message.structuredAnswer)
            }
        } else {
            Box(
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    color = Color(0xFF212121)
                )
            }
        }
    }
}

@Composable
private fun StructuredGuideCard(answer: StructuredGuideAnswer) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Title
        Text(
            text = answer.title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF212121)
        )

        // Summary
        if (answer.summary.isNotEmpty()) {
            Text(
                text = answer.summary,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF424242)
            )
        }

        // Key Actions
        if (answer.key_actions.isNotEmpty()) {
            SectionBox(
                backgroundColor = Color(0xFFE8F5E9),
                barColor = Color(0xFF4CAF50),
                title = "핵심 행동요령",
                titleColor = Color(0xFF2E7D32)
            ) {
                answer.key_actions.forEachIndexed { index, action ->
                    Text(
                        text = "${index + 1}. $action",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF212121)
                    )
                }
            }
        }

        // Detail Steps
        if (answer.detail_steps.isNotEmpty()) {
            SectionBox(
                backgroundColor = Color(0xFFF5F5F5),
                barColor = Color(0xFF9E9E9E),
                title = "상세 단계",
                titleColor = Color(0xFF616161)
            ) {
                answer.detail_steps.forEach { step ->
                    Text(
                        text = "\u2022 $step",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF212121)
                    )
                }
            }
        }

        // Warning
        if (answer.warning.isNotEmpty()) {
            val warningBarColor = Color(0xFFFFA000)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFF8E1))
                    .drawBehind {
                        drawRect(
                            color = warningBarColor,
                            topLeft = Offset.Zero,
                            size = size.copy(width = 4.dp.toPx())
                        )
                    }
                    .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFFA000),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = answer.warning,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE65100)
                )
            }
        }

        // Sources
        if (answer.source_titles.isNotEmpty()) {
            Text(
                text = answer.source_titles.joinToString(" \u00B7 "),
                fontSize = 12.sp,
                color = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun SectionBox(
    backgroundColor: Color,
    barColor: Color,
    title: String,
    titleColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .drawBehind {
                drawRect(
                    color = barColor,
                    topLeft = Offset.Zero,
                    size = size.copy(width = 4.dp.toPx())
                )
            }
            .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = titleColor
        )
        content()
    }
}

@Composable
private fun LoadingBubble() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .background(Color(0xFFF5F5F5))
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFFD32F2F)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "답변 생성 중...",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
