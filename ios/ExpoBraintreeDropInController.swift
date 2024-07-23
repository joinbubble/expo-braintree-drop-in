import ExpoModulesCore
import BraintreeDropIn
import Braintree

struct Result {
    var nonce: String
    var description: String
    var error: String
}

class ExpoBraintreeDropInController: UIViewController, BTThreeDSecureRequestDelegate, BTViewControllerPresentingDelegate, PKPaymentAuthorizationViewControllerDelegate {
    private var promise: Promise? = nil
    private var btApiClient: BTAPIClient? = nil
    
    func paymentDriver(_ driver: Any, requestsPresentationOf viewController: UIViewController) {
        self.reactRoot()?.present(viewController, animated: true)
    }

    func paymentDriver(_ driver: Any, requestsDismissalOf viewController: UIViewController) {
        self.reactRoot()?.present(viewController, animated: true)
    }

    public func onLookupComplete(_ request: BTThreeDSecureRequest, lookupResult result: BTThreeDSecureResult, next: @escaping () -> Void) {
        next()
    }

    func getDropInController(payload: Payload, token: String, promise: Promise) -> UIViewController{
        let request = BTDropInRequest()
        let threeDSecureRequest = self.generateThreeDSecureRequest(payload: payload)

        request.threeDSecureRequest = threeDSecureRequest
        request.paypalDisabled = true // Disable PayPal option
        request.applePayDisabled = !payload.showMobilePay

        let dropInController = BTDropInController(authorization: token, request: request)
        { (controller, result, error) in
            if let error = error {
                promise.reject(error)
            } else if let result = result {
                if result.isCanceled {
                    let cancelledError = NSError(domain: "", code: 400, userInfo: [ NSLocalizedDescriptionKey: "Transaction cancelled by user"])
                    promise.reject(cancelledError)
                } else if result.paymentMethodType == .applePay {
                    self.promise = promise
                    self.btApiClient = BTAPIClient(authorization: token)
                    let paymentRequest = self.createApplePayRequest(payload: payload)
                    let applePayController = PKPaymentAuthorizationViewController(paymentRequest: paymentRequest)
                    applePayController!.delegate = self

                    self.reactRoot()?.present(applePayController!, animated: true)
                    
                } else {
                    let selectedPaymentMethod = result.paymentMethod
                    promise.resolve(selectedPaymentMethod?.nonce ?? "")
                }
            }
            controller.dismiss(animated: true, completion: nil)
        }

        return dropInController!
    }

    func verify(payload: Payload, token: String, promise: Promise)-> Void{
        let threeDSecureRequest = self.generateThreeDSecureRequest(payload: payload)

        if (payload.nonce.isEmpty) {
            let noNonceError = NSError(domain: "", code: 400, userInfo: [ NSLocalizedDescriptionKey: "No nonce provided"])
            promise.reject(noNonceError)
            return
        }

        threeDSecureRequest.nonce = payload.nonce

        let braintreeApiClient = BTAPIClient(authorization: token)
        let paymentFlowDriver = BTPaymentFlowDriver(apiClient: braintreeApiClient!)

        paymentFlowDriver.viewControllerPresentingDelegate = self

        paymentFlowDriver.startPaymentFlow(threeDSecureRequest, completion: {(result, error) in
            let threeDSecureResult = result as! BTThreeDSecureResult

            if let error = error {
                promise.reject(error)
                return
            }

            guard let tokenizedCard = threeDSecureResult.tokenizedCard else {
                let noTokenisedCardError = NSError(domain: "", code: 400, userInfo: [ NSLocalizedDescriptionKey: "No tokenised card returned"])
                promise.reject(noTokenisedCardError)
                return
            }

            promise.resolve(tokenizedCard.nonce)
        })
    }
    
