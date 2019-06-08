package com.xyx.moneyme.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController

import com.xyx.moneyme.R
import kotlinx.android.synthetic.main.fragment_step1.*

class Step1Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step1, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        money_seek.apply {
            customTickTexts(arrayOf("\$${min.toInt()}", "\$${max.toInt()}"))
            setIndicatorTextFormat("\$\${PROGRESS}")
        }
        month_seek.apply {
            customTickTexts(arrayOf("${min.toInt()} months", "${max.toInt()} months"))
            setIndicatorTextFormat("\${PROGRESS} months")
        }
        calculate_btn.setOnClickListener {
            val bundle = bundleOf(
                "value" to money_seek.progress,
                "month" to month_seek.progress
            )
            findNavController().navigate(R.id.action_calculate, bundle)
        }
    }

}
