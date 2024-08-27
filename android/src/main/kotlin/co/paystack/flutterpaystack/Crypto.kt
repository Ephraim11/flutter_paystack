package co.paystack.flutterpaystack

import android.util.Base64
import java.security.KeyFactory
import java.security.PublicKey
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher

/**
 * Class for encrypting card details for token creation.
 *
 * @author {androidsupport@paystack.co} on 8/10/15.
 */
object Crypto {

    private const val PAYSTACK_RSA_PUBLIC_KEY = "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBANIsL+RHqfkBiKGn/D1y1QnNrMkKzxWP" +
            "2wkeSokw2OJrCI+d6YGJPrHHx+nmb/Qn885/R01Gw6d7M824qofmCvkCAwEAAQ=="
    private const val ALGORITHM = "RSA"
    private const val CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding"

    /**
     * Encrypts the given text using the RSA public key.
     *
     * @param text The plain text to be encrypted.
     * @return The encrypted string, encoded in Base64.
     * @throws SecurityException If encryption fails.
     */
    @Throws(SecurityException::class)
    fun encrypt(text: String): String {
        val publicKey = getPublicKeyFromString(PAYSTACK_RSA_PUBLIC_KEY)
        val cipherText = encryptWithPublicKey(text, publicKey)
            ?: throw SecurityException("Encryption failed")
        
        return Base64.encodeToString(cipherText, Base64.NO_WRAP)
    }

    /**
     * Encrypts the given text using the provided public key.
     *
     * @param text The plain text to be encrypted.
     * @param key The public key used for encryption.
     * @return The encrypted byte array, or null if an error occurs.
     */
    private fun encryptWithPublicKey(text: String, key: PublicKey): ByteArray? {
        return try {
            val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION).apply {
                init(Cipher.ENCRYPT_MODE, key)
            }
            cipher.doFinal(text.toByteArray())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Retrieves a public key from its string representation.
     *
     * @param pubKey The string representation of the public key.
     * @return The PublicKey object.
     * @throws SecurityException If the key is invalid.
     */
    @Throws(SecurityException::class)
    private fun getPublicKeyFromString(pubKey: String): PublicKey {
        return try {
            val keyFactory = KeyFactory.getInstance(ALGORITHM)
            val keyBytes = Base64.decode(pubKey, Base64.NO_WRAP)
            val keySpec = X509EncodedKeySpec(keyBytes)
            keyFactory.generatePublic(keySpec)
        } catch (e: Exception) {
            throw SecurityException("Invalid public key: ${e.message}")
        }
    }
}
