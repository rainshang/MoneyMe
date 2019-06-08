package com.xyx.moneyme.fragment


import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.xyx.moneyme.DBKeyHelper
import com.xyx.moneyme.R
import kotlinx.android.synthetic.main.fragment_step3.*

const val TAG = "Firebase"

class Step3Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_step3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        step3_ur_info_ed.setOnClickListener {
            if (it.tag != null) {
                it.tag as Boolean
            } else {
                false
            }
                .let { isEditing ->
                    it.tag = !isEditing
                    step3_name_ev.isEnabled = !isEditing
                    step3_mobile_ev.isEnabled = !isEditing

                    // only allow new user to modify email, cause it's not registered yet
                    if (step3_pwd_ev.visibility == View.VISIBLE) {
                        step3_email_ev.isEnabled = !isEditing
                        step3_pwd_ev.isEnabled = !isEditing
                    }

                    step3_ur_info_ed.setText(
                        if (isEditing) {
                            R.string.edit
                        } else {
                            step3_name_ev.requestFocus()
                            R.string.save
                        }
                    )
                }
        }
        FirebaseAuth.getInstance()
            .currentUser
            ?.run {
                step3_email_ev.setText(email)
                FirebaseFirestore.getInstance()
                    .collection(DBKeyHelper.DB_NAME)
                    .document(uid)
                    .get()
                    .addOnSuccessListener {
                        step3_name_ev.setText(it[DBKeyHelper.KEY_UNAME] as? String)
                        step3_mobile_ev.setText(it[DBKeyHelper.KEY_PHONE] as? String)
                    }

                step3_pwd.visibility = View.GONE
                step3_pwd_ev.visibility = View.GONE
            }
        step3_fin_detail_ed.setOnClickListener {
            findNavController().navigate(R.id.action_back2step1)
        }
        arguments?.run {
            val amount = getInt(getString(R.string.nav_arg_key_amount))
            val month = getInt(getString(R.string.nav_arg_key_month))
            step3_amount_txt.text = getString(R.string.fin_amount_format, amount, month)
            step3_repay_txt.text = getString(R.string.fin_repay_format, calculatePMT(amount, month))
        }
        step3_apply_btn.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                FirebaseFirestore.getInstance()
                    .collection(DBKeyHelper.DB_NAME)
                    .document(user.uid)
                    .set(
                        mapOf(
                            DBKeyHelper.KEY_UNAME to step3_name_ev.text.trim().toString(),
                            DBKeyHelper.KEY_PHONE to step3_mobile_ev.text.trim().toString()
                        ),
                        SetOptions.merge()
                    )
                    .addOnCompleteListener {
                        if (it.isSuccessful) {
                            onSuccess()
                        } else {
                            onFail()
                            Log.e(TAG, it.exception.toString())
                        }
                    }
            } else {
                val email = step3_email_ev.text.trim()
                val pwd = step3_pwd_ev.text.trim()
                if (!TextUtils.isEmpty(email) &&
                    Patterns.EMAIL_ADDRESS.matcher(email).matches()
                    && !TextUtils.isEmpty(pwd)
                ) {
                    FirebaseAuth.getInstance()
                        .createUserWithEmailAndPassword(email.toString(), pwd.toString())
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                onSuccess()
                                FirebaseFirestore.getInstance()
                                    .collection(DBKeyHelper.DB_NAME)
                                    .document(it.result!!.user.uid)
                                    .set(
                                        mapOf(
                                            DBKeyHelper.KEY_EMAIL to email.toString(),
                                            DBKeyHelper.KEY_UNAME to step3_name_ev.text.trim().toString(),
                                            DBKeyHelper.KEY_PHONE to step3_mobile_ev.text.trim().toString()
                                        )
                                    )
                            } else {
                                onFail()
                                Log.e(TAG, it.exception.toString())
                            }
                        }
                } else {
                    AlertDialog.Builder(context!!)
                        .setTitle(android.R.string.dialog_alert_title)
                        .setMessage(getString(R.string.tip_invaild_email_pwd_4register))
                        .setPositiveButton(android.R.string.ok) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .show()
                }
            }
        }
    }

    private fun onFail() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.app_name)
            .setMessage(R.string.tip_fail)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun onSuccess() {
        AlertDialog.Builder(context!!)
            .setTitle(R.string.app_name)
            .setMessage(R.string.tip_success)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                dialog.dismiss()
                findNavController().navigateUp()
            }
            .show()
    }

    /*
    modified from

    https://apache.googlesource.com/poi/+/4d81d34d5d566cb22f21999e653a5829cc678ed5/src/java/org/apache/poi/ss/formula/functions/Finance.java

     I'm totally fucked with the finance terms...........
     */
    private fun calculatePMT(amount: Int, month: Int): Double {
        val r = 0.007 //- periodic interest rate represented as a decimal.
        val nper = amount / month * 1.0 //- number of total payments / periods.
        val pv = amount //- present value -- borrowed or invested principal.
        val fv = 0 //- future value of loan or annuity.
        val type = 0 //- when payment is made: beginning of period is 1; end, 0.
        return r * (pv * Math.pow(1 + r, nper) + fv) / ((1 + r * type) * (Math.pow(1 + r, nper) - 1))
    }

}
