export interface Payload {
  givenName: string;
  surname: string;
  streetAddress: string;
  locality: string;
  postalCode: string;
  email: string;
  amount: number;
  countryCode?: string;
  currencyCode?: string;
  nonce?: string;
  showMobilePay?: boolean;
  transactionLabel?: string;
  vaultManagerEnabled?: boolean;
}
