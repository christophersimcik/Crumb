package com.example.tightboules

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.provider.FontRequest
import androidx.emoji.text.EmojiCompat
import androidx.emoji.text.FontRequestEmojiCompatConfig
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import net.danlew.android.joda.JodaTimeAndroid

class MainActivity : AppCompatActivity(), OverrideCheckBoxOnClick, PlayViewModel.ActiveAlarms {

    val fragmentManager: FragmentManager by lazy { supportFragmentManager }
    val addButton by lazy { findViewById<ButtonNew>(R.id.add_button) }
    val topLeftButton by lazy { findViewById<ImageButton>(R.id.duplicate_button) }
    val topRightButton: ImageButton by lazy { findViewById<ImageButton>(R.id.notes_button) }
    val topLeftOfRoghtButton: ImageButton by lazy { findViewById<ImageButton>(R.id.edit_button) }
    val headerTextView: TextView by lazy { findViewById<TextView>(R.id.heading) }
    val mainLayout by lazy { findViewById<MainRelativeLayout>(R.id.main_container) }
    val navHostFragment: NavHostFragment by lazy { fragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment }
    val sharedViewModel by lazy { ViewModelProviders.of(this).get(SharedViewModel::class.java) }

    val scrollObserver: Observer<Fragment> by lazy {
        Observer<Fragment> { fragment ->
            when (fragment) {
                is ScheduleFragment -> fragment.registerScrollObserver(addButton)
                is IntervalFragment -> fragment.registerScrollObserver(addButton)
                is SavedRecipeFragment -> fragment.registerScrollObserver(addButton)
                is PlayFragment -> fragment.viewModel.registerActiveAlarmsWatcher(this)
            }
        }
    }

    val modeObservable: Observer<Int> by lazy {
        Observer { mode: Int ->
            addButton.mode = mode

            when (mode) {
                ButtonNew.RECIPES -> {
                    topRightButton.visibility = View.INVISIBLE
                    topLeftButton.visibility = View.INVISIBLE
                    topLeftOfRoghtButton.visibility = View.INVISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.RECIPES
                    addButton.toCircle()
                }
                ButtonNew.STEPS -> {
                    topRightButton.visibility = View.INVISIBLE
                    topLeftButton.visibility = View.INVISIBLE
                    topLeftOfRoghtButton.visibility = View.INVISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.STEPS
                    addButton.toSquare()
                }
                ButtonNew.DETAIL -> {
                    topRightButton.visibility = View.VISIBLE
                    topLeftButton.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.copy_img_color_selector,
                            null
                        )
                    )
                    topLeftButton.visibility = View.VISIBLE
                    topLeftOfRoghtButton.visibility = View.VISIBLE
                    addButton.visibility = View.VISIBLE
                    addButton.mode = ButtonNew.DETAIL
                    addButton.toRect()
                }
                ButtonNew.PLAY -> {
                    topRightButton.visibility = View.VISIBLE
                    topLeftButton.setImageDrawable(
                        resources.getDrawable(
                            R.drawable.cancel_img_selector,
                            null
                        )
                    )
                    topLeftButton.visibility = View.VISIBLE
                    topLeftOfRoghtButton.visibility = View.INVISIBLE
                    addButton.visibility = View.INVISIBLE
                    addButton.mode = ButtonNew.PLAY
                }
            }
        }
    }
    val headerObserver: Observer<String> by lazy {
        Observer { newHeader: String ->
            headerTextView.text = newHeader
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        topLeftButton.setOnClickListener {
            sharedViewModel.topLeftButtonClick(navHostFragment.navController)
        }
        topRightButton.setOnClickListener {
            sharedViewModel.topRightButtonClick()
        }
        topLeftOfRoghtButton.setOnClickListener {
            sharedViewModel.topLeftOfRightButtonClick(navHostFragment.navController)
        }
        addButton.setOnClickListener {
            addButton.imageRotation = 0f
            if (addButton.mode.equals(ButtonNew.RECIPES)) {
                addButton.animatePlusSymbol()
            }
            if (addButton.mode.equals(ButtonNew.STEPS)) {
                addButton.animatePlusSymbol()
            }
            sharedViewModel.buttonClick(navHostFragment.navController, this.applicationContext)
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
        sharedViewModel.checkIfLaunchedByAlarm(navHostFragment.navController, intent)
        sharedViewModel.scrollWatcher.observe(this, scrollObserver)

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
        topLeftButton.visibility = View.VISIBLE
    }

    override fun noActiveAlarms() {
        sharedViewModel.setCheckCanBackPress(true)
        topLeftButton.visibility = View.INVISIBLE
    }

}
