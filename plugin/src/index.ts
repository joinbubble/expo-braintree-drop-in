import { GradleProjectFile } from "@expo/config-plugins/build/android/Paths";
import {
  ConfigPlugin,
  ExportedConfigWithProps,
  createRunOncePlugin,
  withAppBuildGradle,
  withAndroidManifest,
  withInfoPlist,
  AndroidConfig,
} from "expo/config-plugins";

const pkg = require("../../package.json");

const withBraintreeDropIn: ConfigPlugin<{ braintreeMerchantId: string }> = (
  config,
  { braintreeMerchantId }
) => {
  config = withInfoPlist(config, (internalConfig) => {
    internalConfig.modResults.BRAINTREE_MERCHANT_ID = braintreeMerchantId;

    return internalConfig;
  });

  config = withAndroidManifest(config, (internalConfig) => {
    const mainApplication = AndroidConfig.Manifest.getMainApplicationOrThrow(
      internalConfig.modResults
    );

    AndroidConfig.Manifest.addMetaDataItemToMainApplication(
      mainApplication,
      "expo.modules.braintreedropin.BRAINTREE_MERCHANT_ID",
      braintreeMerchantId,
      "value"
    );

    return internalConfig;
  });

  return withAppBuildGradle(config, (internalConfig) => {
    if (internalConfig.modResults.language === "groovy") {
      appendCardinalMobileRepository(internalConfig);
    } else {
      throw new Error(
        "Cannot add Braintree DropIn maven gradle because the build.gradle is not groovy"
      );
    }
    return internalConfig;
  });
};

function appendCardinalMobileRepository(
  config: ExportedConfigWithProps<GradleProjectFile>
) {
  const stringToAppend = `
repositories {
  maven {
    url "https://cardinalcommerceprod.jfrog.io/artifactory/android"
    credentials {
        username 'braintree_team_sdk'
        password 'AKCp8jQcoDy2hxSWhDAUQKXLDPDx6NYRkqrgFLRc3qDrayg6rrCbJpsKKyMwaykVL8FWusJpp'
    }
  }
}`;

  return (config.modResults.contents = `${config.modResults.contents}${stringToAppend}\n`);
}

export default createRunOncePlugin(withBraintreeDropIn, pkg.name, pkg.version);
