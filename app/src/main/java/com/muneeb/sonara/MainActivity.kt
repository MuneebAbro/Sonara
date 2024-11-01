package com.muneeb.sonara

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.MediaStore
import android.provider.Settings
import android.support.v4.media.session.MediaSessionCompat
import android.text.Editable
import android.text.TextWatcher
import android.util.Size
import android.view.GestureDetector
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RemoteViews
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.navigation.NavigationView
import com.muneeb.sonara.databinding.ActivityMainBinding
import jp.wasabeef.glide.transformations.BlurTransformation
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper
import java.util.Locale
import java.util.Random
import kotlin.math.abs

class MainActivity : AppCompatActivity() {

    private lateinit var overlayContainer: FrameLayout
    private lateinit var collapseButton: ImageButton
    private lateinit var cardPlayPauseLL: LinearLayout
    private lateinit var playPauseButton: ImageView
    private lateinit var nextBtn: ImageView
    private lateinit var previousBtn: ImageView
    private lateinit var albumCoverImageView2: ImageView
    private lateinit var songTitleTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var searchBar: EditText
    private lateinit var trackCount: TextView
    private lateinit var albumCoverImageView: ImageView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var songs: MutableList<Song>
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var seekBar: SeekBar
    private lateinit var currentTimeTextView: TextView
    private lateinit var totalTimeTextView: TextView
    private lateinit var handler: Handler
    private var currentSongIndex: Int = -1
    private var isPlayerCardExpanded = false
    private lateinit var compactPlayerCard: ConstraintLayout
    private lateinit var expandedPlayerCard: ConstraintLayout
    private lateinit var binding: ActivityMainBinding
    private lateinit var originalSongs: List<Song> // Store original songs list
    private var isMediaPlayerInitialized = false
    private var bounceAnimator: ValueAnimator? = null
    private var bounceAnimator2: ValueAnimator? = null
    private var backPressedOnce = false
    private lateinit var lottieAnimationView: LottieAnimationView
    private val permissions: Array<String>
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                android.Manifest.permission.READ_MEDIA_AUDIO,
                android.Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var toggle: ActionBarDrawerToggle
    private var isDarkMode: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {

        loadAndApplyTheme()

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupActivity()

    }

    // UI CODE
    private fun setupActivity() {
        initializeViews()
        drawerInitialization()
        setupPermissions()
        setupPlayerControls()
        onBackPressFromMainActivity()
        setupCardHeightListener()
        checkMediaPlayerInitialization()
        setupPlayerCardSwipeGestures()
    }

// -----------------------------------------------------------------------------------------------//

    private fun initializeViews() {
        setupHandler()
        setupWindow()
        setupViews()
        setupSearchBar()
        setupPlayerCard()
        setupButtons()
        setupSeekBar()
        setupBackPressHandler()
    }

    /* Initialize Views METHODS */

    private fun setupHandler() {
        handler = Handler(Looper.getMainLooper())
    }

    private fun setupWindow() {     /* Initialize Views METHODS ^ */
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
    }

