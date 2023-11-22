package com.example.restaurantreviewapp.data

/**
 * A data class used to create Review objects from the database, so that they can be easily adapted
 * by the adapter class.
 *
 * @author Sam Harry 1901522
 * @param restaurant the restaurant id the review is associated with
 * @param user the user id the review is associated with
 * @param reviewImage an image in the review
 * @param profileImage the profile image of the user in the review
 * @param name the name of the user leaving the review
 * @param rating the rating given by the user in the review
 * @param review the text contents of the review
 * @param id the id of the review document on firestore
 */
data class Review(
    var restaurantId: String? = null,
    var user: String? = null,
    var reviewImage: String? = null,
    var profileImage: String? = null,
    val name: String? = null,
    val restaurantName : String? = null,
    val rating: Float? = null,
    val review: String? = null,
    var id: String? = null
)





