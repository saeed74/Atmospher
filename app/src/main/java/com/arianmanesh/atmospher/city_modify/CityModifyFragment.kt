package com.arianmanesh.atmospher.city_modify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.databinding.FragmentCityModifyBinding

class CityModifyFragment : Fragment() {

    private val cityModifyViewModel: CityModifyViewModel by viewModels()
    private lateinit var binding : FragmentCityModifyBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCityModifyBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handleClickListeners()
        setObservers()
    }

    private fun handleClickListeners() {
        binding.button.setOnClickListener {
            val cityName: String = binding.edtCityName.text.toString()
            if(cityName.isEmpty()) {
                Toast.makeText(requireContext(),"Please Enter City Name",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            cityModifyViewModel.updateWeather(cityName)
        }
    }

    private fun setObservers(){
        cityModifyViewModel.weatherData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let {
                        binding.txtShowState.text = "SUCCESS: " + it.location.name
                    }
                }
                is ResponseResult.Error -> {
                    response.errorResponseBody?.let {
                        //todo: better show error
                        binding.txtShowState.text = it.string()
                    }
                }
                is ResponseResult.DataBaseError -> {
                    response.error?.let {
                        binding.txtShowState.text = it
                    }
                }
                is ResponseResult.Loading -> {
                    //todo: better show loading
                    binding.txtShowState.text = "loading"
                }
            }
        })
    }





}