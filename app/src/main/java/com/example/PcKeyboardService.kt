package com.example

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class PcKeyboardService : InputMethodService(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private var clipboardManager: ClipboardManager? = null
    private val clipListener = ClipboardManager.OnPrimaryClipChangedListener {
        capturePrimaryClip()
    }

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private fun installViewTreeOwners() {
        try {
            window?.window?.decorView?.let { decorView ->
                decorView.setViewTreeLifecycleOwner(this)
                decorView.setViewTreeViewModelStoreOwner(this)
                decorView.setViewTreeSavedStateRegistryOwner(this)
            }
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error installing view tree owners on decorView", e)
        }
    }

    override fun onCreate() {
        super.onCreate()
        try {
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.currentState = Lifecycle.State.CREATED
            installViewTreeOwners()

            // Initialize clipboard monitor safely
            clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboardManager?.addPrimaryClipChangedListener(clipListener)
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error in onCreate", e)
        }
    }

    private fun capturePrimaryClip() {
        try {
            val settings = SettingsManager.getInstance(this)
            if (!settings.isClipboardEnabled.value) return

            val cm = clipboardManager ?: return
            if (!cm.hasPrimaryClip()) return
            val clip = cm.primaryClip ?: return
            if (clip.itemCount == 0) return

            // Respect Android 13+ sensitive content flag (e.g. passwords/OTPs)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (clip.description?.extras?.getBoolean(ClipDescription.EXTRA_IS_SENSITIVE, false) == true) {
                    return
                }
            }

            val text = clip.getItemAt(0)?.coerceToText(this)?.toString()?.trim() ?: return
            if (text.isNotEmpty() && text.length <= 4000) {
                serviceScope.launch {
                    val db = AppDatabase.getDatabase(this@PcKeyboardService)
                    db.clipboardDao().insert(ClipboardItem(text = text))
                    db.clipboardDao().trimHistory(settings.clipboardLimit.value)
                }
            }
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error capturing clipboard item", e)
        }
    }

    override fun onCreateInputView(): View {
        installViewTreeOwners()

        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@PcKeyboardService)
            setViewTreeViewModelStoreOwner(this@PcKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@PcKeyboardService)
            
            setContent {
                MyApplicationTheme {
                    KeyboardView(inputMethodService = this@PcKeyboardService)
                }
            }
        }

        installViewTreeOwners()
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        try {
            installViewTreeOwners()
            lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error in onStartInputView", e)
        }
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        try {
            lifecycleRegistry.currentState = Lifecycle.State.STARTED
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error in onFinishInputView", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            clipboardManager?.removePrimaryClipChangedListener(clipListener)
            serviceScope.cancel()
            lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
            store.clear()
        } catch (e: Exception) {
            Log.e("PcKeyboardService", "Error in onDestroy", e)
        }
    }
}
