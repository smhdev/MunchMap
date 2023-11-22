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
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.BaseTransientBottomBar.LENGTH_LONG
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_edit_profile.*
import java.text.SimpleDateFormat
import java.util.*

private lateinit var resultLauncher: ActivityResultLauncher<String>
private var imageUri: Uri? = null

/**
 * A class of the fragment for editing the profile of a user.
 *
 * @author Sam Harry 1901522
 */
class EditProfileFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_edit_profile, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        populateLayout()

        //get the result of the image activity, set it to the imageUri attribute
        resultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { result ->

            imageUri = result
            profilePictureEditProfileImage.setImageURI(result)

        }

        view.apply {

            //listener for the save changes button
            saveChangesProfileButton.setOnClickListener {

                when {
                    //if the string is empty, prompt user to enter a display name and return
                        // to listener
                    TextUtils.isEmpty(
                        displayNameEditProfile.text.toString().trim { it <= ' ' }) -> {
                        Snackbar.make(it, R.string.no_display_name, LENGTH_LONG)
                            .show()
                        return@setOnClickListener
                    }

                    //if the bio text is empty, prompt the user to enter a bio and return
                    //to listener
                    TextUtils.isEmpty(bioEditProfile.text.toString().trim { it <= ' ' }) -> {
                        Snackbar.make(
                            it, R.string.no_bio, LENGTH_LONG
                        )
                            .show()
                        return@setOnClickListener
                    }

                    //otherwise, fetch the values of the containers and update the database with the
                    //new details
                    else -> {
                        val displayName = displayNameEditProfile.text.toString()
                        val bio = bioEditProfile.text.toString()

                        //access the users collection, open the document with the current users
                        //ID
                        val db = FirebaseFirestore.getInstance()
                        val document = db.collection("users")
                            .document(FirebaseAuth.getInstance().currentUser!!.uid)
                        document.update("name", displayName)
                        document.update("bio", bio)
                        //upload the image to cloud storage
                        uploadImage(document)


                    }
                }
                //call the next fragment (profile fragment) once complete
                var fragment = ProfileFragment()
                val manager = parentFragmentManager
                val transaction = manager.beginTransaction()
                transaction.replace(R.id.nav_fragment, fragment)
                //allows for usage of the android back button in the next fragment.
                transaction.addToBackStack(null)
                transaction.commit()
            }

        }

        //on click listener for the uploadPictureButton
        uploadProfilePictureButton.setOnClickListener {
            //launch the image activity, the user is navigated to the image selection activity
            resultLauncher.launch("image/*")

        }
    }

    /**
     * Populates the page with the users information for editing
     */
    private fun populateLayout() {
        //open the users collection, and find the users associated document with their id
        val db = FirebaseFirestore.getInstance()
        val listener =
            db.collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid).get()
        listener.addOnSuccessListener {
            //using glide to update the profile image container with the image retrieved
            Glide.with(requireContext().applicationContext).load(it?.get("image"))
                .into(profilePictureEditProfileImage)
            //set the text for the display name and bio of the user.
            displayNameEditProfile.setText(it?.get("name").toString())
            bioEditProfile.setText(it?.get("bio").toString())

        }
    }


    /**
     * Uploads an image to the cloud storage on firebase.
     * @param document the document reference for the document uploaded
     */
    private fun uploadImage(document: DocumentReference) {

        //if no image exists then exit function
        if (imageUri == null) {
            return
        }

        //format in a time format
        val formatter = SimpleDateFormat("yyyy_MM__dd__HH_mm_ss", Locale.getDefault())
        val now = Date()
        val fileName = formatter.format(now)
        //new cloud storage reference for the new file to be uploaded.
        val storageReference =
            FirebaseStorage.getInstance().getReference("images/users/$fileName")
        //upload the file to the reference
        val uploadTask = storageReference.putFile(imageUri!!)
        //add a listener for the upload task.
        uploadTask.addOnSuccessListener {
            //if successful, get the download url for the image.
            val downloadUrl = storageReference.downloadUrl
            downloadUrl.addOnSuccessListener {
                //if successful, send this to be updated in firestore database
                var imageUrl = it.toString()
                updatePhoto(imageUrl, document)
            }
        }
    }

    /**
     * Updates the image address in the firestore database for the new image uploaded
     * @param imageUrl the url of the image downloaded after the upload of the image
     * @param document the document to be updated.
     */
    private fun updatePhoto(imageUrl: String, document: DocumentReference) {
        val db = FirebaseFirestore.getInstance()
        db.collection("users").document(document.id).update("image", imageUrl)
    }

}

