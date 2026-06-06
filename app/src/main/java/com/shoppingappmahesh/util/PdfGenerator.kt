package com.shoppingappmahesh.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import com.shoppingappmahesh.domain.model.Order
import java.io.OutputStream
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
        canvas.drawText("Shopping App with Premium Fashion", 40f, 50f, titlePaint)

        titlePaint.textSize = 14f
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        canvas.drawText("Official Payment Receipt", 40f, 80f, titlePaint)

        // Order Info
        paint.textSize = 12f
        canvas.drawText("Order ID: ${order.orderId}", 40f, 120f, paint)
        canvas.drawText("Payment ID: ${order.paymentId.ifEmpty { "N/A" }}", 40f, 140f, paint)
        
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Date: ${sdf.format(Date(order.createdAt))}", 40f, 160f, paint)
        canvas.drawText("Status: ${order.status}", 40f, 180f, paint)

        canvas.drawLine(40f, 210f, 555f, 210f, paint)

        // Items Header
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Product Details", 40f, 240f, titlePaint)
        canvas.drawText("Qty", 400f, 240f, titlePaint)
        canvas.drawText("Price", 500f, 240f, titlePaint)

        canvas.drawLine(40f, 255f, 555f, 255f, paint)

        // Items List
        var yPos = 280f
        order.products.forEach { item ->
            // Handle long product names
            val displayName = if (item.productName.length > 35) item.productName.substring(0, 32) + "..." else item.productName
            canvas.drawText(displayName, 40f, yPos, paint)
            canvas.drawText(item.quantity.toString(), 400f, yPos, paint)
            canvas.drawText("₹${item.productPrice.toInt()}", 500f, yPos, paint)
            yPos += 25f
            
            // Check if we need a new page (simplified for small orders)
            if (yPos > 750f) return@forEach 
        }

        canvas.drawLine(40f, yPos + 10f, 555f, yPos + 10f, paint)

        // Total
        titlePaint.textSize = 16f
        canvas.drawText("Total Amount Paid:", 350f, yPos + 40f, titlePaint)
        canvas.drawText("₹${order.amount.toInt()}", 500f, yPos + 40f, titlePaint)

        // Footer
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("This is an electronically generated receipt.", 40f, 800f, paint)
        canvas.drawText("Thank you for shopping with LuxiQue!", 40f, 815f, paint)

        pdfDocument.finishPage(page)

        val fileName = "Shopping_Invoice_${if(order.orderId.length > 6) order.orderId.takeLast(6) else "Receipt"}.pdf"

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/Shopping_App")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = resolver.openOutputStream(it)
                    outputStream?.use { os ->
                        pdfDocument.writeTo(os)
                    }
                    Toast.makeText(context, "Invoice saved to Downloads/Shopping_App", Toast.LENGTH_LONG).show()
                }
            } else {
                // Legacy support could be added here, but sticking to MediaStore for modern devices
                Toast.makeText(context, "Permission needed for older Android versions", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }
}