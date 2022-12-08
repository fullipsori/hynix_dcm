package com.skhynix.neesp.log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {
	
	private static Utils instance = new Utils();	
	public static Utils getInstnace( ) { return instance;}
	public static final long currentTime = 0;
	
	private Utils() {
		// default constructor
	}
	
	public String calcDate(long millisecs) {
        SimpleDateFormat date_format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
	
	public String yyyymmdd_hhmmssSSS(long millisecs) {
		SimpleDateFormat date_format = new SimpleDateFormat("yyyyMMdd_HHmmssSSS");
		if (millisecs == 0 ) {
			Date resultdate = new Date(System.currentTimeMillis());
	        return date_format.format(resultdate);
		}
		
        Date resultdate = new Date(millisecs);
        return date_format.format(resultdate);
    }
}
