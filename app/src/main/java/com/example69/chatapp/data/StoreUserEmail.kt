package com.example69.chatapp.data

import android.content.Context
import android.util.Log
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
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import android.util.Base64
import java.security.spec.RSAPrivateCrtKeySpec
import java.security.spec.RSAPublicKeySpec
import javax.crypto.Cipher


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
        val USER_USERNAME = stringPreferencesKey("user_username")
    }

    val getEmail: Flow<String> = context.dataStore.data.map { preferences ->
        val email = preferences[USER_EMAIL_KEY] ?: ""
        Log.d("STORE", "Retrieved email: $email")
        email
    }

    val getUsername: Flow<String> = context.dataStore.data.map { preferences ->
        val username = preferences[USER_USERNAME] ?: ""
        Log.d("STORE", "Retrieved Username: $username")
        username
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

    suspend fun saveEmail(email: String) {
        Log.e("PASSWORD", "saveEmail CALLED")
            context.dataStore.edit { preferences ->
                preferences[USER_EMAIL_KEY] = email
                Log.d("PASSWORD", "Stored email is: $email")
            }
    }

    suspend fun saveUsername(username: String) {
        Log.e("PASSWORD", "saveUsername CALLED")
        context.dataStore.edit { preferences ->
            preferences[USER_USERNAME] = username
            Log.d("PASSWORD", "Stored username is: $username")
        }
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

    fun generateRSAKeyPair(inputString: String): Pair<PrivateKey, PublicKey> {
        val n = p.multiply(q)

        val phi = (p.subtract(BigInteger.ONE)).multiply(q.subtract(BigInteger.ONE))

        var e = generateEFromString(inputString)

        while (e.gcd(phi) != BigInteger.ONE) {
            e+=BigInteger.ONE
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

    // Function to encode a private key to Base64
    fun encodePrivateKey(privateKey: PrivateKey): String {
//        val keyFactory = KeyFactory.getInstance(privateKey.algorithm)
//        val keySpec = keyFactory.getKeySpec(privateKey, PKCS8EncodedKeySpec::class.java)
//        val privateKeyBytes = keySpec.encoded
//        return privateKeyBytes.toString(Charsets.ISO_8859_1)
        return Base64.encodeToString(privateKey.encoded, Base64.DEFAULT)
    }

    // Function to encode a public key to Base64
    fun encodePublicKey(publicKey: PublicKey): String {
//        val keyFactory = KeyFactory.getInstance(publicKey.algorithm)
//        val keySpec = keyFactory.getKeySpec(publicKey, X509EncodedKeySpec::class.java)
//        val publicKeyBytes = keySpec.encoded
//        return publicKeyBytes.toString(Charsets.ISO_8859_1)
        return Base64.encodeToString(publicKey.encoded, Base64.DEFAULT)
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
                    preferences[USER_PRIVATE_KEY] = encodePrivateKey(pk.first)
                    preferences[USER_PUBLIC_KEY] = encodePublicKey(pk.second)
                    Log.e("PASSWORD", "${pk.first} and ${pk.second} is the password")
                } else {
                    val privateKeyHex = preferences[USER_PRIVATE_KEY]!!
                    val publicKeyHex = preferences[USER_PUBLIC_KEY]!!
                    val privateKeyBytes = Base64.decode(privateKeyHex, Base64.DEFAULT)
                    val privateKeyFactory = KeyFactory.getInstance("RSA")
                    val privateKeySpec = PKCS8EncodedKeySpec(privateKeyBytes)
                    val publicKeyBytes = Base64.decode(publicKeyHex, Base64.DEFAULT)
                    Log.e("CREATEUSER","Cslled getPublicKey, end my life please part2: $publicKeyBytes ")
                    val publicKeyFactory = KeyFactory.getInstance("RSA")
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

    fun decryptWithOAEP(
        encryptedText: String,
        privateKey: PrivateKey
    ): String? {
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
        val cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        cipher.init(Cipher.ENCRYPT_MODE, publicKey)
        val encryptedBytes = cipher.doFinal(textToEncrypt.toByteArray())
        return encryptedBytes.toString(Charsets.ISO_8859_1)
    }

}