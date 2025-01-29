package com.colleagues.austrom.managers

import android.util.Base64
import com.colleagues.austrom.models.Asset
import com.colleagues.austrom.models.Invitation
import com.colleagues.austrom.models.Transaction
import com.colleagues.austrom.models.TransactionDetail
import com.google.gson.Gson
import org.mindrot.jbcrypt.BCrypt
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class EncryptionManager {
    fun hashPassword(password: String): String { return BCrypt.hashpw(password, BCrypt.gensalt()) }
    fun isPasswordFitsHash(candidate: String, hash: String): Boolean { return BCrypt.checkpw(candidate, hash) }

    fun encrypt(text: String, secretKey: SecretKey): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
        val encryptedBytes = cipher.doFinal(text.toByteArray(Charsets.UTF_8))
        return Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
    }
    //fun encrypt(asset: Asset, secretKey: SecretKey): String { return encrypt(Gson().toJson(asset), secretKey) }
    fun encrypt(asset: Asset, secretKey: SecretKey): String { return encrypt(asset.serialize(), secretKey) }
    fun encrypt(transaction: Transaction, secretKey: SecretKey): String { return encrypt(transaction.serialize(), secretKey) }
    fun encrypt(transactionDetail: TransactionDetail, secretKey: SecretKey): String { return encrypt(transactionDetail.serialize(), secretKey) }
    fun encrypt(invitation: Invitation, secretKey: SecretKey): String {return encrypt(invitation.serialize(), secretKey)}

    fun decrypt(encryptedData: String, secretKey: SecretKey): String {
        val encryptedBytes = Base64.decode(encryptedData, Base64.DEFAULT)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, secretKey, IvParameterSpec(ByteArray(16)))
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }
    //fun decryptAsset(encryptedData: String, secretKey: SecretKey): Asset { return Gson().fromJson(decrypt(encryptedData, secretKey), Asset::class.java) }
    fun decryptAsset(encryptedData: String, secretKey: SecretKey): Asset { return Asset.deserialize(decrypt(encryptedData, secretKey)) }
    fun decryptTransaction(encryptedData: String, secretKey: SecretKey): Transaction { return Transaction.deserialize(decrypt(encryptedData, secretKey))  }
    fun decryptTransactionDetail(encryptedData: String, secretKey: SecretKey): TransactionDetail { return TransactionDetail.deserialize(decrypt(encryptedData, secretKey)) }
    fun decryptInvitation(encryptedData: String, secretKey: SecretKey): Invitation {return Invitation.deserialize(decrypt(encryptedData, secretKey))}

    fun generateEncryptionKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance("AES")
        keyGenerator.init(256)
        val key = keyGenerator.generateKey()
        return key
    }

    fun generateEncryptionKey(seed: String, salt: ByteArray): SecretKeySpec {
        val spec = PBEKeySpec(seed.toCharArray(), salt, 65536, 256)
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val keyBytes = factory.generateSecret(spec).encoded
        return SecretKeySpec(keyBytes, "AES")
    }

    fun convertSecretKeyToString(secretKey: SecretKey): String {
        return Base64.encodeToString(secretKey.encoded, Base64.DEFAULT)
    }

    fun convertStringToSecretKey(stringWithSecretKey: String?): SecretKey {
        val keyBytes = Base64.decode(stringWithSecretKey, Base64.DEFAULT)
        return SecretKeySpec(keyBytes, 0, keyBytes.size, "AES")
    }
}