package com.example.salvaagua

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager2.adapter.FragmentStateAdapter

class ChartPagerAdapter(fragment: Fragment) :
    FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int  = 4

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> DailyChartFragment()
            1 -> WeeklyChartFragment()
            2 -> MonthlyChartFragment()
            else -> YearlyChartFragment()
        }
    }

}