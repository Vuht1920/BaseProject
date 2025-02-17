package com.mmt.app.ui

import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.mmt.BuildConfig
import com.mmt.R
import com.mmt.ads.AdsModule
import com.mmt.ads.ConsentStatus
import com.mmt.ads.GoogleConsentManager
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.OPAStatus
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.Utils
import com.mmt.ads.wrapper.InterstitialOPA
import com.mmt.app.BaseApplication
import com.mmt.app.base.BaseActivity
import com.mmt.app.utils.log.DebugLog
import com.mmt.databinding.ActivityMainBinding
import dev.androidbroadcast.vbpd.viewBinding

class MainActivity : BaseActivity(R.layout.activity_main) {
    private val binding: ActivityMainBinding by viewBinding(ActivityMainBinding::bind)
    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController }
    private val appBarConfiguration by lazy { AppBarConfiguration(setOf(R.id.home, R.id.history, R.id.setting)) }

    // Logic show quảng cáo
    private val mGoogleConsentManager: GoogleConsentManager by lazy { GoogleConsentManager.getInstance(applicationContext) }
    private var isActivityRecreated = false

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        isActivityRecreated = savedInstanceState != null
        setTheme(R.style.AppTheme_NoBackground_Dark_Accent1)

        initView()

        // Init Ads module
        initAdsModule()


    }

    private fun initView() {
        // Setup the bottom navigation view with navController
        binding.bottomNav.setupWithNavController(navController)

        // Setup the ActionBar with navController and 2 top level destinations
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
    }

    private fun initAdsModule() {
// Init AdsConfigs
        BaseApplication.instance.initAdsConfig(application)
        // Check & init AdsModule
        if (!AdsConfig.getInstance().isFullVersion) {
            AdsModule.getInstance().setSession(hashCode())
            application?.let {
                if (BuildConfig.DEBUG || BuildConfig.TEST_AD) {
                    val testDeviceIds = listOf(Utils.getDeviceId(this))
                    val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                    MobileAds.setRequestConfiguration(configuration)
                }

                if (canShowAds()) {
                    DebugLog.logd("initializeMobileAdsSdk immediate")
                    val initOPA = !isActivityRecreated
                    initializeMobileAdsSdk(initOPA)
                } else {
                    initConsentForm()
                }
            }
        } else {
            onAdOPACompleted() // initAdModule isFullVersion
        }
    }

    private fun canShowAds(): Boolean {
        return !AdsConfig.getInstance().isFullVersion && mGoogleConsentManager.canRequestAds()
    }

    private fun onAdOPACompleted() {

    }

    /** Init AdsModule & show Ads */
    private fun initializeMobileAdsSdk(initOPA: Boolean) {
        application?.let {
            DebugLog.logi(" ${this@MainActivity.hashCode()} - initializeMobileAdsSdk, initOPA = $initOPA")
            // Show/hide splash
            // Init AdsModule
            AdsModule.getInstance().init(it, object : AdsModule.InitCallback {
                override fun onInitializeCompleted() {
                    initOtherAds()
                }
            })
            // InterOPA
            if (initOPA) {
                initInterstitialOPA()
            }
            // Bottom Banner
            showBottomBanner()
        }
    }

    // initOtherAds
    private fun initOtherAds() {

    }

    /** Keep splash của hệ thống cho tới khi lấy được Consent status */
    private fun waitConsentStatus() {
        DebugLog.logi("waitConsentStatus")
        mConsentStatus = ConsentStatus.REQUESTING
        showCustomSplash()
    }

    private fun showCustomSplash() {

    }

    // Init & show InterOPA
    private fun initInterstitialOPA() {
        if (InterstitialOPA.initAndShowOPA(this@MainActivity)
    }
}