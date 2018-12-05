package org.metahash.metawallet.extensions

import org.metahash.metawallet.WalletApplication
import org.web3j.crypto.*
import java.io.File
import java.math.BigInteger

class EthereumExt {

    companion object {

        private const val WALLET_FOLDER = "metahashwallets"

        fun createETHWallet(password: String): String {
            return try {
                WalletUtils.generateFullNewWalletFile(password, getEtherWalletDirectory())
            } catch (ex: Exception) {
                ""
            }
        }

        fun getWalletAddress(password: String, filename: String): String {
            return try {
                getWalletCredentials(password, filename).address
            } catch (ex: Exception) {
                ""
            }
        }

        private fun getEtherWalletDirectory(): File {
            val directoryPath = "${WalletApplication.appContext.filesDir}${File.separator}$WALLET_FOLDER"
            val file = File(directoryPath)
            if (file.exists().not()) {
                file.mkdir()
            }
            return file
        }

        fun createTransaction(password: String, filename: String,
                              nonce: BigInteger, gasPrice: BigInteger,
                              gasLimit: BigInteger, to: String,
                              amount: BigInteger) {
            try {
                val credentials = getWalletCredentials(password, filename)
                val transactionRaw = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, to, amount, "")
                val transactionHash = TransactionUtils.generateTransactionHash(transactionRaw, credentials)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        }

        private fun getWalletFile(filename: String): File {
            val directory = getEtherWalletDirectory()
            return File("${directory.path}${File.separator}$filename")
        }

        @Throws(Exception::class)
        private fun getWalletCredentials(password: String, filename: String): Credentials {
            val file = getWalletFile(filename)
            return WalletUtils.loadCredentials(password, file)
        }
    }
}