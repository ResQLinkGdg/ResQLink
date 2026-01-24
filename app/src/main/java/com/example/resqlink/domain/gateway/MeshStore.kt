package com.example.resqlink.domain.gateway

import com.example.resqlink.domain.model.NodeSummary
import com.example.resqlink.domain.model.SeenResult

interface MeshStore {

    /** 중복 메시지 제거용 (msgId 기준) */
    fun markSeen(msgId: String, now: Long): SeenResult

    /** 특정 origin(피해자/발신자) 노드가 관측됨을 기록 */
    fun upsertNodeSeen(originId: String, now: Long, hopFromResponder: Int? = null)

    /**
     * 최근 sinceTime 이후에 관측된 노드 목록 반환
     * - 보통 스냅샷(UI) 생성에 사용
     */
    fun listLiveNodes(sinceTime: Long, now: Long): List<NodeSummary>
}