package com.colleagues.austrom.managers

import android.util.Base64
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction
import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import java.security.NoSuchAlgorithmException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager {
    companion object {
        var encryptionKey: SecretKey? = null
        var activeApplication: AustromApplication? = null
    }

    fun hashPassword(password: String): String {
        return BCrypt.hashpw(password, BCrypt.gensalt())
    }

    fun isPasswordFitsHash(candidate: String, hash: String): Boolean {
        return BCrypt.checkpw(candidate, hash)
    }

    fun encrypt(text: String, encryptionKey: SecretKey): String {
//        if (encryptionKey==null) {
//            encryptionKey = generateEncryptionKey(AustromApplication.appUser!!.password, AustromApplication.appUser!!.userId.toByteArray())
//        }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, IvParameterSpec(ByteArray(16)))
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    fun encrypt(asset: Asset, secretKey: SecretKey): String { return encrypt(Gson().toJson(asset), secretKey) }
    fun encrypt(transaction: Transaction, secretKey: SecretKey): String { return encrypt(Gson().toJson(transaction), secretKey)  }

    fun decrypt(encryptedData: String, encryptionKey: SecretKey): String {
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, IvParameterSpec(ByteArray(16)))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
    fun decryptAsset(encryptedData: String, secretKey: SecretKey): Asset { return Gson().fromJson(decrypt(encryptedData, secretKey), Asset::class.java) }
    fun decryptTransaction(encryptedData: String, secretKey: SecretKey): Transaction { return Gson().fromJson(decrypt(encryptedData, secretKey), Transaction::class.java) }

    fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val key = keyGenerator.generateKey()
        //activeApplication!!.setEncryptionKey(key)
        return key
    }

    fun generateEncryptionKey(seed: String, salt: ByteArray): SecretKeySpec {
        val iterations = 65536
        val keyLength = 256
        val spec = PBEKeySpec(seed.toCharArray(), salt, iterations, keyLength)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun convertSecretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    fun convertStringToSecretKey(encodedKey: String?): SecretKey {
        val keyBytes = Base64.decode(encodedKey, Base64.DEFAULT)
        return SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
    }
}