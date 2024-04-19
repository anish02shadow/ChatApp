package com.example69.chatapp.data

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import android.util.Base64
import java.nio.charset.Charset
import java.security.SecureRandom
import java.security.spec.RSAKeyGenParameterSpec
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec


class StoreUserEmail(private val context: Context) {

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply{
        load(null)
    }

    val p = BigInteger("134035458902455680039200430591897818596964591352500394352782469269764064434966427334539198085148731162659565884094632185991217599561588980146051736641992742959122463409456272912706697397995553650414103847067017075142526620730851746159040622257717691134641167369110602697295274158094068166153019608217")
    val q = BigInteger("915871427044476280941167413207596679982273879955127325095410681523756766598348058165850932017099050053004759437466892925098267053352806245770142962761214494139239520454477430161011778408247284156780827617553984491758946781296063826550046651597818716019108896509601026664180631145934142985538938521421")


    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("UserEmail")
        val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        val USER_PRIVATE_KEY = stringPreferencesKey("user_privatekey")
        val USER_PUBLIC_KEY = stringPreferencesKey("user_publickey")
        val USER_MESSAGE_PRIVATE_KEY = stringPreferencesKey("user_message_privatekey")
        val USER_MESSAGE_PUBLIC_KEY = stringPreferencesKey("user_message_publickey")
    }

    val getEmail: Flow<String> = context.dataStore.data.map { preferences ->
        val email = preferences[USER_EMAIL_KEY] ?: ""
        Log.d("STORE", "Retrieved email: $email")
        email
    }

    val getPublicKey: Flow<PublicKey> = context.dataStore.data.map { preferences ->
        var publicKey = preferences[USER_PUBLIC_KEY] ?: ""
        Log.e("CREATEUSER","Cslled getPublicKey, end my life please part1 $publicKey")
        Log.d("STORE", "Retrieved email: $publicKey")
        //val publicKeyBytes = publicKey.hexToByteArray()
        val publicKeyBytes = Base64.decode(publicKey, Base64.DEFAULT)
        Log.e("CREATEUSER","Cslled getPublicKey, end my life please part2: $publicKeyBytes ")
        val publicKeyFactory = KeyFactory.getInstance("RSA")
        val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
        val publicKey2 = publicKeyFactory.generatePublic(publicKeySpec)
        Log.e("CREATEUSER","Cslled getPublicKey, end my life please part3: $publicKey2 ")
        publicKey2
    }

    val getPrivateKey: Flow<PrivateKey> = context.dataStore.data.map { preferences ->
        var privateKey = preferences[USER_PRIVATE_KEY] ?: ""
        //val privateKeyBytes = privateKey.hexToByteArray()
        val privateKeyBytes = Base64.decode(privateKey, Base64.DEFAULT)
        val privateKeyFactory = KeyFactory.getInstance("RSA")
        val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
        val privateKey2 = privateKeyFactory.generatePrivate(privateKeySpec)
        Log.e("CREATEUSER","Cslled getPRIVATE KEY, end my life please part3: $privateKey2 ")
        privateKey2
    }

//    suspend fun getPublicKey2(): PublicKey {
//        var publicKey: PublicKey = getDummyKeyPair().second
//        Log.e("CREATEUSER","Cslled getPublicKey, end my life please part1")
//        var publicKeyHex = ""
//            context.dataStore.data.collect { preferences ->
//                Log.e("CREATEUSER", "Cslled getPublicKey, end my life please part2 inside")
//                publicKeyHex = preferences[StoreUserEmail.USER_PUBLIC_KEY].toString()
//            }
//                Log.e("CREATEUSER","Cslled getPublicKey, end my life please part3: $publicKeyHex ")
//                if (!publicKeyHex.isNullOrEmpty()) {
//                    val publicKeyBytes = publicKeyHex.hexToByteArray()
//                    Log.e("CREATEUSER","Cslled getPublicKey, end my life please part4: $publicKeyBytes ")
//                    val publicKeyFactory = KeyFactory.getInstance("RSA")
//                    val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
//                    publicKey = publicKeyFactory.generatePublic(publicKeySpec)
//                    Log.e("CREATEUSER","Cslled getPublicKey, end my life please part5: $publicKey ")
//                }
//        Log.e("CREATEUSER","Cslled getPublicKey, end my life please part6 is return being called?: $publicKey ")
//        return publicKey
//    }

    suspend fun getPrivateKeyy(): PrivateKey {
        var privateKey: PrivateKey = getDummyKeyPair().first
        withContext(Dispatchers.IO) {
            context.dataStore.data.collect { preferences ->
                val privateKeyHex = preferences[StoreUserEmail.USER_PRIVATE_KEY]
                if (!privateKeyHex.isNullOrEmpty()) {
                    val privateKeyBytes = privateKeyHex.hexToByteArray()
                    val privateKeyFactory = KeyFactory.getInstance("RSA")
                    val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                    privateKey = privateKeyFactory.generatePrivate(privateKeySpec)
                }
            }
        }
        return privateKey
    }

