package com.minimaltask.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.minimaltask.data.model.Priority
import com.minimaltask.data.model.Task
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class ExportRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    // La scrittura su disco avviene fuori dal main thread per non bloccare la UI.
    suspend fun exportTxt(tasks: List<Task>): File = withContext(Dispatchers.IO) {
        val file = createFile("minimal-task-export.txt")
        file.writeText(
            tasks.joinToString(separator = "\n\n") { task ->
                buildString {
                    appendLine(task.title)
                    appendLine("Categoria: ${task.category}")
                    appendLine("Priorità: ${Priority.fromValue(task.priority).label}")
                    appendLine("Completato: ${if (task.completed) "sì" else "no"}")
                    task.description?.takeIf { it.isNotBlank() }?.let { appendLine(it) }
                }
            }
        )
        file
    }

    suspend fun exportPdf(tasks: List<Task>): File = withContext(Dispatchers.IO) {
        val file = createFile("minimal-task-export.pdf")
        val document = PdfDocument()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { textSize = 14f }
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 20f
            isFakeBoldText = true
        }
        var pageNumber = 1
        var page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
        var y = 48f
        page.canvas.drawText("MinimalTask export", 40f, y, titlePaint)
        y += 36f
        tasks.forEach { task ->
            if (y > 790f) {
                document.finishPage(page)
                pageNumber += 1
                page = document.startPage(PdfDocument.PageInfo.Builder(595, 842, pageNumber).create())
                y = 48f
            }
            page.canvas.drawText(fitToWidth("• ${task.title}", paint, MAX_TEXT_WIDTH), 40f, y, paint)
            y += 22f
            page.canvas.drawText("${task.category} · ${Priority.fromValue(task.priority).label}", 56f, y, paint)
            y += 26f
        }
        document.finishPage(page)
        file.outputStream().use(document::writeTo)
        document.close()
        file
    }

    // Tronca con "…" i testi più larghi della pagina invece di farli uscire dal bordo.
    private fun fitToWidth(text: String, paint: Paint, maxWidth: Float): String {
        if (paint.measureText(text) <= maxWidth) return text
        val count = paint.breakText(text, true, maxWidth - paint.measureText("…"), null)
        return text.take(count) + "…"
    }

    private fun createFile(name: String): File {
        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        if (!dir.exists()) dir.mkdirs()
        return File(dir, name)
    }

    private companion object {
        const val MAX_TEXT_WIDTH = 515f
    }
}
