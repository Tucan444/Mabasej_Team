package com.example.wikispot.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.wikispot.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
        builder.setMessage("Do you want to quit the application?")
        builder.setPositiveButton("Yes") {_, _ -> finish()}
        builder.setNegativeButton("No") {_, _ -> }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.mainFragmentHost)
        val bottomNavView = findViewById<BottomNavigationView>(R.id.mainBottomNavigationView)

        bottomNavView.setupWithNavController(navController)
    }
}