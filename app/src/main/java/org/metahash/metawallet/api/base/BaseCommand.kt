package org.metahash.metawallet.api.base

import io.reactivex.Observable

abstract class BaseCommand<RESULT> : BaseCommandWithMapping<RESULT, RESULT>() {

    override fun afterResponse(response: Observable<RESULT>): Observable<RESULT> = response
}