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

import static io.macgyver.neorx.rest.impl.GuavaPreconditions.checkArgument;
import static io.macgyver.neorx.rest.impl.GuavaPreconditions.checkNotNull;
import io.macgyver.neorx.rest.impl.GuavaStrings;
import io.macgyver.neorx.rest.impl.NonStreamingResultImpl;
import io.macgyver.neorx.rest.impl.ResultMetaDataImpl;
import io.macgyver.neorx.rest.impl.SslTrust;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

	public static final boolean CERTIFICATE_VALIDATION_DEFAULT = true;
	public static final String DEFAULT_URL = "http://localhost:7474";
	protected String url = DEFAULT_URL;
	protected String username = null;
	protected String password = null;
	protected boolean validateCertificates = CERTIFICATE_VALIDATION_DEFAULT;
	private boolean streamResponse = true;
	final static ObjectMapper mapper = new ObjectMapper();
	private volatile OkHttpClient httpClient = null;
	boolean includeStats=true;
	
	Logger logger = LoggerFactory.getLogger(NeoRxClient.class);

	private final Level REQUEST_RESPONSE_LEVEL = Level.FINE; // level at which
																// request/response
																// logging will
																// be logged

	protected NeoRxClient(NeoRxClientBuilder builder) {
		
		httpClient = new OkHttpClient();
	}
	
	
	/**
	 * @deprecated use NeoRxClientBuilder instead
	 */
	@Deprecated
	public NeoRxClient() {
		this(DEFAULT_URL);
	}

	/**
	 * @deprecated use NeoRxClientBuilder instead
	 */
	@Deprecated
	public NeoRxClient(String url) {
		this(url, null, null, CERTIFICATE_VALIDATION_DEFAULT);
	}
	/**
	 * @deprecated use NeoRxClientBuilder instead
	 */
	@Deprecated
	public NeoRxClient(String url, boolean validateCertificates) {
		this(url, null, null, validateCertificates);
	}

	/**
	 * @deprecated use NeoRxClientBuilder instead
	 */
	@Deprecated
	public NeoRxClient(String url, String username, String password) {
		this(url, username, password, CERTIFICATE_VALIDATION_DEFAULT);
	}

	@Deprecated
	public NeoRxClient(String url, String username, String password,
			boolean validateCertificates) {

		logger.warn("NeoRxClient constructors are deprecated.  Please use https://github.com/if6was9/neorx/blob/master/src/main/java/io/macgyver/neorx/rest/NeoRxClientBuilder.java");
		while (url.endsWith("/")) {
			url = url.substring(0,url.length()-1);
		}
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

				ArrayNode an = mapper.createArrayNode();

				for (Object item : (List) val) {
					an.add(item.toString());
				}
				n.set(key, an);
			} 
			else if (val instanceof Map) {
				n.set(key, mapToObjectNode((Map) val));
			}
			else if (val instanceof ObjectNode) {
				n.set(key, (ObjectNode) val);
			}
			else if (val instanceof ArrayNode) {
				n.set(key, (ArrayNode)val);
			}
			else if (val instanceof JsonNode) {
				JsonNode x = (JsonNode) val;
				if (x.isValueNode()) {
					n.set(key, x);
				}
				else {
					throw new IllegalArgumentException("parameter '" + key
							+ "' type not supported: " + val.getClass().getName());
				}
			}
			else {
				throw new IllegalArgumentException("parameter '" + key
						+ "' type not supported: " + val.getClass().getName());
			}

		}
		return n;
	}

	public Observable<JsonNode> execCypher(String cypher, ObjectNode params) {
		ObjectNode response = execRawCypher(cypher, params);
		io.macgyver.neorx.rest.impl.GuavaPreconditions.checkNotNull(response);
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
	 * Convenience method for returning neo4j results as a List<JsonNode>. Same
	 * as: client.execCypher(s,params).toList().toBlocking().first()
	 * 
	 * @param cypher
	 * @param params
	 * @return List of JsonNode
	 */
	public List<JsonNode> execCypherAsList(String cypher, Object... params) {
		return execCypherAsList(cypher, createParameters(params));
	}

	/**
	 * Convenience method for returning neo4j results as a List<JsonNode>. Same
	 * as: client.execCypher(s,params).toList().toBlocking().first()
	 * 
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
		io.macgyver.neorx.rest.impl.GuavaPreconditions.checkNotNull(cypher);
		ObjectNode payload = mapper.createObjectNode();
		if (params == null) {
			params = mapper.createObjectNode();
		}

		ArrayNode statements = mapper.createArrayNode();

		ObjectNode statement = mapper.createObjectNode();

		statement.put("statement", cypher);
		statement.set("parameters", params);
		statement.put("includeStats", includeStats);
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
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("request[%s]:\n%s", requestId,
						mapper.writerWithDefaultPrettyPrinter().writeValueAsString(payload)));
			}
			Builder builder = injectCredentials(new Request.Builder())
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
			
			
			if (logger.isDebugEnabled()) {
				logger.debug(String.format("response[%s]:\n%s", requestId,
						mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonResponse)));
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

	public boolean isCeritificateValidationEnabled() {
		return validateCertificates;
	}

	public String getUrl() {
		return url;
	}

	protected Request.Builder injectCredentials(Request.Builder builder) {
		if (!GuavaStrings.isNullOrEmpty(username)
				&& !GuavaStrings.isNullOrEmpty(password)) {
			return builder.addHeader("Authorization",
					Credentials.basic(username, password));
		} else {
			return builder;
		}
	}

	public boolean checkConnection() {
		try {

	
			Response r = getClient().newCall(
					injectCredentials(new Request.Builder()).url(
							getUrl() + "/db/data/").build()).execute();

			
			if (r.isSuccessful()) {

				r.body().close();
				return true;
			}

		} catch (IOException | RuntimeException e) {

			logger.warn(e.toString());

		}
		return false;
	}
	
	private ObjectNode mapToObjectNode(Map m) {
		return mapper.valueToTree(m);
		
	}
}
