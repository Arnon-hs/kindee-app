package com.apoorvdarshan.calorietracker.kindee

import java.net.URI
import java.net.URLDecoder

internal data class KindeeOrderLink(val web: String, val app: String? = null, val packageName: String? = null)

internal fun kindeeOrderLink(provider: String, value: String): KindeeOrderLink? = runCatching {
    require(value.length <= 2048)
    val url = URI(value)
    require(url.scheme == "https" && url.userInfo == null && url.port == -1 && url.fragment == null)
    val host = url.host?.lowercase(java.util.Locale.ROOT) ?: return null
    val web = URI("https", host, url.path, null, null).toASCIIString()
    when (provider) {
        "line_man" -> {
            require(host == "www.wongnai.com")
            val id = Regex("/delivery/businesses/([0-9]{1,12}[A-Za-z]{2})/order/?").matchEntire(url.path)?.groupValues?.get(1) ?: return null
            KindeeOrderLink("https://www.wongnai.com/restaurants/$id/menu", "com.linecorp.linemanth://app/service/food/r/$id", "com.linecorp.linemanth")
        }
        "grab" -> {
            require(host in setOf("app.grab.com", "food.grab.com"))
            when {
                host == "app.grab.com" && Regex("/s/[A-Za-z0-9]{4,32}").matches(url.path) ->
                    KindeeOrderLink(web, web, "com.grabtaxi.passenger")
                host == "food.grab.com" && Regex("/th/(?:en|th)/restaurant/[^/]+/[1-9]-[A-Z0-9]{8,40}/?").matches(url.path) ->
                    KindeeOrderLink(web, web, "com.grabtaxi.passenger")
                host == "app.grab.com" && url.path == "/open" -> {
                    val params = url.rawQuery.orEmpty().split('&').map { part ->
                        val pair = part.split('=', limit = 2)
                        URLDecoder.decode(pair[0], "UTF-8") to URLDecoder.decode(pair.getOrElse(1) { "" }, "UTF-8")
                    }
                    val ids = params.filter { it.first == "merchantIDs" }
                    val screens = params.filter { it.first == "screenType" }
                    require(ids.size == 1 && screens.size == 1 && screens.single().second == "GRABFOOD")
                    val id = ids.single().second
                    require(Regex("[1-9]-[A-Z0-9]{8,40}").matches(id))
                    KindeeOrderLink("https://app.grab.com/open?screenType=GRABFOOD&merchantIDs=$id", "grab://open?screenType=GRABFOOD&merchantIDs=$id", "com.grabtaxi.passenger")
                }
                else -> null
            }
        }
        "merchant_site" -> {
            require(host == "vivarooftopandrestaurant.com" && url.path.startsWith("/menu"))
            KindeeOrderLink(web)
        }
        else -> null
    }
}.getOrNull()
