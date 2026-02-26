package com.example.resqlink.ui.feature_guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.resqlink.rag.RagViewModel

// 🟢 [수정] ViewModel 파라미터 추가
@Composable
fun GuideScreen(
    viewModel: RagViewModel
) {
    // ViewModel 상태 구독
    val answer by viewModel.answer.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 팝업 표시 여부 제어 (로딩 중이거나 답변이 있으면 표시)
    var showDialog by remember { mutableStateOf(false) }

    // 답변이나 로딩 상태가 변하면 다이얼로그 상태 업데이트
    LaunchedEffect(isLoading, answer) {
        if (isLoading || answer.isNotEmpty()) {
            showDialog = true
        }
    }

    // 카테고리 데이터
    val categories = listOf(
        CategoryItem("심폐소생", Icons.Default.Favorite, Color(0xFFFBE9E7), Color(0xFFD32F2F)),
        CategoryItem("출혈", Icons.Default.WaterDrop, Color(0xFFFFF3E0), Color(0xFFE65100)),
        CategoryItem("기도폐쇄", Icons.Default.Air, Color(0xFFE3F2FD), Color(0xFF1565C0)),
        CategoryItem("저체온", Icons.Default.Shield, Color(0xFFF3E5F5), Color(0xFF7B1FA2)),
        CategoryItem("골절/고정", Icons.Default.MedicalServices, Color(0xFFE8F5E9), Color(0xFF2E7D32)),
        CategoryItem("화상", Icons.Default.LocalFireDepartment, Color(0xFFFFFDE7), Color(0xFFFBC02D))
    )

    // 🟢 [추가] 결과 팝업 다이얼로그
    if (showDialog) {
        RagResponseDialog(
            isLoading = isLoading,
            answer = answer,
            onDismiss = {
                showDialog = false
                viewModel.clearAnswer() // 닫을 때 상태 초기화
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(20.dp)
    ) {
        // 1. 헤더
        Text(
            text = "응급가이드",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "오프라인 매뉴얼 제공",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // 2. 검색창 (버튼 포함) -> ViewModel 함수 전달
        GuideSearchBar(
            onSearch = { query ->
                viewModel.ask(query)
                showDialog = true
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 3. 빠른 주제 그리드
        Text(
            text = "빠른 주제",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(categories) { item ->
                CategoryCard(item)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 4. 하단 최근 검색
        RecentSearchSection()
    }
}

// 🟢 [추가] 팝업 UI 컴포넌트
@Composable
fun RagResponseDialog(
    isLoading: Boolean,
    answer: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 500.dp), // 최대 높이 제한
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // 상단 헤더 (제목 + 닫기 버튼)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "AI 가이드 답변",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "닫기",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 본문 내용 (로딩 중 vs 답변)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false) // 내용만큼 높이 차지하되 최대치까지
                ) {
                    if (isLoading) {
                        Column(
                            modifier = Modifier.align(Alignment.Center),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFD32F2F),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("매뉴얼을 분석 중입니다...", color = Color.Gray)
                        }
                    } else {
                        // 스크롤 가능한 텍스트 영역
                        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                            Text(
                                text = answer.ifBlank { "답변을 가져올 수 없습니다." },
                                fontSize = 16.sp,
                                lineHeight = 24.sp,
                                color = Color(0xFF424242)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GuideSearchBar(onSearch: (String) -> Unit) { // 콜백 추가
    var searchText by remember { mutableStateOf("") }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { searchText = it },
            placeholder = {
                Text("예: 피가 멈추지 않아요", color = Color.Gray, fontSize = 14.sp)
            },
            leadingIcon = {
                Icon(Icons.Outlined.Search, contentDescription = null, tint = Color.Gray)
            },
            shape = RoundedCornerShape(12.dp),
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
                .height(56.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(
            onClick = { onSearch(searchText) }, // 🟢 버튼 클릭 시 콜백 호출
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
        ) {
            Text("검색")
        }
    }
}

// ... 나머지 CategoryCard, RecentSearchSection 등은 기존 유지 ...
@Composable
fun CategoryCard(category: CategoryItem) {
    Box(
        modifier = Modifier
            .aspectRatio(1.6f)
            .clip(RoundedCornerShape(16.dp))
            .background(category.backgroundColor)
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = category.iconColor,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = category.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = category.iconColor
            )
        }
    }
}

@Composable
fun RecentSearchSection() {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "최근 검색",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            val recentItems = listOf("출혈 응급처치", "심폐소생술 순서", "저체온증 증상")
            recentItems.forEach { item ->
                Text(
                    text = "•  $item",
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

data class CategoryItem(
    val title: String,
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconColor: Color
)