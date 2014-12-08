package io.macgyver.neorx.rest.impl;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

public class GuavePreconditionsTest {

	@Test
	public void testPositive() {
		GuavaPreconditions.checkNotNull("");
		GuavaPreconditions.checkNotNull("", "message");
		GuavaPreconditions.checkArgument(true);
		GuavaPreconditions.checkArgument(true, "message");
	}

	@Test
	public void testNegative() {
		try {
			GuavaPreconditions.checkNotNull(null);
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(NullPointerException.class);
		}
		
		try {
			GuavaPreconditions.checkNotNull(null,"123 abcdef 456");
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(NullPointerException.class).hasMessageContaining("abcdef");
		}
		try {
			GuavaPreconditions.checkArgument(false);
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(IllegalArgumentException.class);
		}
		
		try {
			GuavaPreconditions.checkArgument(false,"123 abcdef 456");
			Assert.fail();
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(IllegalArgumentException.class).hasMessageContaining("abcdef");
		}
		
	}
}
