package com.example.colonpath_ai.data

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.colonpath_ai.model.*
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SampleDataRepository {
    private var appContext: Context? = null
    private const val FILE_NAME = "colonpath_cases.json"

    var activeCaseId: String? = null
    var selectedImageUri by mutableStateOf<Uri?>(null)
    var selectedBitmap by mutableStateOf<Bitmap?>(null)
    var selectedImageName by mutableStateOf<String>("")

    fun clearCurrentSelection() {
        selectedImageUri = null
        selectedBitmap = null
        selectedImageName = ""
    }

    val samplePatient = PatientInfo(
        patientId = "PT-2026-0847",
        patientName = "Sample Patient"
    )

    val sampleCase = Case(
        caseId = "COL-2026-001",
        patient = samplePatient,
        tissue = "Colorectal",
        sampleType = "Biopsy",
        stain = "H&E",
        analysisDate = "30 Aug 2026",
        status = CaseStatus.COMPLETED
    )

    val sampleImageInfo = ImageInfo(
        imageId = "IMG-001",
        width = 2048,
        height = 1536,
        source = "Demo Sample",
        calibrationStatus = "Calibration unavailable"
    )

    val sampleImageQuality = ImageQuality(
        status = QualityStatus.GOOD,
        resolution = "2048 × 1536",
        dimensions = "2048 × 1536",
        blurStatus = "Acceptable",
        calibrationStatus = "Calibration unavailable",
        accepted = true
    )

    val sampleNuclearAnalysis = NuclearAnalysis(
        nucleiDetected = 1824,
        nuclearDensity = 139.2,
        meanNuclearArea = 47.3,
        medianNuclearArea = 44.1,
        nuclearCircularity = 0.72,
        eccentricity = 0.41,
        aspectRatio = 1.34,
        nearestNeighborDistance = 12.8
    )

    val sampleNuclearClassification = NuclearClassification(
        categories = listOf(
            CellCategory("Epithelial", 892, 48.9),
            CellCategory("Inflammatory", 421, 23.1),
            CellCategory("Connective", 298, 16.3),
            CellCategory("Neoplastic", 156, 8.6),
            CellCategory("Dead", 41, 2.2),
            CellCategory("Other", 16, 0.9)
        )
    )

    val sampleGlandAnalysis = GlandAnalysis(
        glandCount = 146,
        meanGlandArea = 2840.0,
        meanGlandPerimeter = 312.5,
        glandSpacing = 89.4,
        glandDensity = 11.2,
        glandShape = 0.68,
        branching = "Moderate",
        boundaryIrregularity = 0.64,
        crowding = "Moderate"
    )

    val sampleNuclearMorphology = NuclearMorphology(
        meanArea = 47.3,
        medianArea = 44.1,
        areaStdDev = 12.8,
        meanCircularity = 0.72,
        meanEccentricity = 0.41,
        meanAspectRatio = 1.34,
        pleomorphismIndex = 0.38
    )

    val sampleGlandMorphology = GlandMorphology(
        meanArea = 2840.0,
        areaVariance = 485.2,
        meanPerimeter = 312.5,
        meanCircularity = 0.68,
        irregularityIndex = 0.64,
        crowdingScore = 0.57,
        branchingFrequency = 0.23
    )

    val sampleMorphologyMetrics = MorphologyMetrics(
        nuclear = sampleNuclearMorphology,
        gland = sampleGlandMorphology
    )

    val sampleReferenceResults = listOf(
        ReferenceResult(
            referenceId = "REF-021",
            similarityScore = 94.2,
            category = "Adenoma-like morphology",
            relevantMetrics = listOf("Nuclear density", "Gland irregularity", "Circularity")
        ),
        ReferenceResult(
            referenceId = "REF-034",
            similarityScore = 91.8,
            category = "Adenocarcinoma-like morphology",
            relevantMetrics = listOf("Nuclear area", "Crowding", "Shape")
        ),
        ReferenceResult(
            referenceId = "REF-011",
            similarityScore = 88.6,
            category = "Adenomatous morphology",
            relevantMetrics = listOf("Gland density", "Nuclear morphology")
        )
    )

    val sampleComparisonMetrics = listOf(
        ComparisonMetric("Nuclei Count", "1200", "1824"),
        ComparisonMetric("Nuclear Density", "98.5 /mm²", "139.2 /mm²"),
        ComparisonMetric("Mean Nuclear Area", "38.6 px²", "47.3 px²"),
        ComparisonMetric("Nuclear Circularity", "0.86", "0.72"),
        ComparisonMetric("Gland Count", "162", "146"),
        ComparisonMetric("Gland Density", "12.8 /mm²", "11.2 /mm²"),
        ComparisonMetric("Mean Gland Area", "3,420 px²", "2,840 px²"),
        ComparisonMetric("Gland Irregularity", "0.31", "0.64")
    )

    val sampleReferenceComparison = ReferenceComparison(
        references = sampleReferenceResults,
        metrics = sampleComparisonMetrics
    )

    val sampleAIReport = AIReport(
        summary = "Computational analysis reveals measurable variations in glandular architecture and nuclear morphology compared to typical references.",
        computationalFindings = "- Increased nuclear density (139.2 /mm²)\n- Elevated gland irregularity (0.64)\n- Reduced nuclear circularity (0.72)\n- Significant epithelial proliferation",
        interpretation = "Structured computational observations suggest architectural changes consistent with adenomatous features. Deviations observed in critical morphology metrics.",
        supportingEvidence = listOf(
            "High structural similarity to REF-021 (Adenoma-like morphology, 94.2%)",
            "Increased nuclear-to-cytoplasmic ratio observed in 48.9% of epithelial cells",
            "Significant gland crowding with moderate branching"
        ),
        limitations = Limitations(
            items = listOf(
                "Research prototype analysis only",
                "Morphological similarity does not constitute a diagnosis",
                "Limited reference set for comparison",
                "Not an approved diagnostic device"
            )
        ),
        uncertainty = "Computational confidence may be affected by sampling variations and staining consistency.",
        pathologistReview = PathologistReview(
            required = true,
            message = "Morphological deviations exceed typical baseline parameters. Comprehensive manual review of all slides is strongly recommended."
        )
    )

    val sampleAnalysisResult = AnalysisResult(
        case = sampleCase,
        imageInfo = sampleImageInfo,
        imageQuality = sampleImageQuality,
        nuclearAnalysis = sampleNuclearAnalysis,
        nuclearClassification = sampleNuclearClassification,
        glandAnalysis = sampleGlandAnalysis,
        morphologyMetrics = sampleMorphologyMetrics,
        referenceComparison = sampleReferenceComparison,
        aiReport = sampleAIReport
    )

    private fun getDefaultSeedCases(): List<Case> = listOf(
        sampleCase,
        Case(
            caseId = "COL-2026-002",
            patient = PatientInfo("PT-2026-0848", "Demo Patient B"),
            tissue = "Colorectal",
            sampleType = "Biopsy",
            stain = "H&E",
            analysisDate = "28 Aug 2026",
            status = CaseStatus.PENDING_REVIEW
        ),
        Case(
            caseId = "COL-2026-003",
            patient = PatientInfo("PT-2026-0849", "Demo Patient C"),
            tissue = "Colorectal",
            sampleType = "Biopsy",
            stain = "H&E",
            analysisDate = "27 Aug 2026",
            status = CaseStatus.IN_PROGRESS
        ),
        Case(
            caseId = "COL-2026-004",
            patient = PatientInfo("PT-2026-0850", "Demo Patient D"),
            tissue = "Colorectal",
            sampleType = "Biopsy",
            stain = "H&E",
            analysisDate = "25 Aug 2026",
            status = CaseStatus.COMPLETED
        ),
        Case(
            caseId = "COL-2026-005",
            patient = PatientInfo("PT-2026-0851", "Demo Patient E"),
            tissue = "Colorectal",
            sampleType = "Biopsy",
            stain = "H&E",
            analysisDate = "22 Aug 2026",
            status = CaseStatus.FAILED
        )
    )

    val sampleCaseHistory = mutableStateListOf<Case>().apply {
        addAll(getDefaultSeedCases())
    }

    fun initialize(context: Context) {
        appContext = context.applicationContext
        loadFromDisk()
    }

    private fun loadFromDisk() {
        val ctx = appContext ?: return
        val file = File(ctx.filesDir, FILE_NAME)
        if (!file.exists()) {
            saveToDisk()
            return
        }

        try {
            val jsonString = file.readText()
            val jsonArray = JSONArray(jsonString)
            val loadedCases = mutableListOf<Case>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val patientObj = obj.optJSONObject("patient") ?: JSONObject()
                val patient = PatientInfo(
                    patientId = patientObj.optString("patientId", "Unknown"),
                    patientName = patientObj.optString("patientName", "Anonymous"),
                    notes = patientObj.optString("notes", "")
                )
                val statusStr = obj.optString("status", CaseStatus.PENDING.name)
                val status = try {
                    CaseStatus.valueOf(statusStr)
                } catch (e: Exception) {
                    CaseStatus.PENDING
                }
                val c = Case(
                    caseId = obj.getString("caseId"),
                    patient = patient,
                    tissue = obj.optString("tissue", "Colorectal"),
                    sampleType = obj.optString("sampleType", "Biopsy"),
                    stain = obj.optString("stain", "H&E"),
                    analysisDate = obj.optString("analysisDate", "Today"),
                    status = status,
                    notes = obj.optString("notes", "")
                )
                loadedCases.add(c)
            }
            sampleCaseHistory.clear()
            sampleCaseHistory.addAll(loadedCases)
        } catch (e: Exception) {
            e.printStackTrace()
            if (sampleCaseHistory.isEmpty()) {
                sampleCaseHistory.addAll(getDefaultSeedCases())
            }
        }
    }

    private val ioExecutor = java.util.concurrent.Executors.newSingleThreadExecutor()

    private fun saveToDisk() {
        val ctx = appContext ?: return
        val casesSnapshot = sampleCaseHistory.toList()
        ioExecutor.execute {
            try {
                val file = File(ctx.filesDir, FILE_NAME)
                val jsonArray = JSONArray()
                for (c in casesSnapshot) {
                    val obj = JSONObject().apply {
                        put("caseId", c.caseId)
                        put("tissue", c.tissue)
                        put("sampleType", c.sampleType)
                        put("stain", c.stain)
                        put("analysisDate", c.analysisDate)
                        put("status", c.status.name)
                        put("notes", c.notes)
                        put("patient", JSONObject().apply {
                            put("patientId", c.patient.patientId)
                            put("patientName", c.patient.patientName)
                            put("notes", c.patient.notes)
                        })
                    }
                    jsonArray.put(obj)
                }
                file.writeText(jsonArray.toString())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addCase(newCase: Case) {
        val existingIndex = sampleCaseHistory.indexOfFirst { it.caseId == newCase.caseId }
        if (existingIndex >= 0) {
            sampleCaseHistory[existingIndex] = newCase
        } else {
            sampleCaseHistory.add(0, newCase)
        }
        activeCaseId = newCase.caseId
        saveToDisk()
    }

    fun updateCaseStatus(caseId: String, newStatus: CaseStatus) {
        val idx = sampleCaseHistory.indexOfFirst { it.caseId == caseId }
        if (idx >= 0) {
            val updated = sampleCaseHistory[idx].copy(status = newStatus)
            sampleCaseHistory[idx] = updated
            saveToDisk()
        }
    }

    fun deleteCase(caseId: String): Boolean {
        val removed = sampleCaseHistory.removeAll { it.caseId == caseId }
        if (activeCaseId == caseId) {
            activeCaseId = sampleCaseHistory.firstOrNull()?.caseId
        }
        if (removed) {
            saveToDisk()
        }
        return removed
    }

    fun isCaseIdExists(caseId: String): Boolean {
        return sampleCaseHistory.any { it.caseId.equals(caseId.trim(), ignoreCase = true) }
    }

    fun generateNextCaseId(year: Int = 2026): String {
        // Extract all numeric sequence numbers from existing cases (e.g., COL-2026-006 -> 6)
        val existingNums = sampleCaseHistory.mapNotNull { c ->
            val match = Regex("""COL-(?:\d+-)?(\d+)""", RegexOption.IGNORE_CASE).find(c.caseId.trim())
            match?.groupValues?.get(1)?.toIntOrNull()
        }
        val maxNum = existingNums.maxOrNull() ?: 0
        var candidateNum = maxNum + 1
        var candidateId = String.format(java.util.Locale.US, "COL-%d-%03d", year, candidateNum)
        
        // Guarantee uniqueness against any existing case IDs
        while (isCaseIdExists(candidateId)) {
            candidateNum++
            candidateId = String.format(java.util.Locale.US, "COL-%d-%03d", year, candidateNum)
        }
        return candidateId
    }

    fun generateNextPatientId(): String {
        val rand = (1000..9999).random()
        return "PT-2026-$rand"
    }

    fun getAnalysisForCase(caseId: String): AnalysisResult {
        val foundCase = getCaseById(caseId) ?: sampleCase
        val currentImageInfo = if (selectedBitmap != null) {
            sampleImageInfo.copy(
                width = selectedBitmap!!.width,
                height = selectedBitmap!!.height,
                source = selectedImageName
            )
        } else {
            sampleImageInfo
        }
        return sampleAnalysisResult.copy(case = foundCase, imageInfo = currentImageInfo)
    }

    fun getCaseById(caseId: String): Case? {
        return sampleCaseHistory.find { it.caseId == caseId }
    }
}