//    suspend fun getPriPubKeys(email: String): Pair<BigInteger, BigInteger> {
//        val preferences = context.dataStore.data.first()
//        val privateKeyHex = preferences[USER_PRIVATE_KEY] ?: ""
//        val publicKeyHex = preferences[USER_PUBLIC_KEY] ?: ""
//
//        return if (privateKeyHex.isNotEmpty() && publicKeyHex.isNotEmpty()) {
//            BigInteger(privateKeyHex, 16) to BigInteger(publicKeyHex, 16)
//        } else {
//            Log.e("ADDCHAT","FK MAN")
//            val (privateKey, publicKey) = generateRSAKeyPair()
//            Pair(privateKey, publicKey)
//        }
//    }

    val getPriK: Flow<String> = context.dataStore.data.map { preferences ->
        val pk = preferences[USER_PRIVATE_KEY] ?: ""
        Log.d("STORE", "Retrieved Private key: $pk")
        pk
    }

    val getPubK: Flow<String> = context.dataStore.data.map { preferences ->
        val pk = preferences[USER_PRIVATE_KEY] ?: ""
        Log.d("STORE", "Retrieved Private key: $pk")
        pk
    }

    fun getContext(): Context {
        return context
    }
//    fun encryptMessage(message: String, p: BigInteger, g: BigInteger, publicKey: BigInteger): Pair<BigInteger, ByteArray> {
//        return encrypt(message, publicKey, p, g)
//    }
//
//    fun decryptMessage(encryptedMessage: Pair<BigInteger, ByteArray>, privateKey: BigInteger): String {
//        return decrypt(encryptedMessage, privateKey, p)
//    }

    suspend fun saveEmail(email: String) {
        Log.e("PASSWORD", "saveEmail CALLED")
            context.dataStore.edit { preferences ->
                preferences[USER_EMAIL_KEY] = email
                Log.d("PASSWORD", "Stored email is: $email")
            }
    }

    suspend fun savePubK(email: String) {
        Log.e("PASSWORD", "saveEmail CALLED")
        context.dataStore.edit { preferences ->
            preferences[USER_PUBLIC_KEY] = email
            Log.d("PASSWORD", "Stored email is: $email")
        }
    }

    suspend fun savePriK(email: String) {
        Log.e("PASSWORD", "saveEmail CALLED")
        context.dataStore.edit { preferences ->
            preferences[USER_PRIVATE_KEY] = email
            Log.d("PASSWORD", "Stored email is: $email")
        }
    }

    val FIXED_SALT = byteArrayOf(
        0x01, 0x23, 0x45, 0x67,
        0x12, 0x1A, 0xB, 0x69,
        0xF, 0x4B, 0x1C, 0x4E,
        0x76, 0x54, 0x32, 0x10
    )
    fun generateKeyFromPassword(password: String): Pair<ByteArray,ByteArray> {
        //val salt = ByteArray(16).apply { (0 until size).forEach { set(it, (Math.random() * 256).toInt().toByte()) } } // Generate a random salt
        val iterations = 10000 // Recommended minimum iterations
        val keyLength = 256 // Key length in bits
        val spec = PBEKeySpec(password.toCharArray(), FIXED_SALT, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = factory.generateSecret(spec).encoded

        val midpoint = secretKey.size / 2
        val publicKey = secretKey.copyOfRange(0, midpoint)
        val privateKey = secretKey.copyOfRange(midpoint, secretKey.size)

        return (privateKey to publicKey)
    }

    fun generateKeyFromPasswordNew(password: String): ByteArray {
        //val salt = ByteArray(16).apply { (0 until size).forEach { set(it, (Math.random() * 256).toInt().toByte()) } } // Generate a random salt
        val iterations = 10000 // Recommended minimum iterations
        val keyLength = 256 // Key length in bits
        val spec = PBEKeySpec(password.toCharArray(), FIXED_SALT, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val secretKey = factory.generateSecret(spec).encoded

        return (secretKey)
    }


    fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val hex = StringBuilder(2 * bytes.size)
        for (b in bytes) {
            hex.append(hexChars[b.toInt() shr 4 and 0x0F])
            hex.append(hexChars[b.toInt() and 0x0F])
        }
        return hex.toString()
    }

    fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(if (len % 2 == 0) len / 2 else len / 2 + 1)
        var i = 0
        while (i < len) {
            data[i / 2] = (
                    (Character.digit(hex[i], 16) shl 4) +
                            Character.digit(if (i + 1 < len) hex[i + 1] else '0', 16)
                    ).toByte()
            i += 2
        }
        return data
    }



