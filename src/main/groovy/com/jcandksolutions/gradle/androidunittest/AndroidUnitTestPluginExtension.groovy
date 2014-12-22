package com.jcandksolutions.gradle.androidunittest

/**
 * Class that handles the extension of the plugin for configuration.
 */
public class AndroidUnitTestPluginExtension {
  private boolean testReleaseBuildType
  private boolean downloadTestDependenciesSources
  private boolean downloadTestDependenciesJavadoc
  private boolean downloadDependenciesJavadoc
  private boolean downloadDependenciesSources
  /**
   * Retrieves the TestReleaseBuildType property which enables testing if release build types. Only
   * works on App projects, not library projects.
   * @return {@code true} if property enabled, {@code false} otherwise.
   */
  public boolean getTestReleaseBuildType() {
    return testReleaseBuildType;
  }

  /**
   * Sets the TestReleaseBuildType property which enables testing if release build types. Only
   * works on App projects, not library projects.
   * @param value The value to set.
   */
  public void setTestReleaseBuildType(boolean value) {
    testReleaseBuildType = value;
  }

  /**
   * Retrieves the DownloadTestDependenciesSources property which enables the download of the
   * sources of the tests dependencies.
   * @return {@code true} if property enabled, {@code false} otherwise.
   */
  public boolean isDownloadTestDependenciesSources() {
    return downloadTestDependenciesSources
  }

  /**
   * Sets the DownloadTestDependenciesSources property which enables the download of the sources of
   * the tests dependencies.
   * @param value The value to set.
   */
  public void setDownloadTestDependenciesSources(boolean value) {
    downloadTestDependenciesSources = value
  }

  /**
   * Retrieves the DownloadTestDependenciesJavadoc property which enables the download of the
   * Javadoc of the tests dependencies.
   * @return {@code true} if property enabled, {@code false} otherwise.
   */
  public boolean isDownloadTestDependenciesJavadoc() {
    return downloadTestDependenciesJavadoc
  }

  /**
   * Sets the DownloadTestDependenciesJavadoc property which enables the download of the Javadoc of
   * the tests dependencies.
   * @param value The value to set.
   */
  public void setDownloadTestDependenciesJavadoc(boolean value) {
    downloadTestDependenciesJavadoc = value
  }

  /**
   * Retrieves the DownloadDependenciesSources property which enables the download of the
   * sources of the app dependencies.
   * @return {@code true} if property enabled, {@code false} otherwise.
   */
  public boolean isDownloadDependenciesSources() {
    return downloadDependenciesSources
  }

  /**
   * Sets the DownloadDependenciesSources property which enables the download of the sources of
   * the app dependencies.
   * @param value The value to set.
   */
  public void setDownloadDependenciesSources(boolean value) {
    downloadDependenciesSources = value
  }

  /**
   * Retrieves the DownloadDependenciesJavadoc property which enables the download of the
   * Javadoc of the app dependencies.
   * @return {@code true} if property enabled, {@code false} otherwise.
   */
  public boolean isDownloadDependenciesJavadoc() {
    return downloadDependenciesJavadoc
  }

  /**
   * Sets the DownloadDependenciesJavadoc property which enables the download of the Javadoc of
   * the app dependencies.
   * @param value The value to set.
   */
  public void setDownloadDependenciesJavadoc(boolean value) {
    downloadDependenciesJavadoc = value
  }
}
