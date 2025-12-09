package com.example.budgetplanner.util

import com.example.budgetplanner.R

object IconUtils {
    val icons = mapOf(
        "restaurant" to R.drawable.ic_restaurant,
        "bus" to R.drawable.ic_bus,
        "receipt" to R.drawable.ic_receipt,
        "heart" to R.drawable.ic_heart,
        "celebration" to R.drawable.ic_celebration,
        "shopping_cart" to R.drawable.ic_shopping_cart,
        "home" to R.drawable.ic_home,
        "work" to R.drawable.ic_work,
        "school" to R.drawable.ic_school,
        "sports" to R.drawable.ic_sports,
        "flight" to R.drawable.ic_flight,
        "gas_station" to R.drawable.ic_gas_station,
        "phone" to R.drawable.ic_phone,
        "wifi" to R.drawable.ic_wifi,
        "fitness" to R.drawable.ic_fitness,
        "movie" to R.drawable.ic_movie,
        "music" to R.drawable.ic_music,
        "book" to R.drawable.ic_book,
        "pets" to R.drawable.ic_pets,
//        "childcare" to R.drawable.ic_childcare,
        "default" to R.drawable.ic_receipt // Fallback
    )

    fun getIconResId(iconName: String): Int {
        return icons[iconName] ?: R.drawable.ic_receipt
    }
}
