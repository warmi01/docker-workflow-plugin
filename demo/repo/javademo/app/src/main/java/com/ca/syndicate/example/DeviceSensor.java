package com.ca.syndicate.example;


public class DeviceSensor {
	public static String scan(String device, String location){
		String result = "";
		StringBuilder b = new StringBuilder();

		if (device != null && location != null)
		{
			b.append(device);
			b.append(" at ");
			b.append(location);
			b.append(" is running. ");
		}
		result = b.toString();
		return result;
	}
}
