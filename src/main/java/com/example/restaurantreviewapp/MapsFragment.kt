package com.example.restaurantreviewapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import com.example.restaurantreviewapp.data.Restaurant
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*
import kotlinx.android.synthetic.main.fragment_maps.*

/**
 * a class of the maps fragment of the app.
 * @author Sam Harry 1901522
 */
class MapsFragment : Fragment(), OnMapReadyCallback {

    //declare the list of restaurants and the Google Map
    private var restaurantList = ArrayList<Restaurant>()
    private lateinit var googleMap: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        //populate the restaurant list
        populateRestaurantList()
        val view = inflater.inflate(R.layout.fragment_maps, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mapView.onCreate(savedInstanceState)
        mapView.onResume()

        //get the map view
        mapView.getMapAsync(this)

    }

    override fun onMapReady(map: GoogleMap) {

        map?.let {
            googleMap = it

            //for all the restaurants in the list, make a marker from the latitude and longitude
            for (restaurant in restaurantList) {
                val marker = LatLng(restaurant.latitude!!, restaurant.longitude!!)
                map.addMarker(
                    (MarkerOptions())
                        .position(marker)
                        .title(restaurant.name)
                        .snippet(restaurant.description)
                )

                //move the camera to the marker
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker, 12F))

            }
        }
    }

    private fun populateRestaurantList() {
        //clear to stop duplicates
        restaurantList.clear()

        val db = FirebaseFirestore.getInstance()
        db.collection("restaurants").addSnapshotListener(object : EventListener<QuerySnapshot> {

            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {

                if (error != null) {
                    Log.e(R.string.firestore_error.toString(), error.message.toString())
                    return
                }

                //turn all restaurant documents from firestore into restaurant objects
                for (dc: DocumentChange in value?.documentChanges!!) {

                    val restaurant = dc.document.toObject(Restaurant::class.java)
                    restaurant.id = dc.document.id
                    restaurantList.add(restaurant)

                }
            }
        })
    }
}