//    fun generateRSAKeyPair(password: String): Pair<PrivateKey, PublicKey> {
//        val keySpec = PBEKeySpec(password.toCharArray(), FIXED_SALT, 10000, 2048)
//        val secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
//        val derivedKey = secretKeyFactory.generateSecret(keySpec).encoded
//
//        val keyFactory = KeyFactory.getInstance("RSA")
//        val modulus = BigInteger(1, derivedKey.sliceArray(0 until 128))
//        val exponent = BigInteger(1, derivedKey.sliceArray(128 until 256))
//
//        val privateKey = keyFactory.generatePrivate(RSAPrivateKeySpec(modulus, exponent))
//        val publicKey = keyFactory.generatePublic(RSAPublicKeySpec(modulus, exponent))
//
//        return Pair(privateKey, publicKey)
//    }

     //Function to generate RSA key pair from given prime numbers p and q
    fun generateRSAKeyPair(inputString: String): Pair<PrivateKey, PublicKey> {
        // Calculate n = p * q
        val n = p.multiply(q)

        // Calculate φ(n) = (p-1)(q-1)
        val phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))

        // Generate e from the input string
        var e = generateEFromString(inputString)

        while (e.gcd(phi) != BigInteger.ONE) {
            e+=BigInteger.ONE
        }

        // Calculate d such that d ≡ e^(-1) mod φ(n)
        val d = e.modInverse(phi)

         val dP = d.mod(p.subtract(BigInteger.ONE))
         val dQ = d.mod(q.subtract(BigInteger.ONE))
         val qInv = q.modInverse(p)

        // Create RSAPrivateKeySpec for private key
         val privateSpec = RSAPrivateCrtKeySpec(n, e, d, p, q, dP, dQ, qInv)

        // Create RSAPublicKeySpec for public key
        val publicSpec = RSAPublicKeySpec(n, e)

        // Generate private and public keys
        val kf = KeyFactory.getInstance("RSA")
        val privateKey = kf.generatePrivate(privateSpec)
        val publicKey = kf.generatePublic(publicSpec)

        return Pair(privateKey, publicKey)
    }

    fun generateEFromString(input: String): BigInteger {
        // Hash the input string using SHA-256
        val md = MessageDigest.getInstance("SHA-256")
        val hashBytes = md.digest(input.toByteArray())

        // Convert the hash bytes into a BigInteger
        val hashBigInt = BigInteger(1, hashBytes)

        // Ensure that the generated BigInteger is smaller than the maximum value of e
        // (65537 is commonly used as a value for e)
        return hashBigInt.mod(BigInteger.valueOf(65537))
    }


//    fun generateRSAKeyPair(password: String): Pair<PrivateKey, PublicKey> {
//        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
//        val random = SecureRandom()
//        random.setSeed(generatePasswordHash(password).toByteArray())
//        keyPairGenerator.initialize(2048,random)
//        val keyPair = keyPairGenerator.generateKeyPair()
//        return Pair(keyPair.private, keyPair.public)
//    }
//
//
//    private fun generatePasswordHash(password: String): String {
//        val messageDigest = MessageDigest.getInstance("SHA-256")
//        messageDigest.update(password.toByteArray())
//        val hash = messageDigest.digest()
//        return BigInteger(1, hash).toString(16)
//    }

