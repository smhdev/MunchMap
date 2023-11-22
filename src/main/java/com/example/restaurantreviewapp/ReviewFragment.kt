package com.example.restaurantreviewapp

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restaurantreviewapp.adapters.ReviewAdapter
import com.example.restaurantreviewapp.data.Review
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_review.*
import kotlinx.android.synthetic.main.fragment_review.view.*


/**
 * A class for the review fragment of the app. Accessed once the restaurant has been clicked on.
 *
 * @author Sam Harry 1901522
 */
class ReviewFragment : Fragment() {

    //declare the arraylist and adapter.
    private val reviewList = ArrayList<Review>()
    private var adapter: RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Inflate the layout for this fragment and populate the review list
        return inflater.inflate(R.layout.fragment_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get the restaurant associated with the restaurant id passed from the home fragment
        //under the name REF_KEY
        val db = FirebaseFirestore.getInstance()

        db.collection("restaurants").document("${requireArguments().get("REF_KEY")}").get()
            .addOnSuccessListener {

                //populate the containers with the restaurants information from firestore
                Glide.with(view).load(it?.get("picture").toString())
                    .into(restaurantReviewImage)
                restaurantReviewName.text = it?.get("name").toString()
                averageRatingBarReview.rating = it?.get("averageRating").toString().toFloat()
            }


        populateReviewList()
        view.apply {

            //stop the recycler view from unloading elements
            reviewScreenRecycler.recycledViewPool.setMaxRecycledViews(0, 0)
            reviewScreenRecycler.layoutManager = LinearLayoutManager(activity)
            //create the adapter from the review list
            adapter = ReviewAdapter(reviewList)
            reviewScreenRecycler.adapter = adapter
            adapter?.notifyDataSetChanged()
        }


        //on click button for the write review button.
        reviewButtonReview.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {

                //if no user is signed in, stop the user from writing a review.
                if (FirebaseAuth.getInstance().currentUser == null) {
                    Snackbar.make(v!!, R.string.not_signed_in_review, LENGTH_LONG)
                        .show()
                } else {
                    //send the user to the write review page
                    val bundle = Bundle()
                    //pass the id of the restaurant and the restaurant name in the bundle
                    bundle.putString("REF_KEY", requireArguments().get("REF_KEY").toString())
                    bundle.putString("name", requireArguments().get("name").toString())
                    val fragment = WriteReviewFragment()
                    fragment.arguments = bundle
                    val manager = parentFragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.nav_fragment, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            }
        })
    }

    /**
     * populates the review list of the review page
     */
    private fun populateReviewList() {
        reviewList.clear()
        val db = FirebaseFirestore.getInstance()

        val reviews = db.collection("reviews")

        //find all the reviews where the restaurant id of the review is equal to the passed in
        //restaurant id from the REF_KEY
        reviews.whereEqualTo("restaurantId", requireArguments().get("REF_KEY").toString())
            .addSnapshotListener { value, error ->

                for (dc: DocumentChange in value?.documentChanges!!) {

                    //make review objects from all the documents
                    val newReview = dc.document.toObject(Review::class.java)

                    //get the profile image of the reviewer
                    db.collection("users").document(newReview.user.toString())
                        .get().addOnCompleteListener {
                            newReview.profileImage = it.result?.get("image").toString()
                        }

                    newReview?.id = dc.document.id

                    //replaces a review if it is meant to have an image, but doesn't yet due to
                    //firestore still downloading it. The listener will report two different reviews
                    //if this is not present
                    if (replaceEmptyReview(newReview)) {

                    }

                    // if the review is not null and has a profile image, add the review
                    //a bug occurs with uploading new data and a listener responding
                    //otherwise.
                    else if (newReview != null && newReview.profileImage != "") {
                        reviewList.add(newReview)


                        adapter?.notifyDataSetChanged()

                    }
                }
            }
    }

    /**
     * Replaces an empty review of the same ID with the review with the image
     * @param reviewIn the review to potentially replace an empty review
     * @return a boolean if successfully replaced or not
     */
    private fun replaceEmptyReview(reviewIn : Review) : Boolean {

        for (review in reviewList) {
            if (review.id == reviewIn.id && review.reviewImage == "") {
                reviewList.remove(review)
                reviewList.add(reviewIn)
                return true
            }
        }
        return false
    }
}