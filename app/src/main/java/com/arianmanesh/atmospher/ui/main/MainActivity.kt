package com.arianmanesh.atmospher.ui.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.arianmanesh.atmospher.databinding.ActivityMainBinding
import com.arianmanesh.atmospher.ui.weather_list.WeatherListFragment


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