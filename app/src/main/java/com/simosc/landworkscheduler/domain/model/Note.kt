package com.simosc.landworkscheduler.domain.model

import android.os.Build
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.android.gms.maps.model.LatLng
import com.simosc.landworkscheduler.core.config.DefaultNoteColor
import java.time.LocalDateTime

data class Note(
    val id: Long,
    val lid: Long,
    val title: String,
    val desc: String,
    val color: Color,
    val center: LatLng,
    val radius: Double,
    val created: LocalDateTime,
    val edited: LocalDateTime,
): Parcelable{
    @Suppress("DEPRECATION")
    constructor(
        parcel: Parcel
    ):this(
        id = parcel.readLong(),
        lid = parcel.readLong(),
        title = parcel.readString() ?: throw ParcelFormatException(),
        desc = parcel.readString() ?: throw ParcelFormatException(),
        color = Color(parcel.readInt()),
        center = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            parcel.readParcelable(
                LatLng::class.java.classLoader,
                LatLng::class.java
            ) ?: throw ParcelFormatException()
        else
            parcel.readParcelable(
                LatLng::class.java.classLoader
            ) ?: throw ParcelFormatException(),
        radius = parcel.readDouble(),
        created = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            parcel.readSerializable(
                LocalDateTime::class.java.classLoader,
                LocalDateTime::class.java
            ) ?: throw ParcelFormatException()
        else
            parcel.readSerializable() as LocalDateTime,
        edited = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            parcel.readSerializable(
                LocalDateTime::class.java.classLoader,
                LocalDateTime::class.java
            ) ?: throw ParcelFormatException()
        else
            parcel.readSerializable() as LocalDateTime,
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(lid)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeInt(color.toArgb())
        parcel.writeParcelable(center,flags)
        parcel.writeDouble(radius)
        parcel.writeSerializable(created)
        parcel.writeSerializable(edited)
    }

    override fun describeContents(): Int = 0

    companion object {
        fun emptyNote(
            lid: Long,
            center: LatLng
        ) = Note(
            id = 0L,
            lid = lid,
            title = "",
            desc = "",
            color = DefaultNoteColor,
            center = center,
            radius = 50.0,
            created = LocalDateTime.now(),
            edited = LocalDateTime.now()
        )

        @Suppress("unused")
        @JvmField val CREATOR = object : Parcelable.Creator<Note>{
            override fun createFromParcel(parcel: Parcel): Note = Note(parcel)
            override fun newArray(size: Int): Array<Note?> = arrayOfNulls(size)
        }
    }
}
