package sa.pricecompare

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import sa.pricecompare.ui.PriceTheme
import sa.pricecompare.ui.SearchScreen
import sa.pricecompare.ui.SearchViewModel

class MainActivity : ComponentActivity() {
    private val vm: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) handleDeepLink(intent)
        setContent { PriceTheme { SearchScreen(vm) } }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val q = intent?.data?.getQueryParameter("q") ?: return
        vm.setQuery(q)
        vm.search(q)
    }
}
