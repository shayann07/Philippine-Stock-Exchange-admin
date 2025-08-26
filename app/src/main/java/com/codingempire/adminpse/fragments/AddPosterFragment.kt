package com.codingempire.adminpse.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.codingempire.adminpse.R
import com.codingempire.adminpse.adapter.ImageAnnouncementAdapter
import com.codingempire.adminpse.databinding.FragmentAddPosterBinding
import com.codingempire.adminpse.models.ImageAnnouncement
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.net.URLDecoder
import java.util.UUID

class AddPosterFragment : Fragment() {

    // ViewBinding for fragment_add_poster.xml
    private var _binding: FragmentAddPosterBinding? = null
    private val binding get() = _binding!!

    // Firestore collection reference
    private val firestore = FirebaseFirestore.getInstance()
    private val imagesCollection = firestore.collection("announcement_images")

    // Firebase Storage root under "announcement_images"
    private val storageRootRef: StorageReference by lazy {
        FirebaseStorage.getInstance().reference.child("announcement_images")
    }

    // RecyclerView adapter
    private lateinit var adapter: ImageAnnouncementAdapter
    private val imageList = mutableListOf<ImageAnnouncement>()

    // URI of the image picked for upload
    private var selectedImageUri: Uri? = null

    // Reference to the dialog’s ImageView preview (so we can update it)
    private var currentDialogPreview: ImageView? = null

    // Launcher for “pick image from gallery”
    private val pickImageLauncher = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            currentDialogPreview?.setImageURI(it)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddPosterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) Initialize adapter, passing in the delete‐callback
        adapter = ImageAnnouncementAdapter(imageList) { announcement ->
            confirmAndDelete(announcement)
        }
        binding.announcementsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.announcementsRecyclerView.adapter = adapter

        // 2) Load existing announcements
        fetchAllImageAnnouncements()

        // 3) FAB click → show dialog to add a new poster
        binding.addAnnouncementBtn.setOnClickListener {
            showAddPosterDialog()
        }
    }

    /** Fetch all documents under "announcement_images" and display them */
    private fun fetchAllImageAnnouncements() {
        imagesCollection
            .orderBy("time", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { snapshot ->
                imageList.clear()
                for (doc in snapshot.documents) {
                    val url = doc.getString("imageUrl") ?: continue
                    val ts = doc.getTimestamp("time")
                    val id = doc.id
                    imageList.add(ImageAnnouncement(id = id, imageUrl = url, time = ts))
                }
                binding.noAnnouncementText.visibility =
                    if (imageList.isEmpty()) View.VISIBLE else View.GONE
                adapter.updateData(imageList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to load images: ${e.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    /**
     * Show a confirmation dialog. If confirmed, delete both the Storage file
     * and the Firestore document for this announcement.
     */
    private fun confirmAndDelete(announcement: ImageAnnouncement) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Announcement")
            .setMessage("Are you sure you want to delete this image?")
            .setPositiveButton("Delete") { _, _ ->
                deleteImageAnnouncement(announcement)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Deletes the Storage file and Firestore document for the given announcement.
     */
    private fun deleteImageAnnouncement(announcement: ImageAnnouncement) {
        // 1) Derive the Storage path from the download URL
        // Example download URL:
        // https://firebasestorage.googleapis.com/v0/b/<bucket>/o/announcement_images%2Ffilename.jpg?alt=media
        val downloadUrl = announcement.imageUrl
        val pathSegment = try {
            // Extract between ".../o/" and "?"
            val afterO = downloadUrl.substringAfter("/o/")
            val between = afterO.substringBefore("?")
            // URL decode the "%2F" → "/"
            URLDecoder.decode(between, "UTF-8")
        } catch (e: Exception) {
            null
        }

        if (pathSegment == null) {
            Toast.makeText(requireContext(), "Could not parse storage path.", Toast.LENGTH_SHORT)
                .show()
            return
        }

        // 2) Delete from Firebase Storage
        val fileRef = FirebaseStorage.getInstance().reference.child(pathSegment)
        fileRef.delete()
            .addOnSuccessListener {
                // 3) Only if Storage delete succeeded, remove Firestore doc
                imagesCollection.document(announcement.id)
                    .delete()
                    .addOnSuccessListener {
                        Toast.makeText(
                            requireContext(),
                            "Announcement deleted",
                            Toast.LENGTH_SHORT
                        ).show()
                        fetchAllImageAnnouncements()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            requireContext(),
                            "Failed to delete Firestore doc: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    requireContext(),
                    "Failed to delete storage file: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun showAddPosterDialog() {
        // Inflate the dialog’s layout (dialog_add_poster.xml)
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialoge_add_poster, null)

        // Find views inside the inflated dialog
        val previewImageView = dialogView.findViewById<ImageView>(R.id.previewImageView)
        val selectImageBtn = dialogView.findViewById<Button>(R.id.selectImageBtn)
        val uploadImageBtn = dialogView.findViewById<Button>(R.id.uploadImageBtn)
        val uploadProgressBar = dialogView.findViewById<ProgressBar>(R.id.uploadProgressBar)

        // Keep a reference to update preview when an image is chosen
        currentDialogPreview = previewImageView

        // Build & show the AlertDialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setView(dialogView)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // “Select Image” button → open gallery
        selectImageBtn.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // “Upload & Publish Announcement” button
        uploadImageBtn.setOnClickListener {
            val uri = selectedImageUri
            if (uri == null) {
                Toast.makeText(
                    requireContext(),
                    "Please select an image first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // Disable button and show progress bar
            uploadImageBtn.isEnabled = false
            uploadProgressBar.visibility = View.VISIBLE

            // Upload to Firebase Storage
            val filename = "${System.currentTimeMillis()}_${UUID.randomUUID()}.jpg"
            val imageRef = storageRootRef.child(filename)
            imageRef.putFile(uri)
                .addOnProgressListener { snapshot ->
                    val percent =
                        (100.0 * snapshot.bytesTransferred / snapshot.totalByteCount).toInt()
                    uploadProgressBar.progress = percent
                }
                .addOnSuccessListener {
                    // Get download URL
                    imageRef.downloadUrl
                        .addOnSuccessListener { downloadUri ->
                            // Save to Firestore
                            val data = hashMapOf(
                                "imageUrl" to downloadUri.toString(),
                                "time" to Timestamp.now()
                            )
                            imagesCollection
                                .add(data)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        requireContext(),
                                        "Poster uploaded",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    dialog.dismiss()
                                    fetchAllImageAnnouncements()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        requireContext(),
                                        "Failed to publish announcement: ${e.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    uploadImageBtn.isEnabled = true
                                    uploadProgressBar.visibility = View.GONE
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                requireContext(),
                                "Failed to get download URL: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                            uploadImageBtn.isEnabled = true
                            uploadProgressBar.visibility = View.GONE
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        requireContext(),
                        "Upload failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    uploadImageBtn.isEnabled = true
                    uploadProgressBar.visibility = View.GONE
                }
        }

        // When dialog is dismissed, clear references
        dialog.setOnDismissListener {
            currentDialogPreview = null
            selectedImageUri = null
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
