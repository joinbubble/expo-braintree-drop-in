package expo.modules.braintreedropin

import android.content.Context
import android.content.pm.PackageManager
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.BraintreeClient
import com.braintreepayments.api.DropInRequest
import com.braintreepayments.api.GooglePayRequest
import com.braintreepayments.api.ThreeDSecureAdditionalInformation
import com.braintreepayments.api.ThreeDSecureClient
import com.braintreepayments.api.ThreeDSecurePostalAddress
import com.braintreepayments.api.ThreeDSecureRequest
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.WalletConstants
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import expo.modules.kotlin.functions.Queues
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition


class ExpoBraintreeDropInModule : Module() {
  private val context: Context
    get() = requireNotNull(appContext.reactContext) { "React Application Context is null" }

  private val currentActivity
    get() = requireNotNull(appContext.activityProvider?.currentActivity) { "No current activity set" }

  // See https://docs.expo.dev/modules/module-api
  override fun definition() = ModuleDefinition {
    Name("ExpoBraintreeDropIn")

    // Defines a JavaScript synchronous function that runs the native code on the JavaScript thread.
    AsyncFunction("showDropIn") { payload: Payload, token: String, promise: Promise ->
      if (token.isEmpty()) {
        promise.reject(CodedException("You must provide a client token"))

        return@AsyncFunction
      }

      val threeDSecureRequest = this@ExpoBraintreeDropInModule.generateThreeDSecureRequest(payload)
      val dropInRequest = DropInRequest()
      dropInRequest.threeDSecureRequest = threeDSecureRequest
      dropInRequest.isPayPalDisabled = true // Disable PayPal option
      dropInRequest.isVaultManagerEnabled = payload.vaultManagerEnabled

      if (payload.showMobilePay) {
        val googlePayRequest = generateGooglePayRequest(payload)
        dropInRequest.googlePayRequest = googlePayRequest
      } else {
        dropInRequest.isGooglePayDisabled = true
      }

      val listener = ExpoBraintreeDropInListener(promise)

      val tokenProvider = ExpoBraintreeDropInTokenProvider.getInstance()
      tokenProvider.token = token
      val dropInClient = ExpoBraintreeDropInClientHolder.getInstance().braintreeDropInClient
      dropInClient.setListener(listener)

      try {
        dropInClient.launchDropIn(dropInRequest)
      } catch (exception: Exception) {
        promise.reject(CodedException(exception.message, exception))
      }
    }.runOnQueue(Queues.MAIN)

    AsyncFunction("verify") { payload: Payload, token: String, promise: Promise ->
      if (token.isEmpty()) {
        promise.reject(CodedException("You must provide a client token"))

        return@AsyncFunction
      }

      if (payload.nonce.isEmpty()) {
        promise.reject(CodedException("You must provide a nonce to verify"))

        return@AsyncFunction
      }

      val threeDSecureRequest = this@ExpoBraintreeDropInModule.generateThreeDSecureRequest(payload)
      threeDSecureRequest.nonce = payload.nonce
      val dropInRequest = DropInRequest()
      dropInRequest.threeDSecureRequest = threeDSecureRequest
      dropInRequest.isPayPalDisabled = true // Disable PayPal option

      val listener = ExpoBraintreeThreeDSListener(promise)
      val tokenProvider = ExpoBraintreeDropInTokenProvider.getInstance()
      tokenProvider.token = token
      val threeDSecureClient = ExpoBraintreeDropInClientHolder.getInstance().threeDSecureClient
      threeDSecureClient.setListener(listener)

      threeDSecureClient.performVerification(currentActivity as FragmentActivity, threeDSecureRequest) { threeDSecureLookupResult, lookupError ->
        if (lookupError !== null) {
          promise.reject(CodedException(lookupError.message, lookupError))

          return@performVerification
        }

        if (threeDSecureLookupResult === null) {
          promise.reject(CodedException("No threeDS lookup result returned by Braintree"))

          return@performVerification
        }

        if (threeDSecureLookupResult.tokenizedCard?.threeDSecureInfo?.status  !== "authenticate_successful") {
          threeDSecureClient.continuePerformVerification(currentActivity as FragmentActivity, threeDSecureRequest, threeDSecureLookupResult)

          return@performVerification
        }

        promise.resolve(threeDSecureLookupResult.tokenizedCard?.string)
      }
    }.runOnQueue(Queues.MAIN)
  }

  // Use this function when initializing GooglePlayClient
  private fun getGooglePayEnvironment(): String {
    val applicationInfo = context.packageManager?.getApplicationInfo(context.packageName.toString(), PackageManager.GET_META_DATA)

    val metadataValue = applicationInfo?.metaData?.getString("expo.modules.braintreedropin.BRAINTREE_MERCHANT_ID")

    if (metadataValue != null && metadataValue.contains("sandbox")) {
      return "TEST"
    }

    return "PRODUCTION"
  }

  private fun generateThreeDSecureRequest (payload: Payload): ThreeDSecureRequest {
    val address = ThreeDSecurePostalAddress()
    address.givenName = payload.givenName
    address.surname = payload.surname
    address.streetAddress = payload.streetAddress
    address.postalCode = payload.postalCode
    address.countryCodeAlpha2 = payload.countryCode

    val additionalInformation = ThreeDSecureAdditionalInformation()
    additionalInformation.shippingAddress = address

    val threeDSecureRequest = ThreeDSecureRequest()

    threeDSecureRequest.amount = payload.amount.toString()
    threeDSecureRequest.email = payload.email
    threeDSecureRequest.billingAddress = address
    threeDSecureRequest.versionRequested = ThreeDSecureRequest.VERSION_2
    threeDSecureRequest.additionalInformation = additionalInformation

    return threeDSecureRequest
  }

  private fun generateGooglePayRequest(payload: Payload): GooglePayRequest {
    val googlePayRequest = GooglePayRequest()
    googlePayRequest.transactionInfo = TransactionInfo.newBuilder()
      .setTotalPrice(payload.amount.toString())
      .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
      .setCurrencyCode(payload.currencyCode)
      .build()
    googlePayRequest.isBillingAddressRequired = true
    googlePayRequest.environment = getGooglePayEnvironment()

    return googlePayRequest
  }
}
