package com.mmt.app.ui

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.ump.FormError
import com.mmt.BuildConfig
import com.mmt.R
import com.mmt.ads.AdsModule
import com.mmt.ads.ConsentListener
import com.mmt.ads.ConsentStatus
import com.mmt.ads.GoogleConsentManager
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.OPAStatus
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.Utils
import com.mmt.ads.wrapper.AdOPAListener
import com.mmt.app.BaseApplication
import com.mmt.app.base.BaseActivity
import com.mmt.app.data.repository.dataStore.PrefDataStore
import com.mmt.app.ui.dialog.ExitAppDialog
import com.mmt.app.utils.log.DebugLog
import com.mmt.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(), AdOPAListener {
    private lateinit var binding: ActivityMainBinding
    private val navController by lazy { (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController }

    // Logic show quảng cáo
    private val mGoogleConsentManager: GoogleConsentManager by lazy { GoogleConsentManager.getInstance(applicationContext) }

    /** Flag chờ load OPA -> dùng để keep show MH Splash cho tới khi flag mOPAStatus != OPAStatus.LOADING */
    @Volatile
    private var mOPAStatus = OPAStatus.NONE

    /** Flag chờ request consent status -> dùng để keep show MH Splash cho tới khi flag mConsentStatus != ConsentStatus.REQUESTING */
    @Volatile
    private var mConsentStatus = ConsentStatus.NONE

    private var isActivityRecreated = false // Flag đánh dấu bỏ qua show dialog xin quyền khi Activity recreate lại

    @Inject
    lateinit var prefDataStore: PrefDataStore

    @Inject
    lateinit var dialogExitApp: ExitAppDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme_NoBackground_Dark_Accent1)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.rootView)
        isActivityRecreated = savedInstanceState != null

        // BackPressed
        onBackPressedDispatcher.addCallback(onBackPressedCallback)

        // InitView
        initView()

        // Init Ads module
        initAdsModule()


    }

    private fun initView() {
        // Setup the bottom navigation view with navController
        binding.bottomNav.setupWithNavController(navController)

    }

    private fun initAdsModule() {
        if (!AdsConfig.getInstance().isFullVersion) {
            val application = application
            // Init AdsConfigs
            BaseApplication.instance.initAdsConfig(application)
            AdsModule.getInstance().setSession(hashCode())
            if (BuildConfig.DEBUG || BuildConfig.TEST_AD) {
                val testDeviceIds: List<String> = listOf(Utils.getDeviceId(this))
                val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
                MobileAds.setRequestConfiguration(configuration)
            }

            if (mGoogleConsentManager.canRequestAds()) {
                DebugLog.loge("initializeMobileAdsSdk immediate")
                mConsentStatus = ConsentStatus.GATHERED
                AdsModule.getInstance().resetInitState()
                initializeMobileAdsSdk(true)
            } else {
                initConsentForm()
            }
        } else {
            onAdOPACompleted()
        }
    }

    private fun initializeMobileAdsSdk(initOPA: Boolean) {
        application?.let {
            AdDebugLog.logi("$TAG ${this@MainActivity.hashCode()} - initializeMobileAdsSdk, initOPA = $initOPA")
            // Init AdsModule
            AdsModule.getInstance().init(it, object : AdsModule.InitCallback {
                override fun onInitializeCompleted() {
                    initOtherAds()
                }
            })
            // InterOPA
            if (initOPA) {
                showCustomSplash()
                initInterstitialOPA()
            } else {
                hideCustomSplash()
            }
            // Bottom Banner
            showBottomBanner()
        }
    }

    private fun showCustomSplash() {
//        binding.viewSplash.root.visible()
    }

    private fun hideCustomSplash() {
//        binding.viewSplash.root.gone()
    }

    /**
     * Init & show Google Consent Form (GDPR)
     * */
    private fun initConsentForm() {
        waitConsentStatus()
        mGoogleConsentManager.gatherConsent(this, mConsentListener)
    }

    /** Keep splash của hệ thống cho tới khi lấy được Consent status */
    private fun waitConsentStatus() {
        DebugLog.logi("waitConsentStatus")
        mConsentStatus = ConsentStatus.REQUESTING
        showCustomSplash()
    }

    private val mConsentListener = object : ConsentListener {
        override fun consentGatheringComplete(error: FormError?) {
            DebugLog.logi("consentGatheringComplete, canShowAds = ${canShowAds()}")
            consentCompleted()
            if (mConsentStatus != ConsentStatus.SHOWING) {
                mConsentStatus = ConsentStatus.GATHERED
            }
        }

        override fun consentTimeout() {
            DebugLog.loge("consentTimeout")
            mConsentStatus = ConsentStatus.TIMEOUT
            onAdOPACompleted() // Không load được GDPR status -> ẩn Splash & start load data
        }

        override fun consentFormLoaded(consentManager: GoogleConsentManager) {
            if (mConsentStatus == ConsentStatus.SHOWING) {
                return
            }
            if (mConsentStatus == ConsentStatus.REQUESTING) {
                mConsentStatus = ConsentStatus.UPDATED
            }
            // // Show GDPR form cho user consent
            if (lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                mHandler.post {
                    val show = consentManager.show(this@MainActivity) { formError: FormError? ->
                        // Result from GDPR dialog
                        if (formError != null) {
                            DebugLog.loge("Show form error: " + formError.message)
                        }
                        mConsentStatus = ConsentStatus.GATHERED
                        consentCompleted()
                    }
                    if (show) {
                        mConsentStatus = ConsentStatus.SHOWING
                    } else {
                        onAdOPACompleted() // consentFormLoaded - Can't show GDPR
                    }
                }
            } else {
                onAdOPACompleted()  // consentFormLoaded - lifecycle.currentState is not RESUMED
            }
        }
    }

    private fun canShowAds(): Boolean {
        return !AdsConfig.getInstance().isFullVersion && mGoogleConsentManager.canRequestAds()
    }

    private fun initOtherAds() {
        mHandler.postDelayed({
            AdsModule.getInstance().showBannerExitDialog(null)
            AdsModule.getInstance().showBannerEmptyScreen(null)
        }, 2000)
    }

    // Init & show InterOPA
    private fun initInterstitialOPA() {
        AdsModule.getInstance().getInterstitialOPA(applicationContext)?.apply {
            if (AdsConfig.getInstance().canShowOPA()) {
                mOPAStatus = OPAStatus.LOADING
                mProgressBgResourceId = R.drawable.bg_default
                mOPAListener = this@MainActivity
                resetStates()
                // Show
                initAndShowOPA(this@MainActivity)
            } else {
                mOPAStatus = OPAStatus.COMPLETED
                preLoad()
            }
        }
    }

    /**
     * Đc gọi khi đã lấy được trạng thái của GDPR
     * */
    private fun consentCompleted() {
        if (isDestroyed) return
        if (canShowAds()) {
            /** Chỉ init OPA khi GDPR đã được user consent trước đó hoặc với user cũ sau khi consent xong */
            val initOPA = /*isCustomSplashVisible() &&*/ !isActivityRecreated
            initializeMobileAdsSdk(initOPA = initOPA)
        } else if (mOPAStatus != OPAStatus.COMPLETED) {
            onAdOPACompleted() // consentCompleted - can't show Ads
        }
    }

    private fun isCustomSplashVisible(): Boolean {
        return /*binding.viewSplash.root.isVisible()*/ false
    }

    override fun onAdOPACompleted() {
        hideCustomSplash()
        AdDebugLog.loge("$TAG ${this@MainActivity.hashCode()} - onAdOPACompleted \nmOPAStatus: $mOPAStatus, mConsentStatus: $mConsentStatus")
        if (mOPAStatus == OPAStatus.COMPLETED) return

        mOPAStatus = OPAStatus.COMPLETED

        if (isDestroyed) return

        // TODO: Request permissions or do thing
    }

    override fun getBottomAdContainer() = binding.frBottomAds

    private fun destroyAds() {
        AdsModule.getInstance().destroyAds(hashCode())
    }

    private fun showExitDialog() {
        if (dialogExitApp.isShowing()) return
        dialogExitApp.show(this)
    }

    private fun dismissExitDialog() {
        if (dialogExitApp.isShowing()) dialogExitApp.dismiss()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            if (AdsModule.getInstance().mInterstitialOPA?.isCounting() == true) {
                return
            }
            onQuitApp()
        }
    }

    private fun onQuitApp() {
        if (prefDataStore.isShowExitDialog) {
            showExitDialog()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyAds()
    }
}