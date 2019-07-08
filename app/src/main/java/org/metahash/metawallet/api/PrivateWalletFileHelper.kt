package org.metahash.metawallet.api

import android.content.Context
import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.extensions.PrivateWalletHelper
import java.io.File

class PrivateWalletFileHelper {

    companion object {

        private const val WALLET_FILE_POSTFIX = ".ec"
        private const val WALLET_FILE_EXTENSION = ".priv"

        fun isWalletHasFile(wallet: Wallet): Boolean = getWalletFile(wallet.address).exists()

        fun saveWalletToFile(wallet: Wallet) {
            val encryptedPrivateKey = PrivateWalletHelper
                .encryptWalletPrivateKey(wallet.privateKeyPKCS1, wallet.password)
            if (encryptedPrivateKey.isNotEmpty()) {
                deleteWalletFileIfExist(wallet.address)
                val fileName = createFileName(wallet.address)
                WalletApplication.appContext.openFileOutput(fileName, Context.MODE_PRIVATE).use {
                    it.write(encryptedPrivateKey.toByteArray(Charsets.UTF_8))
                }
            }
        }

        fun readWalletFile(wallet: Wallet) {
            val fileName = createFileName(wallet.address)
            val file = getWalletFile(wallet.address)
            if (file.exists()) {
                val data = file.readText(Charsets.UTF_8)
                data.length
            }
        }

        private fun deleteWalletFileIfExist(address: String) {
            val file = getWalletFile(address)
            if (file.exists()) {
                file.delete()
            }
        }

        private fun getWalletFile(address: String): File {
            return File(WalletApplication.appContext.filesDir, createFileName(address))
        }

        private fun createFileName(address: String): String {
            return "$address$WALLET_FILE_POSTFIX$WALLET_FILE_EXTENSION"
        }
    }
}