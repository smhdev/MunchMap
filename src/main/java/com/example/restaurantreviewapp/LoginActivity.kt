package com.example.restaurantreviewapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.editTextTextEmailAddress
import kotlinx.android.synthetic.main.activity_login.editTextTextPassword

/**
 * A class for the login activity of the app
 *
 * @author Sam Harry 1901522
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        //set the on click listener for the register text prompt on the log in screen
        logInActivityRegisterButton.setOnClickListener {
            //open the register activity
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        //set the on click listener for the log in button on the log in screen
        logInActivityButton.setOnClickListener {
            when {
                //if the email address is found to be empty, prompt the user to enter text
                TextUtils.isEmpty(editTextTextEmailAddress.text.toString().trim { it <= ' ' }) -> {
                    Snackbar.make(
                        it, R.string.no_email,
                        BaseTransientBottomBar.LENGTH_LONG
                    )
                        .show()
                    return@setOnClickListener
                }
                //if the pass word is found to be empty, prompt the user to enter a password
                TextUtils.isEmpty(editTextTextPassword.text.toString().trim { it <= ' ' }) -> {
                    Snackbar.make(it, R.string.no_password, BaseTransientBottomBar.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }

                //otherwise, log the user in
                else -> {
                    //trim the input appropriately
                    val email: String = editTextTextEmailAddress.text.toString().trim { it <= ' ' }
                    val password: String = editTextTextPassword.text.toString().trim { it <= ' ' }

                    //sign the current user in with credentials
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    Snackbar.make(
                                        it,
                                        R.string.log_in_success,
                                        BaseTransientBottomBar.LENGTH_LONG
                                    ).show()
                                    //open the home activity, and send userid across
                                    val intent =
                                        Intent(this, HomeActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra(
                                        "user_id",
                                        FirebaseAuth.getInstance().currentUser!!.uid
                                    )
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Snackbar.make(
                                        it,
                                        task.exception!!.message.toString(),
                                        BaseTransientBottomBar.LENGTH_LONG
                                    ).show()
                                }
                            })
                }
            }
        }
    }
}