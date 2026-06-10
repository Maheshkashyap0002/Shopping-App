package com.shoppingappmahesh.ui.screens.checkout

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayActivity : Activity(), PaymentResultListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Checkout.preload(applicationContext) // Optional optimization
        startPayment()
    }

    private fun startPayment() {
        val checkout = Checkout()
        // Essential: Set the key before calling open
      // For Live  checkout.setKeyID("rzp_live_Sxy8QuLWKIIHtg")
        checkout.setKeyID("rzp_test_SxaYxPl2MgoPdZ")

        try {
            val options = JSONObject()
            options.put("name", "Plant Shop")
            options.put("description", "Premium Nature Purchase")
            options.put("theme.color", "#2D6A4F") // Match app theme
            options.put("currency", "INR")
            
            // Getting data from intent
            val amountInRupees = intent.getDoubleExtra("amount", 0.0)
            options.put("amount", (amountInRupees * 100).toInt()) // Amount in paise
            
            val prefill = JSONObject()
            // Using user's actual email and phone for auto-generated Razorpay receipts
            prefill.put("email", intent.getStringExtra("email") ?: "customer@example.com")
            prefill.put("contact", intent.getStringExtra("phone") ?: "9876543210")
            options.put("prefill", prefill)

            Log.d("Razorpay", "Opening checkout with options: $options")
            checkout.open(this, options)
        } catch (e: Exception) {
            Log.e("Razorpay", "Error in payment: ${e.message}")
            Toast.makeText(this, "Error in payment: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.d("Razorpay", "Payment Success: $razorpayPaymentId")
        Toast.makeText(this, "Payment Successful", Toast.LENGTH_LONG).show()
        val data = android.content.Intent().apply {
            putExtra("payment_id", razorpayPaymentId)
        }
        setResult(RESULT_OK, data)
        finish()
    }

    override fun onPaymentError(code: Int, response: String?) {
        Log.e("Razorpay", "Payment Error: $code - $response")
        Toast.makeText(this, "Payment Failed: $response", Toast.LENGTH_LONG).show()
        setResult(RESULT_CANCELED)
        finish()
    }
}