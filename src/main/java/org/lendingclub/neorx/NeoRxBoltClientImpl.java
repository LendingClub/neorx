package org.lendingclub.neorx;

import static org.neo4j.driver.v1.Values.parameters;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import io.reactivex.ObservableEmitter;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.exceptions.ClientException;
import org.neo4j.driver.v1.types.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.ValueNode;

import io.reactivex.Observable;

class NeoRxBoltClientImpl extends NeoRxClient {

	private final Driver driver;
	private static final Logger logger = LoggerFactory.getLogger(NeoRxBoltClientImpl.class);
	private static final ObjectMapper mapper = new ObjectMapper();

	protected NeoRxBoltClientImpl(Driver driver) {
		this.driver = driver;
	}

	@SuppressWarnings("unchecked")
	static JsonNode convertResultToJson(Object obj) {
		if (obj == null) {
			return NullNode.instance;
		} else if (obj instanceof JsonNode) {
			// this should not happen, but passing it through is fine
			return (JsonNode) obj;
		} else if (obj instanceof List) {
			List<Object> x = (List<Object>) obj;
			ArrayNode n = mapper.createArrayNode();
			x.forEach(it -> n.add(convertResultToJson(it)));
			return n;
		} else if (obj instanceof Node) {
			ObjectNode x = mapper.createObjectNode();
			Node node = (Node) obj;
			Map<String,Object> m = node.asMap();
			m.forEach((k, v) -> x.set(k, convertResultToJson(v)));
			return x;
		} else if (obj instanceof Record) {

			Record record = (Record) obj;

			Map<String, Object> m = record.asMap();

			if (m.size() == 1) {
				// If we have one element in the record, then "unwrap" it
				return convertResultToJson(m.values().iterator().next());
			} else {
				ObjectNode n = mapper.createObjectNode();
				m.forEach((key, value) -> n.set(key, convertResultToJson(value)));
				return n;
			}
		} else {
			return mapper.convertValue(obj, JsonNode.class);
		}
	}

	private static void emitEvents(StatementResult sr, ObservableEmitter<JsonNode> emitter) {
		try {
			while (sr.hasNext()) {
				Record record = sr.next();
				emitter.onNext(convertResultToJson(record));
			}
		} catch (ClientException e) {
			emitter.onError(e);
		}
	}

	static Object convertParameterValueType(Object input) {
		Object rval = input;
		if (input == null) {
			rval = null; // unnecessary, but clear
		} else if (input instanceof String) {
			rval = input;
		} else if (input instanceof ObjectNode) {
			rval = mapper.convertValue(input, Map.class);
		} else if (input instanceof ArrayNode) {
			rval = mapper.convertValue(input, List.class);
		} else if (input instanceof MissingNode) {
			rval = null;
		} else if (input instanceof NullNode) {
			rval = null;
		} else if (input instanceof ValueNode) {
			ValueNode vn = ValueNode.class.cast(input);

			if (vn.isTextual()) {
				rval = TextNode.class.cast(input).asText();
			} else if (vn.isLong()) {
				rval = vn.asLong();
			} else if (vn.isInt()) {
				rval = vn.asInt();
			} else if (vn.isBoolean()) {
				rval = vn.booleanValue();
			} else if (vn.isDouble()) {
				rval = vn.asDouble();
			}
		}

		if (logger.isDebugEnabled()) {
			logger.debug("convert <<{}>> ({}) to <<{}>> ({})", input, input == null ? "null" : input.getClass(), rval,
					rval == null ? "null" : rval.getClass());
		}
		return rval;
	}

	private static Object[] convertExtraTypes(Object... keysAndValues) {
		if (keysAndValues.length % 2 != 0) {
			// we throw the same client exception as the underlying Driver
			// would, but wrap it for consistency with
			// NeoRx exception hierarchy.

			throw new NeoRxException(
					new ClientException("Parameters function requires an even number " + "of arguments, "
							+ "alternating key and value. Arguments were: " + Arrays.toString(keysAndValues) + "."));
		}
		for (int i = 0; i < keysAndValues.length; i += 2) {
			keysAndValues[i + 1] = convertParameterValueType(keysAndValues[i + 1]);
		}
		return keysAndValues;
	}

	@Override
	public Observable<JsonNode> execCypher(String cypher, Session session,  Object... args) {
		return _execCypher(cypher, session, true, args);
	}

	private Observable<JsonNode> _execCypher(String cypher, Session session, boolean clientManagesSession,  Object... args) {
		return Observable.create(emitter -> {
			try {
				if (logger.isDebugEnabled()) {
					logger.debug("cypher={}", cypher);
				}
				StatementResult result = session.run(cypher, parameters(convertExtraTypes(args)));
				emitEvents(result, emitter);
			} catch (ClientException e) {
				emitter.onError(e);
			} finally {
				emitter.onComplete();
				if (!clientManagesSession) {
					session.close();
				}
			}
		});
	}

	@Override
	public Observable<JsonNode> execCypher(String cypher, Object... args) {
		return _execCypher(cypher, driver.session(), false, args);
	}

	public Driver getDriver() {
		return driver;
	}

	@Override
	public boolean checkConnection() {
		try {
			execCypher("match (a:Check__Session) return count(a)").blockingFirst();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
