package com.example.crumb.ViewModels

import android.app.Application
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import com.example.crumb.*
import com.example.crumb.Dao.IntervalDao
import com.example.crumb.Dao.ScheduleDao
import com.example.crumb.Database.DatabaseScheduler
import com.example.crumb.Dialogs.RecipeDialog
import com.example.crumb.Fragments.FragmentInterfaces.FragmentCallback
import com.example.crumb.Fragments.IntervalFragment
import com.example.crumb.Fragments.PlayFragment
import com.example.crumb.Fragments.SavedRecipeFragment
import com.example.crumb.Fragments.ScheduleFragment
import com.example.crumb.Helpers.AlarmHelper
import com.example.crumb.UI.ButtonNew
import kotlinx.coroutines.launch
import org.joda.time.DateTime

const val TAG = "SHARED_VIEW_MODEL"

class SharedViewModel(application: Application) : AndroidViewModel(application),
    FragmentCallback {

    companion object {
        const val SHARED_PREFERENCES = "shared_preferences"
    }

    val header = MutableLiveData<String>()
    val mode = MutableLiveData<Int>()
    private val database = DatabaseScheduler.getInstance(application)
    private val intervalDao: IntervalDao? = database?.getIntervalDao()
    private val scheduleDao: ScheduleDao? = database?.getScheduleDao()
    val scrollWatcher = MutableLiveData<Fragment>()
    private var canBackPress = true
    private val alarmHelper: AlarmHelper by lazy {
        AlarmHelper(
            application.getSharedPreferences(
                SHARED_PREFERENCES,
                0
            )
        )
    }

    private val recipeDialog: RecipeDialog by lazy { RecipeDialog() }
    fun displayNotesClick(activeFragment: Fragment? = null) {
        when (activeFragment) {
            is SavedRecipeFragment -> {
                activeFragment.showNotes()
            }
            is PlayFragment -> {
                activeFragment.showNotes()
            }
        }
    }

    fun shareClick(activeFragment: Fragment? = null) {
        val colors = intArrayOf(
            getApplication<Application>().resources.getColor(R.color.am_text, null),
            getApplication<Application>().getColor(R.color.pm_color),
            getApplication<Application>().getColor(R.color.default_dark)
        )
        when (activeFragment) {
            is SavedRecipeFragment -> {
                activeFragment.viewModel.shareTextVersionOfRecipe(
                    colors,
                    activeFragment.requireActivity()
                )
            }
        }
    }

    fun editClick(activeFragment: Fragment?, navController: NavController) {
        when (activeFragment) {
            is SavedRecipeFragment -> {
                val bundle = Bundle()
                bundle.putLong("parent_id", activeFragment.viewModel.parent_id)
                bundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, false)
                navController.navigate(
                    R.id.action_savedRecipeFragment_to_intervalFragment,
                    bundle
                )
            }
        }
    }

    fun duplicateClick(activeFragment: Fragment?, navController: NavController) {
        Log.i(TAG, activeFragment.toString())
        when (activeFragment) {
            is SavedRecipeFragment -> {
                activeFragment.viewModel.duplicate(navController)
                Log.i(TAG, "duplicate pressed")

            }
            is PlayFragment -> {
                activeFragment.showDeleteDialog()

            }
        }
    }

    fun buttonClick(activeFragment: Fragment?, navController: NavController) {
        when (activeFragment) {
            is ScheduleFragment -> {
                recipeDialog.setTargetFragment(activeFragment, 0)
                recipeDialog.isCancelable = false
                recipeDialog.setFragAndNav(activeFragment, navController)
                recipeDialog.show(activeFragment.parentFragmentManager, "recipe_name_dialog")
            }
            is IntervalFragment -> {
                activeFragment.viewModel.create()
            }
            is SavedRecipeFragment -> {
                val list = activeFragment.viewModel.intervalData?.value
                viewModelScope.launch {
                    val recipeStart =
                        scheduleDao?.getStartTime(activeFragment.viewModel.parent_id) ?: 0
                    if (!list.isNullOrEmpty()) {
                        intervalDao?.updateAll(
                            activeFragment.context?.let {
                                alarmHelper.setAlarms(
                                    recipeStart,
                                    list,
                                    it,
                                    activeFragment.viewModel.parent_id
                                )
                            } ?: emptyList()
                        )
                        scheduleDao?.updateScheduleName(
                            activeFragment.viewModel.parent_id,
                            activeFragment.viewModel.nameField
                        )
                        val bundle = Bundle()
                        bundle.putLong("parent_id", activeFragment.viewModel.parent_id)
                        bundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, false)
                        navController.navigate(
                            R.id.action_savedRecipeFragment_to_playFragment,
                            bundle
                        )
                    }
                }
                // end of coroutine block
            }

        }
    }


    override fun fragmentAttached(fragment: Fragment) {
        when (fragment) {
            is ScheduleFragment -> {
                header.postValue("RECIPES")
                mode.postValue(ButtonNew.RECIPES)
                scrollWatcher.postValue(fragment)
            }
            is IntervalFragment -> {
                header.postValue("STEPS")
                mode.postValue(ButtonNew.STEPS)
                scrollWatcher.postValue(fragment)
            }
            is SavedRecipeFragment -> {
                header.postValue("START RECIPE")
                mode.postValue(ButtonNew.DETAIL)
                scrollWatcher.postValue(fragment)
            }
            is PlayFragment -> {
                header.postValue("ACTIVE RECIPE")
                mode.postValue(ButtonNew.PLAY)
                scrollWatcher.postValue(fragment)
            }
        }
    }

    fun wasLaunchedByAlarm(intent: Intent): Boolean {
        return intent.hasExtra(AlarmHelper.PARENT_ID)
    }

    fun hasAnActiveAlarm(sharedPreferences: SharedPreferences): Boolean {
        return sharedPreferences.getInt(AlarmHelper.ACTIVE_ALARMS, 0) != 0
    }

    fun createAlarmBundle(intent: Intent, sharedPreferences: SharedPreferences): Bundle {
        return Bundle().also { bundle ->
            if (intent.hasExtra(AlarmHelper.PARENT_ID)) {
                bundle.putLong(
                    AlarmHelper.PARENT_ID,
                    intent.getLongExtra(AlarmHelper.PARENT_ID, 0L)
                )
            } else {
                bundle.putLong(
                    AlarmHelper.PARENT_ID,
                    sharedPreferences.getLong(AlarmHelper.PARENT_ID, 0L)
                )
            }
        }
    }

    fun navigateToAlarm(navController: NavController, bundle: Bundle) {
        navController.navigate(R.id.action_scheduleFragment_to_playFragment, bundle)
    }

    fun checkCanBackPress(): Boolean {
        return canBackPress
    }

    fun setCheckCanBackPress(bool: Boolean) {
        this.canBackPress = bool
    }


}