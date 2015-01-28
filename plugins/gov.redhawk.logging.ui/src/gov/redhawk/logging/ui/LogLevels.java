package gov.redhawk.logging.ui;


public enum LogLevels {
	  OFF("OFF", 60000),
	  FATAL("FATAL", 50000),
	  ERROR("ERROR", 40000),
	  WARN("WARN", 30000),
	  INFO("INFO", 20000),
	  DEBUG("DEBUG", 10000),
	  TRACE("TRACE", 5000),
	  ALL("ALL", 0);
	  
	  private int level;
	  private String label;
	  
	  private LogLevels(String label, int level) {
		  this.label = label;
		  this.level = level;
	}

	public int getLevel() {
		return level;
	}

	public String getLabel() {
		return label;
	}
	
	public static LogLevels intToLogLevel(int value) {
		LogLevels retVal = ALL; // If the value is below 0, we assume it's ALL.
		
		for (LogLevels logLevel : LogLevels.values()) {
			if (value >= logLevel.getLevel()) {
				retVal = logLevel;
				break;
			}
		}
		
		return retVal;
	}
}
