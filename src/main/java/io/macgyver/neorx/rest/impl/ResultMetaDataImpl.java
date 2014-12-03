package io.macgyver.neorx.rest.impl;

import io.macgyver.neorx.rest.NeoRxException;
import io.macgyver.neorx.rest.ResultMetaData;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class ResultMetaDataImpl implements ResultMetaData {

	Map<String, Integer> columnMap;
	List<String> columnNames;

	public ResultMetaDataImpl(JsonNode response) {

		Preconditions.checkNotNull(response);
		ArrayNode an = (ArrayNode) response.get("columns");

		Preconditions.checkArgument(an!=null,"response must have columns element");
		Iterator<JsonNode> t = an.elements();
		int column = 0;
		Map<String, Integer> map = Maps.newConcurrentMap();
		List<String> list = Lists.newArrayList();
		while (t.hasNext()) {

			String name = t.next().asText();
			map.put(name, column);
			list.add(name);

			column++;
		}
		this.columnNames = ImmutableList.copyOf(list);
		this.columnMap = ImmutableMap.copyOf(map);

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
