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


/**
 * @author jottley
 * 
 */
public class ClientUtil
{
    private static final String              CONSUMER_KEY    = "l7xx16247a05ab7b46968625d4dda1f45aeb";
    private static final String              CONSUMER_SECRET = "c7d189f23aaf432a8b7aadd346e35101";

    private static final String              REDIRECT_URI    = "http://localhost:9876";
    private static final String              STATE           = "test";

    private static final String              USERNAME        = "";
    private static final String              PASSWORD        = "";

    private static Server                    server;
    private static AlfrescoConnectionFactory connectionFactory;
    private static AuthUrl                   authUrlObject;
    private static String                    accessToken;


    /**
     * @param args
     */
    public static void main(String[] args)
    {
        ClientUtil client = new ClientUtil();
        try
        {
            client.setupServer();
            client.authenticate();
            accessToken = client.GetAccessToken(USERNAME, PASSWORD);

            System.out.println("Access Token: " + accessToken);
        }

        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        finally
        {
            try
            {
                server.stop();
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }


    }


    protected void setupServer()
        throws Exception
    {
        server = new Server(9876);
        server.setHandler(new RewriteHandler());
        server.start();
    }


    protected void authenticate()
        throws MalformedURLException
    {
        connectionFactory = new AlfrescoConnectionFactory(CONSUMER_KEY, CONSUMER_SECRET);

        OAuth2Parameters parameters = new OAuth2Parameters();
        parameters.setRedirectUri(REDIRECT_URI);
        parameters.setScope(Alfresco.DEFAULT_SCOPE);
        parameters.setState(STATE);

        authUrlObject = new AuthUrl(connectionFactory.getOAuthOperations().buildAuthenticateUrl(GrantType.AUTHORIZATION_CODE, parameters));
        System.out.println(authUrlObject);
    }


    protected String GetAccessToken(String username, String password)
        throws IOException
    {
        HtmlUnitDriver driver = new HtmlUnitDriver();
        driver.get(authUrlObject.toString());

        List<WebElement> webElements = driver.findElementsByTagName("form");

        WebElement usernameElement = driver.findElementById("username");
        usernameElement.sendKeys(username);
        WebElement passwordElement = driver.findElementById("password");
        passwordElement.sendKeys(password);
        webElements.get(0).submit();

        CodeUrl codeUrl = new CodeUrl(driver.getCurrentUrl());

        return connectionFactory.getOAuthOperations().exchangeForAccess(codeUrl.getQueryMap().get(CodeUrl.CODE), REDIRECT_URI, null).getAccessToken();
    }

}