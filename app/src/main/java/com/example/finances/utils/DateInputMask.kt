package com.example.finances.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class DateInputMask(private val input: EditText) : TextWatcher {
    private var isUpdating = false
    private val mask = "##.##.####"

    override fun beforeTextChanged(str: CharSequence?, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(str: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun afterTextChanged(str: Editable?) {
        if (isUpdating) {
            return
        }

        isUpdating = true

        val digits = str.toString().replace("[^\\d]".toRegex(), "")

        var formatted = ""
        var index = 0

        for (m in mask.toCharArray()) {
            if (m != '#') {
                if (index < digits.length) {
                    formatted += m
                }
            } else {
                if (index < digits.length) {
                    formatted += digits[index]
                    index++
                }
            }
        }

        input.setText(formatted)
        input.setSelection(formatted.length)

        isUpdating = false
    }
}