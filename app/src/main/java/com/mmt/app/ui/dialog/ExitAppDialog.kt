package com.mmt.app.ui.dialog

import android.annotation.SuppressLint
import android.app.Activity
import android.content.DialogInterface
import android.content.DialogInterface.OnDismissListener
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.customview.customView
import com.mmt.R
import com.mmt.ads.AdsModule
import com.mmt.ads.wrapper.AdWrapperListener
import com.mmt.app.data.repository.dataStore.PrefDataStore
import com.mmt.app.utils.dialog.DialogUtils
import com.mmt.app.utils.extensions.gone
import com.mmt.app.utils.extensions.invisible
import com.mmt.app.utils.extensions.visible
import com.mmt.app.utils.log.DebugLog
import com.mmt.databinding.DialogExitAppBinding
import javax.inject.Inject


class ExitAppDialog @Inject constructor() : DefaultLifecycleObserver, AdWrapperListener() {

    @Inject
    lateinit var prefDataStore: PrefDataStore

    private var mDialogExitApp: MaterialDialog? = null
    private var mActivity: AppCompatActivity? = null
    private val mHandler = Handler(Looper.getMainLooper())


    private var ivThankYou: View? = null
    private var adsContainer: ViewGroup? = null

    fun show(activity: AppCompatActivity) {
        mActivity = activity
        mActivity?.lifecycle?.addObserver(this)

        val binding = DialogExitAppBinding.inflate(activity.layoutInflater)

        // Show medium banner
        showMediumAd()

        // Checkbox never show again
        binding.cbNeverShowAgain.setOnCheckedChangeListener { _: CompoundButton?, isChecked: Boolean ->
            prefDataStore.setShowExitDialog(!isChecked)
        }
        try {
            mDialogExitApp = MaterialDialog(mActivity!!).show {
                title(R.string.msg_exit_app)
                customView(view = binding.root)
                    .negativeButton(R.string.action_cancel)
                    .positiveButton(R.string.action_yes) {
                        finishApplication(activity)
                    }
                    .onDismiss { dismissListener.onDismiss(null) }

            }
        } catch (e: Exception) {
            DebugLog.loge(e)
        }

    }

    private fun showMediumAd() {
        AdsModule.getInstance().getBannerExitDialog(mActivity)?.apply {
            addListener(this@ExitAppDialog)
            showMediumBanner(adsContainer)
        }
    }

    override fun onAdClicked() {
        super.onAdClicked()
        ivThankYou?.visible()
        adsContainer?.gone()
    }

    override fun onAdLoaded() {
        super.onAdLoaded()
        ivThankYou?.invisible()
        adsContainer?.let {
            it.visible()
            AdsModule.getInstance().getBannerExitDialog(mActivity)?.addAdsToContainer()
        }
    }

    override fun onAdFailedToLoad(error: Int) {
        super.onAdFailedToLoad(error)
        ivThankYou?.visible()
        adsContainer?.apply {
            removeAllViews()
            gone()
        }
    }

    fun isShowing(): Boolean {
        return mDialogExitApp != null && mDialogExitApp!!.isShowing
    }

    private val dismissListener = OnDismissListener {
        mActivity?.lifecycle?.removeObserver(this@ExitAppDialog)
        AdsModule.getInstance().getBannerExitDialog(mActivity)?.let { adViewWrapper ->
            adViewWrapper.removeListener(this@ExitAppDialog)
            adsContainer?.let { adViewWrapper.removeAdsFromContainer() }
        }
    }

    fun dismiss() {
        mDialogExitApp?.let { dialog ->
            if (dialog.isShowing) dialog.dismiss()
        }
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun finishApplication(activity: Activity?) {
        mHandler.postDelayed({ activity?.finish() }, 150)
    }

    override fun onResume(owner: LifecycleOwner) {
        super.onResume(owner)
        // Show medium banner
        showMediumAd()
    }

    override fun onDestroy(owner: LifecycleOwner) {
        super.onDestroy(owner)
        dismiss()
    }
}