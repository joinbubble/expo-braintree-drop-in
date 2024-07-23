import BraintreeDropIn
import ExpoModulesCore


public class ExpoBraintreeDropInAppDelegate: ExpoAppDelegateSubscriber {
    public func application(_ application: UIApplication,
                     didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {


        let BraintreeAppDelegatePaymentsURLScheme = Bundle.main.bundleIdentifier! + ".payment"
        BTAppContextSwitcher.setReturnURLScheme(BraintreeAppDelegatePaymentsURLScheme)

        return true
    }
}
