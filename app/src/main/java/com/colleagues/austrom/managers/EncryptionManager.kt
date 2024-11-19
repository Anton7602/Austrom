package com.colleagues.austrom.managers
import android.content.Context
import android.util.Base64
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Transaction
import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.IvParameterSpec

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

    fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(128)
        val key = keyGenerator.generateKey()
        activeApplication!!.setEncryptionKey(key)
        return key
    }

    fun encrypt(text: String): String {
        if (encryptionKey==null) {
            encryptionKey = generateEncryptionKey()
        }
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, IvParameterSpec(ByteArray(16)))
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }

    fun encrypt(asset: Asset): String {
        return encrypt(Gson().toJson(asset))
    }

    fun encrypt(transaction: Transaction): String {
        return encrypt(Gson().toJson(transaction))
    }

    fun decrypt(encryptedData: String): String {
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, IvParameterSpec(ByteArray(16)))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun decryptAsset(encryptedData: String): Asset {
        return Gson().fromJson(decrypt(encryptedData), Asset::class.java)
    }
    
    fun decryptTransaction(encryptedData: String): Transaction {
        return Gson().fromJson(decrypt(encryptedData), Transaction::class.java)
    }
}