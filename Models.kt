package sa.pricecompare.data

enum class Store(val nameAr: String) {
    AMAZON("أمازون السعودية"),
    NOON("نون"),
    NINJA("نينجا"),
    HUNGERSTATION("هنقرستيشن"),
    OTHAIM("أسواق العثيم"),
    QMARKET("كيو ماركت"),
    CARREFOUR("كارفور")
}

data class Product(
    val title: String,
    val price: Double,
    val store: Store,
    val url: String,
    val imageUrl: String?
)
