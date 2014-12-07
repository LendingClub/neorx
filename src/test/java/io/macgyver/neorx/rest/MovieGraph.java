package io.macgyver.neorx.rest;

import io.macgyver.neorx.rest.impl.GuavaPreconditions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

public class MovieGraph {

	NeoRxClient client;

	public static void main(String[] args) throws IOException {

		NeoRxClient c = new NeoRxClient();

		MovieGraph mg = new MovieGraph(c);

		mg.replaceMovieGraph();

	}

	public MovieGraph(NeoRxClient c) {
		GuavaPreconditions.checkNotNull(c);
		this.client = c;
	}

	public void replaceMovieGraph() {

		try {
			client.execCypher("MATCH (n:Person)-[r]-() delete r");

			client.execCypher("MATCH (n:Movie)-[r]-() delete r");

			client.execCypher("MATCH (p:Person) delete p");

			client.execCypher("MATCH (m:Movie) delete m");

			executeClasspath("movies.cypher");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void executeClasspath(String name) throws IOException {

		URL url = Thread.currentThread().getContextClassLoader()
				.getResource(name);

		BufferedReader sr = new BufferedReader(new InputStreamReader(
				url.openStream()));
		String line = null;
		StringWriter sw = new StringWriter();
		while ((line = sr.readLine()) != null) {
			sw.write(line);
			sw.write("\n");
		}
		String val = sw.toString();

		client.execCypher(val);

	}

}
