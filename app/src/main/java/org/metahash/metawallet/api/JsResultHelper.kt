package org.metahash.metawallet.api

import org.metahash.metawallet.WalletApplication
import org.metahash.metawallet.data.models.NodeInfo
import org.metahash.metawallet.data.models.NodeItem

object JsResultHelper {

    fun importPrivateWalletResult(
        address: String,
        status: String
    ): String {
        return WalletApplication.gson.toJson(ImportPrivateWalletResult(address, status))
    }

    fun getPrivateKeyDecryptedResult(
        key: String,
        status: String
    ): String {
        return WalletApplication.gson.toJson(PrivateKeyDecryptedResult(key, status))
    }

    fun nodesListResult(
        nodes: List<NodeItem>,
        status: String
    ): String {
        return WalletApplication.gson.toJson(NodeListResult(nodes, status))
    }

    fun nodesInfoResult(
        node: NodeInfo?,
        status: String
    ): String {
        return WalletApplication.gson.toJson(NodeInfoResult(node, status))
    }
}

data class ImportPrivateWalletResult(
    private val address: String,
    private val status: String
)

data class PrivateKeyDecryptedResult(
    private val key: String,
    private val status: String
)

data class NodeListResult(
    private val data: List<NodeItem>,
    private val status: String
)

data class NodeInfoResult(
    private val data: NodeInfo?,
    private val status: String
)