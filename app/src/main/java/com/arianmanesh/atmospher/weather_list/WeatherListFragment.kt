package com.arianmanesh.atmospher.weather_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.databinding.FragmentWeatherListBinding
import com.arianmanesh.atmospher.city_modify.CityModifyFragment
import com.arianmanesh.atmospher.main.MainActivity
import com.arianmanesh.atmospher.R
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.database.CitiesDBModel
import com.arianmanesh.atmospher.main.SharedViewModel
import com.arianmanesh.atmospher.weather_list.adapters.WeatherItemAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.*


class WeatherListFragment : Fragment() {

    private val weatherListViewModel: WeatherListViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding : FragmentWeatherListBinding
    private var currentCity = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWeatherListBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weatherListViewModel.getAllCitiesFromDB()
        sharedViewModel.checkInternetConnectivity()

        handleCurrentSelectedCity()
        setObservers()
        handleClickListeners()

    }

    private fun handleCurrentSelectedCity() {
        currentCity = weatherListViewModel.retrieveCurrentSelectedCity(requireContext())
        if(currentCity.isEmpty()){
            binding.emptyList.visibility = View.VISIBLE
            binding.frlMainHeader.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
        }else{
            binding.emptyList.visibility = View.GONE
            binding.frlMainHeader.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.VISIBLE
            weatherListViewModel.updateWeather(currentCity)
        }
    }

    private fun handleClickListeners() {
        binding.floatingActionButton.setOnClickListener {
            (activity as MainActivity).replaceFragment(CityModifyFragment())
        }
    }

    private fun setObservers(){

        weatherListViewModel.weatherData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let {
                        binding.prgLoading.visibility = View.GONE
                        binding.imgMapMarkerIcon.visibility = View.VISIBLE
                        binding.txtCityName.visibility = View.VISIBLE
                        binding.txtCityTemperature.visibility = View.VISIBLE
                        binding.imgWeatherIcon.visibility = View.VISIBLE
                        binding.txtCityName.text = buildCityText(it)
                        binding.txtCityTemperature.text = buildTemperatureText(it)
                        Glide.with(requireContext()).load("https:" + it.current.condition.icon).into(binding.imgWeatherIcon)
                    }
                }
                is ResponseResult.Error -> {
                    response.errorResponseBody?.let {
                        binding.prgLoading.visibility = View.GONE
                        binding.txtCityName.visibility = View.VISIBLE
                        binding.txtCityName.text = it.string()
                    }
                }
                is ResponseResult.Loading -> {
                    binding.imgMapMarkerIcon.visibility = View.INVISIBLE
                    binding.prgLoading.visibility = View.VISIBLE
                    binding.txtCityName.visibility = View.INVISIBLE
                    binding.imgWeatherIcon.visibility = View.INVISIBLE
                    binding.txtCityTemperature.visibility = View.INVISIBLE
                }
                else -> {}
            }
        })

        weatherListViewModel.citiesData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let {
                        showListOfDBCities(it)
                    }
                }
                is ResponseResult.Loading -> {
                    //todo: better to show to loading but load is fast enough for now
                }
                else -> {}
            }
        })

        sharedViewModel.internetConnection.observe(viewLifecycleOwner, Observer { net ->
            when (net) {
                is Boolean -> {
                    if(net){
                        if(binding.crdInternetNotifContainer.visibility == View.VISIBLE){
                            binding.crdInternetNotifContainer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.success))
                            binding.txtInternetNotif.text = getString(R.string.back_online)
                            if(!currentCity.isEmpty()) weatherListViewModel.updateWeather(currentCity)
                            viewLifecycleOwner.lifecycleScope.launch {
                                delay(3000)
                                binding.crdInternetNotifContainer.visibility = View.GONE
                            }
                        }
                    }else{
                        binding.crdInternetNotifContainer.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.error))
                        binding.txtInternetNotif.text = getString(R.string.no_internet_connection)
                        binding.crdInternetNotifContainer.visibility = View.VISIBLE
                    }
                }
                else -> {}
            }
        })
    }

    private fun buildCityText(it: WeatherItemResponse): String {
        return (it.location.country + " | " + (it.location.name).lowercase().replaceFirstChar { it.uppercase() } )
    }

    private fun buildTemperatureText(it: WeatherItemResponse): String {
        return (it.current.temp_c.toString() + getString(R.string.centigrade_sign))
    }

    private fun showListOfDBCities(it: List<CitiesDBModel>){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val adapter = WeatherItemAdapter(it,requireContext())
        binding.recyclerView.adapter = adapter
        adapter.notifyItemRangeInserted(0,it.size)
        adapter.onCityNameClick = {
            weatherListViewModel.storeCurrentSelectedCity(it.name,requireContext())
            currentCity = it.name
            weatherListViewModel.updateWeather(it.name)
        }
        adapter.onCityDeleteClick = {
            if(currentCity == it.name){
                Toast.makeText(context, getString(R.string.cant_delete_current_city), Toast.LENGTH_SHORT).show()
            }else{
                weatherListViewModel.removeCityFromDB(it)
            }
        }
        adapter.onCityEditClick = {
            val cityModifyFragment = CityModifyFragment()
            val bundle = Bundle()
            bundle.putString("city", it.name)
            cityModifyFragment.arguments = bundle
            (activity as MainActivity).replaceFragment(cityModifyFragment)
        }
    }


}