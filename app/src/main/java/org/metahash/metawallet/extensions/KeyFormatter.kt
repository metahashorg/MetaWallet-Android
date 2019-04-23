package org.metahash.metawallet.extensions

object KeyFormatter {

    private const val SECP256k1 = "30740201010"
    private const val SECP256r1 = "30770201010"
    private const val AES_TAG = "BEGIN EC PRIVATE KEY"

    fun isKeyFormat(src: String): Boolean {
        return src.startsWith(SECP256k1) ||
                src.startsWith(SECP256r1)
    }

    fun isEncryptedFormat(src: String): Boolean {
        return src.contains(AES_TAG)
    }

    fun formatKey(key: String): String {
        val newKey = key
                .replace("http://", "")
                .replace("https://", "")
        return if (newKey.startsWith("0x")) {
            newKey.replaceFirst("0x", "")
        } else {
            newKey
        }
    }

    fun isSECP256k1(key: String) = key.startsWith(SECP256k1)
}