/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.macgyver.neorx.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;

import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action1;
import rx.functions.Func1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.NumericNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxClientIntegrationTest extends RxNeoIntegrationTest {

	Logger logger = LoggerFactory.getLogger(NeoRxClientIntegrationTest.class);



	@Test
	public void testUnwrapped() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher("match (m:Person) where m.born>{born} return m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode t1) {
				Assertions.assertThat(t1.fieldNames()).contains("born", "name");
				Assertions.assertThat(t1.fieldNames()).doesNotContain("m");
			}
		});
	}

	@Test
	public void testUnwrappedNull() {
		// since we only return a single entity, we "unwrap" the value to make
		// it easier to process

		getClient().execCypher(
				"match (m:Person) where m.born>{born} return null as m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode t1) {
				assertNotNull(t1);

				assertTrue(t1.isNull());
			}
		});
	}

	@Test
	public void testCreateParameters() {
		ObjectNode n = getClient().createParameters("abc",null);
		Assertions.assertThat(n.get("abc").isNull()).isTrue();
	}
	@Test
	public void testWrapped() {

		getClient().execCypher(
				"match (m:Person) where m.born>{born} return m.born, m",
				"born", 1960).subscribe(new Action1<JsonNode>() {

			@Override
			public void call(JsonNode r) {

				assertThat(r.get("m.born")).isNotNull().isInstanceOf(
						NumericNode.class);
				assertTrue(r.get("m.born").isNumber());
				assertThat(r.get("m")).isNotNull().isInstanceOf(
						ObjectNode.class);
				Assertions.assertThat(r.get("notfound")).isNull();
			}
		});

	}

	@Test
	public void testInvalidUrl() {
		NeoRxClient c = new NeoRxClient("http://blahblah.example.com");
		try {
			c.execCypher("match m");
			Assertions.fail("should throw");
		} catch (Exception e) {

			Assertions.assertThat(e)
					.hasRootCauseInstanceOf(UnknownHostException.class)
					.isInstanceOf(NeoRxException.class);
		}
	}

	@Test
	public void testInvalidCypher() {

		try {
			getClient().execCypher("match m");
			Assertions.fail("should throw");
		} catch (Exception e) {
			Assertions.assertThat(e).isInstanceOf(NeoRxException.class);

		}
	}
}
