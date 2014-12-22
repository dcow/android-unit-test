package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.ProductFlavorData

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.logging.Logger

/**
 * Class that provides the dependencies for the plugin.
 */
public class DependencyProvider {
  private Project project
  private BasePlugin plugin
  private Boolean isAppPlugin
  private AndroidUnitTestPluginExtension extension
  private String bootClasspath
  private File reportDestinationDir
  private ModelManager modelManager
  /**
   * Instantiates a DependencyProvider.
   * @param project The Project being configured.
   */
  public DependencyProvider(Project project) {
    this.project = project
  }

  /**
   * Provides the Project that is applying the plugin.
   * @return The Project.
   */
  public Project provideProject() {
    return project
  }

  /**
   * Provides the Extension used by this plugin. If it doesn't exist, it is created first.
   * @return The Extension.
   */
  public AndroidUnitTestPluginExtension provideExtension() {
    if (extension == null) {
      extension = project.extensions.create("androidUnitTest", AndroidUnitTestPluginExtension)
      extension.downloadDependenciesSources = true
      extension.downloadTestDependenciesSources = true
    }
    return extension
  }

  /**
   * Provides the Model Manager that registers the model of the test source sets.
   * @return The ModelManager.
   */
  public ModelManager provideModelManager() {
    if (modelManager == null) {
      modelManager = new ModelManager(provideAndroidPlugin())
    }
    return modelManager
  }

  /**
   * Provides the Configuration Manager that creates the test configurations. Always creates a new
   * one, so it should only be called once.
   * @return The ConfigurationManager.
   */
  public ConfigurationManager provideConfigurationManager() {
    return new ConfigurationManager(provideAndroidExtension(), provideConfigurations(), provideProject(), provideExtension(), provideModelManager(), provideLogger())
  }

  /**
   * Provides the Task Manager that creates the tasks required to run the tests. Always creates a
   * new one, so it should only be called once.
   * @return The TaskManager.
   */
  public TaskManager provideTaskManager() {
    return new TaskManager(provideProject(), provideBootClasspath(), providePackageExtractor(), provideReportDestinationDir(), provideLogger())
  }

  /**
   * Provides the Default Configuration Data of the Android Plugin. Used to know the application id.
   * @return The Default Configuration.
   */
  public ProductFlavorData provideDefaultConfigData() {
    return provideAndroidPlugin().defaultConfigData
  }

  /**
   * Returns whether the Android Plugin applied is the App plugin or de Library plugin.
   * @return {@code true} if the app plugin was applied. {@code false} otherwise.
   * @throws IllegalStateException if neither of the plugins is applied.
   */
  public boolean isAppPlugin() {
    if (isAppPlugin == null) {
      isAppPlugin = project.plugins.withType(AppPlugin)
      if (!isAppPlugin && !project.plugins.withType(LibraryPlugin)) {
        isAppPlugin = null
        throw new IllegalStateException("The 'android' or 'android-library' plugin is required.")
      }
    }
    return isAppPlugin
  }

  /**
   * Provides the Android plugin that was applied.
   * @return The Android plugin.
   */
  public BasePlugin provideAndroidPlugin() {
    return appPlugin ? provideAppPlugin() : provideLibraryPlugin()
  }

  /**
   * Provides the Package Extractor used to know the application id. Always creates a new one, so it
   * should only be called once.
   * @return The PackageExtractor.
   */
  public PackageExtractor providePackageExtractor() {
    return new PackageExtractor(provideDefaultConfigData(), provideLogger())
  }

  /**
   * Provides the Android plugin's Extension. Used to know the flavors and build types created by
   * the Android plugin.
   * @return The Android plugin's Extension.
   */
  public BaseExtension provideAndroidExtension() {
    return provideAndroidPlugin().extension
  }

  /**
   * Provides the Configurations Container of the Project. Used by the {@link ConfigurationManager}.
   * @return The Configurations Container.
   */
  public ConfigurationContainer provideConfigurations() {
    return provideProject().configurations
  }

  /**
   * Provides the BootClasspath for the tests to run. It is lazily extracted from the Android plugin.
   * @return The BootClasspath.
   */
  public String provideBootClasspath() {
    if (bootClasspath == null) {
      bootClasspath = provideAndroidPlugin().bootClasspath.join(File.pathSeparator)
    }
    return bootClasspath
  }

  /**
   * Provides the Variants of the Android plugin.
   * @return The Variants.
   */
  public DefaultDomainObjectSet<BaseVariant> provideVariants() {
    BaseExtension extension = provideAndroidExtension()
    if (appPlugin) {
      return ((AppExtension) extension).applicationVariants
    } else {
      return ((LibraryExtension) extension).libraryVariants
    }
  }

  /**
   * Provides the Report Destination Directory. Normally it should be {@code /build/test-report/}.
   * @return The Report Destination Directory.
   */
  public File provideReportDestinationDir() {
    if (reportDestinationDir == null) {
      reportDestinationDir = project.file("$project.buildDir${File.separator}test-report")
    }
    return reportDestinationDir
  }

  /**
   * Provides the Logger for the plugin.
   * @return The Logger.
   */
  public Logger provideLogger() {
    return project.logger
  }

  /**
   * Provides the Handler for the plugin. This Handler controls the business logic of the plugin.
   * @return The Handler.
   */
  public MainHandler provideHandler() {
    return appPlugin ? new AppHandler(this) : new LibraryHandler(this)
  }

  /**
   * Provides an AppVariantWrapper that simplifies the extraction of info form it.
   * @param applicationVariant The Application Variant.
   * @return The Wrapper.
   */
  public AppVariantWrapper provideAppVariantWrapper(final ApplicationVariant applicationVariant) {
    return new AppVariantWrapper(applicationVariant, provideProject(), provideConfigurations(), provideBootClasspath(), provideLogger())
  }

  /**
   * Provides a LibraryVariantWrapper that simplifies the extraction of info form it.
   * @param libraryVariant The Library Variant.
   * @return The Wrapper.
   */
  public LibraryVariantWrapper provideLibraryVariantWrapper(final LibraryVariant libraryVariant) {
    return new LibraryVariantWrapper(libraryVariant, provideProject(), provideConfigurations(), provideBootClasspath(), provideLogger())
  }

  private BasePlugin provideLibraryPlugin() {
    if (plugin == null) {
      plugin = project.plugins.withType(LibraryPlugin).toList()[0]
    }
    return plugin
  }

  private BasePlugin provideAppPlugin() {
    if (plugin == null) {
      plugin = project.plugins.withType(AppPlugin).toList()[0]
    }
    return plugin
  }
}
