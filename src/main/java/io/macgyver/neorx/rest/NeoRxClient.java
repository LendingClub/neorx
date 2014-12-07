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

import static io.macgyver.neorx.rest.impl.guava.GuavaPreconditions.checkArgument;
import static io.macgyver.neorx.rest.impl.guava.GuavaPreconditions.checkNotNull;
import io.macgyver.neorx.rest.impl.NonStreamingResultImpl;
import io.macgyver.neorx.rest.impl.ResultMetaDataImpl;
import io.macgyver.neorx.rest.impl.SslTrust;
import io.macgyver.neorx.rest.impl.guava.GuavaStrings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.squareup.okhttp.Credentials;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Request.Builder;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

public class NeoRxClient {


	public static final String DEFAULT_URL = "http://localhost:7474";
	private String url = DEFAULT_URL;
	private String username = null;
	private String password = null;
	private boolean validateCertificates = false;
	private boolean streamResponse = true;
	final static ObjectMapper mapper = new ObjectMapper();
	private volatile OkHttpClient httpClient = null;
	java.util.logging.Logger logger = java.util.logging.Logger.getLogger(NeoRxClient.class.getName());
	
	private final Level REQUEST_RESPONSE_LEVEL=Level.FINE;  // level at which request/response logging will be logged
	public NeoRxClient() {
		this(DEFAULT_URL);
	}

	public NeoRxClient(String url) {
		this(url, null, null, false);
	}
	public NeoRxClient(String url, boolean validateCertificates) {
		this(url, null, null, validateCertificates);
	}
	public NeoRxClient(String url, String username, String password) {
		this(url, username, password, false);
	}

	public NeoRxClient(String url, String username, String password,
			boolean validateCertificates) {

		this.url = url;
		this.username = username;
		this.password = password;
		this.validateCertificates = validateCertificates;

		OkHttpClient client = new OkHttpClient();

		if (!validateCertificates) {
			client.setHostnameVerifier(SslTrust.withoutHostnameVerification());
			client.setSslSocketFactory(SslTrust.withoutCertificateValidation()
					.getSocketFactory());

		}

		this.httpClient = client;
	}

	protected OkHttpClient getClient() {

		return httpClient;
	}

	protected ObjectNode createParameters(Object... args) {
		checkNotNull(args);
		checkArgument(args.length % 2 == 0,
				"must be an even number of arguments (key/value pairs)");
		ObjectNode n = mapper.createObjectNode();
		for (int i = 0; i < args.length; i += 2) {
			String key = args[i].toString();

			Object val = args[i + 1];
			if (val == null) {
				n.set(key, NullNode.getInstance());
			} else if (val instanceof String) {
				n.put(key, val.toString());
			} else if (val instanceof Integer) {
				n.put(key, (Integer) val);
			} else if (val instanceof Long) {
				n.put(key, (Long) val);
			} else if (val instanceof Double) {
				n.put(key, (Double) val);
			} else if (val instanceof Boolean) {
				n.put(key, (Boolean) val);
			} else if (val instanceof List) {
				List x = new ArrayList<>();
				ArrayNode an = mapper.createArrayNode();

				for (Object item : (List) val) {
					an.add(item.toString());
				}
				n.set(key, an);
			} else {
				throw new IllegalArgumentException("type for param " + key
						+ " not supported: " + val.getClass().getName());
			}

		}
		return n;
	}

	public Observable<JsonNode> execCypher(String cypher, ObjectNode params) {
		ObjectNode response = execRawCypher(cypher, params);
		io.macgyver.neorx.rest.impl.guava.GuavaPreconditions.checkNotNull(response);
		NonStreamingResultImpl r = new NonStreamingResultImpl(response);
		ResultMetaDataImpl md = r.getResultMetaData();
		if (md.getFieldNames().size() == 1) {

			return r.rows().flatMap(
					
					NeoRxFunctions.extractField(md.getFieldNames().get(0)));
		} else {
			return r.rows();
		}

	}

