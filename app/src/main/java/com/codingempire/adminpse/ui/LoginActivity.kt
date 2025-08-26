package com.codingempire.adminpse.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.codingempire.adminpse.R
import com.codingempire.adminpse.Utils
import com.codingempire.adminpse.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var utils: Utils
    private lateinit var auth: FirebaseAuth

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val fromLogout = intent.getBooleanExtra("fromLogout", false)
        val current = FirebaseAuth.getInstance().currentUser
        if (!fromLogout && current != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("MyPref", MODE_PRIVATE)
        utils = Utils(this)
        auth = FirebaseAuth.getInstance()
        // Show/Hide Password Feature
        binding.passwordEditText.setOnTouchListener { v, event ->
            val DRAWABLE_RIGHT = 2
            if (event.action === MotionEvent.ACTION_UP) {
                if (event.rawX >= (binding.passwordEditText.right - binding.passwordEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds()
                        .width())
                ) {
                    if (binding.passwordEditText.transformationMethod
                            .equals(PasswordTransformationMethod.getInstance())
                    ) {
                        binding.passwordEditText.transformationMethod =
                            HideReturnsTransformationMethod.getInstance()
                        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.baseline_visibility_off_24,
                            0
                        )
                    } else {
                        binding.passwordEditText.transformationMethod =
                            PasswordTransformationMethod.getInstance()
                        binding.passwordEditText.setCompoundDrawablesWithIntrinsicBounds(
                            0,
                            0,
                            R.drawable.baseline_remove_red_eye_24,
                            0
                        )
                    }
                    binding.passwordEditText.setSelection(binding.passwordEditText.getText().length)
                    return@setOnTouchListener true
                }
            }
            false
        }

        binding.loginButton.setOnClickListener {
            utils.startLoadingAnimation()

            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                binding.emailEditText.error = "Email is required"
                binding.passwordEditText.error = "Password is required"
                utils.endLoadingAnimation()
                return@setOnClickListener
            }

            // --- Use FirebaseAuth to sign in ---
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    val adminId = it.user!!.uid

                    firestore.collection("Admin")
                        .whereEqualTo("email", email)
                        .limit(1)
                        .get()
                        .addOnSuccessListener { snap ->
                            if (snap.isEmpty) {
                                utils.endLoadingAnimation()
                                showMessage("Admin record not found")
                                return@addOnSuccessListener
                            }

                            val docRef = snap.documents[0].reference
                            docRef.update("id", adminId)  // keep your existing line

                            FirebaseMessaging.getInstance().token
                                .addOnCompleteListener { tokenTask ->
                                    if (!tokenTask.isSuccessful) {
                                        utils.endLoadingAnimation()
                                        showMessage(
                                            tokenTask.exception?.message
                                                ?: "Failed to get FCM token"
                                        )
                                        return@addOnCompleteListener
                                    }
                                    val token = tokenTask.result ?: return@addOnCompleteListener
                                    docRef.update("deviceToken", token)
                                        .addOnSuccessListener {
                                            utils.endLoadingAnimation()
                                            showMessage("Login successful!")
                                            startActivity(
                                                Intent(
                                                    this,
                                                    MainActivity::class.java
                                                )
                                            ); finish()
                                        }
                                        .addOnFailureListener { e ->
                                            utils.endLoadingAnimation()
                                            showMessage("Token save failed: ${e.localizedMessage}")
                                        }
                                }
                        }
                }
                .addOnFailureListener { e ->
                    utils.endLoadingAnimation()
                    showMessage(e.localizedMessage ?: "Authentication error")
                }
        }
    }

    fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showAnimation() {
//        val animationContainer = binding.animationContainer
//        animationContainer.visibility = View.VISIBLE
    }

    private fun hideAnimation() {
//        binding.animationContainer.visibility = View.GONE
    }
}