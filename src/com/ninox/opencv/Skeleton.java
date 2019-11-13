package com.ninox.opencv;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class Skeleton {
	//public static final String URL_START = "http://192.168.0.22?";
	public static final String URL_START = "http://10.0.0.99?";
	int currentAngle = 90;
	boolean eyesLit = true;
	boolean connected = true;
	
	/** Rotate to the given angle */
	public void rotate(int a) {
		int next = a;
		next = Math.max(next, 15);
		next = Math.min(next, 165);
		//System.out.println("Moving to " + next);
		if(next != currentAngle) {
			currentAngle = next;
			moveToCurrent();
		}
	}
	
	/** Actually move the angle */
	public void moveToCurrent() {
		simpleGet(URL_START + "scott=" + currentAngle + ".0");
	}
	
	/** Change the eye state */
	public void eyes(boolean lit) {
		if(!eyesLit && lit)		// Not lit now but should be
			simpleGet(URL_START + "eyes=1,0,0,1,0,0,");		
		else if(eyesLit && !lit)					// Lit now but shouldn't be
			simpleGet(URL_START + "eyes=0,0,0,0,0,0,");
		eyesLit = lit;
	}
	
	/** Chatter teeth */
	public void jaw() {
		simpleGet("command=C");
	}
	
	
	String simpleGet(String url) {
		URL obj;
		HttpURLConnection con;
		BufferedReader in;
		StringBuffer response = new StringBuffer();
		String inputLine;

		if(connected) {
			try {
				// Submit the form with the password and recapcha
				obj = new URL(URL_START + url);
				con = (HttpURLConnection) obj.openConnection();
				
				in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		
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
		return response.toString(); 
	}
	
	public static void main(String args[]) {
		
//		Skeleton me = new Skeleton();
//		me.eyes(true);
	}
}
