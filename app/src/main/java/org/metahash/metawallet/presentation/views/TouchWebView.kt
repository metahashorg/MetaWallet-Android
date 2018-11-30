package org.metahash.metawallet.presentation.views

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.webkit.WebView

class TouchWebView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyle: Int = 0)
    : WebView(context, attrs, defStyle) {

    var onActionUp = {}

    fun clearActionListener() {
        onActionUp = {}
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_UP) {
            onActionUp.invoke()
        }
        return super.onTouchEvent(event)
    }
}