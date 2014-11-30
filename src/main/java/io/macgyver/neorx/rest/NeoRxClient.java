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

import io.macgyver.neorx.rest.impl.NonStreamingResultImpl;
import io.macgyver.neorx.rest.impl.SslTrust;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.Observable;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClient.BoundRequestBuilder;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.ListenableFuture;
import com.ning.http.client.Response;

public class NeoRxClient {

	private Logger logger = LoggerFactory.getLogger(NeoRxClient.class);

	public static final String DEFAULT_URL = "http://localhost:7474";
	private String url = DEFAULT_URL;
	private String username = null;
	private String password = null;
	private boolean validateCertificates = false;
	private boolean streamResponse = true;
	final ObjectMapper mapper = new ObjectMapper();
	private volatile AsyncHttpClient asyncClient = null;

	public NeoRxClient() {
		this(DEFAULT_URL);
	}

	public NeoRxClient(String url) {
		this(url, null, null, false);
	}

	public NeoRxClient(String url, String username, String password) {
		this(url, username, password, false);
	}

	public NeoRxClient(String url, String username, String password,
			AsyncHttpClient client) {
		Preconditions.checkNotNull(url);
		Preconditions.checkNotNull(client);
		this.url = url;
		this.username = username;
		this.password = password;
		this.asyncClient = client;
	}

	public NeoRxClient(String url, String username, String password,
			boolean validateCertificates) {

		this(url, username, password, newClient(validateCertificates));

	}

	private static AsyncHttpClient newClient(boolean validateCertificates) {
		AsyncHttpClientConfig.Builder builder = new AsyncHttpClientConfig.Builder();
		if (!validateCertificates) {
			builder = builder
					.setSSLContext(SslTrust.withoutCertificateValidation())
					.setHostnameVerifier(SslTrust.withoutHostnameVerification());
		}
		builder = builder.setConnectionTimeoutInMs(5000);
		AsyncHttpClient asyncClient = new AsyncHttpClient(builder.build());
		
		return asyncClient;
	}

	public void setAsyncHttpClient(AsyncHttpClient client) {
		Preconditions.checkNotNull(client);
		if (asyncClient != null && !asyncClient.isClosed()) {
			asyncClient.close();
		}
		asyncClient = client;
	}

	protected AsyncHttpClient getClient() {

		return asyncClient;
	}

	public ObjectNode createParameters(Object... args) {
		Preconditions.checkNotNull(args);
		Preconditions.checkArgument(args.length % 2 == 0,
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
				List x = Lists.newArrayList();
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

	public Observable<Row> execCypher(String cypher, ObjectNode params) {
		ObjectNode response = execCypherWithJsonResponse(cypher, params);
		Preconditions.checkNotNull(response);
		return new NonStreamingResultImpl(response).rows();

	}

	public Observable<Row> execCypher(String cypher, Object... params) {
		return execCypher(cypher, createParameters(params));
	}

	protected ObjectNode execCypherWithJsonResponse(String cypher,
			Object... args) {
		return execCypherWithJsonResponse(cypher, createParameters(args));
	}

	protected ObjectNode formatPayload(String cypher, ObjectNode params) {
		Preconditions.checkNotNull(cypher);
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

	protected ObjectNode execCypherWithJsonResponse(String cypher,
			ObjectNode params) {

		try {

			ObjectNode payload = formatPayload(cypher, params);

			AsyncCompletionHandler<ObjectNode> ch = new AsyncCompletionHandler<ObjectNode>() {

				@Override
				public void onThrowable(Throwable t) {
					logger.warn("onThrowable", t);
				}

				@Override
				public ObjectNode onCompleted(Response response)
						throws Exception {

					if (response.getStatusCode() >= 300) {
						if (response.getContentType().contains(
								"application/json")) {
							ObjectNode n = (ObjectNode) mapper
									.readTree(response
											.getResponseBodyAsStream());

							throw new NeoRxException(n);
						} else {
							throw new NeoRxException("status code: "
									+ response.getStatusCode());
						}

					}

					return (ObjectNode) mapper.readTree(response
							.getResponseBodyAsStream());

				}

			};

			String payloadString = payload.toString();
			AsyncHttpClient c = getClient();
			Preconditions.checkNotNull(c);
			BoundRequestBuilder brb = c
					.preparePost(getUrl() + "/db/data/transaction/commit")
					.addHeader("X-Stream", Boolean.toString(streamResponse))
					.addHeader("Accept", "application/json");

			if (!Strings.isNullOrEmpty(username)
					&& !Strings.isNullOrEmpty(password)) {
				brb.addHeader(
						"Authorization",
						"Basic "
								+ BaseEncoding
										.base64()
										.encode((username + ":" + password)
												.getBytes(Charset
														.forName("UTF8")))
										.toString());
			}

			// There is nothing async about this processing. We just block here
			// for now and process synchronously.
			ListenableFuture<ObjectNode> f = brb.setBody(payloadString)
					.execute(ch);
			ObjectNode n = f.get();
			JsonNode error = n.path("errors").path(0);
			
		
			if (error instanceof ObjectNode) {
		
				throw new NeoRxException(((ObjectNode)error));
			}
			
			return n;

		} catch (IOException e) {
			throw new NeoRxException(e);
		} catch (InterruptedException e) {
			throw new NeoRxException(e);
		} catch (ExecutionException e) {
			throw new NeoRxException(e);
		}
	}

	public boolean isCertificateVerificationEnabled() {
		return validateCertificates;
	}

	public void close() {
		getClient().close();
	}
	public void setCertificateValidationEnabled(boolean validateCertificates) {
		this.validateCertificates = validateCertificates;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean checkConnection() {
		try {
			ListenableFuture<Response> x = getClient().prepareGet(getUrl()).execute();
			Response r = x.get(10, TimeUnit.SECONDS);
			return r.getStatusCode()==200 && r.getContentType().contains("application/json");
		} catch (Exception e) {
			logger.warn(e.toString());
		}
		return false;
	}
}
