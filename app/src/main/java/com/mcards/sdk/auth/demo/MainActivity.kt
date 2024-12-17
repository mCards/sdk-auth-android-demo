package com.mcards.sdk.auth.demo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.demo.databinding.ActivityMainBinding

//TODO replace with your auth0 aud gotten from the mCards team
private const val AUTH0_AUD = "https://staging.mcards.com/api"

//TODO replace with your auth0 client ID gotten from the mCards team
private const val AUTH0_CLIENT_ID = "DL8XpUmzegVl9dR8QpO9djDifTY7nGyd"


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val authSdk = AuthSdkProvider.getInstance()

        // if login appears to succeed, but the 2step screen spins indefinitely without
        // redirecting to your app, the auth0 scheme might be incorrect. Also make
        // sure your auth0 domain has no trailing /
        authSdk.init(getString(R.string.auth0_domain),
            AUTH0_CLIENT_ID,
            AUTH0_AUD,
            BuildConfig.APPLICATION_ID)

        if (BuildConfig.DEBUG) {
            authSdk.debug()
        }

        //TODO if using firebase
        //authSdk.useFirebase()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}
