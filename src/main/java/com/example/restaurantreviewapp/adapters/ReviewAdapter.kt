package com.example.restaurantreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restaurantreviewapp.R
import com.example.restaurantreviewapp.data.Review
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.review_recycler_view.view.*

/**
 * Adapter class for the recycler view found on the review page for each restaurant.
 * Adapts information from objects constructed from Firestore to be displayed in a ViewHolder
 * in the profile page recycler view.
 *
 * @author Sam Harry 1901522
 * @param reviews takes a list of Review objects passed to the constructor to be adapted
 * @return a RecyclerView adapter for the objects passed in adapted for the RecyclerView
 */
class ReviewAdapter(private val reviews: MutableList<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder>() {

    /**
     * Inner class representing the individual view holder elements
     * @param itemView a View of the item in the adapter
     * @return the RecyclerView view holder associated with an individual review
     */
    inner class ReviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    }

    /**
     * Executed at the onCreate stage for the view holder. Inflates the layout of a recycler view
     * entry and passes a ReviewViewHolder back.
     * @param parent the RecyclerView layout associated with the view holder
     * @return a ReviewView holder of the layout and listener.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.review_recycler_view, parent, false)
        return ReviewViewHolder(v)
    }

    /**
     * Binds the object attribute values to the view holders. This is done for each holder.
     * @param holder the holder being bound
     * @param position the position in the RecyclerView being bound
     */
    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        val currReview = reviews[position]

        holder.itemView.apply {

            //Using glide to load images into both the profile image and the review image for
            //each review.
            Glide.with(holder.itemView).load(currReview.profileImage).into(userProfilePicture)
            Glide.with(holder.itemView).load(currReview.reviewImage).into(reviewPicture)

            //If the current review does not have an image, do not allocate any screen real estate
            if (currReview.reviewImage.isNullOrEmpty()) {
                this.findViewById<ImageView>(R.id.reviewPicture).layoutParams =
                    (LinearLayout.LayoutParams(this.width, 0))
            }

            //Setting the user name text, user review rating and the user review text
            userNameText.text = currReview.name
            userReviewRatingBar.rating = currReview.rating?.toFloat()!!
            userReviewText.text = currReview.review

            //if no user is signed in, disable the like button for the review
            if (FirebaseAuth.getInstance().currentUser == null) {
                likeReviewButtonToggle.isEnabled = false
                return
            }

            //if the like button has been pressed, the next press will dislike and vice versa
            val likeReviewButtonToggle = likeReviewButtonToggle
            likeReviewButtonToggle.setOnCheckedChangeListener { compoundButton, b ->

                if (b) {
                    !likeReviewButtonToggle.isChecked
                    // editor.putBoolean(position.toString(), false)

                } else {
                    likeReviewButtonToggle.isChecked
                    //editor.putBoolean(position.toString(), true)
                }
                //access the database and update the likes of the review
                //TODO - implement properly.
                val db = FirebaseFirestore.getInstance()
                db.collection("users").document(currReview.user.toString())
                    .update("sandwich points", FieldValue.increment(1))
            }
        }
    }


    /**
     * Gets the number of items in the review list
     * @return the number of reviews.
     */
    override fun getItemCount(): Int {
        return reviews.size
    }
}