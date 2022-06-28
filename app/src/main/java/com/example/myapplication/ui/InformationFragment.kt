package com.example.myapplication.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentInformationBinding
import com.example.myapplication.model.MainViewModel
import com.google.android.gms.location.*

class InformationFragment: Fragment() {

    private var binding: FragmentInformationBinding? = null
    private val sharedViewModel: MainViewModel by activityViewModels()
    private lateinit var recyclerView: RecyclerView

    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var requestingLocationUpdates: Boolean = false

    //位置情報使用の権限許可を確認
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // 使用が許可された
            //locationStart()
            requestingLocationUpdates = true

        } else {
            // それでも拒否された時の対応
            val toast = Toast.makeText(requireActivity(),
                "これ以上なにもできません", Toast.LENGTH_SHORT)
            toast.show()

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentInformationBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.informationFragment = this


        if(sharedViewModel.images == null) {
            setTopTitle()
        } else {
            setInformationTitle()
            setRecyclerView()
        }
        setButton()

        //位置情報の権限許可
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            requestingLocationUpdates = true
        }

        locationRequest = LocationRequest.create()
        locationRequest.setPriority(
            LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setFastestInterval(5000)
            .setInterval(10000)

        //位置情報に変更があったら呼び出される
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for(location in locationResult.locations) {
                    var str1 = "Latitude:" + location.latitude
                    var str2 = "Longitude:" + location.longitude
                    Log.d("debug", str1)
                    Log.d("debug", str2)

                    //address取得
                    getAddress(location.latitude, location.longitude)
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

    }

    //緯度経度をもとに住所の取得
    private fun getAddress(lat: Double, lng: Double) {
        val geocoder = Geocoder(requireActivity())
        val address = geocoder.getFromLocation(lat, lng, 1)

        //位置情報の表示
        Log.d("debug", "市町村：" + address[0].locality.toString())

    }

    private fun setButton() {
        requireActivity().let {
            if(it is MainActivity) {
                it.setButton(binding!!.startButton, sharedViewModel.startFlag)
            }
        }
    }

    private fun setTopTitle() {
        binding!!.topTitle.setImageResource(R.drawable.top_title)
        binding!!.startText.setText(R.string.start_text)
    }

    private fun setInformationTitle() {
        binding!!.informationTitle.setImageResource(R.drawable.information)
    }

    fun pushButton() {
        sharedViewModel.changeStartFlag()
        setButton()

        if(sharedViewModel.startFlag) {
            if(sharedViewModel.images == null) {
                binding!!.topTitle.setImageDrawable(null)
                binding!!.startText.text = null
            }
            setInformationTitle()
            setRecyclerView()
        }
    }

    private fun setRecyclerView() {
        getInformation()
        recyclerView = binding!!.recyclerView
        recyclerView.adapter =
            RecyclerAdapter(sharedViewModel.images, sharedViewModel.favoriteFlag, sharedViewModel.titles, sharedViewModel.describes)
        recyclerView.layoutManager = LinearLayoutManager(MainActivity())
    }

    private fun getInformation() {
        sharedViewModel.getImages()
        sharedViewModel.getFavoriteFlag()
        sharedViewModel.getTitles()
        sharedViewModel.getDescribes()
        sharedViewModel.getContents()
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION
            )
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())

    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        if(requestingLocationUpdates) {
            startLocationUpdates()
        }
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }
}