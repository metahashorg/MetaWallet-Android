package org.metahash.metawallet.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebView

class TouchWebView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : WebView(context, attrs, defStyle) {

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        when (ev?.action) {
            MotionEvent.ACTION_DOWN -> Log.d("MIINE", "down")
            MotionEvent.ACTION_UP -> Log.d("MIINE", "up")
        }
        return super.onInterceptTouchEvent(ev)
    }
}