package io.legado.app.base

import android.graphics.Color
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import io.legado.app.R
import io.legado.app.lib.theme.ColorUtils
import io.legado.app.lib.theme.ThemeStore
import io.legado.app.utils.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel


abstract class BaseActivity<VM : ViewModel>(private val fullScreen: Boolean = true) : AppCompatActivity(), CoroutineScope by MainScope() {

    protected abstract val viewModel: VM

    protected abstract val layoutID: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        window.decorView.disableAutoFill()
        initTheme()
        setupSystemBar()
        super.onCreate(savedInstanceState)
        setContentView(layoutID)
        onViewModelCreated(viewModel, savedInstanceState)
        observeLiveBus()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    abstract fun onViewModelCreated(viewModel: VM, savedInstanceState: Bundle?)

    final override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return menu?.let {
            val bool = onCompatCreateOptionsMenu(it)
            it.setIconColor(this)
            bool
        } ?: super.onCreateOptionsMenu(menu)
    }


    open fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        return super.onCreateOptionsMenu(menu)
    }

    final override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        item?.let {
            if (it.itemId == android.R.id.home) {
                supportFinishAfterTransition()
                return true
            }
        }
        return item != null && onCompatOptionsItemSelected(item)
    }

    open fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    private fun initTheme() {
        window.decorView.setBackgroundColor(ThemeStore.backgroundColor(this))
        if (ColorUtils.isColorLight(ThemeStore.primaryColor(this))) {
            setTheme(R.style.AppTheme_Light)
        } else {
            setTheme(R.style.AppTheme_Dark)
        }
    }

    private fun setupSystemBar() {
        if (fullScreen) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION)
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            if (getPrefBoolean("transparentStatusBar")) {
                window.statusBarColor = Color.TRANSPARENT
            } else {
                window.statusBarColor = getCompatColor(R.color.status_bar_bag)
            }
        } else {
            window.statusBarColor = ThemeStore.statusBarColor(this, getPrefBoolean("transparentStatusBar"))
        }
    }

    open fun observeLiveBus() {

    }

    override fun finish() {
        currentFocus?.hideSoftInput()
        super.finish()
    }
}