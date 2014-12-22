package com.jcandksolutions.gradle.androidunittest

import org.gradle.api.file.SourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class TestSourceProviderTest {
  private TestSourceProvider target
  private File mergedManifest
  private Set<File> javaDirectories
  private HashSet resourcesDirectories
  private File mergedResourcesDir
  private File mergedAssetsDir

  @Before
  public void setUp() {
    VariantWrapper wrapper = mock(VariantWrapper.class)
    SourceSet sourceSet = mock(SourceSet.class)
    SourceDirectorySet java = mock(SourceDirectorySet.class)
    javaDirectories = new HashSet<>()
    mergedManifest = new File("mergedManifest")
    SourceDirectorySet resources = mock(SourceDirectorySet.class)
    resourcesDirectories = new HashSet<>()
    mergedResourcesDir = new File("mergedResourcesDir")
    mergedAssetsDir = new File("mergedAssetsDir")
    when(java.srcDirs).thenReturn(javaDirectories)
    when(resources.srcDirs).thenReturn(resourcesDirectories)
    when(wrapper.sourceSet).thenReturn(sourceSet)
    when(wrapper.mergedManifest).thenReturn(mergedManifest)
    when(wrapper.mergedResourcesDir).thenReturn(mergedResourcesDir)
    when(wrapper.mergedAssetsDir).thenReturn(mergedAssetsDir)
    when(sourceSet.name).thenReturn("name")
    when(sourceSet.java).thenReturn(java)
    when(sourceSet.resources).thenReturn(resources)
    target = new TestSourceProvider(wrapper)
  }

  @Test
  public void testGetName() {
    assertThat(target.name).isEqualTo("name")
  }

  @Test
  public void testGetManifestFile() {
    assertThat(target.manifestFile).isEqualTo(mergedManifest)
  }

  @Test
  public void testGetJavaDirectories() {
    assertThat(target.javaDirectories).isEqualTo(javaDirectories)
  }

  @Test
  public void testGetResourcesDirectories() {
    assertThat(target.resourcesDirectories).isEqualTo(resourcesDirectories)
  }

  @Test
  public void testGetAidlDirectories() {
    assertThat(target.aidlDirectories).isEqualTo(Collections.emptyList())
  }

  @Test
  public void testGetRenderscriptDirectories() {
    assertThat(target.renderscriptDirectories).isEqualTo(Collections.emptyList())
  }

  @Test
  public void testGetCDirectories() {
    assertThat(target.CDirectories).isEqualTo(Collections.emptyList())
  }

  @Test
  public void testGetCppDirectories() {
    assertThat(target.cppDirectories).isEqualTo(Collections.emptyList())
  }

  @Test
  public void testGetResDirectories() {
    assertThat(target.resDirectories).containsExactly(mergedResourcesDir)
  }

  @Test
  public void testGetAssetsDirectories() {
    assertThat(target.assetsDirectories).containsExactly(mergedAssetsDir)
  }

  @Test
  public void testGetJniLibsDirectories() {
    assertThat(target.jniLibsDirectories).isEqualTo(Collections.emptyList())
  }
}