    private fun setupViews() {      /* Initialize Views METHODS ^ */
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.navigation_view)
        compactPlayerCard = findViewById(R.id.compactPlayerCard)
        expandedPlayerCard = findViewById(R.id.expandedPlayerCard)
        trackCount = findViewById(R.id.trackCount)
        searchBar = findViewById(R.id.searchBar)
        lottieAnimationView = expandedPlayerCard.findViewById(R.id.lottieAnimation)
        overlayContainer = findViewById(R.id.overlayContainer)
        songTitleTextView = expandedPlayerCard.findViewById(R.id.expanded_song_title)
        artistNameTextView = expandedPlayerCard.findViewById(R.id.expanded_artistName)
        albumCoverImageView = expandedPlayerCard.findViewById(R.id.expanded_albumCover)
        albumCoverImageView2 = compactPlayerCard.findViewById(R.id.compact_albumCover)
        collapseButton = expandedPlayerCard.findViewById(R.id.collapseButton)
        playPauseButton = expandedPlayerCard.findViewById(R.id.expanded_playPauseButton)
        nextBtn = expandedPlayerCard.findViewById(R.id.expanded_nextBtn)
        previousBtn = expandedPlayerCard.findViewById(R.id.expanded_previousBtn)
        cardPlayPauseLL = compactPlayerCard.findViewById(R.id.cardPlayPause)
        seekBar = expandedPlayerCard.findViewById(R.id.songProgressBar)
        currentTimeTextView = expandedPlayerCard.findViewById(R.id.elapsedTime)
        totalTimeTextView = expandedPlayerCard.findViewById(R.id.totalTime)
    }

    private fun loadAndApplyTheme() {       /* Initialize Views METHODS ^ */
        isDarkMode = loadThemePreference()
        AppCompatDelegate.setDefaultNightMode(
            if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    private fun setupSearchBar() {      /* Initialize Views METHODS ^ */
        searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                (findViewById<RecyclerView>(R.id.SongsRecyclerView).adapter as SongAdapter).filterSongs(
                    query
                )
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun setupPlayerCard() {         /* Initialize Views METHODS ^ */
        compactPlayerCard.setOnClickListener { expandPlayerCard() }
        collapseButton.setOnClickListener { collapsePlayerCard() }
    }

    private fun setupButtons() {            /* Initialize Views METHODS ^ */
        cardPlayPauseLL.setOnClickListener { onPlayPauseToggle() }
        findViewById<ImageView>(R.id.compact_nextBtn).setOnClickListener { playNextSong() }
        findViewById<ImageView>(R.id.compact_previousBtn).setOnClickListener { playPreviousSong() }
    }

    private fun setupSeekBar() {            /* Initialize Views METHODS ^ */
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress)
                    updateCurrentTimeTextView(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun setupBackPressHandler() {           /* Initialize Views METHODS ^ */
        onBackPressedDispatcher.addCallback(this) {
            if (searchBar.isFocused) {
                searchBar.clearFocus()
                searchBar.text.clear()
                hideKeyboard(searchBar)
            } else {
                Toast.makeText(this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT)
                    .show()
                finish()
            }
        }
    }

// -----------------------------------------------------------------------------------------------//
    /* Setup Activity METHODS ^ */

    private fun drawerInitialization() {         /* Setup Activity METHODS ^ */
        drawerLayout = findViewById(R.id.drawer_layout)
        findViewById<ImageButton>(R.id.menu_button).setOnClickListener {
            vibrateDevice(this)
            drawerLayout.openDrawer(GravityCompat.START)
        }

        setupDrawer()

        val headerView = navView.inflateHeaderView(R.layout.theme_toggle_layout)
        val switch = headerView.findViewById<MaterialSwitch>(R.id.theme_switch)

        // Set switch state based on current theme
        switch.isChecked = isDarkThemeOn()

        // Theme switch listener with smooth fade animations
        switch.setOnCheckedChangeListener { _, isChecked ->
            applyFadeAnimations(isChecked)
        }
    }

    private fun setupPermissions() {         /* Setup Activity METHODS ^ */

        val permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val allPermissionsGranted = permissions.all { it.value }
                if (allPermissionsGranted) {
                    setupRecyclerView()
                } else {
                    showPermissionDeniedDialog()
                }
            }
        val requiredPermissions = permissions
        if (requiredPermissions.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            setupRecyclerView()
        } else {
            permissionLauncher.launch(requiredPermissions)
        }
    }

    private fun showPermissionDeniedDialog() {

        Toast.makeText(this, "Please Allow Permissions", Toast.LENGTH_SHORT).show()

        AlertDialog.Builder(this).setTitle("Permission Required")
            .setMessage("This app requires permission to access audio files. Please enable the permission in App Settings.")
            .setPositiveButton("Go to Settings") { dialog, _ ->
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", packageName, null)
                }
                startActivity(intent)
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }.setCancelable(false)
            .show()
    }

    private fun setupPlayerControls() {      /* Setup Activity METHODS ^ */
        playPauseButton.setOnClickListener { onPlayPauseToggle() }
        nextBtn.setOnClickListener { playNextSong() }
        previousBtn.setOnClickListener { playPreviousSong() }

    }

    private fun onBackPressFromMainActivity() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Check if the player card is expanded
                if (isPlayerCardExpanded) {
                    collapsePlayerCard()
                } else if (backPressedOnce) {
                    finishAffinity()
                } else {
                    backPressedOnce = true
                    Toast.makeText(
                        this@MainActivity, "Press back again to exit", Toast.LENGTH_SHORT
                    ).show()

                    // Reset the flag after 2 seconds
                    window.decorView.postDelayed({
                        backPressedOnce = false
                    }, 1500)
                }
            }
        })
    }

    private fun setupCardHeightListener() {     /* Setup Activity METHODS ^ */
        expandedPlayerCard.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                expandedPlayerCard.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (isPlayerCardExpanded) {
                    expandPlayerCard()
                }
            }
        })
    }

    private fun checkMediaPlayerInitialization() {      /* Setup Activity METHODS ^ */
        mediaSession = MediaSessionCompat(this, "MediaSessionTag").apply {
            isActive = true
        }
        if (!isMediaPlayerInitialized) {
            mediaPlayer = MediaPlayer().apply {
                setOnCompletionListener { playNextSong() }
                isMediaPlayerInitialized = true
            }
        }
    }


