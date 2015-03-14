package io.macgyver.neorx.rest.impl;

import static org.assertj.core.api.Assertions.assertThat;
import io.macgyver.neorx.rest.NeoRxUnitTest;

import org.junit.Test;

public class GuavaStringsTest extends NeoRxUnitTest {

	
	@Test
	public void testNullOrEmpty() {
		assertThat(GuavaStrings.isNullOrEmpty("")).isTrue();
		assertThat(GuavaStrings.isNullOrEmpty(null)).isTrue();
		assertThat(GuavaStrings.isNullOrEmpty(" ")).isFalse();
		assertThat(GuavaStrings.isNullOrEmpty("hello")).isFalse();
	}
}
