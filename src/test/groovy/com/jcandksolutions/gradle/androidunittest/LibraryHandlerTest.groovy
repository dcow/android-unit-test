package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.LibraryVariant
import com.android.build.gradle.api.TestVariant

import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class LibraryHandlerTest {
  private LibraryHandler target
  private MockProvider provider
  private LibraryVariantWrapper wrapper

  @Before
  public void setUp() {
    provider = new MockProvider()
    wrapper = provider.provideLibraryVariantWrapper(null)
    target = new LibraryHandler(provider)
  }

  @Test
  public void testIsVariantInvalid() {
    LibraryVariant variant = mock(LibraryVariant.class)
    when(variant.testVariant).thenReturn(null, mock(TestVariant.class))
    assertThat(target.isVariantInvalid(variant)).isTrue()
    assertThat(target.isVariantInvalid(variant)).isFalse()
  }

  @Test
  public void testCreateVariantWrapper() {
    LibraryVariant variant = mock(LibraryVariant.class)
    VariantWrapper wrapper = target.createVariantWrapper(variant)
    assertThat(wrapper).isEqualTo(wrapper)
  }
}
