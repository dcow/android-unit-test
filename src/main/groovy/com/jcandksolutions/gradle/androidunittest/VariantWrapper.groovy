package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.TestVariant

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.FileCollection
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet

/**
 * Base class that wraps the info of the variant for easier retrieval of the actual data needed.
 */
public abstract class VariantWrapper {
  protected final Project project
  protected final ConfigurationContainer configurations
  protected final BaseVariant variant
  protected final TestVariant testVariant
  protected final Logger logger
  protected ArrayList<File> testsSourcePath
  protected Configuration configuration
  private final String bootClasspath
  private FileCollection classpath
  private File compileDestinationDir
  private GString completeName
  private SourceSet sourceSet
  private FileCollection runPath
  private File mergedResourcesDir
  private File mergedManifest
  private File mergedAssetsDir
  private String resourcesCopyTaskName
  private String realMergedResourcesDir
  private List<String> flavorList
  private String flavorName
  private String buildTypeName
  private FileCollection testClasspath
  private File variantReportDestination
  /**
   * Instantiates a new VariantWrapper.
   * @param variant The Variant to wrap.
   * @param project The project.
   * @param configurations The Project Configurations.
   * @param bootClasspath The bootClasspath.
   * @param logger The Logger.
   * @param testVariant The Test Variant of the variant. Can be null for library projects.
   */
  public VariantWrapper(BaseVariant variant, Project project, ConfigurationContainer configurations, String bootClasspath, Logger logger, TestVariant testVariant) {
    this.variant = variant
    this.project = project
    this.configurations = configurations
    this.bootClasspath = bootClasspath
    this.testVariant = testVariant
    this.logger = logger
  }

  /**
   * Configures the SourceSet with the Sourcepath, Classpath and Runpath.
   */
  public void configureSourceSet() {
    //Add standard resources directory
    sourceSet.resources.srcDirs(project.file("src${File.separator}test${File.separator}resources"))
    sourceSet.java.srcDirs = testsSourcePath
    sourceSet.compileClasspath = classpath
    sourceSet.runtimeClasspath = runPath
    //Add this SourceSet to the classes task for compilation
    sourceSet.compiledBy(sourceSet.classesTaskName)
  }

  /**
   * Retrieves the sourcepath for the tests of this variant. It includes the standard test dir that
   * has tests for all flavors, a dir for the buildType tests, a dir for each flavor in the variant
   * and a dir for the variant. For example, the variant FreeBetaDebug will have the following dirs:
   * <br/>
   * <ul>
   *   <li>src/test/java (main test dir)</li>
   *   <li>src/testDebug/java (debug build type test dir)</li>
   *   <li>src/testFree/java (free flavor tests dir)</li>
   *   <li>src/testBeta/java (beta flavor tests dir)</li>
   *   <li>src/testFreeBeta/java (variant tests dir)</li>
   * </ul>
   * @return The sourcePath.
   */
  protected ArrayList<File> getTestsSourcePath() {
    if (testsSourcePath == null) {
      testsSourcePath = []
      testsSourcePath.add(project.file("src${File.separator}test${File.separator}java"))
      testsSourcePath.add(project.file("src${File.separator}test$buildTypeName${File.separator}java"))
      testsSourcePath.add(project.file("src${File.separator}test$flavorName${File.separator}java"))
      testsSourcePath.add(project.file("src${File.separator}test$flavorName$buildTypeName${File.separator}java"))
      flavorList.each { String flavor ->
        testsSourcePath.add(project.file("src${File.separator}test$flavor${File.separator}java"))
        testsSourcePath.add(project.file("src${File.separator}test$flavor$buildTypeName${File.separator}java"))
      }
    }
    return testsSourcePath
  }

  /**
   * Retrieves the dir name of the variant.<br/>
   * For example: freeBeta/debug.
   * @return The dir name of the variant.
   */
  public String getDirName() {
    return variant.dirName
  }

  /**
   * Retrieves the path of the merged manifest of the variant.
   * @return The path.
   */
  public File getMergedManifest() {
    if (mergedManifest == null) {
      mergedManifest = variant.outputs.first().processManifest.manifestOutputFile
    }
    return mergedManifest
  }

  /**
   * Retrieves the path where the merged resources are copied. Usually in
   * build/test-resources/$variantName/res.
   * @return The dir with the copied merged resources.
   */
  public File getMergedResourcesDir() {
    if (mergedResourcesDir == null) {
      mergedResourcesDir = project.file("$project.buildDir${File.separator}test-resources${File.separator}$completeName${File.separator}res")
    }
    return mergedResourcesDir
  }

  /**
   * Retrieves the output dir of the mergeAssets task. This has the merged assets of the variant.
   * @return The merged assets dir.
   */
  public File getMergedAssetsDir() {
    if (mergedAssetsDir == null) {
      mergedAssetsDir = variant.mergeAssets.outputDir
    }
    return mergedAssetsDir
  }

