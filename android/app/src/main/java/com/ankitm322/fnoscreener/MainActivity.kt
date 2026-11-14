package com.ankitm322.fnoscreener

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.text.NumberFormat
import java.util.Locale

private const val PREFS_NAME = "screener_settings"
private const val API_BASE_KEY = "api_base_url"
private const val DEFAULT_API_BASE = "http://10.0.2.2:3000"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFF0A1220)
                ) {
                    ScreenerScreen(applicationContext)
                }
            }
        }
    }
}

private data class Stock(
    val rank: Int,
    val symbol: String,
    val ltp: Double,
    val tradedValue: Double,
    val volume: Double,
    val dayChange: Double,
    val low: Double,
    val high: Double
)

private data class ScreenerResponse(
    val source: String,
    val stocks: List<Stock>
)

@Composable
private fun ScreenerScreen(context: Context) {
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }
    var apiBase by remember { mutableStateOf(prefs.getString(API_BASE_KEY, DEFAULT_API_BASE) ?: DEFAULT_API_BASE) }
    var showSettings by remember { mutableStateOf(false) }
    var settingsInput by remember(apiBase) { mutableStateOf(apiBase) }
    var search by remember { mutableStateOf("") }
    var response by remember { mutableStateOf<ScreenerResponse?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(false) }
    var lastUpdated by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    suspend fun refresh() {
        loading = true
        error = null
        try {
            val result = withContext(Dispatchers.IO) { loadScreener(apiBase) }
            response = result
            lastUpdated = java.text.SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(java.util.Date())
        } catch (exception: Exception) {
            error = exception.message ?: "Unable to load market data"
        } finally {
            loading = false
        }
    }

    LaunchedEffect(apiBase) {
        refresh()
        while (true) {
            delay(60_000)
            refresh()
        }
    }

    val visibleStocks = response?.stocks.orEmpty().filter {
        it.symbol.contains(search.trim(), ignoreCase = true)
    }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("NSE F&O", color = Color(0xFF4FD1C5), style = MaterialTheme.typography.labelLarge)
                Text("Top traded value", color = Color.White, style = MaterialTheme.typography.headlineSmall)
            }
            OutlinedButton(onClick = { settingsInput = apiBase; showSettings = true }) {
                Text("API setup")
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2E))) {
            Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    when (response?.source) {
                        "fyers", "live" -> "● FYERS data"
                        "demo" -> "● Demo data"
                        else -> "● Connecting"
                    },
                    color = when (response?.source) {
                        "fyers", "live" -> Color(0xFF4ADE80)
                        "demo" -> Color(0xFFFBBF24)
                        else -> Color(0xFF94A3B8)
                    },
                    style = MaterialTheme.typography.titleMedium
                )
                Text("Updated: ${lastUpdated.ifBlank { "—" }}  ·  Auto-refresh: 60 sec", color = Color(0xFFA3B3CB))
            }
        }

        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search symbol") },
            singleLine = true
        )

        Button(
            onClick = { scope.launch { refresh() } },
            modifier = Modifier.fillMaxWidth(),
            enabled = !loading
        ) {
            if (loading) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp)
            else Text("Refresh now")
        }

        error?.let {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1D28))) {
                Column(Modifier.padding(16.dp)) {
                    Text("Could not load screener", color = Color(0xFFFCA5A5), style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = Color.White)
                    Text("Check that your API server is running and the URL is correct.", color = Color(0xFFA3B3CB))
                }
            }
        }

        Text("${visibleStocks.size} stocks · sorted by traded value", color = Color(0xFFA3B3CB))
        if (visibleStocks.isEmpty() && error == null && !loading) {
            Text("No matching stocks found.", color = Color(0xFFA3B3CB))
        }
        visibleStocks.forEachIndexed { index, stock ->
            StockCard(stock.copy(rank = index + 1))
        }

        Text("Market data is provided by your configured backend. Verify data freshness with your provider.", color = Color(0xFF718096), style = MaterialTheme.typography.bodySmall)
    }

    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Screener API URL") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Enter the base URL of the Node server that hosts /api/top-traded-fno.")
                    OutlinedTextField(
                        value = settingsInput,
                        onValueChange = { settingsInput = it },
                        label = { Text("Example: http://192.168.1.5:3000") },
                        singleLine = true
                    )
                    Text("Android emulator default: http://10.0.2.2:3000", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val cleaned = settingsInput.trim().trimEnd('/')
                    if (cleaned.startsWith("http://") || cleaned.startsWith("https://")) {
                        apiBase = cleaned
                        prefs.edit().putString(API_BASE_KEY, cleaned).apply()
                        showSettings = false
                    }
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showSettings = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun StockCard(stock: Stock) {
    val changeColor = if (stock.dayChange >= 0) Color(0xFF4ADE80) else Color(0xFFF87171)
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF101B2E))) {
        Column(Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("#${stock.rank}", color = Color(0xFF94A3B8))
                    Text(stock.symbol, color = Color.White, style = MaterialTheme.typography.titleLarge)
                }
                Text("${if (stock.dayChange > 0) "+" else ""}${formatDecimal(stock.dayChange)}%", color = changeColor)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("LTP", "₹${formatDecimal(stock.ltp)}")
                Metric("Traded value", formatRupees(stock.tradedValue))
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Metric("Volume", compactNumber(stock.volume))
                Metric("Day range", "${formatDecimal(stock.low)} – ${formatDecimal(stock.high)}")
            }
        }
    }
}

@Composable
private fun Metric(label: String, value: String) {
    Column {
        Text(label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.labelMedium)
        Text(value, color = Color.White, style = MaterialTheme.typography.bodyLarge)
    }
}

private fun loadScreener(baseUrl: String): ScreenerResponse {
    val endpoint = "${baseUrl.trimEnd('/')}/api/top-traded-fno?limit=50"
    val connection = URL(endpoint).openConnection() as HttpURLConnection
    connection.requestMethod = "GET"
    connection.connectTimeout = 10_000
    connection.readTimeout = 15_000
    try {
        val status = connection.responseCode
        val stream = if (status in 200..299) connection.inputStream else connection.errorStream
        val body = stream.bufferedReader().use { it.readText() }
        if (status !in 200..299) throw IllegalStateException("Server returned HTTP $status")
        val root = JSONObject(body)
        val array = root.optJSONArray("data") ?: org.json.JSONArray()
        val stocks = buildList {
            for (index in 0 until array.length()) {
                val item = array.optJSONObject(index) ?: continue
                add(
                    Stock(
                        rank = item.optInt("rank", index + 1),
                        symbol = item.optString("symbol", "Unknown"),
                        ltp = item.optDouble("ltp", 0.0),
                        tradedValue = item.optDouble("tradedValue", 0.0),
                        volume = item.optDouble("volume", 0.0),
                        dayChange = item.optDouble("dayChange", 0.0),
                        low = item.optDouble("low", 0.0),
                        high = item.optDouble("high", 0.0)
                    )
                )
            }
        }
        return ScreenerResponse(root.optString("source", "unknown"), stocks)
    } finally {
        connection.disconnect()
    }
}

private fun formatDecimal(value: Double): String = String.format(Locale.US, "%,.2f", value)

private fun compactNumber(value: Double): String = NumberFormat.getCompactNumberInstance(Locale("en", "IN"), NumberFormat.Style.SHORT).format(value)

private fun formatRupees(value: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    format.maximumFractionDigits = 1
    format.minimumFractionDigits = 0
    return format.format(value)
}
