package com.jcandksolutions.gradle.androidunittest

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.collections.DefaultConfigurableFileCollection
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.reporting.DirectoryReport
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskInputs
import org.gradle.api.tasks.compile.CompileOptions
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.TestReport
import org.gradle.api.tasks.testing.TestTaskReports
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.fest.assertions.api.Assertions.entry
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class TaskManagerTest {
  private static final String FLAVOR_DEBUG = "FlavorDebug"
  private TaskManager target
  private VariantWrapper variant
  private Task classesTask
  private org.gradle.api.tasks.testing.Test testTask
  private Task testClassesTask
  private Set<Object> from
  private JavaCompile testCompileTask
  private Task androidCompileTask
  private FileCollection classpath
  private SourceDirectorySet java
  private File compileDestinationDir
  private CompileOptions options
  private TaskInputs inputs
  private FileTree source
  private Copy resourcesCopyTask
  private File mergedResourcesDir
  private Task processResourcesTask
  private FileCollection testClasspath
  private DirectoryReport html
  private File variantReportDestination
  private File mergedManifest
  private File mergedAssetsDir
  private PackageExtractor packageExtractor
  private HashMap<String, Object> systemProperties
  private TestReport testReportTask
  private File reportDestinationDir
  private Task checkTask
  private MockProvider provider

  @Before
  public void setUp() {
    provider = new MockProvider()
    Project project = provider.provideProject()
    packageExtractor = provider.providePackageExtractor()
    reportDestinationDir = provider.provideReportDestinationDir()
    TaskContainer tasks = mock(TaskContainer.class)
    testTask = mock(org.gradle.api.tasks.testing.Test.class)
    testClassesTask = mock(Task.class)
    classesTask = mock(Task.class)
    variant = mock(VariantWrapper.class)
    SourceSet sourceSet = mock(SourceSet.class)
    inputs = mock(TaskInputs.class)
    DefaultConfigurableFileCollection files = mock(DefaultConfigurableFileCollection.class)
    from = mock(Set.class)
    androidCompileTask = mock(Task.class)
    testCompileTask = mock(JavaCompile.class)
    classpath = mock(FileCollection.class)
    java = mock(SourceDirectorySet.class)
    compileDestinationDir = new File("destinationDir")
    options = mock(CompileOptions.class)
    source = mock(FileTree.class)
    resourcesCopyTask = mock(Copy.class)
    mergedResourcesDir = new File("mergedResourcesDir")
    testClasspath = mock(FileCollection.class)
    TestTaskReports reports = mock(TestTaskReports.class)
    html = mock(DirectoryReport.class)
    variantReportDestination = new File("reportDestination")
    systemProperties = new HashMap<>()
    mergedManifest = new File("mergedManifest")
    mergedAssetsDir = new File("mergedAssetsDir")
    testReportTask = mock(TestReport.class)
    checkTask = mock(Task.class)
    when(tasks.create("testFlavorDebug", org.gradle.api.tasks.testing.Test)).thenReturn(testTask)
    when(tasks.create("testClasses")).thenReturn(testClassesTask)
    when(tasks.create("test", TestReport)).thenReturn(testReportTask)
    when(tasks.create("resourcesCopyTaskName", Copy.class)).thenReturn(resourcesCopyTask)
    when(tasks.getByName("classesTaskName")).thenReturn(classesTask)
    when(tasks.getByName("compileJavaTaskName")).thenReturn(testCompileTask)
    when(tasks.getByName("check")).thenReturn(checkTask)
    when(project.tasks).thenReturn(tasks)
    when(variant.sourceSet).thenReturn(sourceSet)
    when(variant.completeName).thenReturn("$FLAVOR_DEBUG")
    when(variant.classpath).thenReturn(classpath)
    when(variant.androidCompileTask).thenReturn(androidCompileTask)
    when(variant.compileDestinationDir).thenReturn(compileDestinationDir)
    when(variant.resourcesCopyTaskName).thenReturn("resourcesCopyTaskName")
    when(variant.realMergedResourcesDir).thenReturn("realMergedResourcesDir")
    when(variant.mergedResourcesDir).thenReturn(mergedResourcesDir)
    when(variant.testClasspath).thenReturn(testClasspath)
    when(variant.variantReportDestination).thenReturn(variantReportDestination)
    when(variant.mergedManifest).thenReturn(mergedManifest)
    when(variant.mergedAssetsDir).thenReturn(mergedAssetsDir)
    when(sourceSet.classesTaskName).thenReturn("classesTaskName")
    when(sourceSet.java).thenReturn(java)
    when(sourceSet.compileJavaTaskName).thenReturn("compileJavaTaskName")
    when(testTask.inputs).thenReturn(inputs)
    when(testTask.reports).thenReturn(reports)
    when(testTask.systemProperties).thenReturn(systemProperties)
    when(reports.html).thenReturn(html)
    when(inputs.sourceFiles).thenReturn(files)
    when(files.from).thenReturn(from)
    when(testCompileTask.options).thenReturn(options)
    when(testCompileTask.source).thenReturn(source)
    when(testCompileTask.destinationDir).thenReturn(compileDestinationDir)
    when(packageExtractor.packageName).thenReturn("packageName")
    target = new TaskManager(provider.provideProject(), provider.provideBootClasspath(), provider.providePackageExtractor(), reportDestinationDir, provider.provideLogger())
  }

  @Test
  public void testCreateTestTask() {
    target.createTestTask(variant)
    verify(classesTask).group = null
    verify(classesTask).description = null
    verify(testTask).dependsOn(classesTask)
    verify(testClassesTask).description = "Assembles the test classes directory."
    verify(testClassesTask).dependsOn(classesTask)
    verify(from).clear()
    verify(testCompileTask).dependsOn(androidCompileTask)
    verify(testCompileTask).group = null
    verify(testCompileTask).description = null
    verify(testCompileTask).classpath = classpath
    verify(testCompileTask).source = java
    verify(testCompileTask).destinationDir = compileDestinationDir
    verify(options).bootClasspath = "bootClasspath"
    verify(inputs).source(source)
    verify(resourcesCopyTask).from("realMergedResourcesDir")
    verify(resourcesCopyTask).into(mergedResourcesDir)
    verify(resourcesCopyTask).dependsOn(androidCompileTask)
    verify(testTask).dependsOn(resourcesCopyTask)
    verify(testTask).classpath = testClasspath
    verify(testTask).testClassesDir = compileDestinationDir
    verify(testTask).group = JavaBasePlugin.VERIFICATION_GROUP
    verify(testTask).description = "Run unit tests for Build '$FLAVOR_DEBUG'."
    verify(html).destination = variantReportDestination
    verify(testTask).scanForTestClasses = false
    //TODO:missing pattern testing
    assertThat(systemProperties).contains(entry('android.manifest', mergedManifest), entry('android.resources', mergedResourcesDir), entry('android.assets', mergedAssetsDir), entry('android.package', "packageName"))
    verify(testReportTask).destinationDir = reportDestinationDir
    verify(testReportTask).description = 'Runs all unit tests.'
    verify(testReportTask).group = JavaBasePlugin.VERIFICATION_GROUP
    verify(checkTask).dependsOn(testReportTask)
    verify(testReportTask).reportOn(testTask)
  }
}
