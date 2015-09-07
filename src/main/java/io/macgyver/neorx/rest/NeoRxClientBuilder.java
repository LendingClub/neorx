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

	String url = "http://localhost:7474";
	String username;
	String password;
	boolean validateCertificates = DEFAULT_CERT_VALIDATION;

	Action1<OkHttpClient> configAction = null;

	Logger logger = LoggerFactory.getLogger(NeoRxClientBuilder.class);

	public NeoRxClientBuilder() {

	}

	public NeoRxClientBuilder withUrl(String url) {
		this.url = url;
		return this;
	}

	public NeoRxClientBuilder withCredentials(String username, String password) {
		this.username = username;
		this.password = password;
		return this;
	}

	public NeoRxClientBuilder withCertificateValidation() {
		this.validateCertificates = true;
		return this;
	}

	public void withClientConfig(Action1<OkHttpClient> c) {
		this.configAction = c;
	}

	public NeoRxClient build() {
		NeoRxClient c = new NeoRxClient(this);
		c.username = username;
		c.password = password;
		c.validateCertificates = validateCertificates;
		c.url = url;
		OkHttpClient ok = c.getClient();

		ok.setConnectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS,
				TimeUnit.MILLISECONDS);
		ok.setReadTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		ok.setWriteTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);

		
		if (!validateCertificates) {
			ok.setHostnameVerifier(SslTrust.withoutHostnameVerification());
			ok.setSslSocketFactory(SslTrust.withoutCertificateValidation()
					.getSocketFactory());

		}
		
		if (configAction != null) {
			configAction.call(c.getClient());
		}

		logger.info("url: {}", url);
		logger.info("connectTimeout: {}ms", ok.getConnectTimeout());
		logger.info("readTimeout: {}ms", ok.getReadTimeout());
		logger.info("writeTimeout: {}ms", ok.getWriteTimeout());

		return c;
	}
}