//------------------------------------------------------------------------------------------------//

    // Theme And Animations and Vibration Code

    private fun applyFadeAnimations(isDark: Boolean) {
        val rootView = findViewById<ViewGroup>(android.R.id.content)

        // Create and start fade-out animation
        val fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out_two)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Change the theme dynamically
                AppCompatDelegate.setDefaultNightMode(
                    if (isDark) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )

                // Save the theme preference
                saveThemePreference(isDark)

                // Recreate the activity to apply the theme change immediately
                Handler(Looper.getMainLooper()).postDelayed({
                    // Fade-in animation
                    val fadeIn = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in_two)
                    rootView.startAnimation(fadeIn)
                }, fadeOut.duration) // Delay to match fade-out duration
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Apply fade-out animation to the root view
        rootView.startAnimation(fadeOut)
    }

    private fun isDarkThemeOn(): Boolean {
        return (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupDrawer() {
        navView = findViewById(R.id.navigation_view)

        // Set up ActionBarDrawerToggle for opening/closing drawer
        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Handle navigation item clicks


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            // Toggle the theme mode
            isDarkMode = !isDarkMode

            // Apply the new theme
            AppCompatDelegate.setDefaultNightMode(
                if (isDarkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )

            // Save the theme preference
            saveThemePreference(isDarkMode)

            // Optionally, restart the activity to apply the theme change immediately
            recreate()

            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        with(prefs.edit()) {
            putBoolean("dark_mode", isDarkMode)
            apply()
        }
    }

    private fun loadThemePreference(): Boolean {
        val prefs = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        return prefs.getBoolean("dark_mode", false) // Default to false (light mode)
    }

    private fun startBounceAnimation() {

        bounceAnimator = ValueAnimator.ofFloat(0.99f, 1.01f).apply {
            duration = (100 + Random().nextInt(50)).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val randomDelay = (200 + Random().nextInt(900)).toLong()
                    albumCoverImageView.postDelayed({
                        bounceAnimator?.start()
                    }, randomDelay)
                }
            })

            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                albumCoverImageView.scaleX = scale
                albumCoverImageView.scaleY = scale
            }
            start()

        }

        bounceAnimator2 = ValueAnimator.ofFloat(0.95f, 1.05f).apply {
            duration = (100 + Random().nextInt(50)).toLong()
            interpolator = AccelerateDecelerateInterpolator()
            repeatMode = ValueAnimator.REVERSE
            repeatCount = 1

            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    val randomDelay = (200 + Random().nextInt(900)).toLong()
                    albumCoverImageView2.postDelayed({
                        bounceAnimator2?.start()
                    }, randomDelay)
                }
            })

            addUpdateListener { animator ->
                val scale = animator.animatedValue as Float
                albumCoverImageView2.scaleX = scale
                albumCoverImageView2.scaleY = scale
            }
            start()
        }
    }

    private fun stopBounceAnimation() {

        bounceAnimator?.apply {
            cancel()
            bounceAnimator = null
        }
        albumCoverImageView.apply {
            scaleX = 1f
            scaleY = 1f
            clearAnimation()
        }

        bounceAnimator2?.apply {
            cancel()
            bounceAnimator2 = null
        }
        albumCoverImageView2.apply {
            scaleX = 1f
            scaleY = 1f
            clearAnimation()
        }
    }

    private fun applyFadeAnimation(imageView: ImageView, newIconResId: Int) {
        val fadeOut = AnimationUtils.loadAnimation(imageView.context, R.anim.fade_out)
        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                imageView.setImageResource(newIconResId)
                val fadeIn = AnimationUtils.loadAnimation(imageView.context, R.anim.fade_in)
                imageView.startAnimation(fadeIn)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        imageView.startAnimation(fadeOut)
    }

    private fun animateHeight(
        view: View, fromHeight: Int, toHeight: Int, onEnd: (() -> Unit)? = null
    ) {
        ValueAnimator.ofInt(fromHeight, toHeight).apply {
            addUpdateListener { animator ->
                val value = animator.animatedValue as Int
                view.layoutParams.height = value
                view.requestLayout()
            }
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    onEnd?.invoke()
                }
            })
            start()
        }
    }

    @Suppress("DEPRECATION")
    private fun vibrateDevice(context: Context) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // For Android S (API 31) and above, use VibratorManager
            val vibratorManager =
                context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            // For APIs 29 to 30, use the standard Vibrator service
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        val amplitude = 50 // Adjust this value for softer vibration
        vibrator.vibrate(VibrationEffect.createOneShot(50, amplitude)) // 200ms vibration
    }

    private fun setLottieAnimationPlaying(isPlaying: Boolean) {
        if (isPlaying) {
            lottieAnimationView.playAnimation()
            lottieAnimationView.visibility = View.VISIBLE
        } else {
            lottieAnimationView.pauseAnimation()
            lottieAnimationView.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupPlayerCardSwipeGestures() {
        // Gesture detector for the expanded player card (swipe down to collapse)
        val expandedCardGestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 == null) return false

                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x

                    // Check if it's mostly a vertical swipe
                    if (abs(diffY) > abs(diffX)) {
                        if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                // Swiped down -> collapse card
                                collapsePlayerCard()
                            }
                            return true
                        }
                    }
                    return false
                }
            })

        expandedPlayerCard.setOnTouchListener { _, event ->
            expandedCardGestureDetector.onTouchEvent(event)
            true
        }

        // Gesture detector for the compact player card (swipe up to expand)
        val compactCardGestureDetector =
            GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
                private val SWIPE_THRESHOLD = 100
                private val SWIPE_VELOCITY_THRESHOLD = 100

                override fun onFling(
                    e1: MotionEvent?, e2: MotionEvent, velocityX: Float, velocityY: Float
                ): Boolean {
                    if (e1 == null) return false

                    val diffY = e2.y - e1.y
                    val diffX = e2.x - e1.x

                    // Check if it's mostly a vertical swipe
                    if (abs(diffY) > abs(diffX)) {
                        if (abs(diffY) > SWIPE_THRESHOLD && abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY < 0) {
                                // Swiped up -> expand card
                                expandPlayerCard()
                            }
                            return true
                        }
                    }
                    return false
                }
            })

        // Set OnTouchListener to detect swipe gestures
        compactPlayerCard.setOnTouchListener { _, event ->
            if (compactCardGestureDetector.onTouchEvent(event)) {
                // If gesture was detected, consume the event
                true
            } else {
                // Let click events be processed if no gesture detected
                false
            }
        }

        // Set OnClickListener to detect taps and expand the card on click
        compactPlayerCard.setOnClickListener {
            expandPlayerCard()
        }
    }

