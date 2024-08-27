package co.paystack.flutterpaystack

import android.util.Base64
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

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

    /**
     * Encrypt the given text using the Paystack RSA public key.
     *
     * @throws SecurityException if the encryption process fails.
     */
    @Throws(SecurityException::class)
    fun encrypt(text: String): String {
        return try {
            val publicKey = getPublicKeyFromString(PAYSTACK_RSA_PUBLIC_KEY)
            val cipherText = encrypt(text, publicKey)
            Base64.encodeToString(cipherText, Base64.NO_WRAP)
        } catch (e: Exception) {
            throw SecurityException("Encryption failed: ${e.message}", e)
        }
    }

    /**
     * Encrypt the provided text using the specified public key.
     *
     * @param text The plaintext to be encrypted.
     * @param key The public key for encryption.
     * @return Encrypted byte array.
     */
    private fun encrypt(text: String, key: PublicKey): ByteArray {
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, key)
            cipher.doFinal(text.toByteArray())
        } catch (e: Exception) {
            throw SecurityException("Error while encrypting: ${e.message}", e)
        }
    }

    /**
     * Convert a Base64 encoded public key string to a PublicKey object.
     *
     * @throws SecurityException if the key conversion fails.
     */
    @Throws(SecurityException::class)
    private fun getPublicKeyFromString(pubKey: String): PublicKey {
        return try {
            val keyBytes = Base64.decode(pubKey, Base64.NO_WRAP)
            val spec = X509EncodedKeySpec(keyBytes)
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            keyFactory.generatePublic(spec)
        } catch (e: InvalidKeySpecException) {
            throw SecurityException("Invalid public key specification: ${e.message}", e)
        } catch (e: NoSuchAlgorithmException) {
            throw SecurityException("Algorithm not found: ${e.message}", e)
        }
    }
}
