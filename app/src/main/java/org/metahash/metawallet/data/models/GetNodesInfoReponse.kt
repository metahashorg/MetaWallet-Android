package org.metahash.metawallet.data.models

data class GetNodeInfoResponse(
    val data: NodeInfo
) : BaseResponse()

data class NodeInfo(
    val address: String,
    val host: String,
    val name: String,
    val bench: String,
    val bench_cnt: String,
    val trust: String,
    val online_stat: String,
    val balance: String,
    val status: String,
    val version: String,
    val rate: String,
    val roi: String,
    val max_bench_cnt: String,
    val fake: Boolean,
    val online: Int,
    val avgRps: Int
)