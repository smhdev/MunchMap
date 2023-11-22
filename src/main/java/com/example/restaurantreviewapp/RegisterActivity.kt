package com.example.restaurantreviewapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

/**
 * A class for the register activity of the app
 *
 * @author Sam Harry 1901522
 */
class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        //on click listener for the log in text on the register page
        registerActivityLogInButton.setOnClickListener {
            //send the user to the log in activity
            startActivity(Intent(this, LoginActivity::class.java))
        }

        //on click listener for the register button
        registerActivityButton.setOnClickListener {
            when {
                //if the email address field is empty prompt the user to enter text
                TextUtils.isEmpty(editTextTextEmailAddress.text.toString().trim { it <= ' ' }) -> {
                    Snackbar.make(it, R.string.no_email, LENGTH_LONG)
                        .show()
                }

                //if the password field is empty prompt the user to enter a password
                TextUtils.isEmpty(editTextTextPassword.text.toString().trim() { it <= ' ' }) -> {
                    Snackbar.make(it, R.string.no_password, LENGTH_LONG)
                        .show()
                }

                else -> {
                    //trim the email and password text
                    val email: String = editTextTextEmailAddress.text.toString().trim { it <= ' ' }
                    val password: String = editTextTextPassword.text.toString().trim { it <= ' ' }

                    //create a new user on Firebase with the new information
                    FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(
                            OnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    var firebaseUser: FirebaseUser = task.result!!.user!!

                                    val db = FirebaseFirestore.getInstance()

                                    //set the new users data in the users collection in a new document
                                    val data = hashMapOf(
                                        "name" to email,
                                        "sandwich points" to 0,
                                        "username" to email,
                                        "bio" to getString(R.string.default_bio) as String,
                                        "image" to getString(R.string.default_profile_picture) as String
                                    )

                                    //set the data
                                    db.collection("users").document(firebaseUser.uid).set(data)

                                    //send the user to the Home activity
                                    val intent =
                                        Intent(this, HomeActivity::class.java)
                                    intent.flags =
                                        Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    intent.putExtra("user_id", firebaseUser.uid)
                                    intent.putExtra("email_id", email)
                                    startActivity(intent)
                                    finish()
                                } else {
                                    Snackbar.make(
                                        it,
                                        task.exception!!.message.toString(),
                                        LENGTH_LONG
                                    )
                                        .show()
                                }
                            })
                }
            }
        }
    }
}