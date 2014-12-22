package com.jcandksolutions.gradle.androidunittest

import com.android.builder.model.SourceProvider

/**
 * Class that implements the SourceProvider needed for the Android plugin to register the model.
 */
public class TestSourceProvider implements SourceProvider {
  private VariantWrapper variantWrapper;
  /**
   * Instantiates a new TestSourceProvider.
   * @param variantWrapper The variant for which we are providing the TestSource.
   */
  public TestSourceProvider(VariantWrapper variantWrapper) {
    this.variantWrapper = variantWrapper
  }

  @Override
  public String getName() {
    return variantWrapper.sourceSet.name
  }

  @Override
  public File getManifestFile() {
    return variantWrapper.mergedManifest
  }

  @Override
  public Collection<File> getJavaDirectories() {
    return variantWrapper.sourceSet.java.srcDirs
  }

  @Override
  public Collection<File> getResourcesDirectories() {
    return variantWrapper.sourceSet.resources.srcDirs
  }

  @Override
  public Collection<File> getAidlDirectories() {
    return Collections.emptyList()
  }

  @Override
  public Collection<File> getRenderscriptDirectories() {
    return Collections.emptyList()
  }

  @Override
  Collection<File> getCDirectories() {
    return Collections.emptyList()
  }

  @Override
  Collection<File> getCppDirectories() {
    return Collections.emptyList()
  }

  @Override
  public Collection<File> getResDirectories() {
    return Collections.singleton(variantWrapper.mergedResourcesDir)
  }

  @Override
  public Collection<File> getAssetsDirectories() {
    return Collections.singleton(variantWrapper.mergedAssetsDir)
  }

  @Override
  public Collection<File> getJniLibsDirectories() {
    return Collections.emptyList()
  }
}