//------------------------------------------------------------------------------------------------//

    /* Extra Song METHODS */

    private fun hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun formatTime(milliseconds: Int): String {
        val minutes = (milliseconds / 1000) / 60
        val seconds = (milliseconds / 1000) % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }

    private fun updateCurrentTimeTextView(currentPosition: Int) {
        currentTimeTextView.text = formatTime(currentPosition)
    }

    private fun isTouchInsideView(view: View, event: MotionEvent): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        val x = event.rawX
        val y = event.rawY
        return x >= location[0] && x <= location[0] + view.width && y >= location[1] && y <= location[1] + view.height
    }

//------------------------------------------------------------------------------------------------//

// Recycler View Setup

    private fun setupRecyclerView() {

        initSongsList()

        val recyclerView = findViewById<RecyclerView>(R.id.SongsRecyclerView)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = SongAdapter(songs, onItemClick = ::onSongItemClick)

            OverScrollDecoratorHelper.setUpOverScroll(
                this, OverScrollDecoratorHelper.ORIENTATION_VERTICAL
            )
        }
    }

    private fun initSongsList() {

        originalSongs = getSongsFromDevice(this)
        songs = originalSongs.toMutableList()

        "${songs.size} Tracks".also { trackCount.text = it }
    }

    private fun onSongItemClick(song: Song, position: Int) {
        currentSongIndex = position
        (findViewById<RecyclerView>(R.id.SongsRecyclerView).adapter as SongAdapter).updateSelectedIndex(
            currentSongIndex
        )
        showPlayerCard(song)
        vibrateDevice(this)
    }

