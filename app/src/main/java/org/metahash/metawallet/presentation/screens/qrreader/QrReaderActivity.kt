package org.metahash.metawallet.presentation.screens.qrreader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.google.zxing.Result
import me.dm7.barcodescanner.zxing.ZXingScannerView
import org.metahash.metawallet.presentation.base.BaseActivity

class QrReaderActivity : BaseActivity(), ZXingScannerView.ResultHandler {

    private lateinit var mScannerView: ZXingScannerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mScannerView = ZXingScannerView(this)
        setContentView(mScannerView)
    }

    override fun onResume() {
        super.onResume()
        mScannerView.setResultHandler(this)
        mScannerView.startCamera()
    }

    override fun onPause() {
        super.onPause()
        mScannerView.stopCamera()
    }

    override fun handleResult(result: Result?) {
        deliverResult(result?.text ?: "")
    }

    override fun onBackPressed() {
        deliverResult()
    }

    private fun deliverResult(result: String = "") {
        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra("data", result)
        })
        finish()
    }
}