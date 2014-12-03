package io.macgyver.neorx.rest;

import rx.Observable;
import rx.functions.Func1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.NullNode;

public class NeoRxFunctions {

	public static Func1<JsonNode, Observable<JsonNode>> extractField(
			final String prop) {
		return new Func1<JsonNode, Observable<JsonNode>>() {
			@Override
			public Observable<JsonNode> call(JsonNode t1) {
				JsonNode n = (JsonNode) t1.get(prop);
				if (n==null) {
					n = NullNode.getInstance();
				}
				return Observable.just(n);
			}
		};

	}
}
