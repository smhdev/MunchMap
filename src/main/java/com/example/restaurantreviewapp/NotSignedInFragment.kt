package com.example.restaurantreviewapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_not_signed_in.*


/**
 * A class of the NotSignedIn Fragment of the app, displayed when there is no current user
 *
 * @author Sam Harry 1901522
 */
class NotSignedInFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_not_signed_in, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //start the log in fragment
        logInButton.setOnClickListener() {
            val intent = Intent(this.activity, LoginActivity::class.java)
            startActivity(intent)
        }

        //start the register fragment
        registerButton.setOnClickListener() {
            val intent = Intent(this.activity, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}