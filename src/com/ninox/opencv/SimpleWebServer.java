package com.ninox.opencv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.StringTokenizer;

public class SimpleWebServer implements Runnable { 
	
	static final File WEB_ROOT = new File(".");
	static final String DEFAULT_FILE = "index.html";
	static final String FILE_NOT_FOUND = "404.html";
	static final String METHOD_NOT_SUPPORTED = "not_supported.html";
	// port to listen connection
	static final int PORT = 8080;
	
	HalloweenController controller;
	
	public SimpleWebServer() {
		
	}
	
	public void start() {
		Thread thread = new Thread(this);
		thread.start();		
	}

	public static void main(String[] args) {
		SimpleWebServer me = new SimpleWebServer();
		me.start();
	}

	public void run() {
		ServerSocket serverConnect;
		try {
			serverConnect = new ServerSocket(PORT);
			System.out.println("Server started.\nListening for connections on port : " + PORT + " ...\n");
			while(true) {
				Socket connection = serverConnect.accept();
				handleRequest(connection);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void handleRequest(Socket connect) {
		BufferedReader in = null; 
		PrintWriter out = null;
		
		try {
			in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
			out = new PrintWriter(connect.getOutputStream());
			
			// get first line of the request from the client
			String input = in.readLine();
			if(input != null && input.startsWith("GET")) {
				StringTokenizer parse = new StringTokenizer(input);
				String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
				String fileRequested = parse.nextToken();
				
				int loc = fileRequested.indexOf("command=");
				if(loc > 0 && controller != null)
					controller.handlerWebCommand(fileRequested.charAt(loc + 8));
			
				// send HTTP Headers
				out.println("HTTP/1.1 200 OK");
				out.println("Server: Halloween");
				out.println("Date: " + new Date());
				out.println("Content-Type: text/html");
			    out.println();
			    out.println("<HTML>");
			    out.println("<HEAD>");
			    out.println("<TITLE>2019 Halloween</TITLE>");
			    out.println("</HEAD>");
			    out.println("<BODY>");
			    out.println("<H1></H1>");
			    out.println("<a href=\"a?command=m\">Cloud call</a><br/>");
			    out.println("<a href=\"a?command=d\">Blade Dropped</a><br/>");
			    out.println("</BODY>");
			    out.println("</HTML>");									    
				out.flush(); // flush character output stream buffer
			}
		} catch (IOException ioe) {
			System.err.println("Server error : " + ioe);
		} finally {
			try {
				in.close();
				out.close();
				connect.close(); // we close socket connection
			} catch (Exception e) {
				System.err.println("Error closing stream : " + e.getMessage());
			} 			
		}
	}
}