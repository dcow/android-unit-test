package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.internal.ProductFlavorData
import com.android.builder.core.VariantConfiguration
import org.gradle.api.logging.Logger

/**
 * Class that handles the extraction of the Application ID.
 */
public class PackageExtractor {
  private final ProductFlavorData data
  private final Logger logger
  private String packageName
  /**
   * Instantiates a new PackageExtractor.
   * @param data The data for the Default configuration of the project.
   * @param logger The logger.
   */
  public PackageExtractor(ProductFlavorData data, Logger logger) {
    this.logger = logger
    this.data = data
  }

  /**
   * Retrieves the package name from the Android plugin's default configuration. If not configured,
   * it will try to extract it from the manifest.
   * @return The Application ID which usually is the package name.
   */
  public String getPackageName() {
    if (packageName == null) {
      packageName = data.productFlavor.applicationId
      if (packageName == null) {
        packageName = VariantConfiguration.getManifestPackage(data.sourceSet.manifestFile)
      }
      logger.info("main package: $packageName")
    }
    return packageName
  }
}
