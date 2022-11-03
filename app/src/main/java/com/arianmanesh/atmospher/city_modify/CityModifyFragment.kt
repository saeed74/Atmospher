package com.arianmanesh.atmospher.city_modify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.arianmanesh.atmospher.R
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.databinding.FragmentCityModifyBinding
import com.arianmanesh.atmospher.main.MainActivity
import com.arianmanesh.atmospher.main.SharedViewModel
import java.net.HttpURLConnection

class CityModifyFragment : Fragment() {

    private val cityModifyViewModel: CityModifyViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding : FragmentCityModifyBinding
    private var modifyMode = false
    private var previousCityName = ""

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
        getBundle()
        handleClickListeners()
        setObservers()
    }

    private fun getBundle() {
        val bundle = arguments
        if (bundle != null) {
            val city = bundle.getString("city")
            if(city != null){
                binding.edtCityName.setText(city)
                previousCityName = city
                modifyMode = true
                binding.txtEdtLabel.text = getString(R.string.modify_city_name)
            }
        }
    }

    private fun handleClickListeners() {
        binding.btnSubmit.setOnClickListener {
            val cityName: String = binding.edtCityName.text.toString().trim()
            if(cityName.isEmpty()) {
                Toast.makeText(requireContext(),getString(R.string.city_name_cant_be_empty),Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(sharedViewModel.checkInternetState()){
                cityModifyViewModel.updateWeather(cityName,modifyMode,previousCityName)
            }else{
                Toast.makeText(requireContext(),getString(R.string.please_check_internet_connection),Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun setObservers(){
        cityModifyViewModel.weatherData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let {
                        //binding.txtShowState.text = "SUCCESS: " + it.location.name
                        sharedViewModel.updateWeatherFragment()
                        (activity as MainActivity).popBackStack()
                    }
                }
                is ResponseResult.Error -> {
                    response.errorResponseBody?.let {
                        binding.txtShowState.text = it.string()
                    }
                    if(response.errorCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT){
                        binding.txtShowState.text = getString(R.string.timeout)
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