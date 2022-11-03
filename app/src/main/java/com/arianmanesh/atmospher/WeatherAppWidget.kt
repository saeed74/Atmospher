package com.arianmanesh.atmospher

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.arianmanesh.atmospher.core.ResponseResult
import com.arianmanesh.atmospher.core.RetrofitInstance
import com.arianmanesh.atmospher.database.AtmosphereDataBase
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.AppWidgetTarget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WeatherAppWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

lateinit var atmosphereDB: AtmosphereDataBase

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {

    Log.e("TAG_W","Widget update started")

    atmosphereDB = AtmosphereDataBase.getInstance(context)

    val views = RemoteViews(context.packageName, R.layout.weather_app_widget)

    GlobalScope.launch(Dispatchers.IO) {

        if(!atmosphereDB.citiesDao().isAnyCityAlreadySelected()) {
            views.setViewVisibility(R.id.lnlWidgetDataContainer, View.GONE)
            views.setViewVisibility(R.id.txtWidgetStatus, View.VISIBLE)
            appWidgetManager.updateAppWidget(appWidgetId, views)
            return@launch
        }

        views.setViewVisibility(R.id.txtWidgetStatus, View.GONE)
        views.setViewVisibility(R.id.prgLoading, View.VISIBLE)
        val currentCity = atmosphereDB.citiesDao().getCurrentSelectedCity()

        val response = RetrofitInstance.api.getWeatherDetail(RetrofitInstance.apiKey,currentCity.name)

        withContext(Dispatchers.Main) {
            views.setViewVisibility(R.id.prgLoading, View.GONE)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    showResultOnWidget(views,
                        context,
                        appWidgetId,
                        body.location.name,
                        body.location.country,
                        body.current.temp_c,
                        body.current.condition.icon)
                }
            }else{
                showResultOnWidget(views,
                    context,
                    appWidgetId,
                    currentCity.name,
                    currentCity.country,
                    currentCity.temp_c,
                    currentCity.icon)
            }
        }

    }

    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun showResultOnWidget(views: RemoteViews, context: Context, appWidgetId: Int,  name: String, country: String, tempC: Double, icon: String) {

    views.setTextViewText(R.id.txtCityName, buildCityText(country,name))
    views.setTextViewText(R.id.txtCityTemperature, (tempC.toString() + context.getString(R.string.centigrade_sign)))
    val appWidgetTarget = AppWidgetTarget(context,R.id.imgWeatherIcon,views,appWidgetId)
    Glide.with(context.applicationContext)
        .asBitmap()
        .load("https:${icon}")
        .into(appWidgetTarget)

}

private fun buildCityText(country: String, city: String): String {
    return (country + " | " + (city).lowercase().replaceFirstChar { it.uppercase() } )
}