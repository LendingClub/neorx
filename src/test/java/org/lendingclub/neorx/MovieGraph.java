package org.lendingclub.neorx;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;

import org.lendingclub.neorx.NeoRxClient;

import com.google.common.base.Preconditions;

public class MovieGraph {

	NeoRxClient client;



	public MovieGraph(NeoRxClient c) {
		Preconditions.checkNotNull(c);
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