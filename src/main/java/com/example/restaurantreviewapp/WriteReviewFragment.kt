package com.example.restaurantreviewapp

import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_write_review.*
import java.text.SimpleDateFormat
import java.util.*

private lateinit var resultLauncher: ActivityResultLauncher<String>
private var imageUri: Uri? = null


/**
 * A class for the WriteReview Fragment of the app
 *
 * @author Sam Harry 1901522
 */
class WriteReviewFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_write_review, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //gets the result of the users image selection
        resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->

            imageUri = result
            imageSelectedView.setImageURI(result)

        }

        view.apply {
            //listener for the submit review button
            submitReviewButton.setOnClickListener {

                when {
                    //if the review text is empty, prompt the user to enter text
                    TextUtils.isEmpty(reviewTextField.text.toString().trim { it <= ' ' }) -> {
                        Snackbar.make(it, "Please enter review text.", LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }

                    //if the user has not left a star rating, prompt the user to leave one
                    writeReviewRating.rating == 0F -> {
                            Snackbar.make(it, "You must leave at least a half star rating.", LENGTH_LONG)
                                .show()
                        return@setOnClickListener
                        }


                    else -> {

                        //retrieve the review data
                        val reviewContents = reviewTextField.text.toString()
                        val data = hashMapOf(
                            "reviewImage" to "",
                            "name" to (FirebaseAuth.getInstance().currentUser?.email),
                            "rating" to writeReviewRating.rating,
                            "restaurantId" to requireArguments().get("REF_KEY")
                                .toString(),
                            "review" to reviewContents,
                            "user" to (FirebaseAuth.getInstance().currentUser?.uid),
                            "profileImage" to "",
                            "restaurantName" to requireArguments().get("name")
                         )

                        //upload a new review document to firestore
                        val db = FirebaseFirestore.getInstance()
                        val document = db.collection("reviews").document()
                        document.set(data)
                        //upload the restaurant image to cloud storage
                        uploadRestaurantImage(document)
                        //update the profile picture of the reviewer to firestore
                        updateProfilePicture(document)

                    }
                }

                //send the user to the review page of the restaurant reviewed
                val bundle = Bundle()
                bundle.putString("REF_KEY", requireArguments().get("REF_KEY").toString())
                var fragment = ReviewFragment()
                fragment.arguments = bundle
                val manager = parentFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.nav_fragment, fragment)
                transaction.addToBackStack(null)
                transaction.commit()
            }
        }


        //listener for the upload image button on the review
        uploadImageButton.setOnClickListener {
            //send the user to the image selection activity
            resultLauncher.launch("image/*")

        }
    }

    /**
     * Upload the restaurant image to cloud storage
     *
     * @param document the document of the review for the image uploaded
     */
    private fun uploadRestaurantImage(document: DocumentReference) {

        if (imageSelectedView.drawable != null) {

            //format as a date
            val formatter = SimpleDateFormat("yyyy_MM__dd__HH_mm_ss", Locale.getDefault())
            val now = Date()
            val fileName = formatter.format(now)
            //create a new storage reference for the image
            val storageReference =
                FirebaseStorage.getInstance().getReference("images/reviews/$fileName")
            //upload the image
            val uploadTask = storageReference.putFile(imageUri!!)
            uploadTask.addOnSuccessListener {
                //get the url for the image upon success of upload
                val downloadUrl = storageReference.downloadUrl
                downloadUrl.addOnSuccessListener {
                    var imageUrl = it.toString()
                    //send to method to update the firestore of the new image
                    updatePhoto(imageUrl, document)
                }
            }
        }

    }

    /**
     * Updates the url of the review image on firestore
     * @param imageUrl the new url of the image uploaded
     * @param document the document reference of the new review document
     */
    private fun updatePhoto(imageUrl: String, document: DocumentReference) {
        val db = FirebaseFirestore.getInstance()
        //update the image url with the new image
        db.collection("reviews").document(document.id).update("reviewImage", imageUrl)
    }

    /**
     * Updates the url of the profile image on firestore of the review
     * @param document the document of the review to update
     */
    private fun updateProfilePicture(document: DocumentReference) {
        val db = FirebaseFirestore.getInstance()
        //get the current users document
        db.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid
            .toString()).get().addOnSuccessListener {
                val profilePicture = it.get("image")

            //update the new profile image of the review with the user profile image
            db.collection("reviews").document(document.id).update("profileImage", profilePicture)
        }
    }

}