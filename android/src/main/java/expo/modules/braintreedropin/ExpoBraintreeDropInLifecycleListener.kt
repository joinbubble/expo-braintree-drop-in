package expo.modules.braintreedropin

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.braintreepayments.api.DropInClient
import expo.modules.core.interfaces.ReactActivityLifecycleListener

class ExpoBraintreeDropInLifecycleListener : ReactActivityLifecycleListener {
  override fun onCreate(activity: Activity, savedInstanceState: Bundle?) {
    val clientHolder = ExpoBraintreeDropInClientHolder.getInstance()
    clientHolder.braintreeDropInClient = DropInClient(
      (activity as FragmentActivity),
      ExpoBraintreeDropInTokenProvider.getInstance()
    )
  }
}
