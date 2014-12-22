package com.jcandksolutions.gradle.androidunittest

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariant

import org.junit.Before
import org.junit.Test

import static org.fest.assertions.api.Assertions.assertThat
import static org.mockito.Mockito.mock

public class AppHandlerTest {
  private AppHandler target
  private MockProvider provider
  private VariantWrapper wrapper

  @Before
  public void setUp() {
    provider = new MockProvider()
    wrapper = provider.provideAppVariantWrapper(null)
    target = new AppHandler(provider)
  }

  @Test
  public void testIsVariantInvalid() {
    BaseVariant variant = mock(BaseVariant.class)
    assertThat(target.isVariantInvalid(variant)).isFalse()
  }

  @Test
  public void testCreateVariantWrapper() {
    ApplicationVariant variant = mock(ApplicationVariant.class)
    VariantWrapper wrapper = target.createVariantWrapper(variant)
    assertThat(wrapper).isEqualTo(wrapper)
  }
}
