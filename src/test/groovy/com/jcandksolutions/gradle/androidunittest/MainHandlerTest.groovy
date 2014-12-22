package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.BaseVariant
import com.android.builder.core.DefaultBuildType

import org.gradle.api.internal.DefaultDomainObjectSet
import org.junit.Before
import org.junit.Test

import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class MainHandlerTest {
  private MainHandler target
  private VariantWrapper variantWrapper
  private boolean isVariantInvalid
  private MockProvider provider
  private ModelManager modelManager
  private ConfigurationManager configurationManager
  private BaseVariant variant
  private DefaultBuildType buildType
  private AndroidUnitTestPluginExtension extension
  private TaskManager taskManager

  @Before
  public void setUp() {
    provider = new MockProvider()
    modelManager = provider.provideModelManager()
    configurationManager = provider.provideConfigurationManager()
    taskManager = provider.provideTaskManager()
    extension = provider.provideExtension()
    DefaultDomainObjectSet<BaseVariant> variants = provider.provideVariants()
    variant = mock(BaseVariant.class)
    buildType = mock(DefaultBuildType)
    when(variant.buildType).thenReturn(buildType)
    variants.add(variant)
    variantWrapper = mock(VariantWrapper.class)
    target = new MainHandler(provider) {
      @Override
      protected VariantWrapper createVariantWrapper(final BaseVariant variant) {
        return variantWrapper
      }

      @Override
      protected boolean isVariantInvalid(final BaseVariant baseVariant) {
        return isVariantInvalid
      }
    }
  }

  @Test
  public void testRunWithNonDebuggableVariantAndNoReleaseBuildTypeEnabledDoNothing() {
    when(buildType.debuggable).thenReturn(false)
    extension.testReleaseBuildType = false
    target.run()
    verify(modelManager).register()
    verify(configurationManager).createNewConfigurations()
    verify(taskManager, never()).createTestTask(any(VariantWrapper.class))
  }

  @Test
  public void testRunWithDebuggableVariant() {
    when(buildType.debuggable).thenReturn(true)
    extension.testReleaseBuildType = false
    target.run()
    verify(modelManager).register()
    verify(configurationManager).createNewConfigurations()
    verify(variantWrapper).configureSourceSet()
    verify(taskManager).createTestTask(variantWrapper)
    verify(modelManager).registerArtifact(variantWrapper)
  }

  @Test
  public void testRunWithNonDebuggableVariantAndReleaseBuildTypeEnabled() {
    when(buildType.debuggable).thenReturn(false)
    extension.testReleaseBuildType = true
    target.run()
    verify(modelManager).register()
    verify(configurationManager).createNewConfigurations()
    verify(variantWrapper).configureSourceSet()
    verify(taskManager).createTestTask(variantWrapper)
    verify(modelManager).registerArtifact(variantWrapper)
  }
}
