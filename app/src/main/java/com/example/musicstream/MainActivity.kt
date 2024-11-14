package com.example.musicstream

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicstream.adapter.CategoryAdapter
import com.example.musicstream.adapter.SectionSongListAdapter
import com.example.musicstream.databinding.ActivityMainBinding
import com.example.musicstream.models.CategoryModel
import com.example.musicstream.models.SongModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.toObjects
import android.app.Dialog
import android.graphics.Bitmap
import android.widget.Button
import android.widget.ImageView
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import android.widget.ImageButton
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.FullScreenContentCallback

//import com.google.firebase.firestore.toObjects


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var categoryAdapter: CategoryAdapter
    private val user = FirebaseAuth.getInstance().currentUser
    private val firestore = FirebaseFirestore.getInstance()
   // private lateinit var closeButton: ImageButton

     var closeButton = findViewById(R.id.closeAdButton)

    private var mInterstitialAd: InterstitialAd? = null
    private lateinit var mAdView : AdView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityMainBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Check if the user is logged in
        if (user != null) {
            // Check if the user has a Firestore document
            checkAndCreateUserDocument(user.uid)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mAdView = findViewById(R.id.adView)
        MobileAds.initialize(this@MainActivity) {}
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)

        loadInterstitialAd()

        // Optionally, set an AdListener to your Banner Ad
        mAdView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                // Handle banner ad loaded event if necessary
            }
        }

        // Handle when you want to show the interstitial ad (e.g., on a button click)


        getCategories()
        setupSection("section_1",binding.section1MainLayout,binding.section1Title,binding.section1RecyclerView)
        setupSection("section_2",binding.section2MainLayout,binding.section2Title,binding.section2RecyclerView)
        setupSection("section_3",binding.section3MainLayout,binding.section3Title,binding.section3RecyclerView)
        setupMostlyPlayed("mostly_played",binding.mostlyPlayedMainLayout,binding.mostlyPlayedTitle,binding.mostlyPlayedRecyclerView)

        binding.optionBtn.setOnClickListener {
            showPopupMenu()
        }
        binding.premiumButton.setOnClickListener {
            val upiID = "souvikkisku08@oksbi"
            val amount = "49"
            showQRCodeDialog("upi://pay?pa=$upiID&pn=Crest&am=$amount&cu=INR")

            simulatePaymentSuccess()
        }


         //Navigate to ChatActivity when the button is clicked
