package io.macgyver.neorx.rest.impl;

import io.macgyver.neorx.rest.ResultMetaData;
import io.macgyver.neorx.rest.Row;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Preconditions;

public class RowImpl implements Row {

	ResultMetaDataImpl metaData;
	ArrayNode rowData;
	RowImpl(ResultMetaDataImpl md, JsonNode n) {
		Preconditions.checkNotNull(md);
		Preconditions.checkNotNull(n);
		
		this.metaData = md;
		this.rowData = (ArrayNode) n.get("row");
		
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