  /**
   * Retrieves a configuration for the test variant based on the list of configurations that this
   * variant uses. Which are the build type configuration and a configuration for each flavor.<br>
   * For example: testDebugCompile, testFreeCompile and testBetaCompile.
   * @return The configuration for the test variant.
   */
  public Configuration getConfiguration() {
    if (configuration == null) {
      //we create the sourceset first otherwise the needed configurations won't be available for the compile classpath
      sourceSet
      ArrayList<GString> configurationNames = ["${ConfigurationManager.TEST_COMPILE}"]
      configurationNames.add("test${buildTypeName}Compile")
      flavorList.each { String flavor ->
        configurationNames.add("test${flavor}Compile")
        logger.info("Reading configuration: test${flavor}Compile")
      }
      configuration = configurations.create("_test${completeName.capitalize()}Compile")
      configurationNames.each { configName ->
        configuration.extendsFrom(configurations.findByName(configName))
      }
    }
    return configuration
  }

  /**
   * Retrieves the Classpath used to compile the tests which includes the testCompile configuration,
   * the app's class files, the app's classpath and a configuration for each flavor.
   * @return The classpath.
   */
  public FileCollection getClasspath() {
    if (classpath == null) {
      classpath = configuration.plus(project.files(variant.javaCompile.destinationDir, variant.javaCompile.classpath))
    }
    return classpath
  }

  /**
   * Retrieves the classpath for the Test task. This includes the runPath plus the bootClasspath.
   * @return The testClasspath.
   */
  public FileCollection getTestClasspath() {
    if (testClasspath == null) {
      testClasspath = runPath.plus(project.files(bootClasspath))
    }
    return testClasspath
  }

  /**
   * Retrieves the path for the destination of the test's compilation.<br/>
   * For example: build/test-classes/freeBeta/debug.
   * @return The destination dir.
   */
  public File getCompileDestinationDir() {
    if (compileDestinationDir == null) {
      compileDestinationDir = new File("$project.buildDir${File.separator}test-classes${File.separator}$variant.dirName")
    }
    return compileDestinationDir
  }

  /**
   * Retrieves the complete name of the variant which is the concatenation of the flavors plus the buildType.
   * For example: FreeBetaDebug.
   * @return The complete name.
   */
  public GString getCompleteName() {
    if (completeName == null) {
      completeName = "$flavorName$buildTypeName"
    }
    return completeName
  }

  /**
   * Retrieves the build type of the variant.<br/>
   * For example: Debug.
   * @return The build type.
   */
  protected String getBuildTypeName() {
    if (buildTypeName == null) {
      buildTypeName = variant.buildType.name.capitalize()
    }
    return buildTypeName
  }

  /**
   * Retrieves a list of the flavors of the variant.
   * @return The list of flavors. Empty if no flavors defined.
   */
  protected List<String> getFlavorList() {
    if (flavorList == null) {
      flavorList = variant.productFlavors.collect { it.name.capitalize() }
      if (flavorList.empty) {
        flavorList = [""]
      }
    }
    return flavorList
  }

  /**
   * Retrieves the concatenated name of all the flavors.<br/>
   * For example: FreeBeta.
   * @return The flavor name.
   */
  protected String getFlavorName() {
    if (flavorName == null) {
      flavorName = flavorList.join("")
    }
    return flavorName
  }

  /**
   * Returns the compile task of the app's sources.
   * @return the compile task of the app's sources
   */
  public abstract Task getAndroidCompileTask();

  /**
   * Returns the test SourceSet for this variant.
   * @return The test SourceSet.
   */
  protected SourceSet getSourceSet() {
    if (sourceSet == null) {
      JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention)
      sourceSet = javaConvention.sourceSets.create("test$completeName")
    }
    return sourceSet
  }

  /**
   * Retrieves the Runpath which includes the classpath, the processsed resources and the
   * destination dir of the compilation, that is, where the tests' class files are.
   * @return The Runpath.
   */
  protected FileCollection getRunPath() {
    if (runPath == null) {
      runPath = classpath.plus(project.files("$project.buildDir${File.separator}resources${File.separator}test$completeName")).plus(new SimpleFileCollection(compileDestinationDir))
    }
    return runPath
  }

  /**
   * Retrieves the ResourcesCopyTask name.<br/>
   * For example: copyFreeNormalDebugTestResources.
   * @return The ResourcesCopyTaskName.
   */
  public String getResourcesCopyTaskName() {
    if (resourcesCopyTaskName == null) {
      resourcesCopyTaskName = "copy${completeName}TestResources"
    }
    return resourcesCopyTaskName
  }

  /**
   * Retrieves the path string where the resources are merged by the Android plugin.
   * @return The path string.
   */
  public String getRealMergedResourcesDir() {
    if (realMergedResourcesDir == null) {
      realMergedResourcesDir = "$project.buildDir${File.separator}intermediates${File.separator}res${File.separator}$variant.dirName"
    }
    return realMergedResourcesDir
  }

  /**
   * Retrieves the Base variant of the Android plugin that this is wrapping.
   * @return The Base variant.
   */
  public BaseVariant getBaseVariant() {
    return variant;
  }

  /**
   * Retrieves the report destination dir where this variant's test results should go to.<br/>
   * For example: build/test-report/freeBeta/debug/.
   * @return The report destination.
   */
  public File getVariantReportDestination() {
    if (variantReportDestination == null) {
      variantReportDestination = project.file("$project.buildDir${File.separator}test-report${File.separator}$dirName")
    }
    return variantReportDestination
  }
}
