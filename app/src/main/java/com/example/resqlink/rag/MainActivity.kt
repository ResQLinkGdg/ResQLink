package com.example.resqlink.rag


import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.resqlink.rag.database.AppDatabase
import com.example.resqlink.rag.EmbeddingHelper
import com.example.resqlink.rag.generation.GenAiManager
import com.example.resqlink.rag.database.ManualSearchManager
import com.example.resqlink.rag.RagIntegrationTester
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    // 화면에 로그를 찍어주기 위한 텍스트뷰 (XML 없이 코드로 생성)
    private lateinit var statusTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. 간단한 UI 설정 (테스트 진행상황을 화면에 표시)
        statusTextView = TextView(this).apply {
            text = "RAG 시스템 초기화 중..."
            textSize = 18f
            setPadding(50, 50, 50, 50)
        }
        setContentView(statusTextView)

        // 2. 비동기 작업 시작
        lifecycleScope.launch {
            try {
                runRagSystemTest()
            } catch (e: Exception) {
                Log.e("RAG_TEST", "테스트 중 치명적 오류 발생", e)
                statusTextView.text = "오류 발생: ${e.message}"
            }
        }
    }

    private suspend fun runRagSystemTest() {
        updateStatus("1. 데이터베이스 구축 중...")

        // [초기화] Room DB 빌드
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "rag-database" // DB 파일 이름
        )
            .fallbackToDestructiveMigration() // 스키마 변경 시 기존 데이터 삭제 (테스트용)
            .build()

        // [초기화] 각 매니저 클래스 생성
        val dao = db.manualDao()

        // assets 폴더의 모델 파일을 로드하므로 Context(this)가 필요함
        val embeddingHelper = EmbeddingHelper(this)

        val searchManager = ManualSearchManager(dao, embeddingHelper)

        // GenAiManager 생성 (API Key 방식 또는 Nano 방식에 따라 생성자 파라미터 확인 필요)
        // 만약 API Key를 쓴다면: GenAiManager("YOUR_API_KEY")
        // 만약 Nano(Context)를 쓴다면: GenAiManager(this)
        val genAiManager = GenAiManager(this)

        // [테스트] 통합 테스터 실행
        val tester = RagIntegrationTester(dao, embeddingHelper, searchManager, genAiManager)

        updateStatus("2. 테스트 데이터 주입 및 검색 시작...")
        delay(1000) // UI 갱신을 위해 잠시 대기

        // 실제 테스트 로직 실행 (Logs는 Logcat 확인)
        tester.runFullTest()

        updateStatus("3. 테스트 완료!\nLogcat에서 'RAG_TEST' 태그를 확인하세요.")
    }

    // 화면에 글씨를 바꿔주는 도우미 함수
    private fun updateStatus(message: String) {
        runOnUiThread {
            statusTextView.text = message
        }
        Log.d("RAG_ACTIVITY", message)
    }
}