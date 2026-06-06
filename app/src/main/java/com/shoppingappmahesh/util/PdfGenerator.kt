package com.shoppingappmahesh.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.shoppingappmahesh.domain.model.Order
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    fun generateInvoice(context: Context, order: Order) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        // Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 24f
        titlePaint.color = Color.BLACK
        canvas.drawText("LuxiQue Premium Fashion", 40f, 50f, titlePaint)

        titlePaint.textSize = 14f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Invoice / Payment Receipt", 40f, 80f, titlePaint)

        // Order Info
        paint.textSize = 12f
        canvas.drawText("Order ID: ${order.orderId}", 40f, 120f, paint)
        canvas.drawText("Payment ID: ${order.paymentId}", 40f, 140f, paint)
        
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Date: ${sdf.format(Date(order.createdAt))}", 40f, 160f, paint)
        canvas.drawText("Status: ${order.status}", 40f, 180f, paint)

        canvas.drawLine(40f, 210f, 555f, 210f, paint)

        // Items Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Product Name", 40f, 240f, titlePaint)
        canvas.drawText("Qty", 400f, 240f, titlePaint)
        canvas.drawText("Price", 500f, 240f, titlePaint)

        canvas.drawLine(40f, 255f, 555f, 255f, paint)

        // Items List
        var yPos = 280f
        order.products.forEach { item ->
            canvas.drawText(item.productName, 40f, yPos, paint)
            canvas.drawText(item.quantity.toString(), 400f, yPos, paint)
            canvas.drawText("INR ${item.productPrice.toInt()}", 500f, yPos, paint)
            yPos += 25f
        }

        canvas.drawLine(40f, yPos + 10f, 555f, yPos + 10f, paint)

        // Total
        titlePaint.textSize = 16f
        canvas.drawText("Total Amount:", 400f, yPos + 40f, titlePaint)
        canvas.drawText("INR ${order.amount.toInt()}", 500f, yPos + 40f, titlePaint)

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Thank you for shopping with LuxiQue!", 200f, 800f, paint)

        pdfDocument.finishPage(page)

        // Save File
        val fileName = "Invoice_${order.orderId.takeLast(6)}.pdf"
        val filePath = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

        try {
            pdfDocument.writeTo(FileOutputStream(filePath))
            Toast.makeText(context, "Invoice saved to Downloads", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error generating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        pdfDocument.close()
    }
}