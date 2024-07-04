package com.projects.pexels_app.ui.navigation

import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.projects.pexels_app.R
import com.projects.pexels_app.utils.Keys

class Navigation(private val navController: NavController) {
    private fun getCurrentDestinationId(): Int? {
        return navController.currentDestination?.id
    }

    private fun navigateWithCategory(actionId: Int, args: Bundle?) {
        when (val currentDestinationId = getCurrentDestinationId()) {
            R.id.home_menu, R.id.bookmarks_menu, R.id.details_fragment -> {
                val navOptions = NavOptions.Builder()
                    .setEnterAnim(android.R.anim.fade_in)
                    .setExitAnim(android.R.anim.fade_out)
                    .setPopEnterAnim(android.R.anim.fade_in)
                    .setPopExitAnim(android.R.anim.fade_out)
                    .build()
                navController.navigate(actionId, args, navOptions)
            }
            else -> throw IllegalStateException("Cannot navigate to the specified destination from $currentDestinationId")
        }
    }

    fun navigateHomeToDetails(id: Int?, fragmentName: String?) {
        val bundle = bundleOf(Keys.ID.name to id, Keys.FRAGMENT_NAME.name to fragmentName)
        navigateWithCategory(R.id.action_home_fragment_to_details_fragment, bundle)
    }

    fun navigateHomeToBookmarks() {
        navigateWithCategory(R.id.action_home_fragment_to_bookmarks_fragment, null)
    }

    fun navigateDetailsToHome() {
        navigateWithCategory(R.id.action_details_fragment_to_home_fragment, null)
    }

    fun navigateBookmarksToHome() {
        navigateWithCategory(R.id.action_bookmarks_fragment_to_home_fragment, null)
    }

    fun navigateBookmarksToDetails(id: Int?, fragmentName: String) {
        val bundle = bundleOf(Keys.ID.name to id, Keys.FRAGMENT_NAME.name to fragmentName)
        navigateWithCategory(R.id.action_bookmarks_fragment_to_details_fragment, bundle)
    }
}