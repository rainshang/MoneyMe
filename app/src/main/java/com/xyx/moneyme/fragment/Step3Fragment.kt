package com.xyx.moneyme.fragment


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.xyx.moneyme.R

class Step3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        println("=============>$arguments")
        return inflater.inflate(R.layout.fragment_step3, container, false)
    }


}
