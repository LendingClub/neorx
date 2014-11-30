package io.macgyver.neorx.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.List;

import org.assertj.core.util.Lists;
import org.assertj.core.util.Preconditions;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

import rx.Observable;
import rx.functions.Action1;

public class MovieGraph {

	NeoRxClient client;



	public static void main(String[] args) throws IOException {

		NeoRxClient c = new NeoRxClient();

		MovieGraph mg = new MovieGraph(c);
	
		mg.replaceMovieGraph();

		c.close();
	}

	public MovieGraph(NeoRxClient c) {
		com.google.common.base.Preconditions.checkNotNull(c);
		this.client = c;
	}
	
	public void replaceMovieGraph() throws IOException {

		client.execCypher("MATCH (n:Person)-[r]-() delete r");

		client.execCypher("MATCH (n:Movie)-[r]-() delete r");

		client.execCypher("MATCH (p:Person) delete p");

		client.execCypher("MATCH (m:Movie) delete m");

		executeClasspath("movies.cypher");
	}

	public void executeClasspath(String name) throws IOException {

		URL url = Resources.getResource(name);
		String val = Resources.toString(url, Charsets.UTF_8);

		client.execCypher(val);

	}



}
