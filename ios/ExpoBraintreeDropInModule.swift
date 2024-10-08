import ExpoModulesCore
import BraintreeDropIn
import Braintree

public class ExpoBraintreeDropInModule: Module {
    // See https://docs.expo.dev/modules/module-api
    public func definition() -> ModuleDefinition {
        Name("ExpoBraintreeDropIn")

        AsyncFunction("showDropIn") { (payload: Payload, token: String, promise: Promise) -> Void in

            let screen = UIApplication.shared.windows.filter {$0.isKeyWindow}.first
            let rootController = screen?.rootViewController?.presentedViewController;

            let moduleController = ExpoBraintreeDropInController()
            let bTController = moduleController.getDropInController(payload: payload, token: token, promise: promise)

            if (rootController == nil) {
                screen?.rootViewController?.present(bTController, animated: true)
            } else {
                rootController!.present(bTController, animated: true);
            }
        }.runOnQueue(.main)

        AsyncFunction("verify") { (payload: Payload, token: String, promise: Promise) -> Void in
          
            let screen = UIApplication.shared.windows.filter {$0.isKeyWindow}.first
            let rootController = screen?.rootViewController?.presentedViewController;
            let moduleController = ExpoBraintreeDropInController()
            moduleController.verify(payload: payload, token: token, promise: promise)

            if (rootController == nil) {
                screen?.rootViewController?.present(moduleController, animated: true)
            } else {
                rootController!.present(moduleController, animated: true);
            }
        }.runOnQueue(.main)
    }
}
