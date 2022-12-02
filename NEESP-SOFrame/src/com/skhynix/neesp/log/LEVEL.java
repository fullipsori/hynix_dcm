package com.skhynix.neesp.log;

import java.util.logging.Level;
import java.util.logging.Logger;

// import com.skhynix.narf.log.LogManager.LEVEL;

public enum LEVEL {
	TRACE(0, "TRACE"){
		void apply(Logger logger) {
			logger.setLevel(Level.FINE);
		}
	},
	DEBUG(1, "DEBUG"){
		void apply(Logger logger) {
			logger.setLevel(Level.FINE);
		}
	},
	INFO(2, "INFO"){
		void apply(Logger logger) {
			logger.setLevel(Level.INFO);
		}
	},
	WARN(3, "WARN"){
		void apply(Logger logger) {
			logger.setLevel(Level.WARNING);
		}
	},
	ERROR(4, "ERROR"){
		void apply(Logger logger) {
			logger.setLevel(Level.SEVERE);
		}
	};
	
	public final int intLevel;
	public final String strLevel;

	private LEVEL(int iLevel, String sLevel) {
		this.intLevel = iLevel;
		this.strLevel = sLevel;
	}
	
	abstract void apply(Logger logger);
	
	public LEVEL getLevel(String sLevel) {
		String upperLevel = sLevel.toUpperCase();
		for(LEVEL level : LEVEL.values()) {
			if(level.strLevel.equals(upperLevel)) {
				return level;
			}
		}
		return INFO;
	}
	
	public LEVEL getLevel(int intLevel) {
		for(LEVEL level : LEVEL.values()) {
			if(level.intLevel == intLevel) {
				return level;
			}
		}
		return INFO;
	}		
}