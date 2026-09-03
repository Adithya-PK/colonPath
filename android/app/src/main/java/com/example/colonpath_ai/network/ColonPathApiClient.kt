package com.example.colonpath_ai.network

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

object ColonPathApiClient {
    var baseUrl: String = "http://127.0.0.1:8000"

    private fun getCandidateHosts(): List<String> {
        return listOf(
            baseUrl,
            "http://127.0.0.1:8000",
            "http://172.31.99.171:8000",
            "http://192.168.1.7:8000",
            "http://10.0.2.2:8000"
        ).distinct()
    }

    /**
     * Uploads an H&E image and executes the full dynamic CV + multimodal pipeline on FastAPI backend.
     */
    suspend fun analyzeImage(
        imageBitmap: Bitmap,
        caseId: String? = null,
        fileName: String = "specimen.png"
    ): Result<CaseResultDto> = withContext(Dispatchers.IO) {
        var lastException: Exception? = null

        for (host in getCandidateHosts()) {
            try {
                val boundary = "Boundary-" + UUID.randomUUID().toString()
                val url = URL("${host.trimEnd('/')}/analyze")
                val conn = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    doInput = true
                    doOutput = true
                    useCaches = false
                    connectTimeout = 4000
                    readTimeout = 300000
                    setRequestProperty("Content-Type", "multipart/form-data; boundary=$boundary")
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("Connection", "Keep-Alive")
                }

                val outputStream = DataOutputStream(conn.outputStream)
                val lineEnd = "\r\n"
                val twoHyphens = "--"

                // 1. Write case_id field if present
                if (!caseId.isNullOrBlank()) {
                    outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                    outputStream.writeBytes("Content-Disposition: form-data; name=\"case_id\"$lineEnd$lineEnd")
                    outputStream.writeBytes(caseId + lineEnd)
                }

                // 2. Write image file part
                outputStream.writeBytes(twoHyphens + boundary + lineEnd)
                outputStream.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"$fileName\"$lineEnd")
                outputStream.writeBytes("Content-Type: image/png$lineEnd$lineEnd")

                val byteStream = ByteArrayOutputStream()
                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream)
                val imageBytes = byteStream.toByteArray()
                outputStream.write(imageBytes)
                outputStream.writeBytes(lineEnd)