    func paymentAuthorizationViewController(_ controller: PKPaymentAuthorizationViewController,
        didAuthorizePayment payment: PKPayment,
        handler completion: @escaping (PKPaymentAuthorizationResult) -> Void) {
            if (self.promise == nil) {
                let noPromiseError = NSError(domain: "", code: 500, userInfo: [ NSLocalizedDescriptionKey: "No promise to resolve"])

                
                completion(PKPaymentAuthorizationResult(status: .failure, errors: [noPromiseError]))
                return
            }
        
            if (self.btApiClient == nil) {
                let noClientError = NSError(domain: "", code: 500, userInfo: [ NSLocalizedDescriptionKey: "No connected Braintree client"])

                self.promise!.reject(noClientError)
                completion(PKPaymentAuthorizationResult(status: .failure, errors: [noClientError]))
                return
            }
        
            let applePayClient = BTApplePayClient(apiClient: self.btApiClient!)
            
            // Tokenize the Apple Pay payment
            applePayClient.tokenizeApplePay(payment) { (tokenizedApplePayPayment, error) in
                if error != nil {
                    self.promise!.reject(error!)
                    completion(PKPaymentAuthorizationResult(status: .failure, errors: nil))
                    return
                }

                self.promise!.resolve(tokenizedApplePayPayment?.nonce)
                completion(PKPaymentAuthorizationResult(status: .success, errors: nil))
            }
    }

    func paymentAuthorizationViewControllerDidFinish(_ controller : PKPaymentAuthorizationViewController) {
        controller.dismiss(animated: true, completion: nil)
    }
    
    private func generateThreeDSecureRequest(payload: Payload) -> BTThreeDSecureRequest {
        let threeDSecureRequest = BTThreeDSecureRequest()
        threeDSecureRequest.threeDSecureRequestDelegate = self

        let formattedAmount = String(format: "%.2f", payload.amount)
        let decimalNumber = NSDecimalNumber(string: formattedAmount)
        threeDSecureRequest.amount = decimalNumber
        threeDSecureRequest.email = payload.email
        threeDSecureRequest.versionRequested = .version2

        let address = BTThreeDSecurePostalAddress()
        address.givenName = payload.givenName
        address.surname = payload.surname
        address.streetAddress = payload.streetAddress
        address.postalCode = payload.postalCode
        address.countryCodeAlpha2 = payload.countryCode
        threeDSecureRequest.billingAddress = address

        // Optional additional information.
        // For best results, provide as many of these elements as possible.
        let additionalInformation = BTThreeDSecureAdditionalInformation()
        additionalInformation.shippingAddress = address
        threeDSecureRequest.additionalInformation = additionalInformation

        return threeDSecureRequest
    }

    private func reactRoot() -> UIViewController? {
        let screen = UIApplication.shared.windows.filter {$0.isKeyWindow}.first
        let rootController = screen?.rootViewController?.presentedViewController;

        if (rootController != nil) {
            return rootController
        }

        return screen?.rootViewController
    }
    
    private func createApplePayRequest(payload: Payload) -> PKPaymentRequest {
        let paymentRequest = PKPaymentRequest()
        let merchantIdentifier = Bundle.main.object(forInfoDictionaryKey: "BRAINTREE_MERCHANT_ID") as? String
        let applePayClient = BTApplePayClient(apiClient: self.btApiClient!)
        
        paymentRequest.requiredBillingContactFields = [.postalAddress]

        paymentRequest.merchantIdentifier = merchantIdentifier ?? ""
        paymentRequest.merchantCapabilities = .capability3DS
        paymentRequest.countryCode = payload.countryCode
        paymentRequest.currencyCode = payload.currencyCode
        
        // @todo make the label dynamic
        paymentRequest.paymentSummaryItems = [
            PKPaymentSummaryItem(label: payload.transactionLabel, amount: NSDecimalNumber(value: payload.amount))
        ]
        
        paymentRequest.supportedNetworks = [PKPaymentNetwork.amex, PKPaymentNetwork.visa, PKPaymentNetwork.masterCard, PKPaymentNetwork.discover, PKPaymentNetwork.maestro]
        
        return paymentRequest
    }
}

enum ModuleError: Error {
    case invalidBraintreeResponse
    case invalidArgumentType
    case missingArgument(String)
}
