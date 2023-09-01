package com.simosc.landworkscheduler.domain.model

import android.os.Build
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.core.config.DefaultZoneColor

data class Zone(
    val id: Long,
    val lid: Long,
    val title: String,
    val color: Color,
    val border: List<LatLng>,
    val holes: List<List<LatLng>>
): Parcelable{

    @Suppress("DEPRECATION")
    constructor(
        parcel: Parcel
    ):this(
        id = parcel.readLong(),
        lid = parcel.readLong(),
        title = parcel.readString() ?: throw ParcelFormatException(),
        color = Color(parcel.readInt()),
        border = mutableListOf<LatLng>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readList(
                    this,
                    this.javaClass.classLoader,
                    LatLng::class.java
                )
            }else{
                parcel.readList(
                    this,
                    this.javaClass.classLoader,
                )
            }
        }.toList(),
        holes = mutableListOf<List<LatLng>>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                parcel.readList(
                    this,
                    this.javaClass.classLoader,
                    emptyList<LatLng>()::class.java
                )
            }else{
                parcel.readList(
                    this,
                    this.javaClass.classLoader,
                )
            }
        }.toList(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(lid)
        parcel.writeString(title)
        parcel.writeInt(color.toArgb())
        parcel.writeList(border)
        parcel.writeList(holes)
    }

    override fun describeContents(): Int = 0

    companion object{
        fun emptyZone(
            lid: Long
        ) = Zone(
            id = 0L,
            lid = lid,
            title = "",
            color = DefaultZoneColor,
            border = emptyList(),
            holes = emptyList()
        )

        @Suppress("unused")
        @JvmField val CREATOR = object : Parcelable.Creator<Zone>{
            override fun createFromParcel(parcel: Parcel): Zone = Zone(parcel)
            override fun newArray(size: Int): Array<Zone?> = arrayOfNulls(size)
        }
    }
}