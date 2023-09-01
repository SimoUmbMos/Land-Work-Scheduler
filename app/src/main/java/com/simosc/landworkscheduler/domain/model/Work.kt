package com.simosc.landworkscheduler.domain.model

import android.os.Build
import android.os.Parcel
import android.os.ParcelFormatException
import android.os.Parcelable
import java.time.LocalDateTime

data class Work(
    val id: Long,
    val lid: Long?,
    val zid: Long?,
    val title: String,
    val desc: String,
    val date: LocalDateTime,
    val created: LocalDateTime,
    val edited: LocalDateTime,
): Parcelable{

    @Suppress("DEPRECATION")
    constructor(
        parcel: Parcel
    ):this(
        id = parcel.readLong(),
        lid = parcel.readLong().let{ if(it > 0) it else null },
        zid = parcel.readLong().let{ if(it > 0) it else null },
        title = parcel.readString() ?: throw ParcelFormatException(),
        desc = parcel.readString() ?: throw ParcelFormatException(),
        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            parcel.readSerializable(
                LocalDateTime::class.java.classLoader,
                LocalDateTime::class.java
            ) ?: throw ParcelFormatException()
        else
            parcel.readSerializable() as LocalDateTime,
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
        parcel.writeLong(lid ?: 0)
        parcel.writeLong(zid ?: 0)
        parcel.writeString(title)
        parcel.writeString(desc)
        parcel.writeSerializable(date)
        parcel.writeSerializable(created)
        parcel.writeSerializable(edited)
    }

    override fun describeContents(): Int = 0

    companion object {
        fun emptyWork(
            lid: Long?,
            zid: Long?
        ) = Work(
            id = 0L,
            lid = lid,
            zid = zid,
            title = "",
            desc = "",
            date = LocalDateTime.now().plusHours(1L),
            created = LocalDateTime.now(),
            edited = LocalDateTime.now()
        )

        @Suppress("unused")
        @JvmField val CREATOR = object : Parcelable.Creator<Work>{
            override fun createFromParcel(parcel: Parcel): Work = Work(parcel)
            override fun newArray(size: Int): Array<Work?> = arrayOfNulls(size)
        }
    }
}