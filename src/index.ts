import { Payload } from './ExpoBraintreeDropIn.types';
import ExpoBraintreeDropInModule from './ExpoBraintreeDropInModule';

export async function showDropIn(
  payload: Payload,
  token: string
): Promise<string> {
  return ExpoBraintreeDropInModule.showDropIn(
    { countryCode: 'GB', currencyCode: 'GBP', ...payload },
    token
  );
}

export async function verify(payload: Payload, token: string): Promise<string> {
  return ExpoBraintreeDropInModule.verify(
    { countryCode: 'GB', currencyCode: 'GBP', ...payload },
    token
  );
}

export * from './ExpoBraintreeDropIn.types';
