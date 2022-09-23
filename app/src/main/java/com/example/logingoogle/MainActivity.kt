package com.example.logingoogle

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import com.example.logingoogle.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var googleSignInClient: GoogleSignInClient
    private var account: GoogleSignInAccount? = null

    private val signInWithGoogleLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d("TAG", "res: $result")
            if (result.resultCode == RESULT_CANCELED) {
                val builder = AlertDialog.Builder(this)
                builder.apply {
                    setCancelable(false)
                    setTitle("Mohon maaf")
                    setMessage("Terjadi kesalahan saat mencoba mengambil daftar akun google anda")
                    setPositiveButton("Ok") { dialog, _ ->
                        dialog.dismiss()
                    }
                }
                val alertDialog = builder.create()
                try {
                    alertDialog.show()
                } catch (e: Exception) {
                }
            } else {
                signInWithGoogleResult(result.data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initGoogleSignIn()
        val signInButton = binding.signInButton
        signInButton.setSize(SignInButton.SIZE_STANDARD)
        signInButton.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            signInWithGoogleLauncher.launch(signInIntent)
        }
    }

    private fun initGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestServerAuthCode("841252957926-qbv99cbr0ps6m4rvsg04qsbkprcgftv5.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        account = GoogleSignIn.getLastSignedInAccount(this)
    }


    private fun signInWithGoogleResult(data: Intent?) {
        googleSignInClient.signOut()
        val lastSignedAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (lastSignedAccount != null) {
            simpanDataKeSharedPreference(lastSignedAccount)
        } else {
            val accountTask = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = accountTask.getResult(ApiException::class.java)
                simpanDataKeSharedPreference(account)
            } catch (e: Exception) {
            }
        }
    }

    private fun simpanDataKeSharedPreference(account: GoogleSignInAccount) {
        val preferences = getSharedPreferences("AUTHENTICATION_DATA", MODE_PRIVATE)
        preferences.edit {
            putString("email", account.email)
            putString("name", account.displayName)
            putString("id", account.id)
        }
        preferences.edit().apply()
        Intent(this@MainActivity, HomeActivity::class.java).apply {
            startActivity(this)
            finish()
        }
    }
}