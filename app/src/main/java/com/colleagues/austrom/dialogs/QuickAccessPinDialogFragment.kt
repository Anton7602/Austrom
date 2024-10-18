package com.colleagues.austrom.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.colleagues.austrom.AustromApplication
import com.colleagues.austrom.R
import com.colleagues.austrom.interfaces.IDialogInitiator
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class QuickAccessPinDialogFragment(private val receiver: IDialogInitiator?=null) : BottomSheetDialogFragment() {
    private lateinit var dialogHolder: CardView
    private lateinit var button1: Button
    private lateinit var button2: Button
    private lateinit var button3: Button
    private lateinit var button4: Button
    private lateinit var button5: Button
    private lateinit var button6: Button
    private lateinit var button7: Button
    private lateinit var button8: Button
    private lateinit var button9: Button
    private lateinit var button0: Button
    private lateinit var buttonRemove: ImageButton
    private lateinit var pinDot1: ImageView
    private lateinit var pinDot2: ImageView
    private lateinit var pinDot3: ImageView
    private lateinit var pinDot4: ImageView
    private lateinit var callToAction: TextView
    private lateinit var acceptButton: Button
    private lateinit var cancelButton: Button
    private var input = ""
    private var currentCode = ""
    private var mode = QuickAccessDialogMode.VALIDATE

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_quick_access_pin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        currentCode = (requireActivity().application as AustromApplication).getRememberedPin() ?: ""
        bindViews(view)
        dialogHolder.setBackgroundResource(R.drawable.sh_bottomsheet_background_colorless)
        applyMode()

        val keyboardButtonClickListener = View.OnClickListener { buttonPressed ->
            if (input.length == 4) {
                input = ""
            }
            if (input.length < 4) {
                input += when (buttonPressed.id) {
                    R.id.qapdial_button1_btn -> "1"
                    R.id.qapdial_button2_btn -> "2"
                    R.id.qapdial_button3_btn -> "3"
                    R.id.qapdial_button4_btn -> "4"
                    R.id.qapdial_button5_btn -> "5"
                    R.id.qapdial_button6_btn -> "6"
                    R.id.qapdial_button7_btn -> "7"
                    R.id.qapdial_button8_btn -> "8"
                    R.id.qapdial_button9_btn -> "9"
                    R.id.qapdial_button0_btn -> "0"
                    else -> ""
                }
            }
            if (buttonPressed.id == R.id.qapdial_buttonRemove_btn && input.isNotEmpty()) {
                input = input.substring(0, input.length - 1)
            }
            if (input.length == 4) {
                when (mode) {
                    QuickAccessDialogMode.VALIDATE -> {
                        if (input==currentCode) {
                            currentCode = ""
                            mode = QuickAccessDialogMode.SETUP
                            input = ""
                            applyMode()
                        } else {
                            callToAction.text = getString(R.string.wrong_quick_access_code)
                            input = ""
                        }
                    }
                    QuickAccessDialogMode.SETUP -> {
                        currentCode = input
                        mode = QuickAccessDialogMode.REPEAT
                        input = ""
                        applyMode()
                    }
                    QuickAccessDialogMode.REPEAT -> {
                        if (input == currentCode) {
                            (requireActivity().application as AustromApplication).setRememberedPin(input)
                            receiver?.receiveValue("****", "QuickAccessCode")
                            dismiss()
                        } else {
                            callToAction.text = getString(R.string.quick_access_code_doesn_t_match)
                            input = ""
                        }
                    }
                }
            }
            pinDot1.setColorFilter(ContextCompat.getColor(requireActivity(), if (input.isNotEmpty()) {R.color.blue} else {R.color.dark_grey}))
            pinDot2.setColorFilter(ContextCompat.getColor(requireActivity(), if (input.length>1) {R.color.blue} else {R.color.dark_grey}))
            pinDot3.setColorFilter(ContextCompat.getColor(requireActivity(), if (input.length>2) {R.color.blue} else {R.color.dark_grey}))
            pinDot4.setColorFilter(ContextCompat.getColor(requireActivity(), if (input.length>3) {R.color.blue} else {R.color.dark_grey}))
        }

        button1.setOnClickListener(keyboardButtonClickListener)
        button2.setOnClickListener(keyboardButtonClickListener)
        button3.setOnClickListener(keyboardButtonClickListener)
        button4.setOnClickListener(keyboardButtonClickListener)
        button5.setOnClickListener(keyboardButtonClickListener)
        button6.setOnClickListener(keyboardButtonClickListener)
        button7.setOnClickListener(keyboardButtonClickListener)
        button8.setOnClickListener(keyboardButtonClickListener)
        button9.setOnClickListener(keyboardButtonClickListener)
        button0.setOnClickListener(keyboardButtonClickListener)
        buttonRemove.setOnClickListener(keyboardButtonClickListener)
    }

    private fun bindViews(view: View) {
        acceptButton = view.findViewById(R.id.qapdial_accept_btn)
        cancelButton = view.findViewById(R.id.qapdial_cancel_btn)
        button1 = view.findViewById(R.id.qapdial_button1_btn)
        button2 = view.findViewById(R.id.qapdial_button2_btn)
        button3 = view.findViewById(R.id.qapdial_button3_btn)
        button4 = view.findViewById(R.id.qapdial_button4_btn)
        button5 = view.findViewById(R.id.qapdial_button5_btn)
        button6 = view.findViewById(R.id.qapdial_button6_btn)
        button7 = view.findViewById(R.id.qapdial_button7_btn)
        button8 = view.findViewById(R.id.qapdial_button8_btn)
        button9 = view.findViewById(R.id.qapdial_button9_btn)
        button0 = view.findViewById(R.id.qapdial_button0_btn)
        buttonRemove = view.findViewById(R.id.qapdial_buttonRemove_btn)
        pinDot1 = view.findViewById(R.id.qapdial_pinFirst_img)
        pinDot2 = view.findViewById(R.id.qapdial_pinSecond_img)
        pinDot3 = view.findViewById(R.id.qapdial_pinThird_img)
        pinDot4 = view.findViewById(R.id.qapdial_pinForth_img)
        callToAction = view.findViewById(R.id.qapdial_ctaText_cta)
        dialogHolder = view.findViewById(R.id.qapdial_holder_crv)
    }

    private fun applyMode() {

        if (currentCode.isEmpty()) {
            mode = QuickAccessDialogMode.SETUP
        }
        callToAction.text = when (mode) {
            QuickAccessDialogMode.VALIDATE -> getString(R.string.enter_current_quick_access_code)
            QuickAccessDialogMode.SETUP -> getString(R.string.enter_new_quick_access_code)
            QuickAccessDialogMode.REPEAT -> getString(R.string.repeat_new_quick_access_code)
        }
    }
}

enum class QuickAccessDialogMode {
    VALIDATE, SETUP, REPEAT
}