package org.metahash.metawallet.data.models

data class GetNodeInfoResponse(
    val data: NodeInfo
) : BaseResponse()

data class NodeInfo(
    val address: String,
    val host: String,
    val name: String,
    val type: Array<String>,
    val bench: Bench,
    val bench_cnt: String,
    val trust: String,
    val online_stat: String,
    val balance: Balance,
    val status: String,
    val version: String,
    val rate: String,
    val roi: String,
    val max_bench_cnt: String,
    val fake: Boolean,
    val online: Int,
    val avgRps: Int
)

data class Bench(
    val ip: String,
    val qps: String,
    val rps: String,
    val closed: String,
    val timeouts: String,
    val geo: String,
    val success: String,
    val version: String,
    val type: String
)

data class Balance(
    val delegate: String,
    val delegate_tec: String,
    val delegate_self: String,
    val delegated: String,
    val balance: String
)