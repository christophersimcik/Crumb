package com.example.crumb.Activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.example.crumb.UI.ButtonNew
import com.example.crumb.Fragments.IntervalFragment
import com.example.crumb.Fragments.PlayFragment
import com.example.crumb.Fragments.SavedRecipeFragment
import com.example.crumb.Fragments.ScheduleFragment
import com.example.crumb.R
import com.example.crumb.ViewModels.PlayViewModel
import com.example.crumb.ViewModels.SharedViewModel
import net.danlew.android.joda.JodaTimeAndroid

const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity(), PlayViewModel.ActiveAlarms {

    private val fragmentManager: FragmentManager by lazy { supportFragmentManager }
    private lateinit var currentFragment: Fragment
    private var previousRootYValue= 0.0f
    private lateinit var view: View
    private val globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener{
        toggleAddButton(view.height.toFloat() < previousRootYValue)
        previousRootYValue = view.height.toFloat()
    }
    private val addButton: ButtonNew by lazy { findViewById<ButtonNew>(R.id.add_button) }
    private val duplicateButton: ImageButton by lazy { findViewById<ImageButton>(R.id.duplicate_button) }
    private val shareButton: ImageButton by lazy { findViewById<ImageButton>(R.id.share_button) }
    private val displayNotesButton: ImageButton by lazy { findViewById<ImageButton>(R.id.notes_button) }
    private val editButton: ImageButton by lazy { findViewById<ImageButton>(R.id.edit_button) }
    private val headerTextView: TextView by lazy { findViewById<TextView>(R.id.heading) }
    private val navHostFragment: NavHostFragment by lazy {
        fragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
    }
    val sharedViewModel by lazy { ViewModelProviders.of(this).get(SharedViewModel::class.java) }
    private val scrollObserver: Observer<Fragment> by lazy {
        Observer<Fragment> { fragment ->
            when (fragment) {
                is ScheduleFragment -> {
                    fragment.registerScrollObserver(addButton)
                    currentFragment = fragment
                }
                is IntervalFragment -> {
                    fragment.registerScrollObserver(addButton)
                    currentFragment = fragment
                }
                is SavedRecipeFragment -> {
                    fragment.registerScrollObserver(addButton)
                    currentFragment = fragment
                }
                is PlayFragment -> {
                    fragment.viewModel.registerActiveAlarmsWatcher(this)
                    currentFragment = fragment
                }
            }
        }
    }

    private val modeObservable: Observer<Int> by lazy {
        Observer { mode: Int ->
            addButton.mode = mode

            when (mode) {
                ButtonNew.RECIPES -> {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                    shareButton.visibility = View.INVISIBLE
                    displayNotesButton.visibility = View.INVISIBLE
                    duplicateButton.visibility = View.INVISIBLE
                    editButton.visibility = View.INVISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.RECIPES
                    addButton.toCircle()
                }
                ButtonNew.STEPS -> {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                    shareButton.visibility = View.INVISIBLE
                    displayNotesButton.visibility = View.INVISIBLE
                    duplicateButton.visibility = View.INVISIBLE
                    editButton.visibility = View.INVISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.STEPS
                    addButton.toSquare()

                }
                ButtonNew.DETAIL -> {
                    addOnGlobalLayoutChangedListener()
                    shareButton.visibility = View.VISIBLE
                    displayNotesButton.visibility = View.VISIBLE
                    duplicateButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.copy_img_color_selector
                        )
                    )
                    duplicateButton.visibility = View.VISIBLE
                    editButton.visibility = View.VISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.DETAIL
                    addButton.toRect()
                }
                ButtonNew.PLAY -> {
                    view.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
                    shareButton.visibility = View.INVISIBLE
                    displayNotesButton.visibility = View.VISIBLE
                    duplicateButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.cancel_img_selector
                        )
                    )
                    duplicateButton.visibility = View.VISIBLE
                    editButton.visibility = View.INVISIBLE
                    addButton.visibility = View.INVISIBLE
                    addButton.mode = ButtonNew.PLAY
                }
            }
        }
    }

    private val headerObserver: Observer<String> by lazy {
        Observer { newHeader: String ->
            headerTextView.text = newHeader
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        view = findViewById(android.R.id.content)
        duplicateButton.setOnClickListener {
            sharedViewModel.duplicateClick(currentFragment, navHostFragment.navController)
            Log.i(TAG, "duplicate pressed")
        }
        displayNotesButton.setOnClickListener {
            sharedViewModel.displayNotesClick(currentFragment)
        }
        editButton.setOnClickListener {
            sharedViewModel.editClick(currentFragment, navHostFragment.navController)
        }
        shareButton.setOnClickListener {
            sharedViewModel.shareClick(currentFragment)
        }
        addButton.setOnClickListener {
            addButton.imageRotation = 0f
            if (addButton.mode == ButtonNew.RECIPES) {
                addButton.animatePlusSymbol()
            }
            if (addButton.mode == ButtonNew.STEPS) {
                addButton.animatePlusSymbol()
            }
            sharedViewModel.buttonClick(currentFragment, navHostFragment.navController)
        }
        val fontRequest = FontRequest(
            "com.google.android.gms.fonts",
            "com.google.android.gms",
            "Noto Color Emoji Compat",
            R.array.com_google_android_gms_fonts_certs
        )
        val config = FontRequestEmojiCompatConfig(this, fontRequest)
        EmojiCompat.init(config)
        JodaTimeAndroid.init(this)
        sharedViewModel.header.observe(this, headerObserver)
        sharedViewModel.mode.observe(this, modeObservable)
        //sharedViewModel.checkIfLaunchedByAlarm(navHostFragment.navController, intent)
        launchAlarmIfActive(intent, getSharedPreferences(SharedViewModel.SHARED_PREFERENCES, 0))
        sharedViewModel.scrollWatcher.observe(this, scrollObserver)
        getSharedPreferences(
            SharedViewModel.SHARED_PREFERENCES, 0
        )
    }

    private fun launchAlarmIfActive(intent: Intent, sharedPrefs: SharedPreferences) {
        if (sharedViewModel.wasLaunchedByAlarm(intent) || sharedViewModel.hasAnActiveAlarm(
                sharedPrefs
            )
        ) {
            val bundle = sharedViewModel.createAlarmBundle(intent, sharedPrefs)
            sharedViewModel.navigateToAlarm(navHostFragment.navController, bundle)
        }
    }

    override fun onBackPressed() {
        addButton.playing = false
        if (sharedViewModel.checkCanBackPress()) {
            super.onBackPressed()
        } else {
            finish()
        }
    }

    override fun hasActiveAlarms() {
        sharedViewModel.setCheckCanBackPress(false)
        duplicateButton.visibility = View.VISIBLE
    }

    override fun noActiveAlarms() {
        sharedViewModel.setCheckCanBackPress(true)
        duplicateButton.visibility = View.INVISIBLE
    }

    private fun addOnGlobalLayoutChangedListener() {
        view.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    private fun toggleAddButton(keyboardIsVisible: Boolean) {
        Log.d(TAG, "BOOLEAN = $keyboardIsVisible")
        when(keyboardIsVisible ){
            true -> addButton.visibility = View.INVISIBLE
            false ->{
                addButton.visibility = View.VISIBLE
            }
        }
    }

}
