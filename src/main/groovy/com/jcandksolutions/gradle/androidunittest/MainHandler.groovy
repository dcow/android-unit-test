package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.BaseVariant

import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.logging.Logger

/**
 * Base class that coordinates the configuration of the project. This class should trigger the
 * creation of the Configurations, SourceSets, Extension, Tasks and Model.
 */
public abstract class MainHandler {
  protected final ModelManager modelManager
  protected final TaskManager taskManager
  protected final AndroidUnitTestPluginExtension extension
  protected final Logger logger
  protected final DependencyProvider provider
  private final ConfigurationManager configurationManager
  private final DefaultDomainObjectSet<BaseVariant> variants
  /**
   * Instantiates a MainHandler.
   * @param provider The Dependency Provider for the plugin.
   */
  public MainHandler(DependencyProvider provider) {
    this.provider = provider
    modelManager = provider.provideModelManager()
    taskManager = provider.provideTaskManager()
    extension = provider.provideExtension()
    configurationManager = provider.provideConfigurationManager()
    logger = provider.provideLogger()
    variants = provider.provideVariants()
  }

  /**
   * Executes the handler. It will trigger the creation of the Configurations, SourceSets,
   * Extension, Tasks and Model.
   */
  public void run() {
    modelManager.register()
    configurationManager.createNewConfigurations()

    //we use "all" instead of "each" because this set is empty until after project evaluated
    //with "all" it will execute the closure when the variants are getting created
    variants.all { BaseVariant variant ->
      owner.logger.info("----------------------------------------")
      if (variant.buildType.debuggable || owner.extension.testReleaseBuildType) {
        if (!isVariantInvalid(variant)) {
          VariantWrapper variantWrapper = createVariantWrapper(variant)
          variantWrapper.configureSourceSet()
          owner.taskManager.createTestTask(variantWrapper)
          owner.modelManager.registerArtifact(variantWrapper)
        }
      } else {
        owner.logger.info("skipping non-debuggable variant: ${variant.name}")
      }
    }
    logger.info("----------------------------------------")
    logger.info("Applied plugin")
  }

  /**
   * Creates a new VariantWrapper instance. Inheritors must implement this method.
   * @param variant The Variant to wrap.
   * @return The wrapper.
   */
  protected abstract VariantWrapper createVariantWrapper(final BaseVariant variant)

  /**
   * Checks if the variant is invalid and should not process it.
   * @param baseVariant The Variant to check.
   * @return {@code true] if invalid, {@code false} otherwise.
   */
  protected abstract boolean isVariantInvalid(final BaseVariant baseVariant)
}
