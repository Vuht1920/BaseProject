package com.tohsoft.ads

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Process
import android.os.SystemClock
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.blankj.utilcode.util.Utils
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAdView
import com.mmt.AdContainerWatcher
import com.mmt.NativeAdCenterLoader
import com.mmt.ads.AppOpenAdsHelper
import com.mmt.ads.config.AdsConfig
import com.mmt.ads.models.AdsId
import com.mmt.ads.models.AdsType
import com.mmt.ads.models.LoadingState
import com.mmt.ads.models.NativeAdType
import com.mmt.ads.utils.AdDebugLog
import com.mmt.ads.utils.AdsUtils
import com.mmt.ads.wapper.AdOPAListener
import com.mmt.ads.wapper.AdViewBottom
import com.mmt.ads.wapper.AdWrapper
import com.mmt.ads.wapper.AdWrapperListener
import com.mmt.ads.wapper.AdsInListHelper
import com.mmt.ads.wapper.InterstitialOPA
import com.mmt.ads.wapper.NativeAdViewWrapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

/**
 * Created by PhongNX on 6/8/2020.
 */
@SuppressLint("StaticFieldLeak")
class AdsModule private constructor() {
    companion object {
        @JvmStatic
        private val sInstance: AdsModule by lazy { AdsModule() }

        @JvmStatic
        fun getInstance(): AdsModule {
            return sInstance
        }
    }

    private var mApplication: Application? = null

    // Banner
//    private var mAdViewBottom: AdViewWrapper? = null

    // NativeAd
    private var mNativeAll: NativeAdViewWrapper? = null
    private var mNativeInList: NativeAdViewWrapper? = null

    // NativeAd in list
    private var mAdsInListHelper: AdsInListHelper? = null

    // Interstitial
    var mInterstitialOPA: InterstitialOPA? = null

    // AppOpenAd
    var mAppOpenAd: AppOpenAdsHelper? = null

    // Configs
    private var mAdsInListInstances = AdsInListHelper.MAX_ITEMS
    private var mIgnoreDestroyStaticAd = false
    private var mLoadingState = LoadingState.NONE
    private var mSession = 0

    val context: Context?
        get() = mApplication

    fun resetInitState() {
        mLoadingState = LoadingState.NONE
    }

    fun setSession(session: Int) {
        mSession = session
    }

    private fun initializeCompleted(): Boolean {
        return mLoadingState == LoadingState.FINISHED
    }

    /**
     * Set Application Context & initialize modules
     */
    @OptIn(DelicateCoroutinesApi::class)
    fun init(application: Application, callback: InitCallback? = null): AdsModule {
        try {
            mApplication = application
            if (mLoadingState == LoadingState.NONE) {
                mLoadingState = LoadingState.LOADING
                Thread { setWebViewDataDirectorySuffix(application) }.start()
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        val start = SystemClock.elapsedRealtime()
                        MobileAds.initialize(application) {
                            mLoadingState = LoadingState.FINISHED
                            AdDebugLog.loge("MobileAds initializationCompleted -> Take " + (SystemClock.elapsedRealtime() - start) + " ms")
                            GlobalScope.launch(Dispatchers.Main) {
                                callback?.onInitializeCompleted()
                            }
                        }
                        MobileAds.setAppMuted(true)
                        MobileAds.setAppVolume(0.0f)
                    } catch (e: Exception) {
                        AdDebugLog.loge(e)
                    }
                }
            }
            Utils.init(application)
            AdsConfig.getInstance().initAdsState(mApplication!!)

            if (initializeCompleted()) {
                callback?.onInitializeCompleted()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@AdsModule
    }

