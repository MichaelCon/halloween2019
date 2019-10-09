package com.ninox.opencv;

import java.text.DecimalFormat;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.opencv.core.Rect;

public class MSFace extends Rect {
	String json;

	String gender;
	int age;
	double bald;
	
	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public void parseJSON() {
		try {
			JSONParser parser = new JSONParser();
			JSONObject jsonObject = (JSONObject) parser.parse(json);
			JSONObject faceRect = (JSONObject) jsonObject.get("faceRectangle");
			x = ((Long) faceRect.get("left")).intValue();
			y = ((Long) faceRect.get("top")).intValue();
			width = ((Long) faceRect.get("width")).intValue();
			height = ((Long) faceRect.get("height")).intValue();
			
			JSONObject faceAttributes = (JSONObject) jsonObject.get("faceAttributes");
			gender = (String) faceAttributes.get("gender");
			age = ((Double) faceAttributes.get("age")).intValue();
			JSONObject hair = (JSONObject) faceAttributes.get("hair");
			bald = ((Double) hair.get("bald")).doubleValue();

		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	/** Get the rect location in percentages */
	@SuppressWarnings("unchecked")
	public JSONObject getJSONObject() {
		JSONObject output = null;
		try {
			JSONParser parser = new JSONParser();
			output = (JSONObject) parser.parse(json);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return output;
		
	}
		
	DecimalFormat numFormat = new DecimalFormat(".###%");
	/** Utility function for converting a long pixel location into a percentage for JS overlay */
	String quotedPercentage(Long location, int pixelCount) {
		double value = (double) location / (double) pixelCount;
		return numFormat.format(value);
	}	
	
	public static void main(String arg[]) {
		MSFace me = new MSFace();
		me.json = "{\"faceRectangle\":{\"top\":283,\"left\":515,\"width\":258,\"height\":258},\"faceAttributes\":{\"makeup\":{\"eyeMakeup\":false,\"lipMakeup\":false},\"facialHair\":{\"sideburns\":0.1,\"beard\":0.1,\"moustache\":0.1},\"gender\":\"male\",\"accessories\":[],\"blur\":{\"blurLevel\":\"medium\",\"value\":0.4},\"headPose\":{\"roll\":-3.8,\"pitch\":7.3,\"yaw\":1.5},\"smile\":0.0,\"glasses\":\"NoGlasses\",\"hair\":{\"bald\":0.9,\"invisible\":false,\"hairColor\":[]},\"emotion\":{\"contempt\":0.001,\"surprise\":0.0,\"happiness\":0.0,\"neutral\":0.998,\"sadness\":0.001,\"disgust\":0.0,\"anger\":0.0,\"fear\":0.0},\"exposure\":{\"value\":0.59,\"exposureLevel\":\"goodExposure\"},\"occlusion\":{\"eyeOccluded\":false,\"mouthOccluded\":false,\"foreheadOccluded\":false},\"noise\":{\"noiseLevel\":\"low\",\"value\":0.0},\"age\":45.0},\"faceId\":\"354a8ccd-15e4-4491-8eac-fe84b47ef18f\",\"faceLandmarks\":{\"pupilLeft\":{\"x\":591.0,\"y\":350.6},\"eyebrowLeftInner\":{\"x\":614.2,\"y\":327.8},\"eyeLeftOuter\":{\"x\":572.6,\"y\":356.6},\"eyeLeftBottom\":{\"x\":590.5,\"y\":360.9},\"eyeRightOuter\":{\"x\":710.3,\"y\":352.0},\"mouthRight\":{\"x\":691.9,\"y\":477.4},\"eyebrowRightOuter\":{\"x\":732.8,\"y\":330.3},\"noseTip\":{\"x\":644.6,\"y\":418.5},\"pupilRight\":{\"x\":694.2,\"y\":348.6},\"noseRootRight\":{\"x\":655.5,\"y\":353.1},\"noseRightAlarTop\":{\"x\":667.8,\"y\":396.4},\"eyeRightTop\":{\"x\":695.5,\"y\":341.3},\"eyeRightBottom\":{\"x\":693.7,\"y\":358.7},\"noseRootLeft\":{\"x\":631.1,\"y\":354.5},\"mouthLeft\":{\"x\":600.7,\"y\":480.0},\"underLipTop\":{\"x\":646.7,\"y\":481.2},\"eyebrowRightInner\":{\"x\":670.9,\"y\":326.2},\"noseLeftAlarTop\":{\"x\":621.7,\"y\":397.7},\"upperLipTop\":{\"x\":645.6,\"y\":468.2},\"eyeLeftInner\":{\"x\":607.5,\"y\":353.3},\"upperLipBottom\":{\"x\":645.5,\"y\":474.7},\"eyeRightInner\":{\"x\":676.1,\"y\":350.1},\"underLipBottom\":{\"x\":646.4,\"y\":490.4},\"noseLeftAlarOutTip\":{\"x\":612.0,\"y\":419.8},\"eyebrowLeftOuter\":{\"x\":550.7,\"y\":333.2},\"eyeLeftTop\":{\"x\":589.1,\"y\":344.5},\"noseRightAlarOutTip\":{\"x\":679.2,\"y\":420.0}}}";
		me.parseJSON();
		System.out.println(me.x);
		System.out.println(me.gender);
		System.out.println(me.age);
	}
}
