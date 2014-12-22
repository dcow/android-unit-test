package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.BaseExtension
import com.android.builder.core.DefaultBuildType
import com.android.builder.core.DefaultProductFlavor

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.DependencyArtifact
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ExternalModuleDependency
import org.gradle.api.artifacts.ResolveException
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.artifacts.DefaultDependencySet
import org.gradle.api.internal.artifacts.dependencies.DefaultDependencyArtifact
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentCaptor
import org.mockito.invocation.InvocationOnMock
import org.mockito.stubbing.Answer

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Matchers.any
import static org.mockito.Matchers.anyString
import static org.mockito.Mockito.doAnswer
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class ConfigurationManagerTest {
  private ConfigurationManager target
  private ConfigurationContainer configurations
  private BaseExtension androidExtension
  private Project project
  private AndroidUnitTestPluginExtension extension
  private ModelManager modelManager

  @Before
  public void setUp() {
    MockProvider provider = new MockProvider()
    configurations = provider.provideConfigurations()
    androidExtension = provider.provideAndroidExtension()
    extension = provider.provideExtension()
    project = provider.provideProject()
    modelManager = provider.provideModelManager()
    target = new ConfigurationManager(androidExtension, configurations, project, extension, modelManager, provider.provideLogger())
  }

  @Test
  public void testCreateNewConfigurations() {
    Project project = ProjectBuilder.builder().build();
    NamedDomainObjectContainer<DefaultBuildType> buildTypes = project.container(DefaultBuildType)
    DefaultBuildType buildType = mock(DefaultBuildType.class)
    when(buildType.name).thenReturn("debug")
    buildTypes.add(buildType)
    when(androidExtension.buildTypes).thenReturn(buildTypes)
    NamedDomainObjectContainer<DefaultProductFlavor> flavors = project.container(DefaultProductFlavor)
    DefaultProductFlavor flavor = mock(DefaultProductFlavor.class)
    when(flavor.name).thenReturn("flavor")
    flavors.add(flavor)
    when(androidExtension.productFlavors).thenReturn(flavors)
    Configuration testCompileConfiguration = mock(Configuration.class)
    DependencySet dependencies = new DefaultDependencySet("lol", new DefaultDomainObjectSet<Dependency>(Dependency.class))
    Dependency dependency = mock(ExternalModuleDependency.class)
    when(dependency.copy()).thenReturn(dependency)
    when(dependency.name).thenReturn("dependency")
    dependencies.add(dependency)
    when(testCompileConfiguration.dependencies).thenReturn(dependencies)
    Configuration tmpConf = mock(Configuration.class)
    DependencySet tmpDependencies = mock(DependencySet.class)
    when(tmpConf.dependencies).thenReturn(tmpDependencies)
    when(tmpConf.files).thenReturn(null).thenThrow(ResolveException.class)
    when(configurations.create(anyString())).thenReturn(tmpConf)
    when(configurations.create(ConfigurationManager.TEST_COMPILE)).thenReturn(testCompileConfiguration)
    when(configurations.getByName(ConfigurationManager.TEST_COMPILE)).thenReturn(testCompileConfiguration)
    Configuration compileConfiguration = mock(Configuration.class)
    when(compileConfiguration.dependencies).thenReturn(dependencies)
    when(configurations.getByName(ConfigurationManager.COMPILE)).thenReturn(compileConfiguration)
    Configuration sourcesConfiguration = mock(Configuration.class)
    when(configurations.create(ConfigurationManager.SOURCES_JAVADOC)).thenReturn(sourcesConfiguration)
    Configuration debugConfiguration = mock(Configuration.class)
    when(debugConfiguration.dependencies).thenReturn(dependencies)
    when(configurations.getByName("debugCompile")).thenReturn(debugConfiguration)
    Configuration flavorConfiguration = mock(Configuration.class)
    when(flavorConfiguration.dependencies).thenReturn(dependencies)
    when(configurations.getByName("flavorCompile")).thenReturn(flavorConfiguration)
    Configuration testDebugConfiguration = mock(Configuration.class)
    when(testDebugConfiguration.dependencies).thenReturn(dependencies)
    when(configurations.getByName("testDebugCompile")).thenReturn(testDebugConfiguration)
    Configuration testFlavorConfiguration = mock(Configuration.class)
    when(testFlavorConfiguration.dependencies).thenReturn(dependencies)
    when(configurations.getByName("testFlavorCompile")).thenReturn(testFlavorConfiguration)
    doAnswer(new Answer<Void>() {
      @Override
      Void answer(final InvocationOnMock invocation) throws Throwable {
        Closure clo = invocation.arguments[0] as Closure
        clo.run()
        return null
      }
    } as Answer).when(this.project).afterEvaluate(any(Closure.class) as Closure)
    extension.downloadDependenciesJavadoc = true
    extension.downloadDependenciesSources = true
    extension.downloadTestDependenciesJavadoc = true
    extension.downloadTestDependenciesSources = true
    target.createNewConfigurations()
    verify(testCompileConfiguration).extendsFrom compileConfiguration
    verify(configurations).create("testDebugCompile")
    verify(configurations).create("testFlavorCompile")
    ArgumentCaptor<DependencyArtifact> captor = ArgumentCaptor.forClass(DependencyArtifact.class)
    verify(dependency, times(2)).addArtifact(captor.capture())
    for (DependencyArtifact value in captor.allValues) {
      assertThat(value).isIn(new DefaultDependencyArtifact("dependency", "jar", "jar", "sources", null), new DefaultDependencyArtifact("dependency", "jar", "jar", "javadoc", null))
    }
    verify(tmpDependencies, times(1)).add(dependency)
    ArgumentCaptor<Configuration> confCaptor = ArgumentCaptor.forClass(Configuration.class)
    verify(sourcesConfiguration, times(1)).extendsFrom(confCaptor.capture())
    assertThat(confCaptor.value).isEqualTo(tmpConf)
    verify(modelManager).registerJavadocSourcesArtifact(sourcesConfiguration)
  }
}
