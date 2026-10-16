package com.example.data

import com.example.model.TableQrData
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object QrSecurityUtil {
    // Secret key for HMAC QR signing (managed centrally on server)
    private const val SERVER_QR_SECRET = "AlMokhtar_Riyadh_Secured_QR_Token_2026_x89"
    private const val BASE_WEB_URL = "https://almokhtar.sa/order"

    fun generateTableQr(branchId: String, tableNumber: Int): TableQrData {
        val timestamp = System.currentTimeMillis()
        val payload = "$branchId:$tableNumber"
        val signature = calculateHmacSha256(payload, SERVER_QR_SECRET)
        val shortSig = signature.take(12)
        val url = "$BASE_WEB_URL?b=$branchId&t=$tableNumber&sig=$shortSig"

        return TableQrData(
            branchId = branchId,
            tableNumber = tableNumber,
            signature = shortSig,
            generatedAtMillis = timestamp,
            url = url
        )
    }

    fun validateTableSignature(branchId: String, tableNumber: Int, signature: String): Boolean {
        if (signature.isBlank()) return false
        val payload = "$branchId:$tableNumber"
        val expected = calculateHmacSha256(payload, SERVER_QR_SECRET).take(12)
        return expected.equals(signature, ignoreCase = true)
    }

    private fun calculateHmacSha256(data: String, key: String): String {
        return try {
            val sha256Hmac = Mac.getInstance("HmacSHA256")
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            sha256Hmac.init(secretKey)
            val bytes = sha256Hmac.doFinal(data.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback digest if Mac unavailable
            val md = MessageDigest.getInstance("SHA-256")
            val digest = md.digest("$data:$key".toByteArray(Charsets.UTF_8))
            digest.joinToString("") { "%02x".format(it) }
        }
    }
}
