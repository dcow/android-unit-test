package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.internal.ProductFlavorData
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet
import com.android.builder.core.DefaultProductFlavor

import org.gradle.api.Project
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.internal.DefaultDomainObjectSet
import org.gradle.api.logging.Logger

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class MockProvider extends DependencyProvider {
  private ModelManager modelManager = mock(ModelManager.class)
  private ConfigurationManager configurationManager = mock(ConfigurationManager.class)
  private TaskManager taskManager = mock(TaskManager.class)
  private AndroidUnitTestPluginExtension extension = new AndroidUnitTestPluginExtension()
  private ProductFlavorData defaultConfigData = createDummyFlavorData()
  private boolean isAppPlugin
  private BasePlugin plugin = mock(BasePlugin.class)
  private PackageExtractor packageExtractor = mock(PackageExtractor.class)
  private BaseExtension androidExtension = mock(BaseExtension.class)
  private ConfigurationContainer configurations = mock(ConfigurationContainer.class)
  private String bootClasspath = "bootClasspath"
  private DefaultDomainObjectSet<BaseVariant> variants = new DefaultDomainObjectSet<>(BaseVariant.class)
  private File reportDestinationDir = new File("reportDestinationDir")
  private Logger logger = mock(Logger.class)
  private MainHandler handler = mock(MainHandler.class)
  private AppVariantWrapper appVariantWrapper = mock(AppVariantWrapper.class)
  private LibraryVariantWrapper libraryVariantWrapper = mock(LibraryVariantWrapper.class)

  public MockProvider() {
    super(createProjectMock())
  }

  private static Project createProjectMock() {
    return mock(Project.class)
  }

  private static ProductFlavorData createDummyFlavorData() {
    DefaultProductFlavor productFlavor = mock(DefaultProductFlavor.class)
    DefaultAndroidSourceSet sourceSet = mock(DefaultAndroidSourceSet.class)
    when(sourceSet.name).thenReturn("main")
    new ProductFlavorData(productFlavor, sourceSet, null, createProjectMock())
  }

  @Override
  public AndroidUnitTestPluginExtension provideExtension() {
    return extension
  }

  @Override
  public ModelManager provideModelManager() {
    return modelManager
  }

  @Override
  public ConfigurationManager provideConfigurationManager() {
    return configurationManager
  }

  @Override
  public TaskManager provideTaskManager() {
    return taskManager
  }

  @Override
  public ProductFlavorData provideDefaultConfigData() {
    return defaultConfigData
  }

  @Override
  public boolean isAppPlugin() {
    return isAppPlugin
  }

  @Override
  public BasePlugin provideAndroidPlugin() {
    return plugin
  }

  @Override
  public PackageExtractor providePackageExtractor() {
    return packageExtractor
  }

  @Override
  public BaseExtension provideAndroidExtension() {
    return androidExtension
  }

  @Override
  public ConfigurationContainer provideConfigurations() {
    return configurations
  }

  @Override
  public String provideBootClasspath() {
    return bootClasspath
  }

  @Override
  public DefaultDomainObjectSet<BaseVariant> provideVariants() {
    return variants
  }

  @Override
  public File provideReportDestinationDir() {
    return reportDestinationDir
  }

  @Override
  public Logger provideLogger() {
    return logger
  }

  @Override
  public MainHandler provideHandler() {
    return handler
  }

  @Override
  public AppVariantWrapper provideAppVariantWrapper(final ApplicationVariant applicationVariant) {
    return appVariantWrapper
  }

  @Override
  public LibraryVariantWrapper provideLibraryVariantWrapper(final LibraryVariant libraryVariant) {
    return libraryVariantWrapper
  }
}
