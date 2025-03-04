# Expo integration for Braintree DropIn UI - Release Notes

## 0.2.5 (2025-03-04)

- Update native ios SDK to ensure compatibility with new Braintree SSL certificate

## 0.2.4 (2024-10-10)

- Prevent BTController from dismissing early when using applePay

## 0.2.3 (2024-10-08)

- Present ExpoModuleController on verify flow to prevent its early dismissal
- Create ThreeDSecureClient outside of expo module init to prevent internal Braintree observer from registering listner while app in RESUME state
