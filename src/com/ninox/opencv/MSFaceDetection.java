package com.ninox.opencv;

// This sample uses the Apache HTTP client from HTTP Components (http://hc.apache.org/httpcomponents-client-ga/)
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MSFaceDetection {
	private static final Logger LOG = Logger.getLogger(MSFaceDetection.class);
	static final String service = "https://eastus.api.cognitive.microsoft.com/face/v1.0/";
	static final String subscriptionKey = "xxx";

	ArrayList<Long> lastAPITimestamps = new ArrayList<Long>();
	boolean okToRetry = true;	// Non thread safe way to prevent error loop
	
	/** Detect faces in an image */ 
	List<MSFace> detectFaces(String imageUrl) {
		HttpClient httpclient = HttpClients.createDefault();

		String json = null;
		delayForRateLimit(); 
		try {
			URIBuilder builder = new URIBuilder(service + "detect");
				
			builder.setParameter("returnFaceId", "true");
			builder.setParameter("returnFaceLandmarks", "true");
			builder.setParameter("returnFaceAttributes", "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise");

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Content-Type", "application/json");
			request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			// Request body
			//String imageUrl = "https://farm8.staticflickr.com/7572/28677399660_c9bc2f9455_o_d.jpg"; // <- Caroline at Bunratty
			StringEntity reqEntity = new StringEntity("{\n\"url\":\"" + imageUrl + "\"\n}");
			request.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				json = EntityUtils.toString(entity);
				LOG.debug(json);
				if(json.indexOf("InvalidImageSize") > 0) {
					LOG.debug("Image too big!!");
				}
				if(okToRetry && json.indexOf("Internal server error") > 0) {
					LOG.debug("Retrying on the internal server error");
					okToRetry = false;
					return detectFaces(imageUrl);
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (org.apache.http.ParseException e) { e.printStackTrace(); } 
		catch (URISyntaxException e) { e.printStackTrace(); }
		okToRetry = true;
		
		return parseFaces(json);
	}

	/** Detect faces in an image */ 
	List<MSFace> detectFaces(byte[] bits) {
		HttpClient httpclient = HttpClients.createDefault();

		String json = null;
		delayForRateLimit(); 
		try {
			URIBuilder builder = new URIBuilder(service + "detect");
				
			builder.setParameter("returnFaceId", "true");
			builder.setParameter("returnFaceLandmarks", "true");
			builder.setParameter("returnFaceAttributes", "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise");

			URI uri = builder.build();
			HttpPost request = new HttpPost(uri);
			request.setHeader("Content-Type", "application/octet-stream");
			request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);

			// Request body
			ByteArrayEntity reqEntity = new ByteArrayEntity(bits);
			request.setEntity(reqEntity);

			HttpResponse response = httpclient.execute(request);
			HttpEntity entity = response.getEntity();

			if (entity != null) {
				json = EntityUtils.toString(entity);
				LOG.debug(json);
				if(json.indexOf("InvalidImageSize") > 0) {
					LOG.debug("Image too big!!");
				}
			}
		}
		catch (IOException e) { e.printStackTrace(); }
		catch (org.apache.http.ParseException e) { e.printStackTrace(); } 
		catch (URISyntaxException e) { e.printStackTrace(); }
		
		return parseFaces(json);
	}

	List<MSFace> parseFaces(String json) {
		List<MSFace> results = new ArrayList<MSFace>();
		try {
			JSONParser parser = new JSONParser();
			JSONArray array = (JSONArray) parser.parse(json);

			for(Object obj : array) {
				JSONObject jsonObject = (JSONObject) obj;
				MSFace f = new MSFace();
				f.json = jsonObject.toJSONString();
				results.add(f);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return results;
	}
	
	/** Only allow this function to be called 20 times per minute */
	void delayForRateLimit() {
		long now = System.currentTimeMillis();
		long wait = 0;	// set this to minimum wait time
		long twentyAgo = 0;
		if(lastAPITimestamps.size() >= 20) {
			twentyAgo = now - lastAPITimestamps.remove(0);
			wait = Math.max(wait,  64000 - twentyAgo);
		}
		lastAPITimestamps.add(now + wait);
		LOG.debug("Wait for " + wait + " ms.  20 (of " + lastAPITimestamps.size() + ") ago was " + twentyAgo);
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) { 
		MSFaceDetection me = new MSFaceDetection();
		
		List<MSFace> list = me.detectFaces("https://live.staticflickr.com/65535/48823531463_0c0b88b851_c_d.jpg");
		for(MSFace f : list) {
			System.out.println(f.json);
		}
		
	}
}
