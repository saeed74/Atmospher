package com.arianmanesh.atmospher.main

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.arianmanesh.atmospher.databinding.ActivityMainBinding
import com.arianmanesh.atmospher.weather_list.WeatherListFragment
import android.R.attr.tag

import android.R.attr.fragment

import android.R
import android.R.attr


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        addFragment(WeatherListFragment(), WeatherListFragment::class.simpleName!! )
    }

    fun addFragment(fragment: Fragment, fragmentTag: String) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.add(binding.fragmentHost.id , fragment,fragmentTag)
        ft.addToBackStack(fragmentTag)
        ft.commit()
    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 1) {
            popBackStack()
        } else {
            finishAffinity()
        }
    }

    fun popBackStack(){
        supportFragmentManager.popBackStack()
    }

}