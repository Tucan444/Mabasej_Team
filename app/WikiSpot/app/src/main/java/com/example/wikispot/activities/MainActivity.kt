package com.example.wikispot.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wikispot.R
import com.example.wikispot.databinding.ActivityMainBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    //private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //binding = ActivityMainBinding.inflate(layoutInflater)
        //setContentView(binding.root)

        val navController = findNavController(R.id.mainFragmentHost)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.mainBottomNavigationView)

        bottomNavView.setupWithNavController(navController)
    }
}