//------------------------------------------------------------------------------------------------//

    // Songs, Player Cards And Media Player Logic

    private fun getSongsFromDevice(context: Context): List<Song> {
        val songList = mutableListOf<Song>()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.MIME_TYPE
        )

        val selection =
            "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND (" + "${MediaStore.Audio.Media.MIME_TYPE} = 'audio/mpeg' OR " + "${MediaStore.Audio.Media.MIME_TYPE} = 'audio/x-wav' OR " + "${MediaStore.Audio.Media.MIME_TYPE} = 'audio/flac' OR " + "${MediaStore.Audio.Media.MIME_TYPE} = 'audio/ogg') AND " + "${MediaStore.Audio.Media.MIME_TYPE} != 'audio/opus' AND " + "${MediaStore.Audio.Media.TITLE} NOT LIKE 'AUD-%-WA%'"

        val cursor = context.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, null
        )

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumIdColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationColumn = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val title = it.getString(titleColumn)
                val artist = it.getString(artistColumn)
                val albumId = it.getLong(albumIdColumn)
                val duration = it.getLong(durationColumn)


                // Exclude songs less than 59 seconds
                if (duration >= 59000) {  // 59000 milliseconds = 59 seconds
                    val albumCover: Bitmap? = try {
                        val albumUri = ContentUris.withAppendedId(
                            MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId

                        )

                        val bitmap =
                            context.contentResolver.loadThumbnail(albumUri, Size(640, 480), null)
                        bitmap

                    } catch (e: Exception) {
                        BitmapFactory.decodeResource(context.resources, R.drawable.logo)
                    }
                    songList.add(Song(id, title, artist, duration, albumCover))
                }
            }
        }

        return songList
    }

    private fun onPlayPauseToggle() {

        vibrateDevice(this)
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            updatePlayPauseDrawables(R.drawable.play_hollow)
            stopBounceAnimation()
            setLottieAnimationPlaying(mediaPlayer.isPlaying)

        } else {
            mediaPlayer.start()
            updatePlayPauseDrawables(R.drawable.pause)
            startBounceAnimation()
            setLottieAnimationPlaying(mediaPlayer.isPlaying)

        }
    }

    private fun updatePlayPauseDrawables(drawableResId: Int) {
        applyFadeAnimation(playPauseButton, drawableResId)
        val compactPlayPauseButton =
            compactPlayerCard.findViewById<ImageView>(R.id.compact_playPauseButton)
        applyFadeAnimation(compactPlayPauseButton, drawableResId)
    }

    private fun showPlayerCard(song: Song) {
        // Update text views only if the song has changed

        songTitleTextView.text = song.title
        artistNameTextView.text = song.artist


        // Cache Glide requests and reuse them
        val glideRequest = Glide.with(this).load(song.albumCover).apply(
            RequestOptions.bitmapTransform(
                MultiTransformation(BlurTransformation(22, 3))
            )
        ).placeholder(R.drawable.error_bkg).error(R.drawable.black)
            .transition(DrawableTransitionOptions.withCrossFade())
            .override(10, 10) // Adjust to appropriate resolution for background blurring

        // Set background images only if necessary
        val compactBackgroundImageView =
            compactPlayerCard.findViewById<ImageView>(R.id.bkgImgCompact)
        val expandBackgroundImageView =
            expandedPlayerCard.findViewById<ImageView>(R.id.bkgImgExpand)
        val mainActivityImgBkg = findViewById<ImageView>(R.id.bkgImgExpand)

        glideRequest.into(expandBackgroundImageView)
        glideRequest.into(compactBackgroundImageView)
        glideRequest.into(mainActivityImgBkg)

        // Load album cover images, reuse Glide request where possible
        val compactAlbumCoverImageView =
            compactPlayerCard.findViewById<ImageView>(R.id.compact_albumCover)
        Glide.with(this).load(song.albumCover).placeholder(R.drawable.black).error(R.drawable.logo)
            .override(70, 70) // Adjust for small compact image
            .into(compactAlbumCoverImageView)

        Glide.with(this).load(song.albumCover).placeholder(R.drawable.black).error(R.drawable.logo)
            .override(250, 250) // Adjust for larger expanded image
            .into(albumCoverImageView)

        // Update compact player text views only if they have changed
        val compactSongTitleTextView =
            compactPlayerCard.findViewById<TextView>(R.id.compact_song_title)
        val compactArtistNameTextView =
            compactPlayerCard.findViewById<TextView>(R.id.compact_artistName)

        if (compactSongTitleTextView.text != song.title) {
            compactSongTitleTextView.text = song.title
        }
        if (compactArtistNameTextView.text != song.artist) {
            compactArtistNameTextView.text = song.artist
        }

        // Handle visibility and animations for player cards
        if (isPlayerCardExpanded) {
            if (expandedPlayerCard.visibility != View.VISIBLE) {
                expandedPlayerCard.alpha = 0f
                expandedPlayerCard.visibility = View.VISIBLE
                expandedPlayerCard.animate().alpha(1f).setDuration(300).start()
            }
            if (compactPlayerCard.visibility == View.VISIBLE) {
                compactPlayerCard.animate().alpha(0f).setDuration(300).withEndAction {
                    compactPlayerCard.visibility = View.GONE
                }.start()
            }
        } else {
            if (compactPlayerCard.visibility != View.VISIBLE) {
                compactPlayerCard.alpha = 0f
                compactPlayerCard.visibility = View.VISIBLE
                compactPlayerCard.animate().alpha(1f).setDuration(300).start()
            }
            if (expandedPlayerCard.visibility == View.VISIBLE) {
                expandedPlayerCard.animate().alpha(0f).setDuration(300).withEndAction {
                    expandedPlayerCard.visibility = View.GONE
                }.start()
            }
        }

        playSong(song)
        showNotification(song)
    }

    private fun playSong(song: Song) {
        val songUri =
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, song.id)
        mediaPlayer.apply {
            reset()
            setDataSource(this@MainActivity, songUri)
            prepare()
            start()
        }
        if (mediaPlayer.isPlaying) {
            startBounceAnimation()
        }
        updateSeekBarAndTime()
        updatePlayPauseButtonDrawable(R.drawable.pause)
        searchBar.text.clear()
    }

    private fun updateSeekBarAndTime() {
        seekBar.apply {
            max = mediaPlayer.duration
            progress = mediaPlayer.currentPosition
        }
        totalTimeTextView.text = formatTime(mediaPlayer.duration)
        startSeekBarUpdate()
    }

    private fun updatePlayPauseButtonDrawable(drawableResId: Int) {
        applyFadeAnimation(playPauseButton, drawableResId)
        val compactPlayPauseButton =
            compactPlayerCard.findViewById<ImageView>(R.id.compact_playPauseButton)
        applyFadeAnimation(compactPlayPauseButton, drawableResId)
    }

    private fun startSeekBarUpdate() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isMediaPlayerInitialized && mediaPlayer.isPlaying) {
                    seekBar.progress = mediaPlayer.currentPosition
                    updateCurrentTimeTextView(mediaPlayer.currentPosition)
                }
                if (isMediaPlayerInitialized) {
                    handler.postDelayed(this, 1000) // Update every second
                }
            }
        }, 0)
    }

    private fun playNextSong() {
        vibrateDevice(this)
        if (songs.isNotEmpty()) {
            currentSongIndex = (currentSongIndex + 1) % songs.size
            val song = songs[currentSongIndex]
            showPlayerCard(song)
            playSong(song)
            updateRecyclerViewSelection()
            lottieAnimationView.playAnimation()
            lottieAnimationView.visibility = View.VISIBLE

            if (mediaPlayer.isPlaying) {
                lottieAnimationView.playAnimation()
            } else {
                lottieAnimationView.pauseAnimation()
            }
        }
    }

    private fun playPreviousSong() {
        vibrateDevice(this)

        if (songs.isNotEmpty()) {
            currentSongIndex = if (currentSongIndex > 0) {
                currentSongIndex - 1
            } else {
                songs.size - 1
            }
            val song = songs[currentSongIndex]
            showPlayerCard(song)
            playSong(song)
            updateRecyclerViewSelection()
            lottieAnimationView.playAnimation()
            lottieAnimationView.visibility = View.VISIBLE
        }
    }

    private fun updateRecyclerViewSelection() {
        val recyclerView = findViewById<RecyclerView>(R.id.SongsRecyclerView)
        val adapter = recyclerView.adapter as? SongAdapter
        val layoutManager = recyclerView.layoutManager as? LinearLayoutManager

        adapter?.let {
            it.updateSelectedIndex(currentSongIndex)

            layoutManager?.let { lm ->
                // Check if the item is not already in view
                val firstVisibleItemPosition = lm.findFirstVisibleItemPosition()
                val lastVisibleItemPosition = lm.findLastVisibleItemPosition()

                if (currentSongIndex < firstVisibleItemPosition || currentSongIndex > lastVisibleItemPosition - 2) {
                    // Create a custom SmoothScroller to position item at the top
                    val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                        override fun getVerticalSnapPreference(): Int {
                            return SNAP_TO_START
                        }
                    }
                    smoothScroller.targetPosition = currentSongIndex
                    lm.startSmoothScroll(smoothScroller)
                }
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun expandPlayerCard() {
        vibrateDevice(this)

        expandedPlayerCard.visibility = View.VISIBLE
        expandedPlayerCard.alpha = 0f

        expandedPlayerCard.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val targetHeight = expandedPlayerCard.measuredHeight
        expandedPlayerCard.layoutParams.height = 0
        expandedPlayerCard.translationY = targetHeight.toFloat()
        expandedPlayerCard.requestLayout()

        animateHeight(expandedPlayerCard, 0, targetHeight) {
            expandedPlayerCard.layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
            expandedPlayerCard.requestLayout()
        }

        expandedPlayerCard.animate().translationY(0f).alpha(1f).setDuration(300).start()
        compactPlayerCard.visibility = View.GONE
        isPlayerCardExpanded = true
        overlayContainer.visibility = View.VISIBLE

        overlayContainer.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (!isTouchInsideView(expandedPlayerCard, event)) {
                    collapsePlayerCard()
                    overlayContainer.performClick()
                    return@setOnTouchListener true
                }
            }

            false
        }
    }

    private fun collapsePlayerCard() {


        expandedPlayerCard.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
        val initialHeight = expandedPlayerCard.measuredHeight

        animateHeight(expandedPlayerCard, initialHeight, 0) {
            expandedPlayerCard.visibility = View.GONE
            compactPlayerCard.visibility = View.VISIBLE

            compactPlayerCard.alpha = 0f
            compactPlayerCard.translationY = compactPlayerCard.height.toFloat()
            compactPlayerCard.animate().translationY(1f).alpha(1f).setDuration(300).start()
        }

        expandedPlayerCard.animate().translationY(initialHeight.toFloat()).alpha(0f)
            .setDuration(300).start()
        isPlayerCardExpanded = false
        overlayContainer.visibility = View.GONE
    }

