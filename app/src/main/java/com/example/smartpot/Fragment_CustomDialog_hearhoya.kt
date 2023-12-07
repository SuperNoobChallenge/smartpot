package com.example.smartpot

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.DialogFragment

class Fragment_CustomDialog_hearhoya : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_custom_layout, container, false)
        val dialogButton = view.findViewById<Button>(R.id.page_end)

        dialogButton.setOnClickListener {
            dismiss()
        }

        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Create and return a custom-styled dialog
        return super.onCreateDialog(savedInstanceState).apply {
            setStyle(STYLE_NO_FRAME, R.style.RoundedDialog)
        }
    }
}
