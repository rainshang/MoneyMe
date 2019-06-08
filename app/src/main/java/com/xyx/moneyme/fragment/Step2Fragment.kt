package com.xyx.moneyme.fragment


import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.navigation.fragment.findNavController
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.xyx.moneyme.DBKeyHelper
import com.xyx.moneyme.R
import kotlinx.android.synthetic.main.fragment_step2.*

const val RC_SIGN_IN = 7

class Step2Fragment : DialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dialog_section1_btn.setOnClickListener {
            gotoStep3()
        }
        dialog_section2_btn.setOnClickListener {
            startActivityForResult(
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .build(),
                RC_SIGN_IN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                FirebaseAuth.getInstance().currentUser
                    ?.run {
                        FirebaseFirestore.getInstance()
                            .collection(DBKeyHelper.DB_NAME)
                            .document(uid)
                            .set(
                                mapOf(
                                    DBKeyHelper.KEY_EMAIL to email
                                ),
                                SetOptions.merge()
                            )
                        gotoStep3()
                    }
            } else {
                response?.run {
                    view?.let { Snackbar.make(it, R.string.tip_fail, Snackbar.LENGTH_LONG).show() }
                }
            }
        }
    }

    private fun gotoStep3() {
        findNavController().navigate(R.id.action_start_apply, arguments)
    }
}
