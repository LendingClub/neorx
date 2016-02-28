package io.macgyver.neorx.rest;

import org.junit.Test;

public class NeoRxClientBuilderTest {

	@Test
	public void testIt() {
		
		
		
		new NeoRxClientBuilder().withOkHttpClientConfig(okhttpBuilder -> {okhttpBuilder.interceptors();}).build();
	}

}
