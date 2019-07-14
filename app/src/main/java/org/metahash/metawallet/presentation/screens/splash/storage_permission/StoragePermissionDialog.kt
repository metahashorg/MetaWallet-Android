package org.metahash.metawallet.presentation.screens.splash.storage_permission

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import org.metahash.metawallet.R

class StoragePermissionDialog : DialogFragment() {

    companion object {
        const val TAG = "StoragePermissionDialog"
    }

    var onAllowClickListener = {}

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        isCancelable = false
        return AlertDialog.Builder(requireActivity())
            .setTitle(R.string.alert_permissions_title)
            .setMessage(R.string.alert_permissions_message)
            .setPositiveButton(R.string.alert_permissions_ok) { _, _ ->
                onAllowClickListener()
            }
            .setNegativeButton(R.string.alert_permissions_cancel, null)
            .create()
    }
}