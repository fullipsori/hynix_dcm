package com.skhynix.neesp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Registry {
    private static Registry instance = new Registry();

    public static Registry getInstance() {
        return instance;
    }

    // Queue for Open Connection
    public BlockingQueue<String> spawnSWWorkerRequestQueue = null;
    
    // Queue for Close Connection

    private Registry() {
    	this.spawnSWWorkerRequestQueue = new ArrayBlockingQueue<>(1000);
    }
    
    
}
