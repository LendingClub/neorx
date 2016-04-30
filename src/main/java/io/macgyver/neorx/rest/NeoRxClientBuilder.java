package io.macgyver.neorx.rest;

import io.macgyver.neorx.rest.impl.GuavaStrings;
import io.macgyver.neorx.rest.impl.SslTrust;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import rx.functions.Action1;

public class NeoRxClientBuilder {

	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
	public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30 * 1000;
	public static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 10 * 1000;

	public static final boolean DEFAULT_CERT_VALIDATION = true;

	boolean validateCertificates = DEFAULT_CERT_VALIDATION;

	public static final String DEFAULT_URL = "http://localhost:7474";
	
	List<Action1<OkHttpClient.Builder>> okHttpConfigActionList = new ArrayList<>();
	List<Action1<NeoRxClientBuilder>> neoRxConfigActionList = new ArrayList<>();
	Logger logger = LoggerFactory.getLogger(NeoRxClientBuilder.class);

	OkHttpClient.Builder okHttpClientBuilder = null;

	String url = DEFAULT_URL;
	
	String username = null;
	String password = null;
	boolean includeStats = false;
	
	public NeoRxClientBuilder() {

		okHttpClientBuilder = new OkHttpClient().newBuilder();
		okHttpClientBuilder = okHttpClientBuilder.connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
				.readTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
				.writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
	}



	public NeoRxClientBuilder withUrl(String url) {
		return url(url);
	}

	public NeoRxClientBuilder url(String url) {
		assertState();
		this.url = url;
		return this;
	}

	public NeoRxClientBuilder withCredentials(String username, String password) {
		return credentials(username, password);
	}

	public NeoRxClientBuilder credentials(String username, String password) {
		assertState();
		this.username = username;
		this.password = password;

		return this;
	}

	public NeoRxClientBuilder withStats(boolean b) {
		assertState();
		this.includeStats = b;
		return this;
	}

	public NeoRxClientBuilder withCertificateValidation(boolean b) {
		assertState();
		this.validateCertificates = b;
		return this;
	}

	public NeoRxClientBuilder withNeoRxConfig(Action1<NeoRxClientBuilder> action) {
		assertState();
		this.neoRxConfigActionList.add(action);
		return this;
	}
	public NeoRxClientBuilder withOkHttpClientConfig(Action1<OkHttpClient.Builder> c) {
		assertState();
		this.okHttpConfigActionList.add(c);
		return this;
	}

	public NeoRxClient build() {
		assertState();

		
	
		if (!validateCertificates) {
			okHttpClientBuilder = okHttpClientBuilder.hostnameVerifier(SslTrust.withoutHostnameVerification()).sslSocketFactory(SslTrust.withoutCertificateValidation()
					.getSocketFactory());

		}
		
		okHttpConfigActionList.forEach(it -> {
			it.call(okHttpClientBuilder);
		});
	
		neoRxConfigActionList.forEach(it -> {
			it.call(this);
		});
		
	
		
		
		if (username!=null || password!=null) {
			
	
			final String authHeader = Credentials.basic(username==null ? "" : username, password==null ? "" : password);
			Interceptor x = new Interceptor() {

				@Override
				public Response intercept(Chain chain) throws IOException {
					
					
					return chain.proceed(chain.request().newBuilder().addHeader("Authorization", authHeader).build());
				}
				
			};
			okHttpClientBuilder = okHttpClientBuilder.addInterceptor(x);
		}
			
		OkHttpClient client = okHttpClientBuilder.build();
		
		
		
		
		NeoRxClient rval = new NeoRxClient();
		rval.includeStats = includeStats;
		rval.httpClient.set(client);
		
		while (url.endsWith("/")) {
			url = url.substring(0, url.length()-1);
		}
		rval.url = url;
		
		logger.info("url: {}", rval.url);
		logger.info("connectTimeout: {}ms", client.connectTimeoutMillis());
		logger.info("readTimeout: {}ms", client.readTimeoutMillis());
		logger.info("writeTimeout: {}ms", client.writeTimeoutMillis());

		
		
		return rval;
	}

	protected void assertState() {
		if (okHttpClientBuilder == null) {
			throw new IllegalStateException("build() already invoked");
		}
	}
}
