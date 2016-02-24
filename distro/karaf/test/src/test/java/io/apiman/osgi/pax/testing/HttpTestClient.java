package io.apiman.osgi.pax.testing;

import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.conn.ssl.X509HostnameVerifier;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HttpTestClient {

	private static final Logger LOG = LoggerFactory.getLogger(HttpTestClient.class);

	protected CloseableHttpClient httpclient;

	private HttpClientContext context = HttpClientContext.create();

	private String user;

	private String password;

	private String keyStore;

	public HttpTestClient() throws Exception {
		this("admin", "admin123!", "keystore.jks");
	}

	public HttpTestClient(String user, String password, String keyStore)
			throws Exception {
		this.user = user;
		this.password = password;
		this.keyStore = keyStore;

		httpclient = (CloseableHttpClient) createHttpClient();
	}

	private CloseableHttpClient createHttpClient()
			throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException,
			KeyManagementException, UnrecoverableKeyException {
		HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;

		KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
		//FileInputStream instream = new FileInputStream(new File(keyStore));
		try {
			//trustStore.load(instream, "password".toCharArray());
			trustStore.load(this.getClass().getResourceAsStream("/keystore.jks"),"apiman".toCharArray());
			LOG.info(">> Trustore loaded.");
			Key key = trustStore.getKey("server-alias","apiman".toCharArray());
			LOG.info("Key Format : " + key.getFormat() + ", algorythm : " + key.getAlgorithm() );
		} finally {
/*			try {
				instream.close();
			} catch (Exception ignore) {
			}*/
		}

		PlainConnectionSocketFactory plainsf = PlainConnectionSocketFactory
				.getSocketFactory();

		SSLContext sslContext = SSLContexts.custom().useTLS()
				.loadTrustMaterial(trustStore).build();
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				sslContext, (X509HostnameVerifier) hostnameVerifier);

		Registry<ConnectionSocketFactory> rb = RegistryBuilder
				.<ConnectionSocketFactory> create().register("http", plainsf)
				.register("https", sslsf).build();

		PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(rb);

		return HttpClients.custom().setConnectionManager(cm).build();

	}

	public void close() throws IOException {
		httpclient.close();
	}

	HttpHost getHttpHost(String path) {
		int schemeSeperator = path.indexOf(":");
		String scheme = path.substring(0, schemeSeperator);

		int portSeperator = path.lastIndexOf(":");
		String hostname = path.substring(schemeSeperator + 3, portSeperator);

		int port = Integer.parseInt(path.substring(portSeperator + 1,
				portSeperator + 5));

		HttpHost targetHost = new HttpHost(hostname, port, scheme);
		return targetHost;
	}

	public String testWebPath(String path, String expectedContent)
			throws Exception {
		return testWebPath(path, expectedContent, 200, false);
	}

	public String testWebPath(String path, int httpRC) throws Exception {
		return testWebPath(path, null, httpRC, false);
	}

	public String testWebPath(String path, String expectedContent, int httpRC,
			boolean authenticate) throws Exception {
		return testWebPath(path, expectedContent, httpRC, authenticate, null);
	}

	public String testWebPath(String path, String expectedContent, int httpRC,
			boolean authenticate, BasicHttpContext basicHttpContext)
			throws Exception {

		int count = 0;
		while (!checkServer(path) && count++ < 5) {
			if (count > 5) {
				break;
			}
		}

		HttpResponse response = null;
		response = getHttpResponse(path, authenticate, basicHttpContext);

		assertEquals("HttpResponseCode", httpRC, response.getStatusLine()
				.getStatusCode());

		if (response.getStatusLine().getStatusCode() == 403) {
			EntityUtils.consumeQuietly(response.getEntity());
			return null;
		} 
		
		String responseBodyAsString = EntityUtils
				.toString(response.getEntity());
		if (expectedContent != null) {
			assertTrue("Content: " + responseBodyAsString,
					responseBodyAsString.contains(expectedContent));
		}

		return responseBodyAsString;
	}

	public void testPost(String path, List<NameValuePair> nameValuePairs,
			String expectedContent, int httpRC) throws IOException {

		HttpPost post = new HttpPost(path);
		post.setEntity(new UrlEncodedFormEntity(
				(List<NameValuePair>) nameValuePairs));
		post.addHeader("Accept-Language", "en-us;q=0.8,en;q=0.5");

		CloseableHttpResponse response = httpclient.execute(post, context);
		assertEquals("HttpResponseCode", httpRC, response.getStatusLine()
				.getStatusCode());

		String responseBodyAsString = EntityUtils
				.toString(response.getEntity());
		if (expectedContent != null) {
			assertTrue("Content: " + responseBodyAsString,
					responseBodyAsString.contains(expectedContent));
		}
		response.close();
	}

	public CloseableHttpResponse getHttpResponse(String path,
			boolean authenticate, BasicHttpContext basicHttpContext)
			throws IOException, KeyManagementException, UnrecoverableKeyException, NoSuchAlgorithmException,
			KeyStoreException, CertificateException, AuthenticationException {
		HttpGet httpget = null;

		HttpHost targetHost = getHttpHost(path);

		BasicHttpContext localcontext = basicHttpContext == null ? new BasicHttpContext()
				: basicHttpContext;

		httpget = new HttpGet(path);
		httpget.addHeader("Accept-Language", "en");
		LOG.info("calling remote {} ...", path);
		CloseableHttpResponse response = null;
		if (!authenticate && basicHttpContext == null) {
			if (localcontext.getAttribute(ClientContext.AUTH_CACHE) != null) {
				localcontext.removeAttribute(ClientContext.AUTH_CACHE);
			}
			response = httpclient.execute(httpget, context);
		} else {
			UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
					user, password);

			// Create AuthCache instance
			AuthCache authCache = new BasicAuthCache();
			// Generate BASIC scheme object and add it to the local auth cache
			BasicScheme basicAuth = new BasicScheme();
			authCache.put(targetHost, basicAuth);

			localcontext.setAttribute(ClientContext.AUTH_CACHE, authCache);
			httpget.addHeader(basicAuth.authenticate(creds, httpget,
					localcontext));
			httpget.addHeader("Accept-Language", "en-us;q=0.8,en;q=0.5");
			response = httpclient.execute(targetHost, httpget, localcontext);
		}

		LOG.info("... responded with: {}", response.getStatusLine()
				.getStatusCode());
		return response;
	}

	public boolean checkServer(String path) throws Exception {
		LOG.info("checking server path {}", path);
		HttpGet httpget = null;
		
		CloseableHttpClient myHttpClient = createHttpClient();
		
		HttpHost targetHost = getHttpHost(path);

		httpget = new HttpGet("/");
		httpget.addHeader("Accept-Language", "en-us;q=0.8,en;q=0.5");
		LOG.info(
				"calling remote {}://{}:{}/ ...",
				new Object[] { targetHost.getSchemeName(),
						targetHost.getHostName(), targetHost.getPort() });
		HttpResponse response = null;
		try {
			response = myHttpClient.execute(targetHost, httpget);
		} catch (IOException ioe) {
			LOG.info("... caught IOException");
			return false;
		} finally {
			myHttpClient.close();
		}
		int statusCode = response.getStatusLine().getStatusCode();
		LOG.info("... responded with: {}", statusCode);
		return statusCode == 404 || statusCode == 200;
	}

}
