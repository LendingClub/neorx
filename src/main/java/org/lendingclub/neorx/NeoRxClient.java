package org.lendingclub.neorx;

import java.util.LinkedList;
import java.util.List;

import org.lendingclub.neorx.mock.MockNeoRxClient;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Config;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.exceptions.Neo4jException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.reactivex.Observable;

public abstract class NeoRxClient {

	public abstract boolean checkConnection();

	public abstract Observable<JsonNode> execCypher(String cypher, Object... params);


	public final io.reactivex.Observable<JsonNode> execCypher(String cypher, ObjectNode args) {
		List<Object> list = new LinkedList<>();
		args.fields().forEachRemaining(it -> {
			list.add(it.getKey());
			list.add(NeoRxBoltClientImpl.convertParameterValueType(it.getValue()));
		});

		return execCypher(cypher, list.toArray());
	}
	
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Convenience method for returning neo4j results as a List. Same as:
	 * client.execCypher(s,params).toList().toBlocking().first()
	 * 
	 * @param cypher
	 * @param params
	 * @return List of JsonNode
	 */
	public List<JsonNode> execCypherAsList(String cypher, ObjectNode params) {
		return execCypher(cypher, params).toList().blockingGet();
	}

	public abstract Driver getDriver();

	/**
	 * Convenience method for returning neo4j results as a List. Same as:
	 * client.execCypher(s,params).toList().toBlocking().first()
	 * 
	 * @param cypher
	 * @param params
	 * @return List of JsonNode
	 */
	public List<JsonNode> execCypherAsList(String cypher, Object... params) {
		return execCypher(cypher, params).toList().blockingGet();
	}

	public static class Builder {

		static Logger logger = LoggerFactory.getLogger(Builder.class);

		String url;
		Driver driver;
		Config config = Config.defaultConfig();
		AuthToken authToken = AuthTokens.none();
		boolean mock = false;

		public Builder withDriver(Driver driver) {
			this.driver = driver;
			return this;
		}

		public Builder withUrl(String url) {
			this.url = url;
			return this;
		}

		public Builder withConfig(Config config) {
			this.config = config;
			return this;
		}

		public Builder withCredentials(String username, String password) {
			return withAuthToken(AuthTokens.basic(username, password));
		}

		public Builder withAuthToken(AuthToken authToken) {
			this.authToken = authToken;
			return this;
		}

		public Builder withMockClient(boolean b) {
			this.mock = b;
			return this;
		}

		@SuppressWarnings("unchecked")
		public <T extends NeoRxClient> T build() {

			if (mock == true) {
				logger.warn("MockNeoRxClient is enabled!");
				return (T) new MockNeoRxClient();
			}
			if (driver == null) {
				try {
					if (GuavaStrings.isNullOrEmpty(url)) {
						url = "bolt://localhost:7687";
						logger.warn("url not specified...defaulting to: {}", url);
					}

					this.driver = GraphDatabase.driver(url, authToken, config);
				}
				catch (Neo4jException e) {
					throw new NeoRxException(e);
				}

			}

			return (T) new NeoRxBoltClientImpl(driver);

		}

	}

}
