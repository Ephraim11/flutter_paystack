package co.paystack.flutterpaystack

import android.util.Base64
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException

/**
 * Class for encrypting the card details, for token creation.
 *
 * @author {androidsupport@paystack.co} on 8/10/15.
 */
object Crypto {

    private const val PAYSTACK_RSA_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANIsL+RHqfkBiKGn/D1y1QnNrMkKzxWP" +
            "2wkeSokw2OJrCI+d6YGJPrHHx+nmb/Qn885/R01Gw6d7M824qofmCvkCAwEAAQ=="
    private const val ALGORITHM = "RSA"
    private const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    private fun encrypt(text: String, key: PublicKey): ByteArray {
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher.doFinal(text.toByteArray())
        } catch (e: NoSuchPaddingException) {
            throw SecurityException("Padding error: ${e.message}", e)
        } catch (e: NoSuchAlgorithmException) {
            throw SecurityException("Algorithm error: ${e.message}", e)
        } catch (e: InvalidKeySpecException) {
            throw SecurityException("Invalid key spec: ${e.message}", e)
        } catch (e: IllegalBlockSizeException) {
            throw SecurityException("Block size error: ${e.message}", e)
        } catch (e: Exception) {
            throw SecurityException("Encryption error: ${e.message}", e)
        }
    }

    @Throws(SecurityException::class)
    fun encrypt(text: String): String {
        val encryptedBytes = encrypt(text, getPublicKeyFromString(PAYSTACK_RSA_PUBLIC_KEY))
        return String(Base64.encode(encryptedBytes, Base64.NO_WRAP))
    }

    @Throws(SecurityException::class)
    private fun getPublicKeyFromString(pubKey: String): PublicKey {
        return try {
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val keyBytes = Base64.decode(pubKey, Base64.NO_WRAP)
            val keySpec = X509EncodedKeySpec(keyBytes)
            keyFactory.generatePublic(keySpec)
        } catch (e: InvalidKeySpecException) {
            throw SecurityException("Invalid public key: ${e.message}", e)
        } catch (e: NoSuchAlgorithmException) {
            throw SecurityException("Invalid algorithm: ${e.message}", e)
        }
    }
}
