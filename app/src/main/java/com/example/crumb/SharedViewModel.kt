package com.example.crumb

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch

class SharedViewModel(application: Application) : AndroidViewModel(application), FragmentCallback{

    companion object {
        val SHARED_PREFERENCES = "shared_preferences"
    }

    var activeFragment: Fragment? = null
    val header = MutableLiveData<String>()
    val mode = MutableLiveData<Int>()
    val database = DatabaseScheduler.getInstance(application)
    val scheduleDao = database?.getScheduleDao()
    val intervalDao: IntervalDao? = database?.getIntervalDao()
    val sharedPreferences = application.getSharedPreferences(SHARED_PREFERENCES, 0)
    val scrollWatcher = MutableLiveData<Fragment>()
    private var canBackPress = true

    val alarmHelper: AlarmHelper by lazy {
        AlarmHelper(
            application.getSharedPreferences(
                SHARED_PREFERENCES,
                0
            )
        )
    }

    val recipeDialog: RecipeDialog by lazy { RecipeDialog() }
    fun topRightButtonClick() {
        when (activeFragment) {
            is SavedRecipeFragment -> {
                val fragment = activeFragment as SavedRecipeFragment
                fragment.showNotes()
            }
            is PlayFragment -> {
                val fragment = activeFragment as PlayFragment
                fragment.showNotes()
            }
        }
    }

    fun topLeftOfRightButtonClick(navController: NavController) {
        when (activeFragment) {
            is SavedRecipeFragment -> {
                val fragment = activeFragment as SavedRecipeFragment
                val bundle = Bundle()
                bundle.putLong("parent_id", fragment.viewModel.parentID)
                bundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, false)
                navController.navigate(
                    R.id.action_savedRecipeFragment_to_intervalFragment,
                    bundle
                )
            }
        }
    }

    fun topLeftButtonClick(navController: NavController) {
        when (activeFragment) {
            is SavedRecipeFragment -> {
                val fragment = activeFragment as SavedRecipeFragment
                fragment.viewModel.duplicate(navController)
            }
            is PlayFragment -> {
                val fragment = activeFragment as PlayFragment
                fragment.viewModel.cancel()
            }
        }
    }

    fun buttonClick(navController: NavController, context: Context) {
        when (activeFragment) {
            is ScheduleFragment -> {
                val fragment = activeFragment as ScheduleFragment
                recipeDialog.setTargetFragment(fragment, 0)
                recipeDialog.isCancelable = false
                recipeDialog.setFragAndNav(fragment, navController)
                recipeDialog.show(fragment.parentFragmentManager, "recipe_name_dialog")
            }
            is IntervalFragment -> {
                val fragment = activeFragment as IntervalFragment
                fragment.viewModel.create()
            }
            is SavedRecipeFragment -> {
                val fragment = activeFragment as SavedRecipeFragment
                val list = fragment.viewModel.intervalData?.value
                viewModelScope.launch {
                    if (list != null) {
                        intervalDao?.updateAll(
                            alarmHelper.setAlarms(
                                list,
                                context,
                                fragment.viewModel.parentID
                            )
                        )
                        val bundle = Bundle()
                        bundle.putLong("parent_id", fragment.viewModel.parentID)
                        bundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, false)
                        navController.navigate(
                            R.id.action_savedRecipeFragment_to_playFragment,
                            bundle
                        )
                    }
                }
            }

            is PlayFragment -> {
                val fragment = activeFragment as PlayFragment
            }

        }
    }

    override fun fragmentAttached(fragment: Fragment) {
        activeFragment = fragment
        when (activeFragment) {
            is ScheduleFragment -> {
                header.postValue("RECIPES"); mode.postValue(ButtonNew.RECIPES)
                scrollWatcher.postValue(activeFragment as ScheduleFragment)
            }
            is IntervalFragment -> {
                header.postValue("STEPS"); mode.postValue(ButtonNew.STEPS)
                scrollWatcher.postValue(activeFragment as IntervalFragment)
            }
            is SavedRecipeFragment -> {
                header.postValue("START RECIPE"); mode.postValue(ButtonNew.DETAIL)
                scrollWatcher.postValue(activeFragment as SavedRecipeFragment)
            }
            is PlayFragment -> {
                val fragment = activeFragment as PlayFragment
                header.postValue("ACTIVE RECIPE"); mode.postValue(ButtonNew.PLAY)
                scrollWatcher.postValue(activeFragment as PlayFragment)
            }
    }
    }

    fun checkIfLaunchedByAlarm(navController: NavController, intent: Intent) {
        if (intent.hasExtra(AlarmHelper.PARENT_ID)) {
            val parentID = intent.getLongExtra(AlarmHelper.PARENT_ID,0L)
            val details = intent.getBundleExtra(AlarmHelper.DETAILS)
            val myBundle = Bundle()
            myBundle.putLong(AlarmHelper.PARENT_ID, parentID)
            myBundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, true)
            myBundle.putString(AlarmHelper.NAME, details.getString(AlarmHelper.NAME))
            myBundle.putString(AlarmHelper.DESCRIPTION, details.getString(AlarmHelper.DESCRIPTION))
            navController.navigate(R.id.action_scheduleFragment_to_playFragment, myBundle)
        } else {
            viewModelScope.launch {
                var parentID : Long = sharedPreferences.getLong(AlarmHelper.PARENT_ID, 0L)
                val count = intervalDao?.getAlarmCount(parentID) ?: 0
                if (count > 0) {
                    val bundle = Bundle()
                    bundle.putLong(AlarmHelper.PARENT_ID, parentID)
                    System.out.println(parentID)
                    bundle.putBoolean(AlarmHelper.ALARM_IS_ACTIVE, false)
                    navController.navigate(R.id.action_scheduleFragment_to_playFragment, bundle)
                }
            }
        }
    }

    fun checkCanBackPress() : Boolean{
        return canBackPress
    }

    fun setCheckCanBackPress(bool : Boolean){
        this.canBackPress = bool
    }
}