package expo.modules.braintreedropin

import com.braintreepayments.api.DropInListener
import com.braintreepayments.api.DropInResult
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import java.lang.Exception

class ExpoBraintreeDropInListener(promise: Promise) : DropInListener {
  private val activePromise = promise

  override fun onDropInSuccess(dropInResult: DropInResult) {
    val paymentMethodNonce = dropInResult.paymentMethodNonce?.string

    if (paymentMethodNonce is String && paymentMethodNonce.isNotEmpty()) {
      activePromise.resolve(paymentMethodNonce)
      return
    }

    activePromise.reject(CodedException("No payment nonce returned. Received value: $paymentMethodNonce"))
  }

  override fun onDropInFailure(exception: Exception) {
    activePromise.reject(CodedException(exception.message, exception))
  }
}
