# Restaurant handoff pilot

Checked on a physical Solana Seeker, Android 16, on 2026-11-11, Asia/Bangkok. This page describes the native Android pilot; no new iPhone runtime check is claimed.

## Grab

For the approved Homa Phuket Town pilot destination, Nearby includes a verified link to `ตะตีโด - หลังราชภัฏ`. The link came from that restaurant's Share action in Grab, rather than a guessed search URL. Open Grab targets the installed Grab package and opens the same restaurant. The app displayed its current menu and required food options. The delivery destination was manually set to Homa Phuket Town Hotel Lobby Entrance inside Grab. Kindee does not silently replace the provider's delivery address.

The server registry separates the restaurant link from short-lived prices. One private phone observation of fried rice, from 69 THB with a required meat choice, was used for the check. When that observation expires, Kindee keeps an unpriced restaurant card with no calorie estimate and asks the user to confirm the address in Grab. It is excluded from automatic meal planning. Destination scope is 50 m around the tested Homa address; this is not general Grab coverage or an automatic menu API.

The live expiry control hid the observed price while keeping Open Grab usable. The original observation expiry was then restored. Provider login and checkout remain inside Grab. No item was added to a cart and no order was placed.

## LINE MAN and Wongnai

The installed LINE MAN 16.25.1 initially stalled on its own Home screen as well as after restaurant links. Location permissions were denied. Enabling location while the app is in use exposed the login flow. The supplied LINE QR confirmation returned an unexpected-error message and the app returned to Continue with LINE. The Play Store offered Open, with no update available. The internal cause of that authentication error is not established.

The mobile Wongnai delivery-order route also redirects back into LINE MAN. Therefore Menu details > Open web menu uses the separate Wongnai restaurant-menu page for the same business ID. The native LINE MAN action remains available separately. Neither a working menu page nor an intent resolution proves successful LINE MAN login or checkout.

## Verification

- Native Android Debug APK built; 1,004 unit tests passed without failures, errors or skips. Four link-contract tests cover supplier/restaurant identity, browser route, invalid origins, credentials/ports and unsafe or duplicate redirect parameters.
- The final private backend suite passed on PHP 8.3.36/PostGIS: 65 tests, 626 assertions, using an isolated temporary database.
- Backend regressions cover fresh/expired observations, destination and account scope, durable links without stale prices, duplicate avoidance and malformed registry configuration.
- Only the Kindee API was redeployed. Other containers retained their IDs and start times. Provider sessions, keys and private observations are not included in this repository or APK.

## Sources and limits

- [Actual Grab restaurant share link](https://app.grab.com/s/1VFAo3gb), verified in the installed app. A desktop browser can redirect this link to Grab's general site; that is not proof that the restaurant menu opened.
- [Wongnai restaurant menu](https://www.wongnai.com/restaurants/3361848Uq/menu).
- [Wongnai delivery-order page](https://www.wongnai.com/delivery/businesses/3361848Uq/order), which can route mobile users into LINE MAN.

Menu prices, food options, delivery eligibility, opening times and links can change. Their sources and check times remain distinct from recipe nutrition and AI estimates. This release adds Android handoff behavior and a bounded server registry, not a provider SDK, a universal restaurant catalog or unattended ordering.
