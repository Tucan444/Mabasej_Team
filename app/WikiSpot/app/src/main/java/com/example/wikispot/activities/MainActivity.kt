package com.example.wikispot.activities

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.chaquo.python.Python
import com.chaquo.python.android.AndroidPlatform
import com.example.wikispot.R
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm")
        builder.setMessage("Do you want to quit the application?")
        builder.setPositiveButton("Yes") { _, _ -> finish()}
        builder.setNegativeButton("No") { _, _ -> }
        builder.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(R.id.mainFragmentHost)

        mainBottomNavigationView.setupWithNavController(navController)
    }

}
