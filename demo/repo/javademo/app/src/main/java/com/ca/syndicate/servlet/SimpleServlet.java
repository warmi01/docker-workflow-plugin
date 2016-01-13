package com.ca.syndicate.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ca.syndicate.example.DeviceSensor;

/**
 * Servlet implementation class TestServlet
 */
@WebServlet("/SimpleServlet")
public class SimpleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SimpleServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);

	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		handleRequest(request, response);
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	public void handleRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try
		{
			response.setContentType("text/html");
			
			if ("/status".equals(request.getServletPath()))
			{
				response.getWriter().write(getStatus());
			}
			else if ("/alerts".equals(request.getServletPath()))
			{
				response.getWriter().write(getAlerts());
			}	
			else if ("/report".equals(request.getServletPath()))
			{
				response.getWriter().write(runReport());
			}

			response.setStatus(200);
		}
		catch (Exception e)
		{
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
		}
		    
		
	}

	public static String getStatus() {

		return "OK";
	}

	public static String getAlerts() {
		return "alerts: 1";
	}

	private String runReport() {
		return DeviceSensor.scan("d1", "floor1");
	}

}
