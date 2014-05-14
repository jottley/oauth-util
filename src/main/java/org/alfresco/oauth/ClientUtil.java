/**
 * 
 */

package org.alfresco.oauth;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.RewriteHandler;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.social.alfresco.api.Alfresco;
import org.springframework.social.alfresco.connect.AlfrescoConnectionFactory;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.alfresco.oauth.AuthUrl;
import org.alfresco.oauth.CodeUrl;

/**
 * @author jottley
 * 
 */
public class ClientUtil {
	private static final String CONSUMER_KEY = "l7xx38d2dda7519148a8928aafb13c8226c8";
	private static final String CONSUMER_SECRET = "672d8739116c493f8eae32123c9c3035";

	private static final String REDIRECT_URI = "http://localhost:9876";
	private static final String STATE = "test";

	private static String USERNAME = "";
	private static String PASSWORD = "";

	private static Server server;
	private static AlfrescoConnectionFactory connectionFactory;
	private static AuthUrl authUrlObject;
	private static String accessToken;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length == 2) {

			USERNAME = args[0];
			PASSWORD = args[1];

			ClientUtil client = new ClientUtil();
			try {
				client.setupServer();
				client.authenticate();
				accessToken = client.GetAccessToken(USERNAME, PASSWORD);

				System.out.println("Access Token: " + accessToken);
			}

			catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					server.stop();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} else {
			throw new RuntimeException("Missing Credentials");
		}

	}

	protected void setupServer() throws Exception {
		server = new Server(9876);
		server.setHandler(new RewriteHandler());
		server.start();
	}

	protected void authenticate() throws MalformedURLException {
		connectionFactory = new AlfrescoConnectionFactory(CONSUMER_KEY,
				CONSUMER_SECRET);

		OAuth2Parameters parameters = new OAuth2Parameters();
		parameters.setRedirectUri(REDIRECT_URI);
		parameters.setScope(Alfresco.DEFAULT_SCOPE);
		parameters.setState(STATE);

		authUrlObject = new AuthUrl(connectionFactory.getOAuthOperations()
				.buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE, parameters));
		System.out.println(authUrlObject);
	}

	protected String GetAccessToken(String username, String password)
			throws IOException {
		HtmlUnitDriver driver = new HtmlUnitDriver();
		driver.get(authUrlObject.toString());

		List<WebElement> webElements = driver.findElementsByTagName("form");

		WebElement usernameElement = driver.findElementById("username");
		usernameElement.sendKeys(username);
		WebElement passwordElement = driver.findElementById("password");
		passwordElement.sendKeys(password);
		webElements.get(0).submit();

		CodeUrl codeUrl = new CodeUrl(driver.getCurrentUrl());

		return connectionFactory
				.getOAuthOperations()
				.exchangeForAccess(codeUrl.getQueryMap().get(CodeUrl.CODE),
						REDIRECT_URI, null).getAccessToken();
	}

}
