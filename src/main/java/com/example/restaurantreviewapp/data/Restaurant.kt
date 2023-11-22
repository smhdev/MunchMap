package com.example.restaurantreviewapp.data

/**
 * A data class, used to create restaurant objects from the database, so that the adapter can
 * populate view holders with the information easily.
 *
 * @author Sam Harry 1901522
 * @param picture a picture of the restaurant
 * @param name the name of the restaurant
 * @param description a description of the restaurant
 * @param averageRating the average rating of the restaurant
 * @param latitude the latitude coordinate of the restaurant geographically
 * @param longitude the longitude coordinate of the restaurant geographically
 * @param id the document id of the restaurant stored on firestore
 */
data class Restaurant(
    val picture: String? = null,
    val name: String? = null,
    val description: String? = null,
    var averageRating: Float? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    var id: String? = null
)
