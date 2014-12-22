package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.internal.ProductFlavorData
import com.android.build.gradle.internal.api.DefaultAndroidSourceSet
import com.android.builder.core.DefaultProductFlavor

import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.when

public class PackageExtractorTest {
  private PackageExtractor target
  private MockProvider provider
  private ProductFlavorData data

  @Before
  public void setUp() {
    provider = new MockProvider()
    data = provider.provideDefaultConfigData()
    target = new PackageExtractor(data, provider.provideLogger())
  }

  @Test
  public void testGetPackageNameWithProvidedAppId() {
    DefaultProductFlavor flavor = data.productFlavor
    when(flavor.applicationId).thenReturn("package")
    assertThat(target.packageName).isEqualTo("package")
  }

  @Test
  public void testGetPackageNameFromManifest() {
    DefaultAndroidSourceSet source = data.sourceSet
    File manifest = new File(getClass().getResource("AndroidManifest.xml").toURI())
    when(source.manifestFile).thenReturn(manifest)
    assertThat(target.packageName).isEqualTo("com.example.app")
  }
}
