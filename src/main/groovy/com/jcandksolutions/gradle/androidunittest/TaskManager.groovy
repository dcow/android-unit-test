package com.jcandksolutions.gradle.androidunittest

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestReport

/**
 * Class that handles the creation of the Tasks needed for the tests to run. This includes a
 * TestReport task that generates the reports from the output of each test task; a TestClasses task
 * that simply triggers the creation of all the class files of all the tests; a TestCompileTask for each
 * variant that will compile only that variants tests sources; a ResourcesCopyTask for each variant
 * that will copy the merged resources to a test dir; and a a TestTask for each variant that will
 * actually run the tests for that variant.
 */
public class TaskManager {
  private final Project project
  private final String bootClasspath
  private final PackageExtractor packageExtractor
  private final File reportDestinationDir
  private final Logger logger
  private Task testClassesTask
  private TestReport testReportTask
  /**
   * Instantiates a TaskManager.
   * @param project The Project.
   * @param bootClasspath The BootClasspath.
   * @param packageExtractor The PackageExtractor.
   * @param reportDestinationDir The Report Destination Directory.
   * @param logger The logger.
   */
  public TaskManager(Project project, String bootClasspath, PackageExtractor packageExtractor, File reportDestinationDir, Logger logger) {
    this.project = project
    this.bootClasspath = bootClasspath
    this.packageExtractor = packageExtractor
    this.reportDestinationDir = reportDestinationDir
    this.logger = logger
  }

  /**
   * Creates and configures the test task that runs the tests.
   * @param variant The wrapper of the variant we are creating the test tasks for.
   */
  public void createTestTask(final VariantWrapper variant) {
    Test testTask = project.tasks.create("test$variant.completeName", Test)
    Task classesTask = configureClassesTask(variant)
    //make the test depend on the classesTask that handles the compilation and resources of tests
    testTask.dependsOn(classesTask)
    testClassesTask.dependsOn(classesTask)
    //Clear the inputs because JavaBasePlugin adds an empty dir which makes it crash.
    testTask.inputs.sourceFiles.from.clear()
    JavaCompile testCompileTask = configureTestCompileTask(variant)
    //Add the same sources of testCompile to the test task. not needed really
    testTask.inputs.source(testCompileTask.source)
    Copy copyTask = createResourcesCopyTask(variant)
    copyTask.dependsOn(variant.androidCompileTask)
    testTask.dependsOn(copyTask)
    testTask.classpath = variant.testClasspath
    //set the location of the class files of the tests to run
    testTask.testClassesDir = testCompileTask.destinationDir
    testTask.group = JavaBasePlugin.VERIFICATION_GROUP
    testTask.description = "Run unit tests for Build '$variant.completeName'."
    //configure the report directory depending on gradle version
    testTask.reports.html.destination = variant.variantReportDestination
    //Include all the class files that end in Test
    testTask.scanForTestClasses = false
    String pattern = System.properties.getProperty("test.single")
    String pattern2 = System.properties.getProperty("test${variant.completeName}.single")
    if (pattern != null) {
      testTask.include("**${File.separator}${pattern}.class")
    }
    if (pattern2 != null) {
      testTask.include("**${File.separator}${pattern2}.class")
    }
    if (pattern == null && pattern2 == null) {
      testTask.include("**${File.separator}*Test.class")
    }
    // Add the path to the merged manifest, resources and assets as well as the main package name as system properties.
    testTask.systemProperties['android.manifest'] = variant.mergedManifest
    testTask.systemProperties['android.resources'] = variant.mergedResourcesDir
    testTask.systemProperties['android.assets'] = variant.mergedAssetsDir
    testTask.systemProperties['android.package'] = packageExtractor.packageName
    testReportTask.reportOn(testTask)
  }

  private TestReport getTestReportTask() {
    if (testReportTask == null) {
      testReportTask = project.tasks.create("test", TestReport)
      logger.info("Created test task")
      testReportTask.destinationDir = reportDestinationDir
      testReportTask.description = 'Runs all unit tests.'
      testReportTask.group = JavaBasePlugin.VERIFICATION_GROUP
      //Make the check task call this report task which will call the test tasks.
      project.tasks.getByName("check").dependsOn(testReportTask)
    }
    return testReportTask
  }

  private Task getTestClassesTask() {
    if (testClassesTask == null) {
      testClassesTask = project.tasks.create("testClasses")
      testClassesTask.description = 'Assembles the test classes directory.'
    }
    return testClassesTask
  }

  private Task configureClassesTask(final VariantWrapper variant) {
    Task classesTask = project.tasks.getByName(variant.sourceSet.classesTaskName)
    logger.info("classTask: $classesTask.name")
    // Clear out the group/description of the classes plugin so it's not top-level.
    classesTask.group = null
    classesTask.description = null
    return classesTask
  }

  private JavaCompile configureTestCompileTask(final VariantWrapper variant) {
    JavaCompile testCompileTask = project.tasks.getByName(variant.sourceSet.compileJavaTaskName) as JavaCompile
    testCompileTask.dependsOn(variant.androidCompileTask)
    testCompileTask.group = null
    testCompileTask.description = null
    testCompileTask.classpath = variant.classpath
    testCompileTask.source = variant.sourceSet.java
    testCompileTask.destinationDir = variant.compileDestinationDir
    testCompileTask.options.bootClasspath = bootClasspath
    return testCompileTask
  }

  private Copy createResourcesCopyTask(final VariantWrapper variant) {
    Copy resourcesCopyTask = project.tasks.create(variant.resourcesCopyTaskName, Copy)
    resourcesCopyTask.from(variant.realMergedResourcesDir)
    resourcesCopyTask.into(variant.mergedResourcesDir)
    return resourcesCopyTask
  }
}
