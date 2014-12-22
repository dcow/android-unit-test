package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.ApplicationVariant

import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class AppVariantWrapperTest {
  private ApplicationVariant variant
  private AppVariantWrapper target
  private Project project
  private MockProvider provider

  @Before
  public void setUp() {
    provider = new MockProvider()
    project = provider.provideProject()
    variant = mock(ApplicationVariant.class)
    target = new AppVariantWrapper(variant, project, provider.provideConfigurations(), provider.provideBootClasspath(), provider.provideLogger())
  }

  @Test
  public void testGetAndroidCompileTask() {
    JavaCompile javaCompile = mock(JavaCompile.class)
    when(variant.javaCompile).thenReturn(javaCompile)
    assertThat(target.androidCompileTask).isEqualTo(javaCompile)
  }
}
