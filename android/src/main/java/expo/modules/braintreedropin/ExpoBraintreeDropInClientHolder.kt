package expo.modules.braintreedropin

import com.braintreepayments.api.DropInClient
import com.braintreepayments.api.ThreeDSecureClient

class ExpoBraintreeDropInClientHolder private constructor() {
  companion object {
    @Volatile
    private var instance: ExpoBraintreeDropInClientHolder? = null

    fun getInstance() =
      instance ?: synchronized(this) { // synchronized to avoid concurrency problem
        instance ?: ExpoBraintreeDropInClientHolder().also { instance = it }
      }
  }

  lateinit var braintreeDropInClient : DropInClient
  lateinit var threeDSecureClient : ThreeDSecureClient
}
