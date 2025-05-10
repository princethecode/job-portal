package com.example.jobportal.ui.home

import androidx.navigation.ActionOnlyNavDirections
import androidx.navigation.NavDirections
import com.example.jobportal.R

public class HomeFragmentDirections private constructor() {
  public companion object {
    public fun actionHomeFragmentToJobDetailsFragment(): NavDirections =
        ActionOnlyNavDirections(R.id.action_homeFragment_to_jobDetailsFragment)
  }
}
