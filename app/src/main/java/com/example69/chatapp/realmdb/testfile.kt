package com.example69.chatapp.realmdb

import android.os.Build
import androidx.annotation.RequiresApi
import java.math.BigInteger
import java.security.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec
import java.util.Base64
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.spec.IvParameterSpec


object AESUtil {
    private const val AES_KEY_SIZE = 256
    private const val AES_CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding"

    fun generateAESKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(AES_KEY_SIZE)
        return keyGenerator.generateKey()
    }

    fun generateIV(): ByteArray {
        val iv = ByteArray(16)
        SecureRandom().nextBytes(iv)
        return iv
    }

    fun encryptText(text: String, key: SecretKey, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
        val secretKeySpec = SecretKeySpec(key.encoded, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
        return cipher.doFinal(text.toByteArray(Charsets.UTF_8))
    }

    fun decryptText(encryptedText: ByteArray, key: SecretKey, iv: ByteArray): String {
        val cipher = Cipher.getInstance(AES_CIPHER_ALGORITHM)
        val secretKeySpec = SecretKeySpec(key.encoded, "AES")
        val ivParameterSpec = IvParameterSpec(iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec)
        val decryptedBytes = cipher.doFinal(encryptedText)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

object RSAUtil {
    val IVV  =  AESUtil.generateIV()
    fun generateRSAKeyPair(inputString: String): Pair<PrivateKey, PublicKey> {
        val p = BigInteger("208351966247")
        val q = BigInteger("275604541573")

        val n = p.multiply(q)
        val phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))

        var e = generateEFromString(inputString)

        while (e.gcd(phi) != BigInteger.ONE) {
            e += BigInteger.ONE
        }

        val d = e.modInverse(phi)
        val dP = d.mod(p.subtract(BigInteger.ONE))
        val dQ = d.mod(q.subtract(BigInteger.ONE))
        val qInv = q.modInverse(p)
        val privateSpec = RSAPrivateCrtKeySpec(n, e, d, p, q, dP, dQ, qInv)
        val publicSpec = RSAPublicKeySpec(n, e)

        val kf = KeyFactory.getInstance("RSA")
        val privateKey = kf.generatePrivate(privateSpec)
        val publicKey = kf.generatePublic(publicSpec)

        return Pair(privateKey, publicKey)
    }

    fun generateEFromString(input: String): BigInteger {
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(input.toByteArray())
        val hashBigInt = BigInteger(1, hashBytes)
        return hashBigInt.mod(BigInteger.valueOf(65537))
    }

    fun encryptPrivateKeyToString(privateKey: PrivateKey, publicKey: PublicKey): String {
        // Generate AES key and IV
        val aesKey = AESUtil.generateAESKey()

        // Encrypt private key with AES key and IV
        val encryptedPrivateKey = AESUtil.encryptText(privateKey.encoded.toString(Charsets.ISO_8859_1), aesKey, IVV)

        // Encrypt AES key with RSA public key
        val encryptedAESKey = encryptWithOAEP(aesKey.encoded.toString(Charsets.ISO_8859_1), publicKey)

        // Concatenate encrypted AES key and encrypted private key
        return "$encryptedAESKey|$encryptedPrivateKey"
    }

    fun decryptPrivateKeyFromString(encryptedData: String, privateKey: PrivateKey): PrivateKey? {
        // Split encrypted data into AES key and encrypted private key
        val (encryptedAESKey, encryptedPrivateKey) = encryptedData.split("|")

        // Decrypt AES key with RSA private key
        val decryptedAESKey = decryptWithOAEP(encryptedAESKey, privateKey)

        // Decrypt private key with AES key
        val decryptedPrivateKey = AESUtil.decryptText(Base64.getDecoder().decode(encryptedPrivateKey), SecretKeySpec(decryptedAESKey!!.toByteArray(), "AES"), IVV)

        // Convert decrypted private key back to PrivateKey object
        val keySpec = PKCS8EncodedKeySpec(decryptedPrivateKey.toByteArray(Charsets.ISO_8859_1))
        val kf = KeyFactory.getInstance("RSA")
        return kf.generatePrivate(keySpec)
    }

    fun encryptWithOAEP(textToEncrypt: String, publicKey: PublicKey): String {
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    fun decryptWithOAEP(encryptedText: String, privateKey: PrivateKey): String {
        val encryptedBytes = Base64.getDecoder().decode(encryptedText)
        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
}

fun main() {
    // Generate RSA key pair
    val inputString = "your_input_string_here"
    val rsaKeyPair = RSAUtil.generateRSAKeyPair(inputString)

    // Encrypt the private key and AES key using RSA public key
    val encryptedData = RSAUtil.encryptPrivateKeyToString(rsaKeyPair.first, rsaKeyPair.second)

    // Decrypt the private key using RSA private key
    val decryptedPrivateKey = RSAUtil.decryptPrivateKeyFromString(encryptedData, rsaKeyPair.first)

    println("Original private key: ${rsaKeyPair.first}")
    println("Decrypted private key: $decryptedPrivateKey")
}
