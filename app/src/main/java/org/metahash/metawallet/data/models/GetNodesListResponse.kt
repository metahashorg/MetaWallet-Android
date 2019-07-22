package org.metahash.metawallet.data.models

data class GetNodesListResponse(
    val data: List<NodeItem>
) : BaseResponse()

data class NodeItem(
    val address: String,
    val name: String,
    val type: Array<String>,
    val delegated: Double,
    val geo: String,
    val status: String,
    val bench_success: Float
)