package com.arianmanesh.atmospher.weather_list.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arianmanesh.atmospher.WeatherItemResponse
import com.arianmanesh.atmospher.database.CitiesDBModel
import com.arianmanesh.atmospher.databinding.ItemWeatherListBinding

class WeatherItemAdapter (private val weatherItems: List<CitiesDBModel>, private val context:Context): RecyclerView.Adapter<WeatherItemAdapter.WeatherItemViewHolder>() {

    var onCityNameClick: ((CitiesDBModel) -> Unit)? = null
    var onCityDeleteClick: ((CitiesDBModel) -> Unit)? = null
    var onCityEditClick: ((CitiesDBModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherItemViewHolder {
        val binding = ItemWeatherListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WeatherItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WeatherItemViewHolder, position: Int) {
        holder.bindItem(position,weatherItems[position],context)
    }

    override fun getItemCount(): Int {
        return weatherItems.size
    }

    inner class WeatherItemViewHolder(private val binding: ItemWeatherListBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindItem(position: Int,cityItem: CitiesDBModel, context: Context){
            binding.txtCityName.text = buildCityText(cityItem)
            binding.txtCityName.setOnClickListener {
                onCityNameClick?.invoke(weatherItems[position])
            }
            binding.imgEditCity.setOnClickListener {
                onCityEditClick?.invoke(weatherItems[position])
            }
            binding.imgEditCity.setOnClickListener {
                onCityEditClick?.invoke(weatherItems[position])
            }
            binding.imgDeleteCity.setOnClickListener {
                onCityDeleteClick?.invoke(weatherItems[position])
            }
        }

        private fun buildCityText(it: CitiesDBModel): String {
            return (it.country + " | " + (it.name).lowercase().replaceFirstChar { it.uppercase() } )
        }
    }
}