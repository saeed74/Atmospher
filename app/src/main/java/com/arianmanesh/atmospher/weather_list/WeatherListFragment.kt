package com.arianmanesh.atmospher.weather_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
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
import com.arianmanesh.atmospher.database.AtmosphereDataBase
import com.arianmanesh.atmospher.database.CitiesDBModel
import com.arianmanesh.atmospher.main.SharedViewModel
import com.arianmanesh.atmospher.weather_list.adapters.WeatherItemAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.net.HttpURLConnection


class WeatherListFragment : Fragment() {

    private val weatherListViewModel: WeatherListViewModel by viewModels()
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private lateinit var binding : FragmentWeatherListBinding
    lateinit var adapter : WeatherItemAdapter


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

        weatherListViewModel.fetchAllWeatherListAndCurrentCity()
        sharedViewModel.checkInternetConnectivity()

//        handleCurrentSelectedCity()
        setObservers()
        handleClickListeners()

    }

    private fun handleClickListeners() {
        binding.floatingActionButton.setOnClickListener {
            (activity as MainActivity).addFragment(CityModifyFragment(), CityModifyFragment::class.simpleName!!)
        }
    }

    private fun setObservers(){

        weatherListViewModel.weatherData.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is ResponseResult.Success -> {
                    response.data?.let {
                        updateHeaderUi(it.location.country,
                            it.location.name,
                            it.current.temp_c.toString(),
                            it.current.condition.icon)
                    }
                }
                is ResponseResult.Error -> {
                    response.errorResponseBody?.let {
                        binding.prgLoading.visibility = View.GONE
                        binding.txtCityName.visibility = View.VISIBLE
                        binding.txtCityName.text = it.string()
                    }
                    if(response.errorCode == HttpURLConnection.HTTP_GATEWAY_TIMEOUT){
                        binding.prgLoading.visibility = View.GONE
                        binding.txtCityName.visibility = View.VISIBLE
                        binding.txtCityName.text = getString(R.string.timeout)
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
                    if (response.data.isNullOrEmpty()){
                        binding.emptyList.visibility = View.VISIBLE
                        binding.frlMainHeader.visibility = View.GONE
                        binding.recyclerView.visibility = View.GONE
                    }else{
                        showListOfDBCities(response.data)
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
                            weatherListViewModel.fetchAllWeatherListAndCurrentCity()
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

        weatherListViewModel.cityDelete.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResponseResult.DataBaseError -> {
                    Toast.makeText(context, it.error , Toast.LENGTH_SHORT).show()
                }
                is ResponseResult.Success -> {
                    it.data?.let {
                        adapter.removeItem(weatherListViewModel.getRemovedPosition())
                    }
                }
                else -> {}
            }
        })

        sharedViewModel.updateWeatherFragment.observe(viewLifecycleOwner, Observer {
            weatherListViewModel.fetchAllWeatherListAndCurrentCity()
        })

        weatherListViewModel.currentSelectedCity.observe(viewLifecycleOwner, Observer {
            when (it) {
                is ResponseResult.DataBaseError -> {
                    Toast.makeText(context, it.error , Toast.LENGTH_SHORT).show()
                }
                is ResponseResult.Success -> {
                    it.data?.let { city ->
                        if(sharedViewModel.checkInternetState()){
                            binding.emptyList.visibility = View.GONE
                            binding.frlMainHeader.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.VISIBLE
                            weatherListViewModel.updateWeather(city.name)
                        }else{
                            binding.emptyList.visibility = View.GONE
                            binding.frlMainHeader.visibility = View.VISIBLE
                            binding.recyclerView.visibility = View.VISIBLE
                            updateHeaderUi(city.country,
                                city.name,
                                city.temp_c.toString(),
                                city.icon)
                        }
                    }
                }
                else -> {}
            }

        })

    }

    private fun updateHeaderUi(country: String, city: String, temperature: String, icon: String) {
        binding.prgLoading.visibility = View.GONE
        binding.imgMapMarkerIcon.visibility = View.VISIBLE
        binding.txtCityName.visibility = View.VISIBLE
        binding.txtCityTemperature.visibility = View.VISIBLE
        binding.imgWeatherIcon.visibility = View.VISIBLE
        binding.txtCityName.text = buildCityText(country,city)
        binding.txtCityTemperature.text = buildTemperatureText(temperature)
        Glide.with(requireContext()).load("https:$icon").into(binding.imgWeatherIcon)
    }

    private fun buildCityText(country: String, city: String): String {
        return (country + " | " + (city).lowercase().replaceFirstChar { it.uppercase() } )
    }

    private fun buildTemperatureText(temperature: String): String {
        return (temperature + getString(R.string.centigrade_sign))
    }

    private fun showListOfDBCities(it: List<CitiesDBModel>){
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        val newList = ArrayList<CitiesDBModel>()
        newList.addAll(it)
        adapter = WeatherItemAdapter(newList,requireContext())
        binding.recyclerView.adapter = adapter
        adapter.notifyItemRangeInserted(0,it.size)
        adapter.onCityNameClick = {

            weatherListViewModel.unsetLastSelectedCity()
            weatherListViewModel.setSelectedCity(it.name.lowercase().trim())

            if(sharedViewModel.checkInternetState()){
                weatherListViewModel.updateWeather(it.name)
            }else{
                binding.emptyList.visibility = View.GONE
                binding.frlMainHeader.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.VISIBLE
                updateHeaderUi(it.country,
                    it.name,
                    it.temp_c.toString(),
                    it.icon)
            }


        }
        adapter.onCityDeleteClick = { city: CitiesDBModel, pos: Int ->
            weatherListViewModel.removeCityFromDB(city,pos)

//            if(currentCity == it.name){
//                Toast.makeText(context, getString(R.string.cant_delete_current_city), Toast.LENGTH_SHORT).show()
//            }else{
//                weatherListViewModel.removeCityFromDB(it)
//            }
        }
        adapter.onCityEditClick = {
            val cityModifyFragment = CityModifyFragment()
            val bundle = Bundle()
            bundle.putString("city", it.name)
            cityModifyFragment.arguments = bundle
            (activity as MainActivity).addFragment(cityModifyFragment, CityModifyFragment::class.simpleName!!)
        }
    }


}