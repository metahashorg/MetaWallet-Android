package org.metahash.metawallet.extensions

import android.util.Log
import org.bitcoinj.core.Sha256Hash
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.data.models.WalletPrivateKey
import org.spongycastle.asn1.pkcs.PrivateKeyInfo
import org.spongycastle.crypto.digests.RIPEMD160Digest
import org.spongycastle.crypto.digests.SHA256Digest
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec
import java.util.*

object CryptoExt {

    private val hexArray = "0123456789ABCDEF".toCharArray()

    fun generateWallet(): Wallet? {
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
}