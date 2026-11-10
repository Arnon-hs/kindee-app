# Nearby Google Maps places

The Android and iPhone pilots add a separate **Plan > Nearby > Find restaurants & stores** action. Choose an address or use the existing location permission flow first. Homa Phuket Town is the test location, not a hardcoded search destination. The button sends the selected coordinates to the Kindee cloud API. Apify credentials remain on that server.

The server searches a 2 km circle for restaurants and supermarkets, deduplicates and filters the results, and returns at most 20 places with names, categories, distance and allowed photo/Google Maps links. Successful results are cached for 24 hours. A new area needs an explicit search; background location changes do not start billable scraping. Check results again if the search is still running. Shared pilot limits are three starts per UTC day, eight per UTC month and USD 0.50 per run, with no automatic retry of an uncertain paid request.

These are directory listings. Their menus, calories and dietary suitability are unknown. They are kept separate from the existing menu observations and mixed cooking/cafe/delivery plan. Photos and names are third-party content, not a freely redistributable photo collection. Check opening, delivery address, availability and final prices in the merchant/provider app.

## Verification on 2026-11-11, Asia/Bangkok

| Level | Observed result |
| --- | --- |
| Live Kindee HTTPS API, Homa, 2 km | Eight restaurants and eight stores; 13 allowed photo links |
| Repeated POST to the same area | Identical cached result, no second actor run |
| Existing nearby-menu endpoint | 50 observations still available |
| Actual Apify charges | Two separate 16-place runs, USD 0.06405 each, existing credits; no subscription purchase |
| Backend PHP 8.3/PostGIS tests | 60 tests, 593 assertions passed in an isolated test database |
| Native Android | 1,000 unit tests passed and Debug APK built; test temp files on external disk |
| iPhone | Swift contracts and signed arm64 Simulator build passed |
| New Nearby screen on Simulator | Initial installs failed for disk space. A later install succeeded, then launch failed with a SpringBoard error at about 115 MiB free; no new UI acceptance claimed |
| Seeker | Not connected during this verification; fresh APK not installed on it |

Only the dedicated Kindee API container was updated. The first Maps release preserved all 44 other containers. The later public-contract release preserved all 47 containers in its fresh baseline; no migrations. Its public health/version matched release marker `3f1922815b12d2074ad2bd0e68f29b229c4fffb461f501ca24a511652c39b49c`. Cached places and the 50 existing menu observations remained available after that update. Temporary test databases, containers, sessions and the contract-registration account were removed.

The inspected FREE account charged USD 0.004 per place and USD 0.00005 for a 1 GB actor start. This is account-specific evidence, not a promise of future pricing or the advertised lowest price for other tiers. See the [provider listing](https://apify.com/compass/crawler-google-places).

The existing menu refresh and reviewed AI diary remain separate server features. This integration does not create a Grab feed, verify health-device synchronization or measure food nutrition.
