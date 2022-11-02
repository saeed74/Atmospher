package com.arianmanesh.atmospher.main

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.arianmanesh.atmospher.databinding.ActivityMainBinding
import com.arianmanesh.atmospher.weather_list.WeatherListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        replaceFragment(WeatherListFragment())
    }

    fun replaceFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.replace(binding.fragmentHost.id , fragment)
        ft.addToBackStack(fragment::class.java.simpleName)
        ft.commit()
    }


}