                // 3. End boundary
                outputStream.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd)
                outputStream.flush()
                outputStream.close()

                val responseCode = conn.responseCode
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(responseText)
                    val dto = parseCaseResultDto(json)
                    baseUrl = host // Cache working host
                    return@withContext Result.success(dto)
                } else {
                    val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                    val errDto = parseApiError(errorText, responseCode)
                    return@withContext Result.failure(Exception(errDto.message))
                }
            } catch (e: Exception) {
                lastException = e
            }
        }
        Result.failure(lastException ?: Exception("Unable to connect to ColonPath-AI backend server."))
    }

    fun parseApiError(errorText: String, statusCode: Int): ApiErrorDto {
        return try {
            val obj = JSONObject(errorText)
            ApiErrorDto(
                error_code = obj.optString("error_code", "HTTP_$statusCode"),
                message = obj.optString("message", if (errorText.isNotBlank()) errorText else "HTTP $statusCode Error"),
                case_id = if (obj.has("case_id") && !obj.isNull("case_id")) obj.optString("case_id") else null,
                stage = if (obj.has("stage") && !obj.isNull("stage")) obj.optString("stage") else null,
                retryable = obj.optBoolean("retryable", statusCode >= 500)
            )
        } catch (e: Exception) {
            ApiErrorDto(
                error_code = "HTTP_$statusCode",
                message = if (errorText.isNotBlank()) errorText else "Server returned HTTP $statusCode",
                retryable = statusCode >= 500
            )
        }
    }

    /**
     * Queries Pathologist Copilot / MedGemma VLM with evidence-grounded questions.
     */
    suspend fun askCopilot(
        caseId: String,
        question: String,
        regionId: String? = null
    ): Result<CopilotAnswerDto> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl.trimEnd('/')}/copilot/ask")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doInput = true
                doOutput = true
                connectTimeout = 5000
                readTimeout = 60000
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("Accept", "application/json")
            }

            val reqBody = JSONObject().apply {
                put("case_id", caseId)
                put("question", question)
                if (!regionId.isNullOrBlank()) {
                    put("region_id", regionId)
                }
            }

            conn.outputStream.bufferedWriter().use { it.write(reqBody.toString()) }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val answerDto = CopilotAnswerDto(
                    case_id = json.optString("case_id", caseId),
                    question = json.optString("question", question),
                    selected_region_id = if (json.has("selected_region_id")) json.optString("selected_region_id") else null,
                    answer = json.optString("answer", "Insufficient evidence."),
                    model = json.optString("model", "Google MedGemma 1.5 4B IT / Grounded Decision-Support"),
                    validated = json.optBoolean("validated", true),
                    validation_errors = parseStringList(json.optJSONArray("validation_errors"))
                )
                Result.success(answerDto)
            } else {
                val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: "HTTP $responseCode"
                Result.failure(Exception("Copilot Query Failed (HTTP $responseCode): $errorText"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches all registered cases from backend SQLite database.
     */
    suspend fun listCases(): Result<List<CaseResultDto>> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl.trimEnd('/')}/cases")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 30000
                setRequestProperty("Accept", "application/json")
            }

            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val array = JSONArray(responseText)
                val list = mutableListOf<CaseResultDto>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        CaseResultDto(
                            case_id = obj.optString("case_id"),
                            timestamp = obj.optString("created_at"),
                            status = obj.optString("status", "COMPLETED"),
                            prediction = PredictionDto(
                                `class` = obj.optString("prediction_class", "UNKNOWN"),
                                confidence = obj.optDouble("confidence", 0.0)
                            ),
                            uncertainty = UncertaintyDto(
                                level = obj.optString("uncertainty_level", "LOW")
                            ),
                            model_agreement = ModelAgreementDto(
                                level = obj.optString("agreement_level", "HIGH")
                            ),
                            nuclear_evidence = NuclearEvidenceDto(
                                total_count = obj.optInt("nuclear_count", 0)
                            ),
                            gland_evidence = GlandEvidenceDto(
                                total_count = obj.optInt("gland_count", 0)
                            )
                        )
                    )
                }
                Result.success(list)
            } else {
                Result.failure(Exception("Failed to list cases (HTTP $responseCode)"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches complete CaseResult from backend for a specific case ID.
     */
    suspend fun getCaseResult(caseId: String): Result<CaseResultDto> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl.trimEnd('/')}/cases/$caseId/result")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 30000
                setRequestProperty("Accept", "application/json")
            }
            val responseCode = conn.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseText = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                val dto = parseCaseResultDto(json)
                Result.success(dto)
            } else {
                val errorText = conn.errorStream?.bufferedReader()?.use { it.readText() } ?: ""
                val errDto = parseApiError(errorText, responseCode)
                Result.failure(Exception(errDto.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }



    /**
     * Downloads CSV morphometry comparison table as text.
     */
    suspend fun getCaseCsvText(caseId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = URL("${baseUrl.trimEnd('/')}/cases/$caseId/csv")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 5000
                readTimeout = 30000
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                val text = conn.inputStream.bufferedReader().use { it.readText() }
                Result.success(text)
            } else {
                Result.failure(Exception("Failed to download CSV (HTTP ${conn.responseCode})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Builds visualization endpoint URL for streaming overlays into ImageViewer.
     */
    fun getVisualizationUrl(caseId: String, visType: String): String {
        return "${baseUrl.trimEnd('/')}/cases/$caseId/visualization/$visType"
    }

    /**
     * Downloads visualization bitmap stream asynchronously.
     */
    suspend fun fetchVisualizationBitmap(caseId: String, visType: String): Bitmap? = withContext(Dispatchers.IO) {
        try {
            val url = URL(getVisualizationUrl(caseId, visType))
            val conn = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 30000
            }
            if (conn.responseCode == HttpURLConnection.HTTP_OK) {
                BitmapFactory.decodeStream(conn.inputStream)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    // Helper JSON parsers
    private fun parseCaseResultDto(json: JSONObject): CaseResultDto {
        val cid = json.optString("case_id", "CASE_UNKNOWN")
        val ts = json.optString("timestamp", "")
        val status = json.optString("status", "completed")

        val quality = json.optJSONObject("image_quality")?.let {
            ImageQualityDto(
                passed = it.optBoolean("passed", true),
                resolution = it.optString("resolution"),
                laplacian_variance = it.optDouble("laplacian_variance", 0.0),
                blur_status = it.optString("blur_status", "ACCEPTABLE"),
                mean_brightness = it.optDouble("mean_brightness", 0.0),
                contrast_std = it.optDouble("contrast_std", 0.0),
                mean_saturation = it.optDouble("mean_saturation", 0.0)
            )
        }

        val pred = json.optJSONObject("prediction")?.let {
            val probs = mutableMapOf<String, Double>()
            it.optJSONObject("multiclass_probabilities")?.let { pJson ->
                for (key in pJson.keys()) {
                    probs[key] = pJson.optDouble(key, 0.0)
                }
            }
            PredictionDto(
                `class` = it.optString("class", "UNKNOWN"),
                confidence = it.optDouble("confidence", 0.0),
                calibrated_confidence = it.optDouble("calibrated_confidence", 0.0),
                tumor_probability = it.optDouble("tumor_probability", 0.0),
                binary_class = it.optString("binary_class", "NON-TUM"),
                multiclass_probabilities = probs
            )
        }

        val unc = json.optJSONObject("uncertainty")?.let {
            UncertaintyDto(
                score = it.optDouble("score", 0.0),
                level = it.optString("level", "LOW"),
                entropy = it.optDouble("entropy", 0.0),
                normalized_entropy = it.optDouble("normalized_entropy", 0.0),
                ood_score = it.optDouble("ood_score", 0.0),
                ood_status = it.optString("ood_status", "IN_DISTRIBUTION"),
                is_ood = it.optBoolean("is_ood", false),
                review_required = it.optBoolean("review_required", false),
                message = it.optString("message", "")
            )
        }

        val agr = json.optJSONObject("model_agreement")?.let {
            ModelAgreementDto(
                level = it.optString("level", "HIGH"),
                score = it.optDouble("score", 1.0),
                concordant_sources = parseStringList(it.optJSONArray("concordant_sources")),
                discordant_sources = parseStringList(it.optJSONArray("discordant_sources")),
                summary = it.optString("summary", "")
            )
        }

        val nuc = json.optJSONObject("nuclear_evidence")?.let {
            val typeCounts = mutableMapOf<String, Int>()
            it.optJSONObject("type_counts")?.let { tc ->
                for (key in tc.keys()) {
                    typeCounts[key] = tc.optInt(key, 0)
                }
            }
            NuclearEvidenceDto(
                total_count = it.optInt("total_count", 0),
                type_counts = typeCounts,
                mean_area_px2 = it.optDouble("mean_area_px2", 0.0),
                mean_perimeter_px = it.optDouble("mean_perimeter_px", 0.0),
                mean_eccentricity = it.optDouble("mean_eccentricity", 0.0),
                mean_circularity = it.optDouble("mean_circularity", 0.0),
                interpretation = it.optString("interpretation", "")
            )
        }

        val gland = json.optJSONObject("gland_evidence")?.let {
            GlandEvidenceDto(
                total_count = it.optInt("total_count", 0),
                mean_area_pixels = it.optDouble("mean_area_pixels", 0.0),
                mean_perimeter_pixels = it.optDouble("mean_perimeter_pixels", 0.0),
                mean_width_pixels = it.optDouble("mean_width_pixels", 0.0),
                mean_height_pixels = it.optDouble("mean_height_pixels", 0.0),
                mean_aspect_ratio = it.optDouble("mean_aspect_ratio", 0.0),
                mean_circularity = it.optDouble("mean_circularity", 0.0),
                interpretation = it.optString("interpretation", "")
            )
        }

        val ref = json.optJSONObject("reference_comparison")?.let {
            val comps = mutableListOf<ReferenceMatchItemDto>()
            it.optJSONArray("comparisons")?.let { cArr ->
                for (j in 0 until cArr.length()) {
                    val cObj = cArr.getJSONObject(j)
                    comps.add(
                        ReferenceMatchItemDto(
                            reference_id = cObj.optString("reference_id"),
                            category = cObj.optString("category"),
                            normalized_distance = cObj.optDouble("normalized_distance", 0.0),
                            similarity_percent = cObj.optDouble("similarity_percent", 0.0),
                            key_concordant_features = parseStringList(cObj.optJSONArray("key_concordant_features"))
                        )
                    )
                }
            }
            ReferenceComparisonDto(
                label = it.optString("label", "REFERENCE-BASED INSIGHT"),
                is_available = it.optBoolean("is_available", true),
                retrieval_engine = it.optString("retrieval_engine", "Local In-Memory Vector Search Engine"),
                active_database = it.optString("active_database", "outputs/reference_cases"),
                top_category = it.optString("top_category", "normal"),
                top_similarity_percent = it.optDouble("top_similarity_percent", 0.0),
                top_reference_id = it.optString("top_reference_id", "none"),
                insight = it.optString("insight", ""),
                comparisons = comps
            )
        }

        val regions = mutableListOf<PriorityRegionDto>()
        json.optJSONArray("priority_regions")?.let { rArr ->
            for (j in 0 until rArr.length()) {
                val rObj = rArr.getJSONObject(j)
                regions.add(
                    PriorityRegionDto(
                        region_id = rObj.optString("region_id", "R_01"),
                        index = rObj.optInt("index", j + 1),
                        x = rObj.optInt("x", 0),
                        y = rObj.optInt("y", 0),
                        width = rObj.optInt("width", 128),
                        height = rObj.optInt("height", 128),
                        prediction = rObj.optString("prediction", "UNKNOWN"),
                        confidence = rObj.optDouble("confidence", 0.0),
                        tumor_probability = rObj.optDouble("tumor_probability", 0.0),
                        uncertainty_score = rObj.optDouble("uncertainty_score", 0.0),
                        uncertainty_level = rObj.optString("uncertainty_level", "LOW"),
                        priority_score = rObj.optDouble("priority_score", 0.0),
                        priority_level = rObj.optString("priority_level", "LOW"),
                        priority_label = rObj.optString("priority_label", "AI-prioritized region"),
                        nuclei_count = rObj.optInt("nuclei_count", 0),
                        glands_count = rObj.optInt("glands_count", 0),
                        agreement_level = rObj.optString("agreement_level", "HIGH"),
                        rationale = rObj.optString("rationale", "")
                    )
                )
            }
        }

        val visMap = mutableMapOf<String, String>()
        json.optJSONObject("visualizations")?.let { vObj ->
            for (k in vObj.keys()) {
                visMap[k] = vObj.optString(k)
            }
        }

        val explanation = json.optJSONObject("explanation")?.let {
            val claimsList = mutableListOf<ExplanationClaimDto>()
            it.optJSONArray("claims")?.let { cArr ->
                for (j in 0 until cArr.length()) {
                    val cObj = cArr.getJSONObject(j)
                    claimsList.add(
                        ExplanationClaimDto(
                            claim_id = cObj.optString("claim_id", "C_${j+1}"),
                            category = cObj.optString("category", ""),
                            claim_statement = cObj.optString("claim_statement", ""),
                            evidence_source = cObj.optString("evidence_source", ""),
                            evidence_value = cObj.opt("evidence_value"),
                            support_type = cObj.optString("support_type", "")
                        )
                    )
                }
            }
            ExplanationDto(
                text = it.optString("text", ""),
                claims = claimsList,
                validated = it.optBoolean("validated", true),
                validation_errors = parseStringList(it.optJSONArray("validation_errors"))
            )
        }

        val digepath = json.optJSONObject("digepath")?.let {
            DigepathMetaDto(
                model_name = it.optString("model_name", "Phikon-v2"),
                architecture = it.optString("architecture", "ViT-L/16 via DINOv2"),
                embedding_dimension = it.optInt("embedding_dimension", 1024),
                device = it.optString("device", "cpu"),
                status = it.optString("status", "active")
            )
        }

        val modelPerf = json.optJSONObject("model_performance_metadata")?.let {
            ModelPerformanceMetadataDto(
                evaluation_dataset = it.optString("evaluation_dataset", "NCT-CRC-HE-100K (45 test patches)"),
                multiclass_accuracy = it.optDouble("multiclass_accuracy", 0.6444),
                multiclass_macro_f1 = it.optDouble("multiclass_macro_f1", 0.5041),
                binary_tumor_accuracy = it.optDouble("binary_tumor_accuracy", 1.0),
                expected_calibration_error_ece = it.optDouble("expected_calibration_error_ece", 0.1570),
                verified_date = it.optString("verified_date", "2026-09-01")
            )
        }

        val repro = json.optJSONObject("reproducibility")?.let {
            val modelsMap = mutableMapOf<String, String>()
            it.optJSONObject("models")?.let { mObj ->
                for (k in mObj.keys()) {
                    modelsMap[k] = mObj.optString(k)
                }
            }
            ReproducibilityDto(
                pipeline_version = it.optString("pipeline_version", "2.0.0"),
                input_image_sha256 = it.optString("input_image_sha256", ""),
                input_image_name = it.optString("input_image_name", ""),
                models = modelsMap,
                temperature_scaling_factor = it.optDouble("temperature_scaling_factor", 1.25),
                timestamp_utc = it.optString("timestamp_utc")
            )
        }

        val stageDurations = mutableMapOf<String, Double>()
        json.optJSONObject("stage_durations_ms")?.let { sObj ->
            for (k in sObj.keys()) {
                stageDurations[k] = sObj.optDouble(k, 0.0)
            }
        }

        return CaseResultDto(
            case_id = cid,
            timestamp = ts,
            status = status,
            lifecycle_state = json.optString("lifecycle_state", "COMPLETED"),
            image_quality = quality,
            digepath = digepath,
            prediction = pred,
            uncertainty = unc,
            model_agreement = agr,
            nuclear_evidence = nuc,
            gland_evidence = gland,
            reference_comparison = ref,
            priority_regions = regions,
            model_performance_metadata = modelPerf,
            stage_durations_ms = stageDurations,
            reproducibility = repro,
            visualizations = visMap,
            explanation = explanation,
            limitations = parseStringList(json.optJSONArray("limitations"))
        )
    }

    private fun parseStringList(jsonArray: JSONArray?): List<String> {
        if (jsonArray == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.optString(i))
        }
        return list
    }
}
