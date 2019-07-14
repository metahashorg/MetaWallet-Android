package org.metahash.metawallet.api

import android.os.Environment
import org.metahash.metawallet.data.models.Wallet
import org.metahash.metawallet.extensions.PrivateWalletHelper
import java.io.File

class PrivateWalletFileHelper {

    companion object {

        private const val ROOT_FOLDER_NAME = "org.metahash.metawallet"
        private const val WALLETS_FOLDER_NAME = "metawallet"

        private const val WALLET_FILE_POSTFIX = ".ec"
        private const val WALLET_FILE_EXTENSION = ".priv"

        fun isWalletHasFile(wallet: Wallet): Boolean = getWalletFile(wallet).exists()

        fun saveWalletToFile(wallet: Wallet) {
            val encryptedPrivateKey = PrivateWalletHelper
                .encryptWalletPrivateKey(wallet.privateKeyPKCS1, wallet.password)
            if (encryptedPrivateKey.isNotEmpty()) {
                deleteWalletFileIfExist(wallet)
                val file = createWalletFile(wallet)
                file.writeText(
                    encryptedPrivateKey,
                    Charsets.UTF_8
                )
            }
        }

        fun readWalletFile(wallet: Wallet) {
            val file = getWalletFile(wallet)
            if (file.exists()) {
                val data = file.readText(Charsets.UTF_8)
                data.length
            }
        }

        private fun deleteWalletFileIfExist(wallet: Wallet) {
            val file = getWalletFile(wallet)
            if (file.exists()) {
                file.delete()
            }
        }

        private fun createWalletFile(wallet: Wallet): File {
            return getWalletFile(wallet).apply {
                createNewFile()
            }
        }

        private fun getWalletFile(wallet: Wallet): File {
            val userFolder = getUserFolder(wallet.userLogin)
            val walletAddress = wallet.address
            return File(userFolder, "$walletAddress$WALLET_FILE_POSTFIX$WALLET_FILE_EXTENSION")
        }

        private fun getUserFolder(userName: String): File {
            val parentFolder = getRootFolder()
            return File(parentFolder, userName).apply {
                if (!exists()) {
                    mkdir()
                }
            }
        }
        private fun getRootFolder(): File {
            val externalRoot = Environment.getExternalStorageDirectory()
            val mainFolder = File(externalRoot, ROOT_FOLDER_NAME)
            return File(mainFolder, WALLETS_FOLDER_NAME).apply {
                if (!exists()) {
                    mkdirs()
                }
            }
        }
    }
}