package io.macgyver.neorx.impl;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import io.macgyver.neorx.ResultMetaData;

public class ResultMetaDataImpl implements ResultMetaData {

	Map<String,Integer> columnMap;
	List<String> columnNames;
	ObjectNode response;
	public ResultMetaDataImpl(ObjectNode response) {
		this.response = response;
	
		ArrayNode an = (ArrayNode) response.get("columns");
		
	
		Iterator<JsonNode> t = an.elements();
		int column=0;
		Map<String,Integer> map = Maps.newConcurrentMap();
		List<String> list = Lists.newArrayList();
		while (t.hasNext()) {
		
			String name =t.next().asText();
			map.put(name,column);
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
		if (c==null) {
			
			throw new IllegalArgumentException("no such field: "+input+" ("+columnMap+")");
		}
		return c;
	}

}
