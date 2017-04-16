package org.lendingclub.neorx.mock;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MockNeoRxClientTest {

	@Test
	public void testIt() {
		ObjectMapper m = new ObjectMapper();
		MockNeoRxClient client = new MockNeoRxClient().enqueue(m.createObjectNode().put("a", 1))
				.enqueue(m.createObjectNode().put("a", 2));
		
		Assertions.assertThat(client.execCypher("foo").blockingFirst().path("a").asInt()).isEqualTo(1);
		Assertions.assertThat(client.execCypher("foo").blockingFirst().path("a").asInt()).isEqualTo(2);
		
		Assertions.assertThat(client.checkConnection()).isTrue();
		
		try {
			client.getDriver();
			Assertions.failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
		}
		catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(UnsupportedOperationException.class);
		}
	}
	

	
}