//    fun encryptWithOAEP(message: ByteArray, publicKey: BigInteger): ByteArray {
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
//        val publicKeySpec = RSAPublicKeySpec(publicKey, BigInteger.valueOf(65537))
//        val publicKeyFactory = KeyFactory.getInstance("RSA")
//        val publicKeyObj = publicKeyFactory.generatePublic(publicKeySpec)
//
//        cipher.init(
//            Cipher.ENCRYPT_MODE,
//            publicKeyObj,
//            OAEPParameterSpec(
//                "SHA-256",
//                "MGF1",
//                MGF1ParameterSpec.SHA1,
//                PSource.PSpecified.DEFAULT
//            )
//        )
//        return cipher.doFinal(message)
//    }
//
//    fun decryptWithOAEP(encryptedMessage: ByteArray, privateKey: BigInteger): ByteArray {
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPPadding")
//        val privateKeySpec = RSAPrivateKeySpec(privateKey, BigInteger.valueOf(65537))
//        val privateKeyFactory = KeyFactory.getInstance("RSA")
//        val privateKeyObj = privateKeyFactory.generatePrivate(privateKeySpec)
//
//        cipher.init(
//            Cipher.DECRYPT_MODE,
//            privateKeyObj,
//            OAEPParameterSpec(
//                "SHA-256",
//                "MGF1",
//                MGF1ParameterSpec.SHA1,
//                PSource.PSpecified.DEFAULT
//            )
//        )
//        return cipher.doFinal(encryptedMessage)
//    }

    // Function to encode a private key to Base64
    fun encodePrivateKey(privateKey: PrivateKey): String {
        val keyFactory = KeyFactory.getInstance(privateKey.algorithm)
        val keySpec = keyFactory.getKeySpec(privateKey, PKCS8EncodedKeySpec::class.java)
        val privateKeyBytes = keySpec.encoded
        return Base64.encodeToString(privateKeyBytes, Base64.DEFAULT)
    }

    // Function to encode a public key to Base64
    fun encodePublicKey(publicKey: PublicKey): String {
        val keyFactory = KeyFactory.getInstance(publicKey.algorithm)
        val keySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec::class.java)
        val publicKeyBytes = keySpec.encoded
        return Base64.encodeToString(publicKeyBytes, Base64.DEFAULT)
    }

    suspend fun savePK(password: String, email: String): Pair<PrivateKey, PublicKey> {
        var pk: Pair<PrivateKey, PublicKey> = getDummyKeyPair()
        Log.e("PASSWORD", "savePK CALLED")

        withContext(Dispatchers.IO) {
            context.dataStore.edit { preferences ->
                val presentornot = preferences[USER_EMAIL_KEY]
                if (presentornot.isNullOrEmpty()) {
                    Log.e("PASSWORD", "$presentornot is the presentornot")
                    pk = generateRSAKeyPair(password)
                    Log.e("PASSWORD", "CANT GENERATE KEYS OR WHAT")
//                    preferences[USER_PRIVATE_KEY] = pk.first.encoded.toHexString()
//                    preferences[USER_PUBLIC_KEY] = pk.second.encoded.toHexString()
                    preferences[USER_PRIVATE_KEY] = encodePrivateKey(pk.first)
                    preferences[USER_PUBLIC_KEY] = encodePublicKey(pk.second)
                    Log.e("PASSWORD", "${pk.first} and ${pk.second} is the password")
                } else {
                    val privateKeyHex = preferences[USER_PRIVATE_KEY]!!
                    val publicKeyHex = preferences[USER_PUBLIC_KEY]!!
                    val privateKeyBytes = privateKeyHex.hexToByteArray()
                    val publicKeyBytes = publicKeyHex.hexToByteArray()
                    val privateKeyFactory = KeyFactory.getInstance("RSA")
                    val publicKeyFactory = KeyFactory.getInstance("RSA")
                    val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                    val publicKeySpec = X509EncodedKeySpec(publicKeyBytes)
                    pk = privateKeyFactory.generatePrivate(privateKeySpec) to publicKeyFactory.generatePublic(publicKeySpec)
                    Log.e("PASSWORD", "${pk.first} and ${pk.second} is the password")
                }
            }
        }

        return pk
    }

     fun getDummyKeyPair(): Pair<PrivateKey, PublicKey> {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()
        return keyPair.private to keyPair.public
    }

    private fun String.hexToByteArray(): ByteArray {
        return chunked(2)
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    private fun ByteArray.toHexString(): String {
        return joinToString("") { "%02X".format(it) }
    }


//    fun encryptWithOAEP(input: String, publicKey: PublicKey): String {
//        val blockSize = 128  // Max block size for RSA encryption with OAEP padding
//        val inputBytes = input.toByteArray(Charsets.UTF_8)
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
//        val encryptedChunks = mutableListOf<String>()
//        var offset = 0
//        while (offset < inputBytes.size) {
//            val chunkSize = minOf(blockSize, inputBytes.size - offset)
//            val encryptedChunk = cipher.doFinal(inputBytes, offset, chunkSize)
//            encryptedChunks.add(Base64.encodeToString(encryptedChunk,Base64.DEFAULT))
//            offset += blockSize
//        }
//        return encryptedChunks.joinToString("")
//    }
//
//    fun decryptWithOAEP(encryptedText: String, privateKey: PrivateKey): String? {
//        val encryptedChunks = Base64.decode(encryptedText, Base64.DEFAULT)
//        val blockSize = 256 // Max block size for RSA encryption with OAEP padding
//
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//
//        val decryptedChunks = mutableListOf<String>()
//        var offset = 0
//
//        while (offset < encryptedChunks.size) {
//            val chunkSize = minOf(blockSize, encryptedChunks.size - offset)
//            val decryptedChunk = cipher.doFinal(encryptedChunks, offset, chunkSize)
//            decryptedChunks.add(String(decryptedChunk, Charsets.UTF_8))
//            offset += blockSize
//        }
//        return decryptedChunks.joinToString("")
//    }

     val CHUNK_SEPARATOR = ":::" // Unique separator between encrypted chunks

//    fun encryptWithOAEP(input: String, publicKey: PublicKey): String {
//        val blockSize = 64 // Max block size for RSA encryption with OAEP padding
//        val inputBytes = input.toByteArray(Charsets.UTF_8)
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
//        val encryptedChunks = mutableListOf<String>()
//        var offset = 0
//        while (offset < inputBytes.size) {
//            val chunkSize = minOf(blockSize, inputBytes.size - offset)
//            val encryptedChunk = cipher.doFinal(inputBytes, offset, chunkSize)
//            encryptedChunks.add(Base64.encodeToString(encryptedChunk, Base64.DEFAULT))
//            encryptedChunks.add(CHUNK_SEPARATOR) // Add the separator after each encrypted chunk
//            offset += blockSize
//        }
//        return encryptedChunks.joinToString("")
//    }
//
//    fun decryptWithOAEP(encryptedText: String, privateKey: PrivateKey): String? {
//        val encryptedChunks = encryptedText.split(CHUNK_SEPARATOR) // Split the input by the separator
//        val blockSize = 256 // Max block size for RSA encryption with OAEP padding
//        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val decryptedChunks = mutableListOf<String>()
//        for (i in encryptedChunks.indices step 2) {
//            val encryptedChunk = Base64.decode(encryptedChunks[i], Base64.DEFAULT)
//            try {
//                val decryptedChunk = cipher.doFinal(encryptedChunk)
//                decryptedChunks.add(String(decryptedChunk, Charsets.UTF_8))
//            } catch (e: BadPaddingException) {
//                // Handle the bad padding exception
//                Log.e("Decryption", "OAEP decoding error: $e")
//                return null
//            }
//        }
//        return decryptedChunks.joinToString("")
//    }


//    fun decryptWithOAEP(
//        encryptedText: String,
//        privateKey: PrivateKey
//    ): String? {
//        //val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        cipher.init(Cipher.DECRYPT_MODE, privateKey)
//        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedText, Base64.DEFAULT))
//        return String(decryptedBytes)
//    }
//
//
//    fun encryptWithOAEP(
//        textToEncrypt: String,
//        publicKey: PublicKey
//    ): String {
//        //val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
//        val cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding")
//        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
//        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray(StandardCharsets.UTF_8))
//        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
//    }

    fun decryptWithOAEP(
        encryptedText: String,
        privateKey: PrivateKey
    ): String? {
        //val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val encryptedBytes = encryptedText.toByteArray(Charsets.ISO_8859_1)
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.DECRYPT_MODE, privateKey)
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes)
    }


    fun encryptWithOAEP(
        textToEncrypt: String,
        publicKey: PublicKey
    ): String {
        //val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray())
        return encryptedBytes.toString(Charsets.ISO_8859_1)
    }

    fun encryptWithOAEPByteArray(
        bytearr: ByteArray,
        publicKey: PublicKey
    ): String {
        //val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(bytearr)
        return encryptedBytes.toString(Charsets.ISO_8859_1)
    }
}