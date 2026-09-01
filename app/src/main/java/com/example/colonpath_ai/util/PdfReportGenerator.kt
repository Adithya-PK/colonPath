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
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply {
                color = Color.BLACK
                textSize = 10f
            }

            val titlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 16f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subtitlePaint = Paint().apply {
                color = Color.WHITE
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val headerPaint = Paint().apply {
                color = Color.BLACK
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            val smallPaint = Paint().apply {
                color = Color.DKGRAY
                textSize = 8f
            }

            var y = 36f
            val leftMargin = 36f
            val rightMargin = 559f
            val contentWidth = rightMargin - leftMargin

            // Header Bar
            val headerRect = RectF(leftMargin, y, rightMargin, y + 40f)
            val headerBgPaint = Paint().apply { color = Color.rgb(26, 35, 126) }
            canvas.drawRoundRect(headerRect, 8f, 8f, headerBgPaint)
            canvas.drawText("ColonPath-AI", leftMargin + 10f, y + 26f, titlePaint)
            canvas.drawText("AI-Assisted Histopathology Report", rightMargin - 180f, y + 24f, subtitlePaint)
            y += 50f

            // Case & Patient Box
            val boxPaint = Paint().apply { color = Color.parseColor("#F0F4F9") }
            val borderPaint = Paint().apply { 
                color = Color.parseColor("#D0DBE5")
                style = Paint.Style.STROKE
                strokeWidth = 1f
            }
            val caseBoxRect = RectF(leftMargin, y, rightMargin, y + 60f)
            canvas.drawRoundRect(caseBoxRect, 8f, 8f, boxPaint)
            canvas.drawRoundRect(caseBoxRect, 8f, 8f, borderPaint)
            
            canvas.drawText("Case ID: ${analysisResult.case.caseId}", leftMargin + 10f, y + 15f, headerPaint)
            canvas.drawText("Patient ID: ${analysisResult.case.patient.patientId}", leftMargin + 10f, y + 30f, paint)
            canvas.drawText("Patient Name: ${analysisResult.case.patient.patientName}", leftMargin + 10f, y + 45f, paint)

            canvas.drawText("Tissue: ${analysisResult.case.tissue}", leftMargin + 250f, y + 15f, paint)
            canvas.drawText("Stain: ${analysisResult.case.stain}", leftMargin + 250f, y + 30f, paint)
            canvas.drawText("Date: ${analysisResult.case.analysisDate} | Status: ${analysisResult.case.status.name}", leftMargin + 250f, y + 45f, paint)
            
            y += 70f

            // Analyzed Specimen & Image Quality Box
            canvas.drawText("Analyzed Specimen & Image Quality", leftMargin, y + 10f, headerPaint)
            y += 15f
            val imgBoxHeight = 90f
            val imgBoxRect = RectF(leftMargin, y, rightMargin, y + imgBoxHeight)
            canvas.drawRoundRect(imgBoxRect, 6f, 6f, boxPaint)
            canvas.drawRoundRect(imgBoxRect, 6f, 6f, borderPaint)

            // Draw embedded HoVer-Net overlay thumbnail
            try {
                val overlayBmp = BitmapFactory.decodeResource(context.resources, R.drawable.hovernet_overlay)
                if (overlayBmp != null) {
                    val thumbRect = RectF(leftMargin + 6f, y + 6f, leftMargin + 80f, y + 80f)
                    canvas.drawBitmap(overlayBmp, null, thumbRect, null)
                    canvas.drawRoundRect(thumbRect, 4f, 4f, borderPaint)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val textOffset = leftMargin + 90f
            canvas.drawText("Image ID: ${analysisResult.imageInfo.imageId} | Source: ${analysisResult.imageInfo.source}", textOffset, y + 16f, paint)
            canvas.drawText("Dimensions: ${analysisResult.imageInfo.width} × ${analysisResult.imageInfo.height} px | Magnification: 40×", textOffset, y + 32f, paint)
            canvas.drawText("Quality: ${analysisResult.imageQuality.status} | Blur: ${analysisResult.imageQuality.blurStatus}", textOffset, y + 48f, paint)
            canvas.drawText("Segmentation: HoVer-Net Instance Model (Green=Nuclei, Red=Epithelial)", textOffset, y + 64f, paint)
            canvas.drawText("Calibration: ${analysisResult.imageQuality.calibrationStatus}", textOffset, y + 78f, smallPaint)

            y += imgBoxHeight + 15f
            
            // Quantitative Morphology Summary
            canvas.drawText("Quantitative Morphology Summary", leftMargin, y + 10f, headerPaint)
            y += 25f
            
            val metricWidth = contentWidth / 3
            canvas.drawText("Nuclei: ${analysisResult.nuclearAnalysis.nucleiDetected}", leftMargin, y, paint)
            canvas.drawText("Density: ${analysisResult.nuclearAnalysis.nuclearDensity}", leftMargin + metricWidth, y, paint)
            canvas.drawText("Circularity: ${analysisResult.nuclearAnalysis.nuclearCircularity}", leftMargin + 2 * metricWidth, y, paint)
            y += 15f
            canvas.drawText("Gland Count: ${analysisResult.glandAnalysis.glandCount}", leftMargin, y, paint)
            canvas.drawText("Mean Area: ${analysisResult.glandAnalysis.meanGlandArea}", leftMargin + metricWidth, y, paint)
            canvas.drawText("Irregularity: ${analysisResult.glandAnalysis.boundaryIrregularity}", leftMargin + 2 * metricWidth, y, paint)
            
            y += 25f

            // Comparison Table
            canvas.drawText("Reference vs Patient Comparison Table", leftMargin, y + 10f, headerPaint)
            y += 20f
            
            val tableHeaderBg = Paint().apply { color = Color.parseColor("#E3ECF6") }
            canvas.drawRect(leftMargin, y, rightMargin, y + 15f, tableHeaderBg)
            canvas.drawRect(leftMargin, y, rightMargin, y + 15f, borderPaint)
            
            val col1X = leftMargin + 5f
            val col2X = leftMargin + 200f
            val col3X = leftMargin + 350f
            
            canvas.drawText("Metric", col1X, y + 12f, headerPaint)
            canvas.drawText("Reference", col2X, y + 12f, headerPaint)
            canvas.drawText("Patient", col3X, y + 12f, headerPaint)
            y += 15f
            
            val altBg1 = Paint().apply { color = Color.WHITE }
            val altBg2 = Paint().apply { color = Color.parseColor("#F9F9F9") }
            
            analysisResult.referenceComparison.metrics.forEachIndexed { index, metric ->
                val bgPaint = if (index % 2 == 0) altBg1 else altBg2
                canvas.drawRect(leftMargin, y, rightMargin, y + 15f, bgPaint)
                canvas.drawRect(leftMargin, y, rightMargin, y + 15f, borderPaint)
                canvas.drawText(metric.name, col1X, y + 12f, paint)
                canvas.drawText(metric.referenceValue, col2X, y + 12f, paint)
                canvas.drawText(metric.patientValue, col3X, y + 12f, paint)
                y += 15f
            }
            
            y += 15f
            
            // AI-Assisted Interpretation & Supporting Evidence
            canvas.drawText("AI-Assisted Interpretation", leftMargin, y + 10f, headerPaint)
            y += 15f
            y = drawWrappedText(canvas, analysisResult.aiReport.interpretation, leftMargin, y + 15f, contentWidth, paint, 12f)
            y += 10f
            
            canvas.drawText("Supporting Evidence", leftMargin, y + 10f, headerPaint)
            y += 15f
            analysisResult.aiReport.supportingEvidence.forEach { evidence ->
                y = drawWrappedText(canvas, "• $evidence", leftMargin + 10f, y + 15f, contentWidth - 10f, paint, 12f)
            }
            y += 10f

            // Pathologist Review Required Box
            val reviewBgPaint = Paint().apply { color = Color.parseColor("#FFF0F0") }
            val reviewBorderPaint = Paint().apply { 
                color = Color.parseColor("#D32F2F")
                style = Paint.Style.STROKE
                strokeWidth = 2f
            }
            val reviewHeaderPaint = Paint().apply {
                color = Color.parseColor("#D32F2F")
                textSize = 12f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            
            // pre-calculate height
            val reviewTextLinesHeight = (analysisResult.aiReport.pathologistReview.message.length / 80 + 2) * 12f
            
            val reviewBoxRect = RectF(leftMargin, y, rightMargin, y + 30f + reviewTextLinesHeight)
            canvas.drawRoundRect(reviewBoxRect, 8f, 8f, reviewBgPaint)
            canvas.drawRoundRect(reviewBoxRect, 8f, 8f, reviewBorderPaint)
            canvas.drawText("PATHOLOGIST REVIEW REQUIRED", leftMargin + 10f, y + 20f, reviewHeaderPaint)
            drawWrappedText(canvas, analysisResult.aiReport.pathologistReview.message, leftMargin + 10f, y + 35f, contentWidth - 20f, paint, 12f)
            
            y = reviewBoxRect.bottom + 15f
            
            // Limitations & Research Use
            canvas.drawText("Limitations & Research Use", leftMargin, y + 10f, headerPaint)
            y += 15f
            analysisResult.aiReport.limitations.items.forEach { limit ->
                y = drawWrappedText(canvas, "• $limit", leftMargin + 10f, y + 15f, contentWidth - 10f, paint, 12f)
            }
            
            // Footer
            val footerY = 820f
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }
            canvas.drawLine(leftMargin, footerY - 15f, rightMargin, footerY - 15f, linePaint)
            canvas.drawText("ColonPath-AI • AI-Assisted Clinical Support • Not for standalone diagnosis • Confidential", leftMargin, footerY, smallPaint)

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
    
    fun drawWrappedText(
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
