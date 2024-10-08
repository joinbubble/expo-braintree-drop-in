# Expo integration for Braintree DropIn UI - Release Notes

## 2.3 (2024-10-08)

- Present ExpoModuleController on verify flow to prevent its early dismissal
- Create ThreeDSecureClient outside of expo module init to prevent internal Braintree observer from registering listner while app in RESUME state
