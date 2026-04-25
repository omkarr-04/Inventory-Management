package com.example.inventory_management

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfHelper {

    fun generateAndShareInvoice(context: Context, job: JobCard) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        val paint = Paint()
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 20f
        }

        var y = 40f
        val margin = 40f

        // Shop Header
        canvas.drawText("KK Auto", margin, y, titlePaint)
        y += 25f
        paint.textSize = 12f
        canvas.drawText("Address: Dahisar, Mumbai", margin, y, paint)
        y += 15f
        canvas.drawText("Phone: +91 7040634460, +91 8779399970", margin, y, paint)
        y += 30f

        // Invoice Info
        canvas.drawLine(margin, y, 555f, y, paint)
        y += 20f
        canvas.drawText("INVOICE NO: ${job.invoiceNumber}", margin, y, paint)
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        canvas.drawText("DATE: ${dateFormat.format(job.date)}", 400f, y, paint)
        y += 30f

        // Customer Info
        canvas.drawText("CUSTOMER: ${job.customerName}", margin, y, paint)
        y += 15f
        canvas.drawText("VEHICLE: ${job.vehicleNumber}", margin, y, paint)
        y += 30f

        // Table Header
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("PART NAME", margin, y, paint)
        canvas.drawText("QTY", 300f, y, paint)
        canvas.drawText("PRICE", 400f, y, paint)
        canvas.drawText("TOTAL", 500f, y, paint)
        y += 10f
        canvas.drawLine(margin, y, 555f, y, paint)
        y += 20f

        // Parts List
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        for (part in job.partsUsed) {
            canvas.drawText(part.partName, margin, y, paint)
            canvas.drawText(part.quantityUsed.toString(), 300f, y, paint)
            canvas.drawText(String.format("%.2f", part.priceAtTime), 400f, y, paint)
            canvas.drawText(String.format("%.2f", part.quantityUsed * part.priceAtTime), 500f, y, paint)
            y += 20f
        }

        y += 20f
        canvas.drawLine(margin, y, 555f, y, paint)
        y += 30f

        // Summary
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Labor Charge:", 350f, y, paint)
        canvas.drawText(String.format("%.2f", job.laborCharge), 500f, y, paint)
        y += 20f
        canvas.drawText("GST (${job.gstPercentage}%):", 350f, y, paint)
        val subtotal = job.partsUsed.sumOf { it.priceAtTime * it.quantityUsed } + job.laborCharge
        val gstAmount = subtotal * (job.gstPercentage / 100.0)
        canvas.drawText(String.format("%.2f", gstAmount), 500f, y, paint)
        y += 25f
        
        paint.textSize = 16f
        canvas.drawText("TOTAL AMOUNT:", 350f, y, paint)
        canvas.drawText("₹${String.format("%.2f", job.totalAmount)}", 480f, y, paint)
        
        y += 40f
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
        canvas.drawText("Thank you for your business!", margin, y, paint)

        pdfDocument.finishPage(page)

        // Save PDF
        val file = File(context.cacheDir, "Invoice_${job.vehicleNumber}.pdf")
        try {
            pdfDocument.writeTo(FileOutputStream(file))
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pdfDocument.close()

        sharePdf(context, file)
    }

    private fun sharePdf(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share Invoice via"))
    }
}
