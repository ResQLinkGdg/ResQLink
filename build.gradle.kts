plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false

    // RAG-base에서 추가된 Serialization 플러그인 (새 기능)
    alias(libs.plugins.kotlin.serialization) apply false
}