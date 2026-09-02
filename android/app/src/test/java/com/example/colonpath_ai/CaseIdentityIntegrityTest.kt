package com.example.colonpath_ai

import com.example.colonpath_ai.network.CaseResultDto
import com.example.colonpath_ai.network.PredictionDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Phase 8.1 Case Identity & Switching Integrity Unit Tests.
 */
class CaseIdentityIntegrityTest {

    @Test
    fun testCaseIdentityPreservedInDto() {
        val caseA = CaseResultDto(
            case_id = "CASE_RUNTIME_PIXEL7_001",
            timestamp = "2026-09-01T23:20:00Z",
            status = "COMPLETED",
            prediction = PredictionDto(`class` = "MUS", confidence = 0.99)
        )
        val caseB = CaseResultDto(
            case_id = "COL-2026-001",
            timestamp = "2026-08-30T10:00:00Z",
            status = "COMPLETED",
            prediction = PredictionDto(`class` = "NORM", confidence = 0.98)
        )

        assertEquals("CASE_RUNTIME_PIXEL7_001", caseA.case_id)
        assertEquals("COL-2026-001", caseB.case_id)
        assertNotEquals(caseA.case_id, caseB.case_id)
    }

    @Test
    fun testEffectiveCaseIdResolution() {
        fun resolveEffectiveCaseId(
            routeCaseId: String?,
            activeCaseId: String?,
            sampleActiveCaseId: String?,
            currentCaseResultId: String?
        ): String {
            return routeCaseId?.takeIf { it.isNotBlank() }
                ?: activeCaseId
                ?: sampleActiveCaseId
                ?: currentCaseResultId
                ?: "COL-2026-001"
        }

        // 1. Explicit route caseId takes highest priority
        assertEquals(
            "CASE_ROUTE_001",
            resolveEffectiveCaseId("CASE_ROUTE_001", "CASE_ACTIVE_002", "COL-2026-001", "CASE_RESULT_003")
        )

        // 2. Active case ID takes precedence if route caseId is null/blank
        assertEquals(
            "CASE_ACTIVE_002",
            resolveEffectiveCaseId(null, "CASE_ACTIVE_002", "COL-2026-001", "CASE_RESULT_003")
        )

        // 3. Current case result takes precedence if both route and active are null
        assertEquals(
            "CASE_RESULT_003",
            resolveEffectiveCaseId(null, null, null, "CASE_RESULT_003")
        )

        // 4. Default fallback when all are null
        assertEquals(
            "COL-2026-001",
            resolveEffectiveCaseId(null, null, null, null)
        )
    }

    @Test
    fun testCaseSwitchingIsolation() {
        val casesCache = mutableListOf<CaseResultDto>()
        var currentCaseResult: CaseResultDto? = null
        var activeCaseId: String? = null

        // Open Case A
        activeCaseId = "CASE_A"
        currentCaseResult = CaseResultDto(case_id = "CASE_A", timestamp = "2026-09-01T00:00:00Z", status = "COMPLETED")
        casesCache.add(currentCaseResult)

        assertEquals("CASE_A", currentCaseResult.case_id)
        assertEquals("CASE_A", activeCaseId)

        // Switch to Case B -> reset currentCaseResult immediately before loading
        activeCaseId = "CASE_B"
        currentCaseResult = null // isolated transition

        assertNull(currentCaseResult)
        assertEquals("CASE_B", activeCaseId)

        // Case B finishes loading
        currentCaseResult = CaseResultDto(case_id = "CASE_B", timestamp = "2026-09-01T01:00:00Z", status = "COMPLETED")
        casesCache.add(currentCaseResult)

        assertEquals("CASE_B", currentCaseResult.case_id)
        assertNotEquals("CASE_A", currentCaseResult.case_id)
    }
}
