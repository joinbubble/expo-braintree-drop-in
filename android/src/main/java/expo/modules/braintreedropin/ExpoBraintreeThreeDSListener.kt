package expo.modules.braintreedropin

import com.braintreepayments.api.ThreeDSecureListener
import com.braintreepayments.api.ThreeDSecureResult
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.CodedException
import java.lang.Exception

class ExpoBraintreeThreeDSListener(promise: Promise) : ThreeDSecureListener {
  private val activePromise = promise

  override fun onThreeDSecureSuccess(threeDSecureResult: ThreeDSecureResult) {

    if (threeDSecureResult.tokenizedCard?.string === null) {
      activePromise.reject(CodedException("Nonce not set after verification"))

      return;
    }

    activePromise.resolve(threeDSecureResult.tokenizedCard?.string)
  }

  override fun onThreeDSecureFailure(exception: Exception) {
    activePromise.reject(CodedException(exception.message, exception))
  }
}
