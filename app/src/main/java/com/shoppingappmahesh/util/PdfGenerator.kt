package com.shoppingappmahesh.util

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.shoppingappmahesh.domain.model.Order
import java.io.File
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

object PdfGenerator {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842
    private const val MARGIN = 40f

    fun generateInvoice(context: Context, order: Order, onComplete: (Uri?) -> Unit = {}) {
        val pdfDocument = PdfDocument()
        val paint = Paint()
        val titlePaint = Paint()

        var pageNumber = 1
        var pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val accentColor = Color.parseColor("#1A73E8") // Premium Blue

        // Helper to check for new page
        fun checkAndCreateNewPage(currentY: Float, neededSpace: Float): Float {
            if (currentY + neededSpace > PAGE_HEIGHT - MARGIN) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                return MARGIN + 20f
            }
            return currentY
        }

        // 1. Header & Company Info
        titlePaint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        titlePaint.textSize = 28f
        titlePaint.color = accentColor
        canvas.drawText("FASHION BAJAR", MARGIN, 60f, titlePaint)

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("Premium Quality, Elite Style", MARGIN, 75f, paint)
        
        paint.color = Color.BLACK
        paint.textSize = 10f
        canvas.drawText("Mahesh Fashion Ltd.", PAGE_WIDTH - 200f, 50f, paint)
        canvas.drawText("Sector 45, Kamrid ", PAGE_WIDTH - 200f, 65f, paint)
        canvas.drawText("Support: +91 7724858379", PAGE_WIDTH - 200f, 80f, paint)
        canvas.drawText("Email: maheshkashyap0002@gmail.com", PAGE_WIDTH - 200f, 95f, paint)

        // 2. Invoice Details
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 12f
        canvas.drawText("INVOICE", MARGIN, 130f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 10f
        val invoiceNo = "INV-${System.currentTimeMillis().toString().takeLast(6)}"
        canvas.drawText("Invoice No: $invoiceNo", MARGIN, 145f, paint)
        canvas.drawText("Order ID: ${order.orderId}", MARGIN, 160f, paint)
        
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        canvas.drawText("Date: ${sdf.format(Date(order.createdAt))}", MARGIN, 175f, paint)

        // 3. Customer Information
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("BILL TO", PAGE_WIDTH - 200f, 130f, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        val addr = order.address
        canvas.drawText(addr?.fullName ?: "Customer Name", PAGE_WIDTH - 200f, 145f, paint)
        canvas.drawText(addr?.phone ?: "", PAGE_WIDTH - 200f, 160f, paint)
        canvas.drawText("${addr?.houseNo}, ${addr?.street}", PAGE_WIDTH - 200f, 175f, paint)
        canvas.drawText("${addr?.city}, ${addr?.state} - ${addr?.pincode}", PAGE_WIDTH - 200f, 190f, paint)

        // 4. Product Table Header
        var yPos = 230f
        paint.color = accentColor
        canvas.drawRect(MARGIN, yPos, PAGE_WIDTH - MARGIN, yPos + 25f, paint)
        
        paint.color = Color.WHITE
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("ITEM DESCRIPTION", MARGIN + 10f, yPos + 17f, paint)
        canvas.drawText("QTY", 380f, yPos + 17f, paint)
        canvas.drawText("PRICE", 440f, yPos + 17f, paint)
        canvas.drawText("TOTAL", 510f, yPos + 17f, paint)

        yPos += 45f
        paint.color = Color.BLACK
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // 5. Product List
        order.products.forEach { item ->
            yPos = checkAndCreateNewPage(yPos, 40f)
            
            val displayName = if (item.productName.length > 40) item.productName.take(37) + "..." else item.productName
            canvas.drawText(displayName, MARGIN + 10f, yPos, paint)
            canvas.drawText(item.quantity.toString(), 385f, yPos, paint)
            canvas.drawText("₹${item.productPrice.toInt()}", 440f, yPos, paint)
            canvas.drawText("₹${(item.productPrice * item.quantity).toInt()}", 510f, yPos, paint)
            
            yPos += 25f
            paint.color = Color.LTGRAY
            canvas.drawLine(MARGIN, yPos - 5f, PAGE_WIDTH - MARGIN, yPos - 5f, paint)
            paint.color = Color.BLACK
            yPos += 15f
        }

        // 6. Pricing Summary
        yPos = checkAndCreateNewPage(yPos, 150f)
        yPos += 20f
        
        val summaryX = PAGE_WIDTH - 200f
        canvas.drawText("Subtotal:", summaryX, yPos, paint)
        canvas.drawText("₹${order.amount.toInt()}", 510f, yPos, paint)
        
        yPos += 20f
        canvas.drawText("Delivery Charges:", summaryX, yPos, paint)
        canvas.drawText("FREE", 510f, yPos, paint)
        
        yPos += 20f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = accentColor
        paint.textSize = 14f
        canvas.drawText("TOTAL AMOUNT:", summaryX, yPos, paint)
        canvas.drawText("₹${order.amount.toInt()}", 510f, yPos, paint)

        // 7. Footer
        yPos = PAGE_HEIGHT - 100f
        paint.color = Color.BLACK
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("Terms & Conditions:", MARGIN, yPos, paint)
        
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.color = Color.GRAY
        yPos += 15f
        canvas.drawText("1. Goods once sold cannot be returned after 7 days.", MARGIN, yPos, paint)
        yPos += 12f
        canvas.drawText("2. Warranty is subject to brand terms.", MARGIN, yPos, paint)
        
        paint.color = accentColor
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("THANK YOU FOR YOUR BUSINESS!", MARGIN, PAGE_HEIGHT - MARGIN, paint)

        pdfDocument.finishPage(page)

        // Save and handle sharing
        val fileName = "FashionBajar_Invoice_${invoiceNo}.pdf"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/FashionBajar")
                }
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                uri?.let {
                    val outputStream: OutputStream? = resolver.openOutputStream(it)
                    outputStream?.use { os ->
                        pdfDocument.writeTo(os)
                    }
                    onComplete(it)
                    Toast.makeText(context, "Invoice saved to Downloads/FashionBajar", Toast.LENGTH_LONG).show()
                }
            } else {
                val file = File(context.getExternalFilesDir(null), fileName)
                pdfDocument.writeTo(file.outputStream())
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                onComplete(uri)
                Toast.makeText(context, "Invoice saved", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        } finally {
            pdfDocument.close()
        }
    }

    fun openPdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "application/pdf")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            context.startActivity(Intent.createChooser(intent, "Open Invoice"))
        } catch (e: Exception) {
            Toast.makeText(context, "No PDF viewer found", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdf(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND)
        intent.type = "application/pdf"
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, "Share Invoice"))
    }
}