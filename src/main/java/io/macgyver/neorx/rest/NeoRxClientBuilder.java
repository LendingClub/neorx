package io.macgyver.neorx.rest;

import io.macgyver.neorx.rest.impl.SslTrust;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.squareup.okhttp.OkHttpClient;

import rx.functions.Action1;

public class NeoRxClientBuilder {

	public static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 10 * 1000;
	public static final int DEFAULT_READ_TIMEOUT_MILLIS = 30 * 1000;
	public static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 10 * 1000;
	
	public static final boolean DEFAULT_CERT_VALIDATION = true;

	
	boolean validateCertificates = DEFAULT_CERT_VALIDATION;

	Action1<OkHttpClient> configAction = null;

	Logger logger = LoggerFactory.getLogger(NeoRxClientBuilder.class);

	NeoRxClient neoRxClient = new NeoRxClient(this);
	
	public NeoRxClientBuilder() {
		OkHttpClient ok = neoRxClient.getClient();

		ok.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS,
				TimeUnit.MILLISECONDS);
		ok.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		ok.setWriteTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
	}

	public NeoRxClientBuilder withUrl(String url) {
		return url(url);
	}
	public NeoRxClientBuilder url(String url) {
		assertState();
		this.neoRxClient.url = url;
		return this;
	}

	public NeoRxClientBuilder withCredentials(String username, String password) {
		return credentials(username, password);
	}
	public NeoRxClientBuilder credentials(String username, String password) {
		assertState();
		neoRxClient.username = username;
		neoRxClient.password = password;
		return this;
	}

	public NeoRxClientBuilder withStats(boolean b) {
		assertState();
		neoRxClient.includeStats = b;
		return this;
	}
	
	public NeoRxClientBuilder withCertificateValidation(boolean b) {
		assertState();
		this.validateCertificates = b;
		return this;
	}

	public NeoRxClientBuilder withClientConfig(Action1<OkHttpClient> c) {
		assertState();
		this.configAction = c;
		return this;
	}

	public NeoRxClient build() {
		assertState();
		neoRxClient.validateCertificates = validateCertificates;
		
		OkHttpClient ok = neoRxClient.getClient();


		
		if (!validateCertificates) {
			ok.setHostnameVerifier(SslTrust.withoutHostnameVerification());
			ok.setSslSocketFactory(SslTrust.withoutCertificateValidation()
					.getSocketFactory());

		}
		
		if (configAction != null) {
			configAction.call(ok);
		}

		logger.info("url: {}", neoRxClient.getUrl());
		logger.info("connectTimeout: {}ms", ok.getConnectTimeout());
		logger.info("readTimeout: {}ms", ok.getReadTimeout());
		logger.info("writeTimeout: {}ms", ok.getWriteTimeout());

		NeoRxClient rval =  neoRxClient;
		
		neoRxClient = null; // reset state so that this can only be called once
		
		return rval;
	}
	protected void assertState() {
		if (neoRxClient==null) {
			throw new IllegalStateException("build() already invoked");
		}
	}
}
