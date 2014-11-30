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
package io.macgyver.neorx;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class NeoRxException extends RuntimeException {

	protected ObjectNode responseData;
	static ObjectMapper mapper = new ObjectMapper();
	public NeoRxException(String message) {
		super(message);
		responseData = mapper.createObjectNode();
	}
	public NeoRxException(Exception e) {
		super(e);
		responseData = mapper.createObjectNode();
	}
	public NeoRxException(ObjectNode n) {
		this(n.path("message").asText());
		responseData = n;
	}
	
	public ObjectNode getResponseData() {
		return responseData;
	}
}
