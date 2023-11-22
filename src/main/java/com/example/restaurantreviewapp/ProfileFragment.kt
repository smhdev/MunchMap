package com.example.restaurantreviewapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restaurantreviewapp.adapters.ProfileReviewAdapter
import com.example.restaurantreviewapp.data.Review
import kotlinx.android.synthetic.main.fragment_review.view.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.android.synthetic.main.profile_recycler_view.view.*
import kotlinx.android.synthetic.main.review_recycler_view.*

/**
 * A class of the Profile Fragment of the app, visible when a user is signed in
 *
 * @author Sam Harry 1901522
 */
class ProfileFragment : Fragment() {

    private var adapter: RecyclerView.Adapter<ProfileReviewAdapter.ProfileReviewViewHolder>? = null
    private var reviewList = ArrayList<Review>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment and populate the review list
        populateReviewList()
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            //stop the recycler view from being emptied
            profileReviewRecycler.recycledViewPool.setMaxRecycledViews(0, 0)
            profileReviewRecycler.layoutManager = LinearLayoutManager(activity)
            //set the adapter to the review list
            adapter = ProfileReviewAdapter(reviewList)
            profileReviewRecycler.adapter = adapter
            adapter?.notifyDataSetChanged()

            //set the on click listener on each row of the recycler view
            (adapter as ProfileReviewAdapter).setOnItemClickListener(object :
                ProfileReviewAdapter.onItemClickListener {
                override fun onItemClick(position: Int) {

                    //send the id of review to the edit review fragment
                    val bundle = Bundle()
                    //REF_KEY is the name of the bundle for the id of the review
                    bundle.putString("REF_KEY", reviewList.get(position).id)
                    //send the user to the edit review fragment
                    val fragment = EditReviewFragment()
                    fragment.arguments = bundle
                    val manager = parentFragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.nav_fragment, fragment)
                    //allow the user to user android back button to navigate
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            })
        }

        //get the users collection, and the document associated with the current user
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid!!).get()
            .addOnSuccessListener {
                //populate layout containers with the users information
                Glide.with(this@ProfileFragment).load(it?.get("image")).into(userProfileImage)
                usernameText.text = it?.get("name").toString()
                sandwichPointsText.text = it?.get("sandwich points").toString()
                bioText.text = it?.get("bio").toString()
            }

        //listener for the logout button
        logoutButton.setOnClickListener {
            //sign the user out of the firebase authentication
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(this.activity, HomeActivity::class.java)
            intent.flags =
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        //listener for the profile edit button
        editProfileButton.setOnClickListener {
            //start the edit profile fragment
            val fragment = EditProfileFragment()
            val manager = parentFragmentManager
            val transaction = manager.beginTransaction()
            transaction.replace(R.id.nav_fragment, fragment)
            //allow the user to navigate backwards using the android back button
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    /**
     * Populate the review list of the profile recycler view.
     */
    private fun populateReviewList() {
        //clear the list to avoid duplicates in the recycler view
        reviewList.clear()
        val db = FirebaseFirestore.getInstance()

        val reviews = db.collection("reviews")

        //get all the users where the user id is the current users id
        reviews.whereEqualTo("user", FirebaseAuth.getInstance().currentUser?.uid).get()
            .addOnSuccessListener { task ->

                //create review objects from all the documents found
                for (dc: DocumentSnapshot in task.documents) {
                    val review = dc.toObject(Review::class.java)

                    review?.id = dc.id

                    if (review != null) {
                        reviewList.add(review)
                    }
                }
                //tell the adapter the data has changed
                this.adapter?.notifyDataSetChanged()
            }
    }
}

