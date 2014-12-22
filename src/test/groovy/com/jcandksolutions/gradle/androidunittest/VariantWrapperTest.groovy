package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.tasks.ManifestProcessorTask
import com.android.build.gradle.tasks.MergeAssets
import com.android.builder.core.DefaultBuildType
import com.android.builder.core.DefaultProductFlavor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.internal.file.collections.SimpleFileCollection
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.api.internal.tasks.DefaultSourceSetContainer
import org.gradle.api.plugins.Convention
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.internal.reflect.Instantiator
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class VariantWrapperTest {
  private BaseVariant variant
  private VariantWrapper target
  private File mergedManifest
  private Project project
  private SourceSet sourceSet
  private SourceDirectorySet resources
  private SourceDirectorySet java
  private ConfigurationContainer configurations
  private Configuration configuration
  private Configuration dummyConfiguration
  private FileCollection classpath
  private FileCollection runpath
  private FileCollection mergedClasspathAndResources
  private String classesTaskName
  private File mergeAssetsOutputDir
  private FileCollection testClasspath
  private MockProvider provider

  @Before
  public void setUp() {
    provider = new MockProvider()
    project = provider.provideProject()
    configurations = provider.provideConfigurations()
    String bootClasspathString = provider.provideBootClasspath()
    Convention convention = mock(Convention.class)
    SourceSetContainer sourceSets = mock(DefaultSourceSetContainer.class)
    Instantiator instantiator = mock(Instantiator.class)
    when(instantiator.newInstance(DefaultSourceSetContainer.class, null, null, instantiator)).thenReturn(sourceSets)
    JavaPluginConvention javaConvention = new JavaPluginConvention(mock(ProjectInternal.class), instantiator);
    sourceSet = mock(SourceSet.class)
    variant = mock(ApplicationVariant.class)
    mergedManifest = mock(File.class)
    List<BaseVariantOutput> outputs = new ArrayList<>()
    BaseVariantOutput output = mock(BaseVariantOutput.class)
    ManifestProcessorTask manTask = mock(ManifestProcessorTask.class)
    DefaultBuildType buildType = mock(DefaultBuildType.class)
    resources = mock(SourceDirectorySet.class)
    java = mock(SourceDirectorySet.class)
    DefaultProductFlavor free = mock(DefaultProductFlavor.class)
    DefaultProductFlavor paid = mock(DefaultProductFlavor.class)
    List<DefaultProductFlavor> productFlavors = [free, paid]
    configuration = mock(Configuration.class)
    dummyConfiguration = mock(Configuration.class)
    JavaCompile androidJavaCompileTask = mock(JavaCompile.class)
    File javaCompileDestinationDir = new File("javaCompileDestinationDir")
    FileCollection javaCompileClasspath = mock(FileCollection.class)
    ConfigurableFileCollection mergedDestDirAndClassPath = mock(ConfigurableFileCollection.class)
    classpath = mock(FileCollection.class)
    File buildDir = new File("build")
    ConfigurableFileCollection resourcesDir = mock(ConfigurableFileCollection.class)
    mergedClasspathAndResources = mock(FileCollection.class)
    runpath = mock(FileCollection.class)
    classesTaskName = "classesTaskName"
    MergeAssets mergeAssets = mock(MergeAssets.class)
    mergeAssetsOutputDir = mock(File.class)
    testClasspath = mock(FileCollection.class)
    ConfigurableFileCollection bootClasspath = mock(ConfigurableFileCollection.class)
    when(project.file(anyString())).thenAnswer(new Answer<File>() {
      public File answer(InvocationOnMock invocation) {
        return new File(invocation.arguments[0] as String)
      }
    })
    when(project.convention).thenReturn(convention)
    when(project.buildDir).thenReturn(buildDir)
    when(project.files(javaCompileDestinationDir, javaCompileClasspath)).thenReturn(mergedDestDirAndClassPath)
    when(project.files("build${File.separator}resources${File.separator}testFreePaidDebug")).thenReturn(resourcesDir)
    when(project.files(bootClasspathString)).thenReturn(bootClasspath)
    when(convention.getPlugin(JavaPluginConvention)).thenReturn(javaConvention)
    when(free.name).thenReturn("free")
    when(paid.name).thenReturn("paid")
    when(variant.productFlavors).thenReturn(productFlavors)
    when(variant.outputs).thenReturn(outputs)
    outputs.add(output)
    when(output.processManifest).thenReturn(manTask)
    when(variant.buildType).thenReturn(buildType)
    when(variant.javaCompile).thenReturn(androidJavaCompileTask)
    when(variant.dirName).thenReturn("variantDirName")
    when(variant.mergeAssets).thenReturn(mergeAssets)
    when(sourceSets.create("testFreePaidDebug")).thenReturn(sourceSet)
    when(sourceSet.resources).thenReturn(resources)
    when(sourceSet.java).thenReturn(java)
    when(sourceSet.classesTaskName).thenReturn(classesTaskName)
    when(manTask.manifestOutputFile).thenReturn(mergedManifest)
    when(buildType.name).thenReturn("debug")
    when(configurations.create("_testFreePaidDebugCompile")).thenReturn(configuration)
    when(configurations.findByName(anyString())).thenReturn(dummyConfiguration)
    when(androidJavaCompileTask.destinationDir).thenReturn(javaCompileDestinationDir)
    when(androidJavaCompileTask.classpath).thenReturn(javaCompileClasspath)
    when(classpath.plus(resourcesDir)).thenReturn(mergedClasspathAndResources)
    when(mergedClasspathAndResources.plus(any(SimpleFileCollection.class))).thenReturn(runpath)
    when(runpath.plus(bootClasspath)).thenReturn(testClasspath)
    when(configuration.plus(mergedDestDirAndClassPath)).thenReturn(classpath)
    when(mergeAssets.outputDir).thenReturn(mergeAssetsOutputDir)
    target = new VariantWrapper(variant, project, configurations, bootClasspathString, provider.provideLogger(), null) {
      @Override
      Task getAndroidCompileTask() {
        return null
      }
    }
  }

  @Test
  public void testConfigureSourceSet() {
    target.configureSourceSet()
    ArgumentCaptor fileCaptor = ArgumentCaptor.forClass(File.class)
    verify(resources).srcDirs(fileCaptor.capture())
    assertThat(fileCaptor.value).isEqualTo(new File("src${File.separator}test${File.separator}resources"))
    ArgumentCaptor fileArrayCaptor = ArgumentCaptor.forClass(ArrayList.class)
    verify(java).setSrcDirs(fileArrayCaptor.capture())
    assertThat(fileArrayCaptor.value).contains(new File("src${File.separator}test${File.separator}java"), new File("src${File.separator}testDebug${File.separator}java"), new File("src${File.separator}testFreePaid${File.separator}java"), new File("src${File.separator}testFreePaidDebug${File.separator}java"), new File("src${File.separator}testFree${File.separator}java"), new File("src${File.separator}testFreeDebug${File.separator}java"), new File("src${File.separator}testPaid${File.separator}java"), new File("src${File.separator}testPaidDebug${File.separator}java"))
    verify(configuration, times(4)).extendsFrom(dummyConfiguration)
    verify(sourceSet).compileClasspath = classpath
    ArgumentCaptor fileCollectionCaptor = ArgumentCaptor.forClass(FileCollection.class)
    verify(mergedClasspathAndResources).plus(fileCollectionCaptor.capture())
    assertThat(fileCollectionCaptor.value.asPath).isEqualTo("build${File.separator}test-classes${File.separator}variantDirName".toString())
    verify(sourceSet).runtimeClasspath = runpath
    verify(sourceSet).compiledBy(classesTaskName)
  }

  @Test
  public void testGetVariantReportDestination() {
    assertThat(target.variantReportDestination).isEqualTo(new File("build${File.separator}test-report${File.separator}variantDirName"))
  }

  @Test
  public void testGetMergedManifest() {
    assertThat(target.mergedManifest).isEqualTo(mergedManifest)
  }

  @Test
  public void testGetMergedResourcesDir() {
    assertThat(target.mergedResourcesDir).isEqualTo(new File("build${File.separator}test-resources${File.separator}FreePaidDebug${File.separator}res"))
  }

  @Test
  public void testGetMergedAssetsDir() {
    assertThat(target.mergedAssetsDir).isEqualTo(mergeAssetsOutputDir)
  }

  @Test
  public void testGetTestClasspath() {
    assertThat(target.testClasspath).isEqualTo(testClasspath)
  }

  @Test
  public void testGetResourcesCopyTaskName() {
    assertThat(target.resourcesCopyTaskName).isEqualTo("copyFreePaidDebugTestResources")
  }

  @Test
  public void testGetBaseVariant() {
    assertThat(target.baseVariant).isEqualTo(variant)
  }

  @Test
  public void testGetRealMergedResourcesDir() {
    assertThat(target.realMergedResourcesDir).isEqualTo("build${File.separator}intermediates${File.separator}res${File.separator}variantDirName".toString())
  }
}
