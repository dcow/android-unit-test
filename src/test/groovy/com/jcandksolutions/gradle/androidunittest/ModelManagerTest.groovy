package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.BaseVariant
import com.android.builder.model.ArtifactMetaData
import com.android.builder.model.SourceProvider

import org.gradle.api.artifacts.Configuration
import org.gradle.api.tasks.SourceSet
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Matchers.isNull
import static org.mockito.Mockito.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class ModelManagerTest {
  private ModelManager target
  private BasePlugin plugin
  private MockProvider provider
  private VariantWrapper variantWrapper
  private BaseVariant variant
  private String javaCompileTaskName
  private Configuration configuration
  private File classesFolder

  @Before
  public void setUp() {
    provider = new MockProvider()
    plugin = provider.provideAndroidPlugin()
    target = new ModelManager(plugin)
    variantWrapper = mock(VariantWrapper.class)
    variant = mock(BaseVariant.class)
    when(variantWrapper.baseVariant).thenReturn(variant)
    when(variant.name).thenReturn("debug")
    SourceSet sourceSet = mock(SourceSet.class)
    when(variantWrapper.sourceSet).thenReturn(sourceSet)
    javaCompileTaskName = "javaCompileTaskName"
    when(sourceSet.compileJavaTaskName).thenReturn(javaCompileTaskName)
    configuration = mock(Configuration.class)
    when(variantWrapper.configuration).thenReturn(configuration)
    classesFolder = new File("classes")
    when(variantWrapper.compileDestinationDir).thenReturn(classesFolder)
  }

  @Test
  public void testRegister() {
    target.register()
    verify(plugin).registerArtifactType("_unit_test_", true, ArtifactMetaData.TYPE_JAVA)
    verify(plugin).registerArtifactType("_sources_javadoc_", true, ArtifactMetaData.TYPE_JAVA)
  }

  @Test
  public void testRegisterArtifact() {
    target.registerArtifact(variantWrapper)
    ArgumentCaptor<TestSourceProvider> captor = ArgumentCaptor.forClass(TestSourceProvider.class)
    verify(plugin).registerJavaArtifact(eq("_unit_test_"), eq(variant), eq(javaCompileTaskName), eq(javaCompileTaskName), eq(configuration), eq(classesFolder), captor.capture())
    assertThat(captor.value).isExactlyInstanceOf(TestSourceProvider.class)
  }

  @Test
  public void testRegisterJavadocSourcesArtifact() {
    Configuration config = mock(Configuration.class)
    target.registerArtifact(variantWrapper)
    target.registerJavadocSourcesArtifact(config)
    ArgumentCaptor<File> fileCaptor = ArgumentCaptor.forClass(File.class)
    verify(plugin).registerJavaArtifact(eq("_sources_javadoc_"), eq(variant), eq("dummyAssembleTaskName"), eq("dummyJavaCompileTaskName"), eq(config), fileCaptor.capture(), isNull(SourceProvider.class))
    assertThat(fileCaptor.value).isEqualTo(new File("dummyClassesFolder"))
  }
}
