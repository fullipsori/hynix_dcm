package com.skhynix.neesp.EventStarter;

import com.skhynix.neesp.Registry;
import com.tibco.bw.palette.shared.java.JavaProcessStarter;

public class SpawnRequestEvent extends JavaProcessStarter {
	
	MyThread myThread = null;
	
	@Override
	public void init() throws Exception {
		myThread = new MyThread(this);
	}

	@Override
	public void onShutdown() {
		
	}

	@Override
	public void onStart() throws Exception {
		Thread thread = new Thread(this.myThread);
		thread.start();
	}

	@Override
	public void onStop() throws Exception {
		this.myThread.setFlowControlEnabled(true);
	}

	public static class MyThread implements Runnable {
		JavaProcessStarter javaProcessStarter;
		boolean flowControl = false;
		public MyThread(JavaProcessStarter javaProcessStarter) {
			this.javaProcessStarter = javaProcessStarter;
		}
		
		@Override
		public void run() {
			while(true) {
				try {
					this.javaProcessStarter.onEvent(new RequestVo(Registry.getInstance().spawnSWWorkerRequestQueue.take()));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		public void setFlowControlEnabled(boolean flowControl) {
			this.flowControl = flowControl;
		}
		
	}
}
