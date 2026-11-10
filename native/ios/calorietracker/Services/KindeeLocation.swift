import Foundation
import Combine
import CoreLocation

@MainActor
final class KindeeLocation: NSObject, ObservableObject, CLLocationManagerDelegate {
    private let manager = CLLocationManager()
    @Published var place: KindeePlace?
    @Published var message: String?
    private var requested = false
    override init() { super.init(); manager.delegate = self; manager.desiredAccuracy = kCLLocationAccuracyHundredMeters }
    func locate() {
        requested = true
        switch manager.authorizationStatus {
        case .notDetermined: manager.requestWhenInUseAuthorization()
        case .authorizedWhenInUse, .authorizedAlways: manager.requestLocation()
        default: message = "Location is unavailable. Search by address instead."
        }
    }
    func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        guard requested else { return }
        if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways { manager.requestLocation() }
    }
    func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let l = locations.last, l.horizontalAccuracy >= 0, abs(l.timestamp.timeIntervalSinceNow) < 300 else { message = "No recent location. Try an address."; return }
        place = KindeePlace(label:"Current location",latitude:l.coordinate.latitude,longitude:l.coordinate.longitude,source:"device")
        requested = false
    }
    func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) { requested = false; message = "Could not locate this phone. Search by address instead." }
}
