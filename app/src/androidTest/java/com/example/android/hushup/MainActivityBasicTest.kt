package com.example.android.hushup

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.test.runner.AndroidJUnit4
import android.support.v7.widget.RecyclerView
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Created by ngtrnhan1205 on 12/14/17.
 */

@RunWith(AndroidJUnit4::class)
class MainActivityBasicTest {
    @Rule val mActivityTestRule = ActivityTestRule<MainActivity>(MainActivity::class.java)

    @Test
    fun addNewLocation() {
        // Find list of locations and swipe on that view
        onView(withId(R.id.addNewLocationButton)).perform(click())
    }
}