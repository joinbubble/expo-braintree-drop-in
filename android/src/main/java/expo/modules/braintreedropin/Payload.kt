package expo.modules.braintreedropin

import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record

class Payload: Record {
  @Field
  val givenName: String = ""

  @Field
  val surname: String = ""

  @Field
  val streetAddress: String = ""

  @Field
  val locality: String = ""

  @Field
  val postalCode: String = ""

  @Field
  val email: String = ""

  @Field
  val amount: Double = 0.00

  @Field
  val countryCode: String = ""

  @Field
  val currencyCode: String = ""

  @Field
  val nonce = ""

  @Field
  val showMobilePay = true

  @Field
  val transactionLabel = ""
}
