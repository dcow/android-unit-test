package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.ProductFlavorData

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.internal.plugins.DefaultPluginCollection
import org.gradle.api.logging.Logger
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.plugins.PluginCollection
import org.gradle.api.plugins.PluginContainer
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.fest.assertions.api.Assertions.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class DependencyProviderTest {
  private Project project
  private DependencyProvider target
  private AppPlugin appPlugin
  private PluginContainer plugins
  private BaseExtension androidExtension
  private ConfigurationContainer configurations
  private AndroidUnitTestPluginExtension extension
  private File reportDestinationDir
  private Logger logger

  @Before
  public void setUp() {
    project = mock(Project.class)
    plugins = mock(PluginContainer)
    androidExtension = mock(AppExtension)
    configurations = mock(ConfigurationContainer)
    PluginCollection<AppPlugin> appPlugins = new DefaultPluginCollection<>(AppPlugin)
    appPlugin = mock(AppPlugin)
    appPlugins.add(appPlugin)
    ExtensionContainer extensions = mock(ExtensionContainer.class)
    extension = mock(AndroidUnitTestPluginExtension)
    reportDestinationDir = new File("reportDestinationDir")
    logger = mock(Logger.class)
    List<String> bootClasspath = ["1", "2", "3"]
    when(project.plugins).thenReturn(plugins)
    when(project.configurations).thenReturn(configurations)
    when(project.extensions).thenReturn(extensions)
    when(project.buildDir).thenReturn(new File("build"))
    when(project.file("build${File.separator}test-report")).thenReturn(reportDestinationDir)
    when(project.logger).thenReturn(logger)
    when(plugins.withType(AppPlugin)).thenReturn(appPlugins)
    when(appPlugin.extension).thenReturn(androidExtension)
    when(extensions.create("androidUnitTest", AndroidUnitTestPluginExtension)).thenReturn(extension)
    when(appPlugin.bootClasspath).thenReturn(bootClasspath)
    target = new DependencyProvider(project)
  }

  @Test
  public void testProvideProject() {
    assertThat(target.provideProject()).isEqualTo(project)
  }

  @Test
  public void testProvideExtension() {
    assertThat(target.provideExtension()).isEqualTo(extension)
    verify(extension).downloadTestDependenciesSources = true
    verify(extension).downloadDependenciesSources = true
  }

  @Test
  public void testProvideModelManager() {
    assertThat(target.provideModelManager()).isExactlyInstanceOf(ModelManager)
  }

  @Test
  public void testProvideConfigurationManager() {
    assertThat(target.provideConfigurationManager()).isExactlyInstanceOf(ConfigurationManager)
  }

  @Test
  public void testProvideTaskManager() {
    assertThat(target.provideTaskManager()).isExactlyInstanceOf(TaskManager)
  }

  @Test
  public void testProvideDefaultConfigData() {
    ProductFlavorData defaultConfigData = mock(ProductFlavorData)
    when(appPlugin.defaultConfigData).thenReturn(defaultConfigData)
    assertThat(target.provideDefaultConfigData()).isEqualTo(defaultConfigData)
  }

  @Test
  public void testIsAppPluginWhenAppPluginProvided() {
    assertThat(target.appPlugin).isTrue()
  }

  @Test
  public void testIsAppPluginWhenLibraryPluginProvided() {
    PluginCollection<LibraryPlugin> libraryPlugins = mock(PluginCollection)
    when(plugins.withType(AppPlugin)).thenReturn(null)
    when(plugins.withType(LibraryPlugin)).thenReturn(libraryPlugins)
    assertThat(target.appPlugin).isFalse()
  }

  @Test
  public void testIsAppPluginWhenNoAndroidPluginProvided() {
    when(plugins.withType(AppPlugin)).thenReturn(null)
    try {
      target.appPlugin
      fail("IllegalStateException should've been thrown")
    } catch (IllegalStateException ignored) {
    }
  }

  @Test
  public void testProvideAndroidPluginWithAppPlugin() {
    assertThat(target.provideAndroidPlugin()).isEqualTo(appPlugin)
  }

  @Test
  public void testProvideAndroidPluginWithLibraryPlugin() {
    PluginCollection<LibraryPlugin> libraryPlugins = new DefaultPluginCollection<>(LibraryPlugin)
    LibraryPlugin libraryPlugin = mock(LibraryPlugin)
    libraryPlugins.add(libraryPlugin)
    when(plugins.withType(AppPlugin)).thenReturn(null)
    when(plugins.withType(LibraryPlugin)).thenReturn(libraryPlugins)
    assertThat(target.provideAndroidPlugin()).isEqualTo(libraryPlugin)
  }

  @Test
  public void testProvidePackageExtractor() {
    assertThat(target.providePackageExtractor()).isExactlyInstanceOf(PackageExtractor)
  }

  @Test
  public void testProvideAndroidExtension() {
    assertThat(target.provideAndroidExtension()).isEqualTo(androidExtension)
  }

  @Test
  public void testProvideConfigurations() {
    assertThat(target.provideConfigurations()).isEqualTo(configurations)
  }

  @Test
  public void testProvideBootClasspath() {
    assertThat(target.provideBootClasspath()).contains("1${File.pathSeparator}2${File.pathSeparator}3")
  }

  @Test
  public void testProvideVariantsWithAppPlugin() {
    DefaultDomainObjectSet<ApplicationVariant> variants = new DefaultDomainObjectSet<>(ApplicationVariant)
    when(((AppExtension) androidExtension).applicationVariants).thenReturn(variants)
    assertThat(target.provideVariants()).isEqualTo(variants)
  }

  @Test
  public void testProvideVariantsWithLibraryPlugin() {
    PluginCollection<LibraryPlugin> libraryPlugins = new DefaultPluginCollection<>(LibraryPlugin)
    LibraryPlugin libraryPlugin = mock(LibraryPlugin)
    libraryPlugins.add(libraryPlugin)
    DefaultDomainObjectSet<LibraryVariant> variants = new DefaultDomainObjectSet<>(LibraryVariant)
    androidExtension = mock(LibraryExtension)
    when(plugins.withType(AppPlugin)).thenReturn(null)
    when(plugins.withType(LibraryPlugin)).thenReturn(libraryPlugins)
    when(libraryPlugin.extension).thenReturn(androidExtension)
    when(((LibraryExtension) androidExtension).libraryVariants).thenReturn(variants)
    assertThat(target.provideVariants()).isEqualTo(variants)
  }

  @Test
  public void testProvideReportDestinationDir() {
    assertThat(target.provideReportDestinationDir()).isEqualTo(reportDestinationDir)
  }

  @Test
  public void testProvideLogger() {
    assertThat(target.provideLogger()).isEqualTo(logger)
  }

  @Test
  public void testProvideAppHandlerWithAppPlugin() {
    assertThat(target.provideHandler()).isInstanceOf(AppHandler.class)
  }

  @Test
  public void testProvideLibraryHandlerWithLibraryPlugin() {
    List<String> bootClasspath = ["1", "2", "3"]
    PluginCollection<LibraryPlugin> libraryPlugins = new DefaultPluginCollection<>(LibraryPlugin)
    LibraryPlugin libraryPlugin = mock(LibraryPlugin)
    libraryPlugins.add(libraryPlugin)
    when(plugins.withType(AppPlugin)).thenReturn(null)
    when(plugins.withType(LibraryPlugin)).thenReturn(libraryPlugins)
    when(libraryPlugin.bootClasspath).thenReturn(bootClasspath)
    LibraryExtension extension = mock(LibraryExtension.class)
    when(libraryPlugin.extension).thenReturn(extension)
    assertThat(target.provideHandler()).isInstanceOf(LibraryHandler.class)
  }

  @Test
  public void testProvideAppVariantWrapper() {
    ApplicationVariant variant = mock(ApplicationVariant.class)
    assertThat(target.provideAppVariantWrapper(variant)).isInstanceOf(AppVariantWrapper.class)
  }

  @Test
  public void testProvideLibraryVariantWrapper() {
    LibraryVariant variant = mock(LibraryVariant.class)
    assertThat(target.provideLibraryVariantWrapper(variant)).isInstanceOf(LibraryVariantWrapper.class)
  }
}
