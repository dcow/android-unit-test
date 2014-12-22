package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant
import com.android.build.gradle.tasks.MergeResources

import org.gradle.api.Project
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class LibraryVariantWrapperTest {
  private LibraryVariant variant
  private LibraryVariantWrapper target
  private Project project
  private TestVariant testVariant
  private MockProvider provider

  @Before
  public void setUp() {
    provider = new MockProvider()
    project = provider.provideProject()
    variant = mock(LibraryVariant.class)
    testVariant = mock(TestVariant.class)
    when(variant.testVariant).thenReturn(testVariant)
    target = new LibraryVariantWrapper(variant, project, provider.provideConfigurations(), provider.provideBootClasspath(), provider.provideLogger())
  }

  @Test
  public void testGetAndroidCompileTask() {
    MergeResources mergeResources = mock(MergeResources.class)
    when(testVariant.mergeResources).thenReturn(mergeResources)
    assertThat(target.androidCompileTask).isEqualTo(mergeResources)
  }
}
