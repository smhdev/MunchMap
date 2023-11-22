package com.example.restaurantreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restaurantreviewapp.R
import com.example.restaurantreviewapp.data.Review
import kotlinx.android.synthetic.main.profile_recycler_view.view.*

/**
 * Adapter class for the recycler view found on the profile page.
 * Adapts information from objects constructed from Firestore to be displayed in a ViewHolder
 * in the profile page recycler view.
 *
 * @author Sam Harry 1901522
 * @param reviews takes a list of Review objects passed to the constructor to be adapted
 * @return a RecyclerView adapter for the objects passed in adapted for the RecyclerView
 */
class ProfileReviewAdapter(private val reviews: MutableList<Review>) :
    RecyclerView.Adapter<ProfileReviewAdapter.ProfileReviewViewHolder>() {

    //Listener on each element of the recycler view
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {

        /**
         * Passes a position, representing the position in the recycler view of a view holder,
         * so that the review selected can be identified.
         * @param position the position to set the on click
         */
        fun onItemClick(position: Int)

    }

    /**
     * associate the onclick listener with the listener declared.
     * @param listener an onItemClickListener for the adapter
     */
    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }

    /**
     * Inner class representing the individual view holder elements
     * @param itemView a View of the item in the adapter
     * @param listener an onItemClickListener that is set on each element of the recycler view
     * @return the RecyclerView view holder associated with an individual review
     */
    inner class ProfileReviewViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {

        //initialise an onItemClick listener for the item view.
        init {
            itemView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    /**
     * Executed at the onCreate stage for the view holder. Inflates the layout of a recycler view
     * entry and passes a ProfileReviewViewHolder back.
     * @param parent the RecyclerView layout associated with the view holder
     * @return a ProfileReviewView holder of the layout and listener.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileReviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.profile_recycler_view, parent, false)

        return ProfileReviewViewHolder(v, mListener)
    }

    /**
     * Binds the object attribute values to the view holders. This is done for each holder.
     * @param holder the holder being bound
     * @param position the position in the RecyclerView being bound
     */
    override fun onBindViewHolder(holder: ProfileReviewViewHolder, position: Int) {
        //set the current review to the position passed in the list of reviews
        val currReview = reviews[position]

        //apply for the view in the holder
        holder.itemView.apply {

            //using Glide to load an image stored in the object to the ImageView
            Glide.with(holder.itemView).load(currReview.reviewImage).into(reviewPictureProfile)

            //If the image is empty, do not allocated any screen real estate to this holder.
            if (currReview.reviewImage.isNullOrEmpty()) {
                this.reviewPictureProfile.layoutParams =
                    (LinearLayout.LayoutParams(this.width, 0))
            }

            //set the rating bar value and the text of the review
            userReviewRatingBarProfile.rating = currReview.rating?.toFloat()!!
            userReviewTextProfile.text = currReview.review
            restaurantNameProfile.text = currReview.restaurantName

        }
    }

    /**
     * Get the item count of the reviews list
     * @return the number of reviews in the list
     */
    override fun getItemCount(): Int {
        return reviews.size
    }


}