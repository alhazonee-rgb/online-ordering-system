package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.AlMokhtarViewModel
import com.example.ui.MainScreen
import com.example.ui.theme.MokhtarTheme

class MainActivity : ComponentActivity() {

    private val viewModel: AlMokhtarViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle possible deep link URI: almokhtar://order?b=mansourah&t=4 or https://almokhtar.sa/order?b=...
        intent?.data?.let { uri ->
            val branch = uri.getQueryParameter("b") ?: uri.getQueryParameter("branch")
            val table = uri.getQueryParameter("t") ?: uri.getQueryParameter("table")
            val sig = uri.getQueryParameter("sig")
            if (!branch.isNullOrBlank() && !table.isNullOrBlank()) {
                val tableNum = table.toIntOrNull() ?: 1
                viewModel.bindBranchAndTable(branch, tableNum, sig)
            }
        }

        setContent {
            MokhtarTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainScreen(viewModel = viewModel)
                }
            }
        }
    }
}