//        binding.chatButton.setOnClickListener {
//            val intent = Intent(this, ChatActivity::class.java)
//            startActivity(intent)
//        }
    }

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-3940256099942544/1033173712", // Replace with your Interstitial ad unit ID
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: InterstitialAd) {
                    mInterstitialAd = interstitialAd
                    // Set up a full-screen content callback to handle events
                    mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            super.onAdDismissedFullScreenContent()
                            // Optionally reload the interstitial ad after it is dismissed
                            loadInterstitialAd()
                        }

                        override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                            super.onAdFailedToShowFullScreenContent(adError)
                            // Optionally handle the error
                        }

                        override fun onAdShowedFullScreenContent() {
                            super.onAdShowedFullScreenContent()
                            // Handle when the ad is shown (e.g., log an event or update UI)
                        }
                    }
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    mInterstitialAd = null
                    // Handle failure to load interstitial ad
                }
            }
        )
    }

    private fun showInterstitialAd() {
        if (mInterstitialAd != null) {
            mInterstitialAd?.show(this)
        } else {
            // Optionally handle the case where the ad isn't loaded yet
            // Maybe load it again or show a toast indicating that the ad isn't ready
            loadInterstitialAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mAdView.destroy()
        // Optionally, call this to clean up interstitial ad resources if needed
        mInterstitialAd = null
    }
    private fun enableEdgeToEdge() {
        // Your custom logic for edge-to-edge support if needed
    }

    private fun simulatePaymentSuccess() {
        // Simulating a successful payment with a short delay
        binding.premiumButton.postDelayed({
            // Save premium status to Firestore
            if (user != null) {
                saveUserPremiumStatus(user.uid, true)
            }

            // Hide the premium button after the payment is successful
            hidePremiumButton()

            // Show success message
            Toast.makeText(this, "Payment Successful! You are now a Premium user.", Toast.LENGTH_LONG).show()
        }, 60000) // Simulate a delay of 1 min for payment confirmation
    }

    private fun checkAndCreateUserDocument(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        userDocRef.get().addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                // User document exists, check the premium status
                val isPremium = document.getBoolean("isPremium") ?: false
                if (isPremium) {
                    hidePremiumButton()
                }
            } else {
                // User document doesn't exist, create it with default data
                createUserDocument(userId)
            }
        }
    }

    private fun createUserDocument(userId: String) {
        val userDocRef = firestore.collection("users").document(userId)

        // Set default data (you can add other fields as needed)
        val userData = hashMapOf(
            "isPremium" to false  // Default value for premium status
        )

        userDocRef.set(userData).addOnSuccessListener {
            // Document created successfully
            Toast.makeText(this, "Welcome 👋👋", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Handle failure case
            Toast.makeText(this, "Some Error occurred ❌❌", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserPremiumStatus(userId: String, isPremium: Boolean) {
        val userDocRef = firestore.collection("users").document(userId)
        userDocRef.update("isPremium", isPremium).addOnSuccessListener {
            // Optionally, show a toast that the data was saved successfully
            Toast.makeText(this, "Premium status updated", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            // Handle failure case
            Toast.makeText(this, "Error updating status", Toast.LENGTH_SHORT).show()
        }
    }

    private fun hidePremiumButton() {
        binding.premiumButton.visibility = Button.GONE
    }

//    fun loadAd(){
//        val adRequest = AdRequest.Builder().build()
//
//        InterstitialAd.load(this,"ca-app-pub-3940256099942544/1033173712", adRequest, object : InterstitialAdLoadCallback() {
//            override fun onAdFailedToLoad(adError: LoadAdError) {
//                mInterstitialAd = null
//            }
//
//            override fun onAdLoaded(interstitialAd: InterstitialAd) {
//                mInterstitialAd = interstitialAd
//            }
//        })
//    }

   private fun showPopupMenu(){

        val popupMenu = PopupMenu(this,binding.optionBtn)
        val inflator = popupMenu.menuInflater
        inflator.inflate(R.menu.option_menu,popupMenu.menu)
        popupMenu.show()
        popupMenu.setOnMenuItemClickListener {
            when(it.itemId){
                R.id.logout -> {
                    logout()
                    //true
                }
            }
            false
        }


    }

    private fun logout(){
        MyExoplayer.getInstance()?.release()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }

    override fun onResume() {
        super.onResume()
        showPlayerView()
    }



    private fun showPlayerView(){
        binding.playerView.setOnClickListener {
            startActivity(Intent(this,PlayerActivity::class.java))
        }
        MyExoplayer.getCurrentSong()?.let {
            binding.playerView.visibility = View.VISIBLE
            binding.songTitleTextView.text = "Now Playing : " + it.title
            Glide.with(binding.songCoverImageView).load(it.coverUrl)
                .apply(
                    RequestOptions().transform(RoundedCorners(32))
                ).into(binding.songCoverImageView)
        } ?: run{
            binding.playerView.visibility = View.GONE
        }
    }

    private fun getCategories(){
        FirebaseFirestore.getInstance().collection("category")
            .get().addOnSuccessListener {
                val categoryList = it.toObjects(CategoryModel::class.java)
                setupCategoryRecyclerView(categoryList)
            }
    }

    private fun setupCategoryRecyclerView(categoryList: List<CategoryModel>){
        categoryAdapter=CategoryAdapter(categoryList)
        binding.categoriesRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,false)
        binding.categoriesRecyclerView.adapter = categoryAdapter
    }
   private fun setupSection(id : String, mainLayout : RelativeLayout, titleView : TextView, recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                val section = it.toObject(CategoryModel::class.java)
                section?.apply {
                    mainLayout.visibility = View.VISIBLE
                    titleView.text = name
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                    recyclerView.adapter = SectionSongListAdapter(songs)
                    mainLayout.setOnClickListener {
                        SongsListActivity.category = section
                        startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                    }
                }
            }

    }

    private fun setupMostlyPlayed(id: String,mainLayout : RelativeLayout,titleView : TextView,recyclerView: RecyclerView){
        FirebaseFirestore.getInstance().collection("sections")
            .document(id)
            .get().addOnSuccessListener {
                //get most played songs
                FirebaseFirestore.getInstance().collection("songs")
                    .orderBy("count", Query.Direction.DESCENDING)
                    .limit(5)
                    .get().addOnSuccessListener {songListSnapshot->
                        val songsModelList = songListSnapshot.toObjects<SongModel>()
                        val songsIdList = songsModelList.map{
                            it.id
                        }.toList()
                        val section = it.toObject(CategoryModel::class.java)
                        section?.apply {
                            section.songs = songsIdList
                            mainLayout.visibility = View.VISIBLE
                            titleView.text = name
                            recyclerView.layoutManager = LinearLayoutManager(this@MainActivity,LinearLayoutManager.HORIZONTAL,false)
                            recyclerView.adapter = SectionSongListAdapter(songs)
                            mainLayout.setOnClickListener {
                                SongsListActivity.category = section
                                startActivity(Intent(this@MainActivity,SongsListActivity::class.java))
                            }
                        }
                    }


            }
    }
    private fun showQRCodeDialog(qrContent: String) {
        val qrCodeDialog = Dialog(this)
        qrCodeDialog.setContentView(R.layout.dialog_qr_code)

        val qrCodeImageView = qrCodeDialog.findViewById<ImageView>(R.id.qrCodeImageView)
        val bitmap = generateQRCode(qrContent)
        qrCodeImageView.setImageBitmap(bitmap)

        qrCodeDialog.show()
    }

    private fun generateQRCode(content: String): Bitmap {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
        return bmp
    }
}