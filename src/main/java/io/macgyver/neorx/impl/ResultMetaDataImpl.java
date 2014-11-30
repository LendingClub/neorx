package io.macgyver.neorx.impl;

import io.macgyver.neorx.ResultMetaData;

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

	Map<String,Integer> columnMap;
	List<String> columnNames;

	public ResultMetaDataImpl(JsonNode response) {
	
		
		ArrayNode an = (ArrayNode) response.get("columns");
		
		Preconditions.checkNotNull(an);
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
