package com.gershaveut.service

import android.os.Bundle
import android.view.Menu
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.Surface
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.gershaveut.service.databinding.ActivityMainBinding
import com.gershaveut.service.ui.theme.ServiceTheme
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {
	private lateinit var appBarConfiguration: AppBarConfiguration
	private lateinit var binding: ActivityMainBinding
	
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		
		setContent {
			ServiceTheme {
				Surface {
				
				}
			}
		}
		
		binding = ActivityMainBinding.inflate(layoutInflater)
		setContentView(binding.root)
		
		setSupportActionBar(binding.appBarMain.toolbar)
		
		val drawerLayout: DrawerLayout = binding.drawerLayout
		val navView: NavigationView = binding.navView
		val navController = findNavController(R.id.nav_host_fragment_content_main)
		
		appBarConfiguration = AppBarConfiguration(
			setOf(
				R.id.nav_chat_ofg
			), drawerLayout
		)
		setupActionBarWithNavController(navController, appBarConfiguration)
		navView.setupWithNavController(navController)
	}
	
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main, menu)
		return true
	}
	
	override fun onSupportNavigateUp(): Boolean {
		val navController = findNavController(R.id.nav_host_fragment_content_main)
		return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
	}
}