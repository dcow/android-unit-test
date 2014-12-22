package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.BaseExtension
import com.android.builder.core.DefaultBuildType
import com.android.builder.core.DefaultProductFlavor

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact
import org.gradle.api.logging.Logger

/**
 * Class that manages the creation of the Configurations for the different source sets.
 */
public class ConfigurationManager {
  public static final String TEST_COMPILE = 'testCompile'
  public static final String SOURCES_JAVADOC = '_SourcesJavadoc_'
  public static final String COMPILE = 'compile'
  private final BaseExtension androidExtension
  private final ConfigurationContainer configurations
  private final Logger logger
  private final AndroidUnitTestPluginExtension pluginExtension
  private final Project project
  private final ModelManager modelManager
  /**
   * Instantiates a ConfigurationManager.
   * @param androidExtension The AndroidExtension.
   * @param configurations The Configurations of the project.
   * @param project The Project.
   * @param pluginExtension The Plugin extension.
   * @param modelManager The Model Manager.
   * @param logger The Logger.
   */
  public ConfigurationManager(BaseExtension androidExtension, ConfigurationContainer configurations, Project project, AndroidUnitTestPluginExtension pluginExtension, ModelManager modelManager, Logger logger) {
    this.androidExtension = androidExtension
    this.configurations = configurations
    this.project = project
    this.pluginExtension = pluginExtension
    this.modelManager = modelManager
    this.logger = logger
  }

  /**
   * Creates new test configurations for each flavor so the user can set dependencies for the
   * different source sets.
   */
  public void createNewConfigurations() {
    logger.info("----------------------------------------")
    logger.info("Found configurations:")
    List<String> buildTypeConfigNames = buildTypeConfigList
    List<String> flavorConfigNames = flavorConfigList
    logger.info("----------------------------------------")
    logger.info("Creating new configurations:")
    List<String> buildTypeTestConfigNames = createTestConfigurations(buildTypeConfigNames)
    List<String> flavorTestConfigNames = createTestConfigurations(flavorConfigNames)
    createTestCompileTaskConfiguration()
    project.afterEvaluate {
      createSourcesJavadocConfiguration(buildTypeConfigNames, flavorConfigNames, buildTypeTestConfigNames, flavorTestConfigNames)
    }
  }

  private void createTestCompileTaskConfiguration() {
    Configuration testCompileTaskConfiguration = configurations.create(TEST_COMPILE)
    testCompileTaskConfiguration.extendsFrom configurations.getByName(COMPILE)
    logger.info(TEST_COMPILE)
  }

  private List<String> createTestConfigurations(final List<String> configNames) {
    List<String> testConfigNames = []
    configNames.each { String configName ->
      String testConfigName = "test${configName.capitalize()}"
      logger.info(testConfigName)
      configurations.create(testConfigName)
      testConfigNames.add(testConfigName)
    }
    return testConfigNames
  }

  private void createSourcesJavadocConfiguration(final List<String> buildTypeConfigNames,
                                                 final List<String> flavorConfigNames,
                                                 final List<String> buildTypeTestConfigNames,
                                                 final List<String> flavorTestConfigNames) {
    if (pluginExtension.downloadTestDependenciesSources || pluginExtension.downloadTestDependenciesJavadoc || pluginExtension.downloadDependenciesSources || pluginExtension.downloadDependenciesJavadoc) {
      Configuration testSourcesJavadocConfiguration = configurations.create(SOURCES_JAVADOC)
      Map<String, Configuration> tempConfigurations = new HashMap<String, Configuration>();
      copyDependencies(tempConfigurations, [COMPILE], pluginExtension.downloadDependenciesSources, pluginExtension.downloadDependenciesJavadoc)
      copyDependencies(tempConfigurations, buildTypeConfigNames, pluginExtension.downloadDependenciesSources, pluginExtension.downloadDependenciesJavadoc)
      copyDependencies(tempConfigurations, flavorConfigNames, pluginExtension.downloadDependenciesSources, pluginExtension.downloadDependenciesJavadoc)
      copyDependencies(tempConfigurations, [TEST_COMPILE], pluginExtension.downloadTestDependenciesSources, pluginExtension.downloadTestDependenciesJavadoc)
      copyDependencies(tempConfigurations, buildTypeTestConfigNames, pluginExtension.downloadTestDependenciesSources, pluginExtension.downloadTestDependenciesJavadoc)
      copyDependencies(tempConfigurations, flavorTestConfigNames, pluginExtension.downloadTestDependenciesSources, pluginExtension.downloadTestDependenciesJavadoc)
      Iterator it = tempConfigurations.entrySet().iterator()
      while (it.hasNext()) {
        Configuration conf = it.next().value
        try {
          conf.files
        } catch (ResolveException ignored) {
          it.remove()
        }
      }
      testSourcesJavadocConfiguration.extendsFrom(tempConfigurations.values().toArray(new Configuration[tempConfigurations.size()]))
      modelManager.registerJavadocSourcesArtifact(testSourcesJavadocConfiguration)
    }
  }

  private void copyDependencies(Map<String, Configuration> testConfigurations, List<String> configNames, boolean sources, boolean javadoc) {
    if (sources || javadoc) {
      configNames.each { String configName ->
        Configuration conf = configurations.getByName(configName)
        conf.dependencies.all { Dependency dependency ->
          if (dependency instanceof ExternalModuleDependency && !testConfigurations.containsKey(dependency.name)) {
            ExternalModuleDependency copy = dependency.copy()
            if (sources) {
              DependencyArtifact artifact = new DefaultDependencyArtifact(copy.name, "jar", "jar", "sources", null);
              copy.addArtifact(artifact)
            }
            if (javadoc) {
              DependencyArtifact artifact = new DefaultDependencyArtifact(copy.name, "jar", "jar", "javadoc", null);
              copy.addArtifact(artifact)
            }
            Configuration tmp = configurations.create("temp_${copy.name}")
            testConfigurations[copy.name] = tmp
            tmp.dependencies.add(copy)
          }
        }
      }
    }
  }

  private List<String> getFlavorConfigList() {
    List<String> flavorConfigNames = []
    androidExtension.productFlavors.each { DefaultProductFlavor flavor ->
      String configName = "${flavor.name}Compile"
      logger.info(configName)
      flavorConfigNames.add(configName)
    }
    return flavorConfigNames
  }

  private List<String> getBuildTypeConfigList() {
    List<String> buildTypeConfigNames = []
    androidExtension.buildTypes.each { DefaultBuildType buildType ->
      String configName = "${buildType.name}Compile"
      logger.info(configName)
      buildTypeConfigNames.add(configName)
    }
    return buildTypeConfigNames
  }
}
