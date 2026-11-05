import Capacitor

@objc(KinDeeBridgeViewController)
final class KinDeeBridgeViewController: CAPBridgeViewController {
    override func capacitorDidLoad() {
        bridge?.registerPluginInstance(KinDeeHealthPlugin())
    }
}
