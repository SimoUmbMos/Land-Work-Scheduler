package com.simosc.landworkscheduler.domain.extension

import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.simosc.landworkscheduler.domain.model.Land

fun Intent.putLand(
    name: String,
    land: Land,
    gson: Gson = GsonBuilder().create()
){
    if(name.isNotBlank()){
        putExtra("${name}_id", land.id)
        putExtra("${name}_title", land.title)
        putExtra("${name}_color", land.color.toArgb())
        putExtra("${name}_border", gson.toJson(land.border))
        putExtra("${name}_holes", gson.toJson(land.holes))
    }else{
        putExtra("id", land.id)
        putExtra("title", land.title)
        putExtra("color", land.color.toArgb())
        putExtra("border", gson.toJson(land.border))
        putExtra("holes", gson.toJson(land.holes))
    }
}

fun Intent.getLand(
    name: String,
    gson: Gson = GsonBuilder().create()
): Land?{
    try{
        Land.emptyLand().let{
            if(name.isNotBlank()){
                return it.copy(
                    id = getLongExtra("${name}_id", it.id),
                    title = getStringExtra("${name}_title") ?: it.title,
                    color = Color(getIntExtra("${name}_color", it.color.toArgb())),
                    border = gson.fromJson(
                        getStringExtra("${name}_border") ?: gson.toJson(it.border),
                        object : TypeToken<List<LatLng>>(){}.type
                    ),
                    holes = gson.fromJson(
                        getStringExtra("${name}_holes") ?: gson.toJson(it.holes),
                        object : TypeToken<List<List<LatLng>>>(){}.type
                    )
                )
            }else{
                return it.copy(
                    id = getLongExtra("id", it.id),
                    title = getStringExtra("title") ?: it.title,
                    color = Color(getIntExtra("color", it.color.toArgb())),
                    border = gson.fromJson(
                        getStringExtra("border") ?: gson.toJson(it.border),
                        object : TypeToken<List<LatLng>>(){}.type
                    ),
                    holes = gson.fromJson(
                        getStringExtra("holes") ?: gson.toJson(it.holes),
                        object : TypeToken<List<List<LatLng>>>(){}.type
                    )
                )
            }
        }
    }catch (ignore: Exception){}
    return null
}