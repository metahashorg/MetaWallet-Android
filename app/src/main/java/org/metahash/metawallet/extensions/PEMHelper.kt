package org.metahash.metawallet.extensions

import org.spongycastle.util.io.pem.*
import java.io.StringReader
import java.io.StringWriter
import java.lang.Exception

object PEMHelper {

    private const val PEM_NAME = "PRIVATE KEY"
    private val TYPE_HEADER = PemHeader("Proc-Type", "4,ENCRYPTED")
    private const val DEK_HEADER_NAME = "DEK-Info"
    private const val DEK_HEADER_CONST_VALUE = "AES-128-CBC,"

    fun createPEMString(
            encryptedKey: String,
            ivBytes: ByteArray
    ): String {
        var result = ""
        try {
            val writer = StringWriter()
            val wr = PemWriter(writer)
            val pemGenerator: PemObjectGenerator =
                    PemObject(
                            PEM_NAME,
                            listOf(
                                    TYPE_HEADER,
                                    PemHeader(
                                            DEK_HEADER_NAME,
                                            DEK_HEADER_CONST_VALUE + ivBytesToHex(ivBytes)
                                    )
                            ),
                            encryptedKey.toByteArray(Charsets.UTF_8)
                    )
            wr.writeObject(pemGenerator)
            wr.close()
            writer.close()

            result = writer.toString()
        } catch (ex: Exception) {
            ex.printStackTrace()
        }
        return result
    }

    fun parsePEMString(data: String): PEMInfo? {
        var pemInfo: PEMInfo? = null
        StringReader(data).use { stringReader ->
            PemReader(stringReader).use { pemReader ->
                val pemObject = pemReader.readPemObject()
                val dekHeader: PemHeader? =
                        pemObject.headers.firstOrNull {
                            (it as? PemHeader)?.name == DEK_HEADER_NAME
                        } as? PemHeader
                pemInfo = PEMInfo(pemObject.content, hexToIVBytes(dekHeader?.value ?: ""))
            }
        }
        return pemInfo
    }

    private fun hexToIVBytes(data: String): ByteArray =
            data
                    .replace(DEK_HEADER_CONST_VALUE, "")
                    .toUpperCase()
                    .hexStringToByteArray()

    private fun ivBytesToHex(bytes: ByteArray): String = CryptoExt.bytesToHex(bytes)

    data class PEMInfo(
            val data: ByteArray,
            val ivParams: ByteArray
    )
}