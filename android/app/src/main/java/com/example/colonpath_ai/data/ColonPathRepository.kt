package com.example.colonpath_ai.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.colonpath_ai.network.CaseResultDto
import com.example.colonpath_ai.network.ColonPathApiClient
import com.example.colonpath_ai.network.CopilotAnswerDto
import com.example.colonpath_ai.network.ApiErrorDto

enum class AnalysisState {
    IDLE,
    UPLOADING,
    PROCESSING,
    SUCCESS,
    ERROR
}

object ColonPathRepository {
    var activeCaseId by mutableStateOf<String?>(null)
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedBitmap by mutableStateOf<Bitmap?>(null)
    var selectedImageName by mutableStateOf<String>("")

    var currentAnalysisState by mutableStateOf(AnalysisState.IDLE)
    var activeCaseStage by mutableStateOf<String?>("RECEIVED")
    var analysisErrorMessage by mutableStateOf<String?>(null)
    var currentApiError by mutableStateOf<ApiErrorDto?>(null)

    // Current real CaseResult received from backend
    var currentCaseResult by mutableStateOf<CaseResultDto?>(null)

    // Cached cases list
    val casesList = mutableStateListOf<CaseResultDto>()

    // Copilot Q&A history
    val copilotHistory = mutableStateListOf<CopilotAnswerDto>()
    var isCopilotLoading by mutableStateOf(false)

    fun clearSelection() {
        selectedImageUri = null
        selectedBitmap = null
        selectedImageName = ""
        currentAnalysisState = AnalysisState.IDLE
        analysisErrorMessage = null
        currentApiError = null
    }

    fun resetState() {
        clearSelection()
        currentCaseResult = null
        activeCaseId = null
        copilotHistory.clear()
    }

    /**
     * Loads a specific CaseResult from backend and switches active case with isolated Copilot session.
     */
    suspend fun loadCaseResult(caseId: String): Result<CaseResultDto> {
        activeCaseId = caseId
        currentCaseResult = null // Strict Case isolation: prevent cross-case stale data during transitions
        copilotHistory.clear() // Strict Copilot session isolation when switching cases
        val result = ColonPathApiClient.getCaseResult(caseId)
        result.onSuccess { dto ->
            currentCaseResult = dto
            activeCaseStage = dto.lifecycle_state ?: "COMPLETED"
        }
        return result
    }

    /**
     * Fetches case list from backend API into casesList.
     */
    suspend fun fetchCasesList(): Result<List<CaseResultDto>> {
        val result = ColonPathApiClient.listCases()
        result.onSuccess { list ->
            casesList.clear()
            casesList.addAll(list)
        }
        return result
    }

    /**
     * Triggers real backend pipeline execution with the selected image bitmap.
     */
    suspend fun executeAnalysis(caseId: String? = null, forceReanalyze: Boolean = false): Result<CaseResultDto> {
        val bmp = selectedBitmap
            ?: return Result.failure(IllegalStateException("No image selected for analysis. Please select an H&E image first."))

        currentAnalysisState = AnalysisState.UPLOADING
        activeCaseStage = "VALIDATING"
        analysisErrorMessage = null
        currentApiError = null

        val cid = caseId ?: activeCaseId ?: "CASE_${System.currentTimeMillis()}"
        activeCaseId = cid

        currentAnalysisState = AnalysisState.PROCESSING
        activeCaseStage = "CV_PROCESSING"
        val result = ColonPathApiClient.analyzeImage(
            imageBitmap = bmp,
            caseId = cid,
            fileName = selectedImageName.ifBlank { "specimen.png" }
        )

        result.onSuccess { dto ->
            currentCaseResult = dto
            activeCaseStage = dto.lifecycle_state ?: "COMPLETED"
            currentAnalysisState = AnalysisState.SUCCESS
            // Prepend to casesList if not already present
            val existingIdx = casesList.indexOfFirst { it.case_id == dto.case_id }
            if (existingIdx >= 0) {
                casesList[existingIdx] = dto
            } else {
                casesList.add(0, dto)
            }
        }.onFailure { err ->
            currentAnalysisState = AnalysisState.ERROR
            activeCaseStage = "FAILED"
            analysisErrorMessage = err.message ?: "Analysis execution failed"
        }

        return result
    }

    /**
     * Asks Pathologist Copilot an evidence-grounded question.
     */
    suspend fun askCopilot(question: String, regionId: String? = null): Result<CopilotAnswerDto> {
        val cid = activeCaseId
            ?: return Result.failure(IllegalStateException("No active case selected for Copilot inquiry."))

        isCopilotLoading = true
        val result = ColonPathApiClient.askCopilot(
            caseId = cid,
            question = question,
            regionId = regionId
        )
        isCopilotLoading = false

        result.onSuccess { answer ->
            copilotHistory.add(answer)
        }
        return result
    }
}
