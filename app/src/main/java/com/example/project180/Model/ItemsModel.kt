package com.example.project180.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.annotations.SerializedName

data class ItemsModel(
    var id: Int = 0,
    var categoryId: Int = 0,
    var categoryName: String? = "",
    var title: String = "",
    var description: String = "",
    var picUrl: ArrayList<String> = ArrayList(),
    var image: List<ImageDto> = listOf(),
    var size: ArrayList<String> = ArrayList(),
    @SerializedName("weight")
    var weight: ArrayList<String> = ArrayList(),
    var price: Double = 0.0,
    var rating: Double = 0.0,
    var stock: Int = 0,
    var numberInCart: Int = 0,
    var discountedPrice: Double = 0.0,
    var sellerName: String = "",
    var sellerTell: String = "",
    var sellerPic: String = ""
) : Parcelable {

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        categoryId = parcel.readInt(),
        categoryName = parcel.readString() ?: "",
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        picUrl = parcel.createStringArrayList() ?: ArrayList(),
        image = Gson().fromJson(
            parcel.readString(),
            object : TypeToken<List<ImageDto>>() {}.type
        ) ?: listOf(),
        size = parcel.createStringArrayList() ?: ArrayList(),
        weight = parcel.createStringArrayList() ?: ArrayList(),
        price = parcel.readDouble(),
        rating = parcel.readDouble(),
        stock = parcel.readInt(),
        numberInCart = parcel.readInt(),
        discountedPrice = parcel.readDouble(),
        sellerName = parcel.readString() ?: "",
        sellerTell = parcel.readString() ?: "",
        sellerPic = parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(categoryId)
        parcel.writeString(categoryName)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeStringList(picUrl)
        parcel.writeString(Gson().toJson(image)) // sérialiser la liste d'images
        parcel.writeStringList(size)
        parcel.writeStringList(weight)
        parcel.writeDouble(price)
        parcel.writeDouble(rating)
        parcel.writeInt(stock)
        parcel.writeInt(numberInCart)
        parcel.writeDouble(discountedPrice)
        parcel.writeString(sellerName)
        parcel.writeString(sellerTell)
        parcel.writeString(sellerPic)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ItemsModel> {
        override fun createFromParcel(parcel: Parcel): ItemsModel {
            return ItemsModel(parcel)
        }

        override fun newArray(size: Int): Array<ItemsModel?> {
            return arrayOfNulls(size)
        }
    }
}
