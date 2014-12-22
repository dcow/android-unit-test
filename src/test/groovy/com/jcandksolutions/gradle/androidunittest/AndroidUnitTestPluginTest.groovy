package com.jcandksolutions.gradle.androidunittest

import org.gradle.api.Project
import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

public class AndroidUnitTestPluginTest {
  private Project project
  private AndroidUnitTestPlugin target
  private DependencyProvider dependencyProvider
  private MainHandler handler

  @Before
  public void setUp() {
    project = mock(Project.class)
  }

  @Test
  public void testApplyRunsHandler() {
    dependencyProvider = mock(DependencyProvider.class)
    handler = mock(MainHandler.class)
    when(dependencyProvider.provideHandler()).thenReturn(handler)
    target = new AndroidUnitTestPlugin() {
      @Override
      protected DependencyProvider createDependencyProvider(Project project) {
        return dependencyProvider
      }
    }
    target.apply(project)
    verify(handler).run()
  }

  @Test
  public void testCreateDependencyProvider() {
    target = new AndroidUnitTestPlugin() {
      @Override
      public void apply(Project project) {
        dependencyProvider = createDependencyProvider(project)
      }
    }
    target.apply(project)
    assertThat(dependencyProvider).isExactlyInstanceOf(DependencyProvider.class)
  }
}
