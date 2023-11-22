package com.example.restaurantreviewapp

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.firestore.EventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_edit_review.*
import kotlinx.android.synthetic.main.fragment_edit_review.reviewEditRatingBar
import kotlinx.android.synthetic.main.fragment_edit_review.reviewEditText
import kotlinx.android.synthetic.main.fragment_edit_review.reviewResubmitButton
import kotlinx.android.synthetic.main.fragment_edit_review.view.*
import kotlinx.android.synthetic.main.fragment_write_review.*
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private lateinit var resultLauncher: ActivityResultLauncher<String>
private var imageUri: Uri? = null

/**
 * A class of the fragment for editing reviews. This is accessed by clicking a review on the
 * users profile page.
 *
 * @author Sam Harry 1901522
 */
class EditReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //populate the containers in the layout
        populateLayout()

        //get the result of the image activity, set it to the imageUri attribute
        resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->

            imageUri = result
            reviewEditImageView.setImageURI(result)

        }


        view.apply {

            reviewResubmitButton.setOnClickListener {

                when {
                    //if no review content, prompt the user to add valid text
                    TextUtils.isEmpty(reviewEditText.text.toString().trim { it <= ' ' }) -> {
                        Snackbar.make(it, R.string.no_review_text_edit, LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }

                        //if no rating value, prompt the user to enter at least half a star
                        reviewEditRatingBar.rating == 0F -> {
                            Snackbar.make(
                                it,
                                R.string.no_rating_edit,
                                LENGTH_LONG
                            )
                                .show()
                            return@setOnClickListener
                        }


                    else -> {
                        //set the value of the review contents and rating
                        val reviewContents = reviewEditText.text.toString()
                        val rating = reviewEditRatingBar.rating

                        //access the review collection, and pass in the reference of the
                        //review we are editing with REF_KEY.
                        val db = FirebaseFirestore.getInstance()
                        val document = db.collection("reviews")
                            .document("${requireArguments().get("REF_KEY")}")
                        //update the document with the new values.
                        document.update("review", reviewContents)
                        document.update("rating", rating)
                        uploadImage(document)
                    }
                }

                //send the user back to the profile fragment after submitting the edited review
                var fragment = ProfileFragment()
                val manager = parentFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.nav_fragment, fragment)
                transaction.addToBackStack(null)
                transaction.commit()

            }
        }


        //set on click listener for the review upload image button.
        reviewReuploadButton.setOnClickListener {
            resultLauncher.launch("image/*")

        }
    }

    /**
     * Populates the containers within the layout with images and text
     */
    private fun populateLayout() {
        val db = FirebaseFirestore.getInstance()
        //add a listener to the current reviews' document
        val listener =
            db.collection("reviews").document("${requireArguments().get("REF_KEY")}").get()
        listener.addOnSuccessListener {
            //using glide to load in the current review image
            Glide.with(this@EditReviewFragment).load(it?.get("reviewImage"))
                .into(reviewEditImageView)
            //set the ratings and the review contents
            reviewEditText.setText(it?.get("review").toString())
            reviewEditRatingBar.rating = it?.get("rating").toString().toFloat()
        }
    }

    /**
     * Uploads the new selected image to the cloud storage
     * @param document the document reference of the review document being edited
     */
    private fun uploadImage(document: DocumentReference) {

        if (imageUri == null) {
            return
        }

        //format the data using a time format
        val formatter = SimpleDateFormat("yyyy_MM__dd__HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        //make a new storage reference for the new image
        val storageReference =
            FirebaseStorage.getInstance().getReference("images/reviews/$fileName")
        val uploadTask = storageReference.putFile(imageUri!!)
        uploadTask.addOnSuccessListener {
            //on success, get the download url of the image in cloud storage
            val downloadUrl = storageReference.downloadUrl
            downloadUrl.addOnSuccessListener {
                var imageUrl = it.toString()
                //send the url and the doc reference to be updated on the database
                updatePhoto(imageUrl, document)
            }
        }
    }

    /**
     * Updates the url of the review photo in firestore database
     * @param imageUrl the url to update the database wth
     * @param document the document to update
     */
    private fun updatePhoto(imageUrl: String, document: DocumentReference) {
        val db = FirebaseFirestore.getInstance()
        db.collection("reviews").document(document.id).update("reviewImage", imageUrl)
    }


}