    private fun setWebViewDataDirectorySuffix(context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val processName = getProcessName(context)
                val packageName = context.packageName
                if (packageName != processName) {
                    WebView.setDataDirectorySuffix(processName!!)
                }
            }
        } catch (e: Exception) {
            AdDebugLog.loge(e)
        }
    }

    private fun getProcessName(context: Context?): String? {
        if (context == null) return null
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val appProcessInfoList = manager.runningAppProcesses
        if (appProcessInfoList.isNotEmpty()) {
            for (processInfo in appProcessInfoList) {
                if (processInfo!!.pid == Process.myPid()) {
                    return processInfo.processName
                }
            }
        }
        return null
    }

    /**
     * Visible or Invisible Ads
     * */
    fun onWindowFocusChanged() {
        val ads: List<AdWrapper?> = listOf(
//            mAdViewBottom,
            mNativeAll,
            mNativeInList
        )
        if (AdsConfig.getInstance().hasWindowFocus) {
            ads.forEach { adWrapper -> adWrapper?.visibleAds() }
            mMapBottomBanner.values.forEach { it?.get()?.visibleAds() }
        } else {
            ads.forEach { adWrapper -> adWrapper?.invisibleAds() }
            mMapBottomBanner.values.forEach { it?.get()?.invisibleAds() }
        }
        mAdsInListHelper?.onWindowFocusChanged()
    }

    /**
     * Set Application Context
     */
    fun setApplication(application: Application): AdsModule {
        try {
            mApplication = application
            Utils.init(application)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return this@AdsModule
    }

    /**
     * Set number of AD items that AdsInListHelper will store to display sequentially in the list
     */
    fun setAdsInListInstances(adsInListInstances: Int): AdsModule {
        if (mAdsInListInstances < adsInListInstances) {
            if (mAdsInListHelper != null) {
                mAdsInListHelper!!.refreshAdsInstance(adsInListInstances)
            }
        }
        mAdsInListInstances = adsInListInstances
        return this@AdsModule
    }

    private fun wrapHeightForContainer(container: ViewGroup?) {
        container?.let {
            // Set height cho container là WrapContent
            AdsUtils.setHeightForContainer(container, 0)
        }
    }

    /**
     * Banner
     *
     * PhongNX: 04/02/2025
     * Update logic load lại Ad khi vào MH cho tất cả các MH khác.
     * Khi vào MH sẽ load Ad lại từ đầu, pause/resume theo trạng thái của MH, destroy khi MH destroy.
     *
     * @param fragment: Truyền vào khi method được gọi để show Bottom Banner ở trong Fragment
     */
    @JvmOverloads
    fun showBannerBottom(container: ViewGroup?, fragment: Fragment? = null) {
        container?.let { adContainer ->
            val context = adContainer.context
            val lifecycle = fragment?.lifecycle ?: (context as? AppCompatActivity)?.lifecycle
            lifecycle?.let {
                showBannerBottomSpecial(context, AdsId.banner_bottom, lifecycle, container)
            }
        }
    }

    @JvmOverloads
    fun showBannerBottomOther(container: ViewGroup?, fragment: Fragment? = null) {
        showBannerBottom(container, fragment)
    }

    /**
     * PhongNX: 15/01/2025
     * Update logic riêng để show Banner cho các MH dùng id3.
     * Khi vào MH sẽ load Ad lại từ đầu, pause/resume theo trạng thái của MH, destroy khi MH destroy.
     *
     * @param fragment: Truyền vào khi method được gọi để show Bottom Banner ở trong Fragment
     * */
    @JvmOverloads
    fun showBannerBottomOther2(container: ViewGroup?, fragment: Fragment? = null) {
        container?.let { adContainer ->
            val context = adContainer.context
            val lifecycle = fragment?.lifecycle ?: (context as? AppCompatActivity)?.lifecycle
            lifecycle?.let {
                showBannerBottomSpecial(context, AdsId.banner_bottom_2, lifecycle, container)
            } ?: showBannerBottom(container)
        }
    }

    /**
     * AdView bottom theo logic riêng cho một số MH. Start loadAd khi vào MH, pause/resume theo trạng thái MH, destroy khi MH destroyed.
     * */
    private val mMapBottomBanner: HashMap<Int, WeakReference<AdViewBottom?>?> = hashMapOf()

    fun destroyAdViewBottom(adViewContainer: ViewGroup) {
        val hashCode = adViewContainer.hashCode()
        if (mMapBottomBanner.containsKey(hashCode)) {
            mMapBottomBanner[hashCode]?.get()?.destroy()
            mMapBottomBanner.remove(hashCode)
        }
    }

    private fun getBannerBottomSpecial(adViewContainer: ViewGroup): AdViewBottom? {
        val hashCode = adViewContainer.hashCode()
        if (mMapBottomBanner.containsKey(hashCode)) {
            return mMapBottomBanner[hashCode]?.get()
        }
        return null
    }

    private fun showBannerBottomSpecial(context: Context, adId: String, lifecycle: Lifecycle, adContainer: ViewGroup) {
        val hashCode = adContainer.hashCode()
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.BANNER_ALL)) {
            var banner = getBannerBottomSpecial(adContainer)
            if (banner == null) {
                banner = AdViewBottom(context, adId, lifecycle, adContainer)
                mMapBottomBanner[hashCode] = WeakReference(banner)
            }
            banner.showAd()
        } else {
            destroyAdViewBottom(adContainer)
            wrapHeightForContainer(adContainer)
        }
    }

    /**
     * NativeAdView style SMALL
     */
    @JvmOverloads
    fun showNativeBottomMain(container: ViewGroup?, nativeAdType: NativeAdType = NativeAdType.SMALL, isPriorityQueue: Boolean = true) {
    }

    @JvmOverloads
    fun showNativeBottomOtherScreen(container: ViewGroup?, priorityQueue: Boolean = true) {
    }

    @JvmOverloads
    fun showNativeBottomOtherScreen2(container: ViewGroup?, priorityQueue: Boolean = true) {
    }

    /**
     * NativeAdView in List (type SMALL)
     */
    @JvmOverloads
    fun showAdsInListForFirstItem(context: Context, container: ViewGroup?, nativeAdType: NativeAdType = NativeAdType.LIST_AUDIO) {
        getNativeInListMain(context)?.let { nativeAd ->
            container?.let { nativeAd.showAds(context, container, nativeAdType) }
            NativeAdCenterLoader.add(nativeAd, container != null)
        } ?: {
            container?.removeAllViews()
            wrapHeightForContainer(container)
        }
    }

    // Show nativeAd in list for first item
    fun getNativeInListMain(context: Context): NativeAdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LIST)) {
            if (mNativeInList == null) {
                mNativeInList = NativeAdViewWrapper(context, AdsId.native_in_list)
            }
            return mNativeInList
        }
        return null
    }

    fun showAdsInList(context: Context, container: ViewGroup, nativeAdType: NativeAdType = NativeAdType.LIST_AUDIO) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LIST)) {
            if (mAdsInListHelper == null) {
                mAdsInListHelper = AdsInListHelper(context, mAdsInListInstances)
                mAdsInListHelper?.initAllAds()
            }
            mAdsInListHelper?.apply { showAds(container, nativeAdType) }
        } else {
            container.removeAllViews()
            wrapHeightForContainer(container)
        }
    }

    @JvmOverloads
    fun showAdsInListVideo(context: Context, container: ViewGroup, nativeAdType: NativeAdType = NativeAdType.LIST_VIDEO) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LIST)) {
            showAdsInList(context, container, nativeAdType)
        } else {
            container.removeAllViews()
            wrapHeightForContainer(container)
        }
    }

    /**
     * NativeAdView in tab Settings
     * */
    fun showNativeSettingsScreen(context: Context, container: ViewGroup?) {
        if (AdsConfig.getInstance().canShowAd()) {
            container?.let { getNativeInListMain(context)?.showAds(context, container, NativeAdType.SETTINGS) }
        } else {
            container?.removeAllViews()
            wrapHeightForContainer(container)
        }
    }

    fun hideNativeSettingsScreen(container: ViewGroup?) {
        if (container?.childCount != 0) {
            (container?.getChildAt(0) as? NativeAdView)?.let { nativeAdView ->
                nativeAdView.destroy()
                container.removeAllViews()
            }
        }
    }

    /**
     * NativeAdView cho tab Lyrics - MH Playing Player
     * */
    @JvmOverloads
    fun showNativeAdLyricsEmpty(container: ViewGroup?, isPriorityQueue: Boolean = container != null) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LYRICS_EMPTY)) {
            mApplication?.let {
                getNativeLyrics(it)?.let { nativeLyrics ->
                    if (isPriorityQueue) {
                        nativeLyrics.showAds(it, container, NativeAdType.LYRICS_EMPTY)
                    } else {
                        NativeAdCenterLoader.add(nativeLyrics)
                    }
                }
            }
        } else {
            container?.removeAllViews()
        }
    }

    @JvmOverloads
    fun showNativeAdLyricsDialog(container: ViewGroup?, listener: AdWrapperListener?, isPriorityQueue: Boolean = container != null) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LYRICS_DIALOG)) {
            mApplication?.let {
                getNativeLyrics(it)?.let { nativeLyrics ->
                    nativeLyrics.addListener(listener)
                    if (isPriorityQueue) {
                        nativeLyrics.showAds(it, container, NativeAdType.LYRICS_DIALOG)
                    } else {
                        NativeAdCenterLoader.add(nativeLyrics)
                    }
                }
            }
        } else {
            container?.removeAllViews()
        }
    }

    fun removeNativeLyricsAdListener(listener: AdWrapperListener?) {
        mApplication?.let {
            getNativeLyrics(it)?.removeListener(listener)
        }
    }

    fun removeNativeLyricsContainer(container: ViewGroup?) {
        container?.let {
            getNativeLyrics(container.context)?.removeContainer(container)
        }
    }

    private fun getNativeLyrics(context: Context): NativeAdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_LYRICS_DIALOG)) {
            if (mNativeAll == null) {
                mNativeAll = NativeAdViewWrapper(context, AdsId.native_all)
            }
            return mNativeAll
        }
        return null
    }

    /**
     * NativeAdView tab Home
     */
    @JvmOverloads
    fun showNativeTabHome(container: ViewGroup?, isPriorityQueue: Boolean = true) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_TAB_HOME)) {
            mApplication?.let {
                getNativeTabHome(it)?.let { nativeTabHome ->
                    if (isPriorityQueue) {
                        nativeTabHome.showAds(it, container, NativeAdType.TAB_HOME)
                    } else {
                        NativeAdCenterLoader.add(nativeTabHome)
                    }
                }
            }
        } else {
            container?.removeAllViews()
        }
    }

    fun hideNativeTabHome(container: ViewGroup?) {
        container?.let {
            getNativeTabHome(it.context)?.removeContainer(container)
        }
    }

    fun getNativeTabHome(context: Context): NativeAdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_TAB_HOME)) {
            if (mNativeAll == null) {
                mNativeAll = NativeAdViewWrapper(context, AdsId.native_all)
            }
            return mNativeAll
        }
        return null
    }

    /**
     * NativeAdView empty screen
     */
    fun showNativeEmptyScreen(container: ViewGroup?) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_EMPTY_SCREEN)) {
            mApplication?.let {
                getNativeEmptyScreen(it)?.let { nativeEmpty ->
                    if (container != null) {
                        nativeEmpty.showAds(it, container, NativeAdType.EMPTY_SCREEN)
                    } else {
                        NativeAdCenterLoader.add(nativeEmpty)
                    }
                }
            }
        } else {
            container?.removeAllViews()
        }
    }

    private fun getNativeEmptyScreen(context: Context): NativeAdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_EMPTY_SCREEN)) {
            if (mNativeAll == null) {
                mNativeAll = NativeAdViewWrapper(context, AdsId.native_all)
            }
            return mNativeAll
        }
        return null
    }

    fun hideNativeEmptyScreen(container: ViewGroup?) {
        container?.let {
            getNativeEmptyScreen(it.context)?.removeContainer(container)
        }
    }

    /**
     * NativeAdView exit dialog
     * */
    fun showNativeExitDialog(container: ViewGroup?, listener: AdWrapperListener?) {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_EXIT_DIALOG)) {
            mApplication?.let {
                getNativeExitDialog(it)?.let { nativeExitDialog ->
                    nativeExitDialog.addListener(listener)
                    if (container != null) {
                        nativeExitDialog.showAds(it, container, NativeAdType.EXIT_DIALOG)
                    } else {
                        NativeAdCenterLoader.add(nativeExitDialog)
                    }
                }
            }
        } else {
            container?.removeAllViews()
            wrapHeightForContainer(container)
            listener?.onAdFailedToLoad(-404)
        }
    }

    private fun getNativeExitDialog(context: Context): NativeAdViewWrapper? {
        if (AdsConfig.getInstance().canShowAd() && initializeCompleted() && AdsConfig.getInstance().isAdEnable(AdsType.NATIVE_EXIT_DIALOG)) {
            if (mNativeAll == null) {
                mNativeAll = NativeAdViewWrapper(context, AdsId.native_all)
            }
            return mNativeAll
        }
        return null
    }

    fun removeExitDialogAdListener(listener: AdWrapperListener?) {
        mNativeAll?.removeListener(listener)
    }

    fun isNativeExitDialogLoading(context: Context): Boolean {
        return getNativeExitDialog(context)?.isLoading == true
    }

    fun isNativeExitDialogLoaded(context: Context): Boolean {
        return getNativeExitDialog(context)?.isAdAvailable() == true
    }

    /**
     * Interstitial OPA
     * */
    fun getInterstitialOPA(context: Context, opaListener: AdOPAListener? = null, adListener: AdWrapperListener? = null): InterstitialOPA? {
        /*if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.INTERSTITIAL_OPA)) {
            if (mInterstitialOPA == null) {
                mInterstitialOPA = InterstitialOPA(context)
            }
            opaListener?.let { mInterstitialOPA?.mOPAListener = opaListener }
            mInterstitialOPA?.addListener(adListener)
            return mInterstitialOPA
        }*/
        return null
    }

    /**
     * AppOpenAd
     * */
    fun getAppOpenAd(context: Context, opaListener: AdOPAListener? = null, adListener: AdWrapperListener? = null): AppOpenAdsHelper? {
        if (AdsConfig.getInstance().canShowAd() && AdsConfig.getInstance().isAdEnable(AdsType.APP_OPEN_ADS)) {
            if (mAppOpenAd == null) {
                mAppOpenAd = AppOpenAdsHelper(context)
            }
            opaListener?.let { mAppOpenAd?.mOPAListener = opaListener }
            mAppOpenAd?.addListener(adListener)
            return mAppOpenAd
        }
        return null
    }

    /* */
    fun setIgnoreDestroyStaticAd(ignoreDestroyStaticAd: Boolean) {
        mIgnoreDestroyStaticAd = ignoreDestroyStaticAd
    }

    fun destroyAllAds() {
        mIgnoreDestroyStaticAd = false
        destroyAds(mSession)
        mInterstitialOPA?.destroy()
        mInterstitialOPA = null
        mAppOpenAd?.destroy()
        mAppOpenAd = null
    }

    fun destroyAds(session: Int, forceDestroy: Boolean = false) {
        AdContainerWatcher.wrapHeightForAllContainer()
        AdContainerWatcher.clear()

        if (mIgnoreDestroyStaticAd) {
            mIgnoreDestroyStaticAd = false
            return
        }
        if (mSession != session) {
            AdDebugLog.loge("RETURN destroyAds when mSession != session")
            return
        }

        mMapBottomBanner.values.forEach { it?.get()?.destroy() }
        mMapBottomBanner.clear()

        if (AdsConfig.getInstance().isCacheAds && !forceDestroy) {
            AdDebugLog.loge(" -> Keep Ad instances, just remove Ad from container")
            // Just remove Ad from container -> try to cache loaded Ad instance to show it immediate when user open app again
            mNativeAll?.detachAdFromContainerWhenKill()
            mNativeInList?.detachAdFromContainerWhenKill()
            mAdsInListHelper?.detachAdFromContainerWhenKill()
        } else {
            AdDebugLog.loge(" -> Destroy Ad instances")
            // NativeAdCenter
            NativeAdCenterLoader.destroy()
            // Native tab Home
            mNativeAll?.destroy()
            mNativeAll = null
            // NativeAd in list
            mNativeInList?.destroy()
            mNativeInList = null
            mAdsInListHelper?.onDestroy()
            mAdsInListHelper = null
        }
    }

    interface InitCallback {
        fun onInitializeCompleted()
    }
}
