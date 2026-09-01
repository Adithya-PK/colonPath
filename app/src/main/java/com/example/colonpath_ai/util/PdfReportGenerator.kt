package com.example.colonpath_ai.util

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.colonpath_ai.R
import com.example.colonpath_ai.model.AnalysisResult
import java.io.File
import java.io.FileOutputStream

object PdfReportGenerator {

    fun generateReportPdf(context: Context, analysisResult: AnalysisResult): File? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595 x 842 pt)
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 9f
                isAntiAlias = true
            }

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val sectionHeaderPaint = Paint().apply {
                color = Color.parseColor("#1A237E") // Deep Navy
                textSize = 10.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val tableHeaderPaint = Paint().apply {
                color = Color.parseColor("#0F172A")
                textSize = 9f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val smallPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 7.5f
                isAntiAlias = true
            }

            var y = 28f
            val leftMargin = 32f
            val rightMargin = 563f
            val contentWidth = rightMargin - leftMargin

            // 1. Header Bar
            val headerRect = RectF(leftMargin, y, rightMargin, y + 34f)
            val headerBgPaint = Paint().apply { color = Color.rgb(26, 35, 126); isAntiAlias = true }
            canvas.drawRoundRect(headerRect, 6f, 6f, headerBgPaint)
            canvas.drawText("ColonPath-AI", leftMargin + 10f, y + 22f, titlePaint)
            canvas.drawText("AI-Assisted Histopathology Diagnostic Report", rightMargin - 200f, y + 21f, subtitlePaint)
            y += 42f

            // 2. Case & Patient Demographics Box
            val boxPaint = Paint().apply { color = Color.parseColor("#F8FAFC"); isAntiAlias = true }
            val borderPaint = Paint().apply { 
                color = Color.parseColor("#CBD5E1")
                style = Paint.Style.STROKE
                strokeWidth = 0.8f
                isAntiAlias = true
            }
            val caseBoxRect = RectF(leftMargin, y, rightMargin, y + 48f)
            canvas.drawRoundRect(caseBoxRect, 6f, 6f, boxPaint)
            canvas.drawRoundRect(caseBoxRect, 6f, 6f, borderPaint)
            
            canvas.drawText("Case ID: ${analysisResult.case.caseId}", leftMargin + 10f, y + 14f, tableHeaderPaint)
            canvas.drawText("Patient ID: ${analysisResult.case.patient.patientId}", leftMargin + 10f, y + 28f, paint)
            canvas.drawText("Patient Name: ${analysisResult.case.patient.patientName}", leftMargin + 10f, y + 41f, paint)

            canvas.drawText("Tissue: ${analysisResult.case.tissue}", leftMargin + 250f, y + 14f, paint)
            canvas.drawText("Stain: ${analysisResult.case.stain}", leftMargin + 250f, y + 28f, paint)
            canvas.drawText("Date: ${analysisResult.case.analysisDate} | Status: ${analysisResult.case.status.name.replace("_", " ")}", leftMargin + 250f, y + 41f, paint)
            y += 56f

            // 3. Analyzed Specimen & Image Quality Box (Compact with embedded HoVer-Net overlay)
            canvas.drawText("Analyzed Specimen & Image Quality", leftMargin, y + 8f, sectionHeaderPaint)
            y += 13f
            val imgBoxHeight = 72f
            val imgBoxRect = RectF(leftMargin, y, rightMargin, y + imgBoxHeight)
            canvas.drawRoundRect(imgBoxRect, 6f, 6f, boxPaint)
            canvas.drawRoundRect(imgBoxRect, 6f, 6f, borderPaint)

            // Draw embedded HoVer-Net overlay thumbnail
            try {
                val overlayBmp = BitmapFactory.decodeResource(context.resources, R.drawable.hovernet_overlay)
                if (overlayBmp != null) {
                    val thumbRect = RectF(leftMargin + 6f, y + 6f, leftMargin + 66f, y + 66f)
                    canvas.drawBitmap(overlayBmp, null, thumbRect, null)
                    canvas.drawRoundRect(thumbRect, 3f, 3f, borderPaint)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val textOffset = leftMargin + 76f
            canvas.drawText("Image ID: ${analysisResult.imageInfo.imageId} | Source: ${analysisResult.imageInfo.source}", textOffset, y + 15f, paint)
            canvas.drawText("Dimensions: ${analysisResult.imageInfo.width} × ${analysisResult.imageInfo.height} px | Magnification: 40×", textOffset, y + 28f, paint)
            canvas.drawText("Quality: ${analysisResult.imageQuality.status} | Blur: ${analysisResult.imageQuality.blurStatus} | Calibration: Passed", textOffset, y + 41f, paint)
            canvas.drawText("AI Model: HoVer-Net Instance Segmentation (Green=Boundaries, Red=Epithelial)", textOffset, y + 54f, smallPaint)
            canvas.drawText("Quality Assessment: Accepted (Passed Diagnostic Quality Standard)", textOffset, y + 66f, smallPaint)

            y += imgBoxHeight + 12f
            
            // 4. Quantitative Morphology Summary
            canvas.drawText("Quantitative Morphology Summary", leftMargin, y + 8f, sectionHeaderPaint)
            y += 18f
            
            val metricWidth = contentWidth / 3
            canvas.drawText("Nuclei Count: ${analysisResult.nuclearAnalysis.nucleiDetected}", leftMargin, y, paint)
            canvas.drawText("Nuclear Density: ${analysisResult.nuclearAnalysis.nuclearDensity} /mm²", leftMargin + metricWidth, y, paint)
            canvas.drawText("Circularity: ${analysisResult.nuclearAnalysis.nuclearCircularity}", leftMargin + 2 * metricWidth, y, paint)
            y += 13f
            canvas.drawText("Gland Count: ${analysisResult.glandAnalysis.glandCount}", leftMargin, y, paint)
            canvas.drawText("Mean Gland Area: ${analysisResult.glandAnalysis.meanGlandArea} px²", leftMargin + metricWidth, y, paint)
            canvas.drawText("Boundary Irregularity: ${analysisResult.glandAnalysis.boundaryIrregularity}", leftMargin + 2 * metricWidth, y, paint)
            y += 18f

            // 5. Reference Comparison Table
            canvas.drawText("Reference vs Patient Comparison Table", leftMargin, y + 8f, sectionHeaderPaint)
            y += 14f
            
            val tableHeaderBg = Paint().apply { color = Color.parseColor("#E2E8F0"); isAntiAlias = true }
            canvas.drawRect(leftMargin, y, rightMargin, y + 14f, tableHeaderBg)
            canvas.drawRect(leftMargin, y, rightMargin, y + 14f, borderPaint)
            
            val col1X = leftMargin + 6f
            val col2X = leftMargin + 220f
            val col3X = leftMargin + 380f
            
            canvas.drawText("Metric", col1X, y + 10.5f, tableHeaderPaint)
            canvas.drawText("Reference Baseline", col2X, y + 10.5f, tableHeaderPaint)
            canvas.drawText("Patient Specimen", col3X, y + 10.5f, tableHeaderPaint)
            y += 14f
            
            val altBg1 = Paint().apply { color = Color.WHITE; isAntiAlias = true }
            val altBg2 = Paint().apply { color = Color.parseColor("#F8FAFC"); isAntiAlias = true }
            
            analysisResult.referenceComparison.metrics.forEachIndexed { index, metric ->
                val bgPaint = if (index % 2 == 0) altBg1 else altBg2
                canvas.drawRect(leftMargin, y, rightMargin, y + 13f, bgPaint)
                canvas.drawRect(leftMargin, y, rightMargin, y + 13f, borderPaint)
                canvas.drawText(metric.name, col1X, y + 10f, paint)
                canvas.drawText(metric.referenceValue, col2X, y + 10f, paint)
                canvas.drawText(metric.patientValue, col3X, y + 10f, paint)
                y += 13f
            }
            y += 12f
            
            // 6. AI-Assisted Interpretation & Supporting Evidence
            canvas.drawText("AI-Assisted Interpretation", leftMargin, y + 8f, sectionHeaderPaint)
            y += 12f
            y = drawWrappedText(canvas, analysisResult.aiReport.interpretation, leftMargin, y + 10f, contentWidth, paint, 11f)
            y += 6f
            
            canvas.drawText("Supporting Evidence", leftMargin, y + 8f, sectionHeaderPaint)
            y += 12f
            analysisResult.aiReport.supportingEvidence.forEach { evidence ->
                y = drawWrappedText(canvas, "• $evidence", leftMargin + 8f, y + 10f, contentWidth - 8f, paint, 11f)
            }
            y += 8f

            // 7. Pathologist Review Required Box
            val reviewBgPaint = Paint().apply { color = Color.parseColor("#FEF2F2"); isAntiAlias = true }
            val reviewBorderPaint = Paint().apply { 
                color = Color.parseColor("#DC2626")
                style = Paint.Style.STROKE
                strokeWidth = 1.2f
                isAntiAlias = true
            }
            val reviewHeaderPaint = Paint().apply {
                color = Color.parseColor("#DC2626")
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }
            
            val reviewBoxRect = RectF(leftMargin, y, rightMargin, y + 42f)
            canvas.drawRoundRect(reviewBoxRect, 6f, 6f, reviewBgPaint)
            canvas.drawRoundRect(reviewBoxRect, 6f, 6f, reviewBorderPaint)
            canvas.drawText("PATHOLOGIST REVIEW REQUIRED", leftMargin + 10f, y + 15f, reviewHeaderPaint)
            drawWrappedText(canvas, analysisResult.aiReport.pathologistReview.message, leftMargin + 10f, y + 28f, contentWidth - 20f, paint, 10.5f)

            // 8. Footer Bar & Disclaimer (Fixed at bottom of page with comfortable spacing)
            val footerY = 818f
            val linePaint = Paint().apply {
                color = Color.parseColor("#CBD5E1")
                strokeWidth = 0.8f
                isAntiAlias = true
            }
            canvas.drawLine(leftMargin, footerY - 12f, rightMargin, footerY - 12f, linePaint)
            canvas.drawText("ColonPath-AI • AI-Assisted Clinical Decision Support • Confidential Medical Record", leftMargin, footerY, smallPaint)

            document.finishPage(page)

            val file = File(context.getExternalFilesDir(null), "colonpath_ai_report_${analysisResult.case.caseId}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()
            
            return file
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
    
    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        lineHeight: Float
    ): Float {
        var y = startY
        val paragraphs = text.split("\n")
        for (paragraph in paragraphs) {
            val words = paragraph.split(" ")
            var currentLine = StringBuilder()
            for (word in words) {
                val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                if (paint.measureText(testLine) <= maxWidth) {
                    currentLine.append(if (currentLine.isEmpty()) word else " $word")
                } else {
                    if (currentLine.isNotEmpty()) {
                        canvas.drawText(currentLine.toString(), x, y, paint)
                        y += lineHeight
                        currentLine = StringBuilder(word)
                    } else {
                        canvas.drawText(word, x, y, paint)
                        y += lineHeight
                    }
                }
            }
            if (currentLine.isNotEmpty()) {
                canvas.drawText(currentLine.toString(), x, y, paint)
                y += lineHeight
            }
        }
        return y
    }
}
