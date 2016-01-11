package com.ca.syndicate.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet implementation class TestServlet
 */
public class TestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

    /**
     * Default constructor. 
     */
    public TestServlet()
    {
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{
		try
		{
			// Return an OK response for the status endpoint, otherwise
			// send the request test request to the app
			if ("/status".equals(request.getServletPath()))
			{
				response.getWriter().write("OK");
			}
			else
			{
				executeAppTest(response);				
			}
			
			response.setStatus(200);
		}
		catch (Exception e)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
				e.getMessage());
		}
	}
	
	/**
	 * Sends request to app test endpoint and passes back the response
	 * @param response
	 * @throws Exception
	 */
	private void executeAppTest(HttpServletResponse response) throws Exception
	{
		// Try to get demoapp host and URL path from environment variables
		String host = System.getenv("DEMOTEST_APP_LINK_ALIAS");
		String urlPath = System.getenv("DEMOTEST_APP_URLPATH");

		if (host == null)
		{
			host = "app:8080";
		}
		
		if (urlPath == null)
		{
			urlPath = "/demoapp/test";
		}
		
		HttpURLConnection httpConnection = (HttpURLConnection)
			new URL("http://" + host + urlPath).openConnection();
		
		int httpRc;
		
		try
		{
			httpRc = httpConnection.getResponseCode();
		}
		catch (Exception e)
		{
			throw new Exception("Failed to connect to app: " + e.getMessage());
		}
		
		try
		{
			if (httpRc == 200)
			{
				// Read body response
				String httpResponse = readInputStream(httpConnection.getInputStream());
				System.out.format("Ran tests.  Response: <%s>\n", httpResponse);
				
				// Pass back the same content type and app response in our response
				response.setContentType(httpConnection.getContentType());
				response.getWriter().write(httpResponse);
			}
			else
			{
				String errorResponse = readInputStream(httpConnection.getErrorStream());
				System.out.format("App Test request failed; HTTP error code <%d> response <%s>\n", httpRc, errorResponse);
				throw new Exception("App Test HTTP Error code <" + httpRc + "> Response <" + errorResponse + ">");
			}
		}
		catch (Exception e)
		{
			throw e;
		}
		finally
		{
			closeHttpURLConnection(httpConnection);
		}
	}
	
	private String readInputStream(InputStream is) throws IOException
	{
		// Auto close reader when done
		try (BufferedReader reader =
			new BufferedReader(new InputStreamReader(is)))
		{
			StringBuilder buffer = new StringBuilder();
			String line = new String();
			
			while ((line = reader.readLine()) != null)
			{
				buffer.append(line);
			}
			
			return buffer.toString();
		}
	}

	/**
	 * Helper method to properly close an HttpURLConnection object
	 * @param connection
	 */
	private void closeHttpURLConnection(HttpURLConnection connection)
	{
		if (connection == null)
		{
			return;
		}
		
		try
		{
			InputStream stream = connection.getInputStream();
			if (stream != null)
			{
				stream.close();
			}
		}
		catch (IOException e)
		{
		}
		
		try
		{
			OutputStream stream = connection.getOutputStream();
			if (stream != null)
			{
				stream.close();
			}
		}
		catch (IOException e)
		{
		}
		
		try
		{
			InputStream stream = connection.getErrorStream();
			if (stream != null)
			{
				stream.close();
			}
		}
		catch (IOException e)
		{
		}
		
		connection.disconnect();		
	}	
}
