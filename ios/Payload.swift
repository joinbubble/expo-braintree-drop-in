import ExpoModulesCore

struct Payload: Record {
    @Field
    var givenName: String
    
    @Field
    var surname: String
    
    @Field
    var streetAddress: String
    
    @Field
    var locality: String
    
    @Field
    var postalCode: String
    
    @Field
    var email: String
    
    @Field
    var amount: Double
    
    @Field
    var countryCode: String
    
    @Field
    var currencyCode: String
    
    @Field
    var nonce: String = ""
    
    @Field
    var showMobilePay: Bool = true
    
    @Field
    var transactionLabel: String = ""
}
