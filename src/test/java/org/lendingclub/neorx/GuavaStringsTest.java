package org.lendingclub.neorx;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class GuavaStringsTest  {

	
	@Test
	public void testNullOrEmpty() {
		assertThat(GuavaStrings.isNullOrEmpty("")).isTrue();
		assertThat(GuavaStrings.isNullOrEmpty(null)).isTrue();
		assertThat(GuavaStrings.isNullOrEmpty(" ")).isFalse();
		assertThat(GuavaStrings.isNullOrEmpty("hello")).isFalse();
	}
}
