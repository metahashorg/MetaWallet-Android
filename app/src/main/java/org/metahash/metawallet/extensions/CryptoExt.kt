package org.metahash.metawallet.extensions

import org.bitcoinj.core.Sha256Hash
import org.metahash.metawallet.data.models.Transaction
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.data.models.WalletPrivateKey
import org.spongycastle.asn1.pkcs.PrivateKeyInfo
import org.spongycastle.crypto.digests.RIPEMD160Digest
import org.spongycastle.crypto.digests.SHA256Digest
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.util.*

object CryptoExt {

    private val hexArray = "0123456789ABCDEF".toCharArray()

    /**
     * generate user wallet
     */
    fun createWallet(): Wallet? {
        try {
            val keyPair = generateKeyPair() ?: return null
            val privKey = convertPrivateKeyToPKCS1(keyPair.private)
            val pubKey = keyPair.public.encoded
            val address = generateHexAddress(Arrays.copyOfRange(
                    pubKey,
                    pubKey.size - 65,
                    pubKey.size)).toLowerCase()
            return Wallet(address, pubKey, privKey, WalletPrivateKey(
                    keyPair.private.algorithm,
                    keyPair.private.format,
                    keyPair.private.encoded
            ))
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    /**
     *  create a transaction
     */

    fun createTransaction(wallet: Wallet, to: String, password: String,
                          nonce: String, amount: String, fee: String, data: String): Transaction {
        try {
            //data in hex format
            val hexData = bytesToHex(data.toByteArray())
            //private key for signing
            val privKey: PrivateKey = object : PrivateKey {
                override fun getAlgorithm() = wallet.privateKey.algorithm

                override fun getEncoded() = wallet.privateKey.encoded

                override fun getFormat() = wallet.privateKey.format
            }
            val formattedFee = if (fee.isEmpty()) 0L else fee.toLong()
            //signature message in ascii byte format
            val message = generateSignatureMessage(to, amount.toLong(), nonce.toLong(), formattedFee, data)
            //signed message
            val signature = signMessage(message, privKey)
            //signature hex
            val signatureHex = bytesToHex(signature).toLowerCase()
            //public key in hex format
            val pubHex = bytesToHex(wallet.publicKey).toLowerCase()
            return Transaction(to, amount, formattedFee.toString(), nonce,
                    hexData, pubHex, signatureHex)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return Transaction()
    }

    @Throws(Exception::class)
    private fun signMessage(message: ByteArray, privKey: PrivateKey): ByteArray {
        return Signature.getInstance("SHA256withECDSA", "BC").apply {
            initSign(privKey)
            update(message)
        }.sign()
    }

    private fun generateKeyPair(): KeyPair? {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("ECDSA", "SC")
            val spec = ECGenParameterSpec("secp256k1")
            keyPairGenerator.initialize(spec, SecureRandom())
            return keyPairGenerator.generateKeyPair()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return null
    }

    private fun convertPrivateKeyToPKCS1(privateKey: PrivateKey): ByteArray {
        try {
            val privBytes = privateKey.getEncoded()
            val pkInfo = PrivateKeyInfo.getInstance(privBytes)
            val encodable = pkInfo.parsePrivateKey()
            val primitive = encodable.toASN1Primitive()
            return primitive.getEncoded()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return byteArrayOf(1)
    }

    private fun generateHexAddress(pbK65: ByteArray): String {
        val shaRnd = sha256hash160(pbK65)
        val mainAddressBytes = ByteArray(shaRnd.size + 1)
        System.arraycopy(shaRnd, 0, mainAddressBytes, 1, shaRnd.size)
        mainAddressBytes[0] = 0x00

        val mainAddressString = bytesToHex(mainAddressBytes)
        val sha256sha256 = bytesToHex(Sha256Hash.hash(Sha256Hash.hash(mainAddressBytes)))
        return "0x" + mainAddressString + sha256sha256.substring(0, 8)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = CharArray(bytes.size * 2)
        for (j in bytes.indices) {
            val v: Int = bytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v.ushr(4)]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    private fun sha256hash160(pubKey: ByteArray) = getRIPEMD160(getSHA256(pubKey))

    private fun getSHA256(input: ByteArray): ByteArray {
        val out = ByteArray(32)
        val sha256Digest = SHA256Digest()
        sha256Digest.update(input, 0, input.size)
        sha256Digest.doFinal(out, 0)
        return out
    }

    private fun getRIPEMD160(input: ByteArray): ByteArray {
        val out = ByteArray(20);
        val digest = RIPEMD160Digest()
        digest.update(input, 0, input.size)
        digest.doFinal(out, 0)
        return out
    }

    private fun generateSignatureMessage(to: String, value: Long, nonce: Long,
                                         fee: Long, data: String): ByteArray {
        var result = ""
        result += if (to.startsWith("0x")) {
            to.replace("0x", "")
        } else {
            to
        }
        result += convertValue(value).toLowerCase()
        result += convertValue(fee).toLowerCase()
        result += convertValue(nonce).toLowerCase()
        result += convertValue(0).toLowerCase()

        return result.toUpperCase().hexStringToByteArray()
    }

    private fun convertValue(value: Long): String {
        return when {
            value < 250 -> String.format("%02X", value)
            value < 65536 -> String.format("%02X", 250) + intToLittleEndian(value.toInt()).substring(0, 4)
            value < 4294967296 -> String.format("%02X", 251) + longToLittleEndian(value).substring(0, 8)
            else -> String.format("%02X", 252) + longToLittleEndian(value)
        }
    }

    private fun longToLittleEndian(value: Long): String {
        return bytesToHex(ByteBuffer
                .allocate(8)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(value)
                .array())
    }

    private fun intToLittleEndian(value: Int): String {
        return bytesToHex(ByteBuffer
                .allocate(4)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putInt(value)
                .array())
    }
}

private val HEX_CHARS = "0123456789ABCDEF"

fun String.hexStringToByteArray(): ByteArray {

    val result = ByteArray(length / 2)

    for (i in 0 until length step 2) {
        val firstIndex = HEX_CHARS.indexOf(this[i]);
        val secondIndex = HEX_CHARS.indexOf(this[i + 1]);

        val octet = firstIndex.shl(4).or(secondIndex)
        result.set(i.shr(1), octet.toByte())
    }

    return result
}