package com.apoorvdarshan.calorietracker.kindee

import org.junit.Assert.*
import org.junit.Test

class KindeeOrderLinkTest {
    @Test fun lineManUsesTheRestaurantInTheHttpsPathAndStripsAppRedirectsForWeb() {
        val link = kindeeOrderLink("line_man", "https://www.wongnai.com/delivery/businesses/3361848Uq/order?af_dp=com.linecorp.linemanth%3A%2F%2Fapp%2Fservice%2Ffood%2Fr%2F99999Ab")!!
        assertEquals("com.linecorp.linemanth://app/service/food/r/3361848Uq", link.app)
        assertEquals("https://www.wongnai.com/restaurants/3361848Uq/menu", link.web)
    }

    @Test fun grabShareKeepsTheExactVerifiedRestaurantLinkAndTargetsGrab() {
        val link = kindeeOrderLink("grab", "https://app.grab.com/s/1VFAo3gb")!!
        assertEquals("https://app.grab.com/s/1VFAo3gb", link.app)
        assertEquals("com.grabtaxi.passenger", link.packageName)
    }

    @Test fun grabOpenRequiresOneFoodMerchantAndDoesNotForwardArbitraryDeepLinks() {
        val link = kindeeOrderLink("grab", "https://app.grab.com/open?screenType=GRABFOOD&merchantIDs=3-C1234567890EXAMPLE&af_dp=tel%3A123")!!
        assertEquals("grab://open?screenType=GRABFOOD&merchantIDs=3-C1234567890EXAMPLE", link.app)
        assertFalse(link.web.contains("af_dp"))
        assertNull(kindeeOrderLink("grab", "https://app.grab.com/open?screenType=GRABMART&merchantIDs=3-C1234567890EXAMPLE"))
        assertNull(kindeeOrderLink("grab", "https://app.grab.com/open?screenType=GRABFOOD&merchantIDs=3-C1234567890EXAMPLE&merchantIDs=3-C9999999999EXAMPLE"))
    }

    @Test fun rejectsUntrustedOriginsCredentialsPortsAndUnrelatedProviderRoutes() {
        for (url in listOf("http://app.grab.com/s/1VFAo3gb", "https://app.grab.com.evil.test/s/1VFAo3gb", "https://user@app.grab.com/s/1VFAo3gb", "https://app.grab.com:443/s/1VFAo3gb", "https://app.grab.com/s/1VFAo3gb#other", "https://app.grab.com/account")) {
            assertNull(url, kindeeOrderLink("grab", url))
        }
        assertNull(kindeeOrderLink("line_man", "https://www.wongnai.com/restaurants/3361848Uq"))
        assertNull(kindeeOrderLink("line_man", "https://app.grab.com/s/1VFAo3gb"))
    }
}
