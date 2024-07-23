package expo.modules.braintreedropin

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.ClientTokenCallback
import com.braintreepayments.api.ClientTokenProvider
import com.braintreepayments.api.DropInClient

class ExpoBraintreeDropInTokenProvider private constructor(): ClientTokenProvider {
  companion object {
    @Volatile
    private var instance: ExpoBraintreeDropInTokenProvider? = null

    fun getInstance() =
      instance ?: synchronized(this) { // synchronized to avoid concurrency problem
        instance ?: ExpoBraintreeDropInTokenProvider().also { instance = it }
      }
  }

  var token: String = ""

  override fun getClientToken(callback: ClientTokenCallback) {
    if (token.isEmpty()) {
      callback.onFailure(Exception("No Braintree Authentication token set"))
      return
    }
    callback.onSuccess(token)
  }
}
