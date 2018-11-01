package org.metahash.metawallet.data.models

data class ResolvingResult(
        val proxy: Info,
        val torrent: Info
) {
    constructor() : this(Info(), Info())

    constructor(stage: Int) : this(Info(stage, Status()), Info(stage, Status()))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ResolvingResult

        if (proxy != other.proxy) return false
        if (torrent != other.torrent) return false

        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

data class Info(
        val stage: Int,
        val status: Status
) {
    constructor() : this(1, Status())

    constructor(total: Int) : this(2, Status(total))

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Info

        if (stage != other.stage) return false
        if (status != other.status) return false

        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}

data class Status(
        val total: Int,
        val cur: Int
) {
    constructor() : this(0, 0)

    constructor(all: Int) : this(all, 0)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Status

        if (total != other.total) return false
        if (cur != other.cur) return false

        return true
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}