//------------------------------------------------------------------------------------------------//

    // Notification Code
    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "song_notification_channel"
    }

    private fun createNotificationChannel() {
        val channelName = "Song Notification Channel"
        val channel =
            NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_LOW)
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun showNotification(song: Song) {
        createNotificationChannel() // Ensure the channel is created

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationLayout =
            RemoteViews(packageName, R.layout.notification_custom_layout).apply {
                setTextViewText(R.id.notification_title, song.title)
                setTextViewText(R.id.notification_artist, song.artist)
                if (song.albumCover != null) {
                    setImageViewBitmap(R.id.notification_image, song.albumCover)
                } else {
                    setImageViewResource(R.id.notification_image, R.drawable.logo)
                }
            }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder =
            NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.noti_logo)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setStyle(NotificationCompat.DecoratedCustomViewStyle())
                .setCustomContentView(notificationLayout).setAutoCancel(false)
                .setContentIntent(pendingIntent).setFullScreenIntent(pendingIntent, true)

        notificationManager.notify(NOTIFICATION_ID, notificationBuilder.build())
    }

//------------------------------------------------------------------------------------------------//

    // override Methods

    override fun onDestroy() {
        super.onDestroy()

        if (isMediaPlayerInitialized) {
            mediaPlayer.release()
            isMediaPlayerInitialized = false
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        val view = currentFocus
        if (event.action == MotionEvent.ACTION_DOWN && view is EditText) {
            val outRect = Rect()
            view.getGlobalVisibleRect(outRect)

            if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                view.clearFocus()
                hideKeyboard(view)
            }
        }
        return super.dispatchTouchEvent(event)
    }

//------------------------------------------------------------------------------------------------//

}