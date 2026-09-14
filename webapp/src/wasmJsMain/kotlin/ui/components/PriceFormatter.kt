package ui.components

import kotlin.math.abs

/** Format price in Indian Rupee conventions: Lakhs and Crores */
object PriceFormatter {
    fun formatRupees(price: Long): String {
        if (price < 0) return "-${formatRupees(abs(price))}"
        return when {
            price >= 10_000_000 -> {
                val crores = price / 10_000_000.0
                "₹${formatDecimal(crores)} Cr"
            }
            price >= 100_000 -> {
                val lakhs = price / 100_000.0
                "₹${formatDecimal(lakhs)} L"
            }
            price >= 1000 -> "₹${addCommas(price)}"
            else -> "₹$price"
        }
    }

    /** Format project prices which are in crores (float) */
    fun formatCrores(priceInCrores: Double): String {
        if (priceInCrores <= 0) return "N/A"
        return when {
            priceInCrores >= 1.0 -> "₹${formatDecimal(priceInCrores)} Cr"
            else -> {
                val lakhs = priceInCrores * 100
                "₹${formatDecimal(lakhs)} L"
            }
        }
    }

    fun formatRent(monthlyRent: Long): String {
        return when {
            monthlyRent >= 100_000 -> "₹${formatDecimal(monthlyRent / 100_000.0)} L/mo"
            monthlyRent >= 1000 -> "₹${addCommas(monthlyRent)}/mo"
            else -> "₹$monthlyRent/mo"
        }
    }

    fun formatArea(sqft: Int): String {
        if (sqft <= 0) return "N/A"
        // Detect likely sq meter values (< 100 sqft for normal apartments is unrealistic)
        return if (sqft < 100) {
            "${sqft} sq.m (${(sqft * 10.764).toInt()} sq.ft)"
        } else {
            "${addCommas(sqft.toLong())} sq.ft"
        }
    }

    private fun formatDecimal(value: Double): String {
        val rounded = (value * 100).toLong() / 100.0
        return if (rounded == rounded.toLong().toDouble()) {
            rounded.toLong().toString()
        } else {
            val s = rounded.toString()
            if (s.contains('.') && s.substringAfter('.').length > 2) {
                s.substring(0, s.indexOf('.') + 3)
            } else s
        }
    }

    private fun addCommas(n: Long): String {
        val s = n.toString()
        if (s.length <= 3) return s
        // Indian comma format: last 3 digits, then groups of 2
        val last3 = s.takeLast(3)
        val rest = s.dropLast(3)
        val groups = mutableListOf<String>()
        var i = rest.length
        while (i > 0) {
            val start = maxOf(0, i - 2)
            groups.add(0, rest.substring(start, i))
            i = start
        }
        return groups.joinToString(",") + ",$last3"
    }
}
