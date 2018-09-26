package org.metahash.metawallet.api.mappers

abstract class BaseMapper<FROM, TO> {

    abstract fun fromEntity(from: FROM): TO
}