package com.example.resqlink.rag.database


// 테스트용 더미 데이터 (와이파이, 배터리, 앱 오류 관련 매뉴얼)
val sampleDataPack = listOf(
    Manual(
        id = "M1",
        title = "와이파이 연결 방법",
        content = "설정 앱에서 네트워크 및 인터넷을 선택한 후 Wi-Fi를 활성화하세요. 목록에서 원하는 네트워크를 선택하고 비밀번호를 입력하면 연결됩니다. 연결이 안 될 경우 라우터를 재부팅하세요.",
        keywords = "인터넷,와이파이,네트워크,연결",
        category = "설정"
    ),
    Manual(
        id = "M2",
        title = "배터리 수명 연장 팁",
        content = "배터리를 오래 쓰려면 화면 밝기를 줄이고, 사용하지 않는 앱의 백그라운드 활동을 제한하세요. 또한 배터리 절약 모드를 켜는 것이 큰 도움이 됩니다.",
        keywords = "전원,배터리,절전,수명",
        category = "관리"
    ),
    Manual(
        id = "M3",
        title = "앱 강제 종료 방법",
        content = "앱이 응답하지 않거나 먹통이 되면, 최근 앱 버튼을 누르고 해당 앱을 위로 밀어서 종료하세요. 설정 > 애플리케이션 정보에서 '강제 중단'을 눌러도 됩니다.",
        keywords = "오류,앱종료,먹통,버그",
        category = "문제해결"
    )
)