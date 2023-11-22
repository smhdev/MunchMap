package com.example.restaurantreviewapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

/**
 * A class of the home activity of the application. This holds the bottom navigation view functionality
 * and its navigation between the different fragments
 *
 * @author Sam Harry 1901522
 */
class HomeActivity : AppCompatActivity() {


    private val homeFragment = HomeFragment()
    private val mapFragment = MapsFragment()
    private val profileFragment = ProfileFragment()
    private val notSignedInFragment = NotSignedInFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //on startup set the layout to the home activity.
        setContentView(R.layout.activity_home)
        //set the app to start on the home fragment.
        replaceFragment(homeFragment)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)

        //when each item of the view is clicked, display the corresponding fragment
        bottomNavigationView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.homeFragment -> replaceFragment(homeFragment)
                R.id.mapFragment -> replaceFragment(mapFragment)
                R.id.profileFragment ->

                    //if the user is not signed in, the profile fragment displays a different page
                    if (FirebaseAuth.getInstance().currentUser == null) {
                        replaceFragment(notSignedInFragment)
                    } else {
                        replaceFragment(profileFragment)
                    }
            }
            true
        }
    }

    /**
     * Replaces the current fragment with a new fragment.
     * @param fragment the fragment to replace the current fragment with.
     */
    private fun replaceFragment(fragment: Fragment) {
        if (fragment != null) {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.nav_fragment, fragment)
            transaction.commit()
        }
    }
}