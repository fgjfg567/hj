package sa.pricecompare.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

interface StoreSource {
    val store: Store
    suspend fun search(query: String): List<Product>
}

private val client = OkHttpClient.Builder()
    .connectTimeout(12, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

private const val UA =
    "Mozilla/5.0 (Linux; Android 14; Pixel 8) AppleWebKit/537.36 " +
        "(KHTML, like Gecko) Chrome/124.0 Mobile Safari/537.36"

/** Parses "ر.س ١٬٢٩٩٫٥٠" / "SAR 1,299.50" -> 1299.5 */
fun parsePrice(text: String): Double? {
    val norm = buildString {
        for (c in text) when (c) {
            in '\u0660'..'\u0669' -> append('0' + (c - '\u0660'))
            '\u066B' -> append('.')
            '\u066C', ',' -> {}
            else -> append(c)
        }
    }
    return Regex("\\d+(\\.\\d+)?").find(norm)?.value?.toDoubleOrNull()
}

/**
 * Generic live HTML scraper. Each store differs only by URL + CSS selectors.
 * Selectors are best-effort: store sites change often, so tune them here.
 */
class HtmlStoreSource(
    override val store: Store,
    private val searchUrl: String,          // %s = encoded query
    private val baseUrl: String,
    private val item: String,
    private val title: String,
    private val price: String,
    private val link: String = "a",
    private val image: String = "img"
) : StoreSource {

    override suspend fun search(query: String): List<Product> = withContext(Dispatchers.IO) {
        val url = searchUrl.format(URLEncoder.encode(query, "UTF-8"))
        val req = Request.Builder().url(url)
            .header("User-Agent", UA)
            .header("Accept-Language", "ar-SA,ar;q=0.9,en;q=0.8")
            .build()
        val html = client.newCall(req).execute().use { r ->
            if (!r.isSuccessful) error("HTTP ${r.code}")
            r.body?.string().orEmpty()
        }
        val doc = Jsoup.parse(html, baseUrl)
        doc.select(item).mapNotNull { el ->
            val t = el.selectFirst(title)?.text()?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val p = el.selectFirst(price)?.text()?.let(::parsePrice) ?: return@mapNotNull null
            val href = el.selectFirst(link)?.absUrl("href")?.takeIf { it.isNotBlank() } ?: url
            val img = el.selectFirst(image)?.let {
                it.absUrl("src").ifBlank { it.absUrl("data-src") }
            }?.takeIf { it.isNotBlank() }
            Product(t, p, store, href, img)
        }.take(30)
    }
}

object StoreSources {
    val all: List<StoreSource> = listOf(
        HtmlStoreSource(
            Store.AMAZON, "https://www.amazon.sa/s?k=%s", "https://www.amazon.sa",
            item = "div[data-component-type=s-search-result]",
            title = "h2 span", price = ".a-price .a-offscreen",
            link = "h2 a, a.a-link-normal", image = "img.s-image"
        ),
        HtmlStoreSource(
            Store.NOON, "https://www.noon.com/saudi-ar/search/?q=%s", "https://www.noon.com",
            item = "[data-qa^=plp-product-box], div[class*=productContainer]",
            title = "[data-qa=plp-product-box-name], [class*=name]",
            price = "strong[class*=amount], [class*=price]"
        ),
        HtmlStoreSource(
            Store.NINJA, "https://www.ninja.sa/search?q=%s", "https://www.ninja.sa",
            item = "[class*=product-card], [class*=ProductCard]",
            title = "[class*=name], [class*=title]", price = "[class*=price]"
        ),
        HtmlStoreSource(
            Store.HUNGERSTATION, "https://hungerstation.com/sa-ar/search?query=%s", "https://hungerstation.com",
            item = "[class*=product], [class*=item-card]",
            title = "[class*=name], [class*=title]", price = "[class*=price]"
        ),
        HtmlStoreSource(
            Store.OTHAIM, "https://www.othaimmarkets.com/search?q=%s", "https://www.othaimmarkets.com",
            item = "[class*=product-item], [class*=product-card]",
            title = "[class*=name], [class*=title]", price = "[class*=price]"
        ),
        HtmlStoreSource(
            Store.QMARKET, "https://qmarket.sa/search?q=%s", "https://qmarket.sa",
            item = "[class*=product-item], [class*=product-card]",
            title = "[class*=name], [class*=title]", price = "[class*=price]"
        ),
        HtmlStoreSource(
            Store.CARREFOUR, "https://www.carrefourksa.com/mafsau/ar/search?keyword=%s", "https://www.carrefourksa.com",
            item = "[data-testid=product_card], [class*=product-card]",
            title = "[class*=name], [class*=title], h3", price = "[class*=price]"
        )
    )
}
