package io.macgyver.neorx.rest.impl;

import io.macgyver.neorx.rest.NeoRxException;
import io.macgyver.neorx.rest.ResultMetaData;
import io.macgyver.neorx.rest.impl.guava.GuavaPreconditions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;


public class ResultMetaDataImpl implements ResultMetaData {

	Map<String, Integer> columnMap;
	List<String> columnNames;

	public ResultMetaDataImpl(JsonNode response) {

		GuavaPreconditions.checkNotNull(response);
		ArrayNode an = (ArrayNode) response.get("columns");

		GuavaPreconditions.checkArgument(an!=null,"response must have columns element");
		Iterator<JsonNode> t = an.elements();
		int column = 0;
		Map<String, Integer> map = new ConcurrentHashMap<>();
		List<String> list = new ArrayList<>();
		while (t.hasNext()) {

			String name = t.next().asText();
			map.put(name, column);
			list.add(name);

			column++;
		}
		
		this.columnNames = Collections.unmodifiableList(list);
	
		this.columnMap = Collections.unmodifiableMap(map);

	}

	@Override
	public List<String> getFieldNames() {
		return columnNames;
	}

	@Override
	public int getField(String input) {
		Integer c = columnMap.get(input);
		if (c == null) {

			throw new NeoRxException("no such field: " + input + " ( "
					+ columnNames +" )");
		}
		return c;
	}

}
