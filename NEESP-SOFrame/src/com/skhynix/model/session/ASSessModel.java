package com.skhynix.model.session;

import java.io.Serializable;
import java.util.Properties;

public class ASSessModel extends BaseSessModel implements Serializable {

	private static final long serialVersionUID = 1L;

    public String gridName = null;
    public String label = "skhynix activespace client";
    public String checkpointName = null;
    public double connectionTimeout = 20;
    public boolean doTxn = false;
    public String trustFileName = null;
    public boolean trustAll = false;
    
    public transient Properties properties = null;
    public transient Object session = null;

}