	/**
	 * Convenience method for returning neo4j results as a List<JsonNode>.
	 * Same as: client.execCypher(s,params).toList().toBlocking().first()
	 * @param cypher
	 * @param params
	 * @return List of JsonNode
	 */
	public List<JsonNode> execCypherAsList(String cypher, Object... params) {
		return execCypherAsList(cypher, createParameters(params));
	}
	/**
	 * Convenience method for returning neo4j results as a List<JsonNode>.
	 * Same as: client.execCypher(s,params).toList().toBlocking().first()
	 * @param cypher
	 * @param params
	 * @return List of JsonNode
	 */
	public List<JsonNode> execCypherAsList(String cypher, ObjectNode n) {
		return execCypher(cypher, n).toList().toBlocking().first();
	}
	public Observable<JsonNode> execCypher(String cypher, Object... params) {
		return execCypher(cypher, createParameters(params));
	}

	protected ObjectNode execRawCypher(String cypher, Object... args) {
		return execRawCypher(cypher, createParameters(args));
	}

	protected ObjectNode formatPayload(String cypher, ObjectNode params) {
		io.macgyver.neorx.rest.impl.guava.GuavaPreconditions.checkNotNull(cypher);
		ObjectNode payload = mapper.createObjectNode();
		if (params == null) {
			params = mapper.createObjectNode();
		}

		ArrayNode statements = mapper.createArrayNode();

		ObjectNode statement = mapper.createObjectNode();

		statement.put("statement", cypher);
		statement.set("parameters", params);
		statements.add(statement);
		payload.set("statements", statements);
		return payload;
	}

	private String newRequestId() {
		return UUID.randomUUID().toString();
	}
	protected ObjectNode execRawCypher(String cypher, ObjectNode params) {

		try {

			ObjectNode payload = formatPayload(cypher, params);

			String payloadString = payload.toString();
			OkHttpClient c = getClient();
			checkNotNull(c);
			String requestId = newRequestId();
			if (logger.isLoggable(REQUEST_RESPONSE_LEVEL)) {
				logger.log(REQUEST_RESPONSE_LEVEL, String.format("request[%s]: %s",requestId,payloadString));
			}
			Builder builder = new Request.Builder()
					.addHeader("X-Stream", Boolean.toString(streamResponse))
					.addHeader("Accept", "application/json")
					.url(getUrl() + "/db/data/transaction/commit")
					.post(RequestBody.create(
							MediaType.parse("application/json"), payloadString));

			
			if (!GuavaStrings.isNullOrEmpty(username)
					&& !GuavaStrings.isNullOrEmpty(password)) {
				builder = builder.addHeader("Authorization",
						Credentials.basic(username, password));
			}

			com.squareup.okhttp.Response r = c.newCall(builder.build())
					.execute();

			ObjectNode jsonResponse = (ObjectNode) mapper.readTree(r.body()
					.charStream());
			if (logger.isLoggable(REQUEST_RESPONSE_LEVEL)) {
				logger.log(REQUEST_RESPONSE_LEVEL,String.format("response[%s]: %s",requestId,jsonResponse.toString()));
			}
			ObjectNode n = jsonResponse;
			JsonNode error = n.path("errors").path(0);

			if (error instanceof ObjectNode) {

				throw new NeoRxException(((ObjectNode) error));
			}

			return n;

		} catch (IOException e) {
			throw new NeoRxException(e);
		}
	}

	public boolean isCertificateVerificationEnabled() {
		return validateCertificates;
	}



	public String getUrl() {
		return url;
	}





	public boolean checkConnection() {
		try {

			Response r = getClient().newCall(
					new Request.Builder().url(getUrl()).build()).execute();

			if (r.isSuccessful()) {
				r.body().close();
				return true;
			}

		} catch (IOException | RuntimeException e) {
			logger.log(java.util.logging.Level.WARNING,e.toString());
		}
		return false;
	}
}
