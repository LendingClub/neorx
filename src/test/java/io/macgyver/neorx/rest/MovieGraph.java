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

	}

	public MovieGraph(NeoRxClient c) {
		com.google.common.base.Preconditions.checkNotNull(c);
		this.client = c;
	}

	public void replaceMovieGraph() {

		try {
			client.exec("MATCH (n:Person)-[r]-() delete r");

			client.exec("MATCH (n:Movie)-[r]-() delete r");

			client.exec("MATCH (p:Person) delete p");

			client.exec("MATCH (m:Movie) delete m");

			executeClasspath("movies.cypher");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void executeClasspath(String name) throws IOException {

		URL url = Resources.getResource(name);
		String val = Resources.toString(url, Charsets.UTF_8);

		client.exec(val);

	}

}
