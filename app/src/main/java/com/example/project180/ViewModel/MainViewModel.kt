package com.example.project180.ViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project180.Model.CategoryDto
import com.example.project180.Model.ItemsModel
import com.example.project180.Model.SliderModel
import com.example.project180.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val firebaseDatabase = FirebaseDatabase.getInstance()

    private val _banner = MutableLiveData<List<SliderModel>>()
    private val _category = MutableLiveData<MutableList<CategoryDto>>()
    private val _bestSeller = MutableLiveData<MutableList<ItemsModel>>()
    val banners: LiveData<List<SliderModel>> = _banner
    val category: LiveData<MutableList<CategoryDto>> = _category
    val bestSeller: LiveData<MutableList<ItemsModel>> = _bestSeller

    fun loadBanners() {
        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            val localBanners = listOf(
                SliderModel(R.drawable.banner1),
                SliderModel(R.drawable.banner2)
            )
            _banner.postValue(localBanners)
        }
    }

    fun loadCategory() {
        val Ref = firebaseDatabase.getReference("Category")
        Ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lists = mutableListOf<CategoryDto>()

                for (childSnapshot in snapshot.children) {
                    val list = childSnapshot.getValue(CategoryDto::class.java)
                    if (list != null) {
                        lists.add(list)
                    }
                }
                _category.value = lists
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun loadBestSeller(){
        val Ref=firebaseDatabase.getReference("Items")
        Ref.addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
              val lists= mutableListOf<ItemsModel>()

                for(childSnapshot in snapshot.children){
                    val list=childSnapshot.getValue(ItemsModel::class.java)
                    if(list!=null){
                        lists.add(list)
                    }
                }
                _bestSeller.value=lists
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}