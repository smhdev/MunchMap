package com.example.restaurantreviewapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.restaurantreviewapp.adapters.RestaurantAdapter
import com.example.restaurantreviewapp.data.Restaurant
import com.example.restaurantreviewapp.data.Review
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_home.view.*

/**
 * A class of the Home Fragment of the app. Consists of a recycler view of restaurants in the area.
 *
 * @author Sam Harry 1901522
 */
class HomeFragment : Fragment() {

    //a global reference to the adapter used, with an arraylist to populate
    private var adapter: RecyclerView.Adapter<RestaurantAdapter.RestaurantViewHolder>? = null
    private var restaurantList = ArrayList<Restaurant>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Populate the restaurant list
        populateRestaurantList()
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    /**
     * Populates the restaurants list
     */
    private fun populateRestaurantList() {
        //stop the duplication of the data in the restaurant list
        restaurantList.clear()

        //get all the restaurant documents from firestore
        val db = FirebaseFirestore.getInstance()
        db.collection("restaurants").addSnapshotListener(object : EventListener<QuerySnapshot> {

            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                if (error != null) {
                    Log.e(R.string.firestore_error.toString(), error.message.toString())
                    return
                }

                //for loop for all the documents
                for (dc: DocumentChange in value?.documentChanges!!) {

                    //create a restaurant object from each document
                    if (dc.type == DocumentChange.Type.ADDED) {
                        val restaurant = dc.document.toObject(Restaurant::class.java)
                        //set the id of the restaurant to the documents id
                        restaurant.id = dc.document.id
                        //sets the average rating of the restaurant visible on the home page
                        setAverageRating(restaurant)
                        //add this restaurant to the arraylist
                        restaurantList.add(restaurant)

                    }
                }
                //update the adapter information
                adapter?.notifyDataSetChanged()
            }
        })
    }

    /**
     * Updates the average rating of the restaurant once all the reviews are loaded.
     */
    private fun updateWithRatingBar() {
        val db = FirebaseFirestore.getInstance()
        val reviews = db.collection("reviews")

        //get all the reviews for each restaurant
        for (restaurant in restaurantList) {
            reviews.whereEqualTo("restaurantId", restaurant.id).get().addOnSuccessListener { task ->

                // get their average ratings
                for (dc: DocumentSnapshot in task.documents) {
                    val avgRating = dc.get("averageRating").toString().toFloat()!!
                    restaurant.averageRating = avgRating

                }
            }
        }
    }

    /**
     * computes and updates the average rating of all the reviews in a particular restaurant
     * @param restaurant the restaurant to set the average rating for
     */
    private fun setAverageRating(restaurant: Restaurant) {
        val db = FirebaseFirestore.getInstance()
        val reviews = db.collection("reviews")

        var sumRating = 0.0
        var averageRating = 0F
        var numRating = 0

        //find the reviews where the restaurantId field is equal to the current restaurants object id
        reviews.whereEqualTo("restaurantId", restaurant.id).get().addOnSuccessListener { task ->

            //create review objects from the database
            for (dc: DocumentSnapshot in task.documents) {
                val review = dc.toObject(Review::class.java)

                //sum is the current review rating plus the previous review rating
                sumRating += review?.rating!!.toDouble()
                numRating++

            }
            //if restaurant has not been reviewed, update average rating accordingly
            if (sumRating <= 0) {
                db.collection("restaurants").document(restaurant.id.toString())
                    .update("averageRating", 0)

            }
            //otherwise calculate the new average rating and update
            else {
                averageRating = (sumRating / numRating).toFloat()
                db.collection("restaurants").document(restaurant.id.toString())
                    .update("averageRating", averageRating)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.apply {
            //update the average ratings first
            updateWithRatingBar()
            //stops the elements of the recycler unloading
            reviewRecycler.recycledViewPool.setMaxRecycledViews(0, 0)
            reviewRecycler.layoutManager = LinearLayoutManager(activity)
            //set the adapter for the recycler view
            adapter = RestaurantAdapter(restaurantList)
            reviewRecycler.adapter = adapter

            //on click listener for each element in the adapter
            (adapter as RestaurantAdapter).setOnItemClickListener(object :
                RestaurantAdapter.onItemClickListener {
                override fun onItemClick(position: Int) {

                    //send information to the next fragment
                    val bundle = Bundle()
                    //send the id of the restaurant to the next fragment
                    bundle.putString("REF_KEY", restaurantList.get(position).id)
                    //send the name of the restaurant to the next fragment
                    bundle.putString("name", restaurantList.get(position).name)

                    var fragment = ReviewFragment()
                    fragment.arguments = bundle

                    //open the review fragment of this restaurant
                    val manager = parentFragmentManager
                    val transaction = manager.beginTransaction()
                    transaction.replace(R.id.nav_fragment, fragment)
                    transaction.addToBackStack(null)
                    transaction.commit()
                }
            })
        }
        setHasOptionsMenu(true)
    }
}


