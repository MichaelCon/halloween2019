package com.ninox.opencv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class Skeleton {
	int currentAngle = 90;
	
	public void rotate(int a) {
		
		int next = currentAngle + a;
		next = Math.max(next, 15);
		next = Math.min(next, 165);
		System.out.println("Moving " + a + " to " + currentAngle);
		if(next != currentAngle) {
			currentAngle = next;
			moveToCurrent();
		}
	}
	
	public void moveToCurrent() {
		URL obj;
		HttpURLConnection con;
		BufferedReader in;
		StringBuffer response;
		String inputLine;

		try {
			// Submit the form with the password and recapcha
			obj = new URL("http://192.168.0.21?scott=" + currentAngle + ".0");
			con = (HttpURLConnection) obj.openConnection();
			
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			response = new StringBuffer();
	
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			//System.out.println("Response : " + response);			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
