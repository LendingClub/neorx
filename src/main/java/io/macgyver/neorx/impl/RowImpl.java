package io.macgyver.neorx.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.macgyver.neorx.ResultMetaData;
import io.macgyver.neorx.Row;

public class RowImpl implements Row {

	ResultMetaDataImpl metaData;
	ArrayNode rowData;
	RowImpl(ResultMetaDataImpl md, JsonNode n) {
		this.metaData = md;
		this.rowData = (ArrayNode) n;
		
	}
	@Override
	public ResultMetaData getMetaData() {
		return metaData;
	}

	@Override
	public JsonNode getField(String s) {
		
		return rowData.get(metaData.getField(s));
	}

	@Override
	public String getString(String s) {
		int f = metaData.getField(s);
		return rowData.path(f).asText();
		
		
	}

}
