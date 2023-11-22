package com.example.restaurantreviewapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.restaurantreviewapp.R
import com.example.restaurantreviewapp.data.Restaurant
import kotlinx.android.synthetic.main.home_recycler_view.view.*

/**
 * Class for the adapter of the recycler view for restaurants visible on the home page of the app
 *
 * @author Sam Harry 1901522
 * @param restaurants a list of restaurants passed in to the constructor to be adapted.
 * @return a RecyclerView adapter of the Restaurants adapted for the recycler view.
 */
class RestaurantAdapter(private val restaurants: MutableList<Restaurant>) :
    RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>(){

    //Listener for each item of the recycler
    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {

        /**
         * Passes a position, representing the position in the recycler view of a view holder,
         * so that the restaurant selected can be identified.
         * @param position the position of the restaurant clicked on
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
     * @return the RecyclerView view holder associated with an individual restaurant
     */
    inner class RestaurantViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) {
        //initialise an on click listener for this item view in the recycler view
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
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestaurantViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val v = inflater.inflate(R.layout.home_recycler_view, parent, false)

        return RestaurantViewHolder(v, mListener)
    }

    /**
     * Binds the object attribute values to the view holders. This is done for each holder.
     * @param holder the holder being bound
     * @param position the position in the RecyclerView being bound
     */
    override fun onBindViewHolder(holder: RestaurantViewHolder, position: Int) {

        //set the current restaurant to the position passed in the list of restaurants
        val currRestaurant = restaurants[position]

        holder.itemView.apply {

            //Using glide to fill the image view for the holder
            Glide.with(holder.itemView).load(currRestaurant.picture).into(restaurantImage)
            //set the text of the description and title and the rating for the average rating bar
            restaurantTitle.text = currRestaurant.name
            restaurantDescription.text = currRestaurant.description
            restaurantRating.rating = currRestaurant.averageRating!!
        }
    }

    /**
     * Gets the number of restaurants in the recycler view
     * @return the number of restaurants
     */
    override fun getItemCount(): Int {
        return restaurants.size
    }
}