package com.hisana.mediaplayer

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataOne(
    val uri : String = "",
    val title : String = "",
    val artist : String = ""
) : Parcelable