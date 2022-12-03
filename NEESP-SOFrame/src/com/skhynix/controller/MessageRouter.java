package com.skhynix.controller;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.skhynix.extern.Messageable;
import com.skhynix.extern.Sessionable;
import com.skhynix.manager.MessageManager;
import com.skhynix.model.session.BaseSessModel;
import com.skhynix.neesp.log.LogManager;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MessageRouter implements Sessionable, Messageable {
	private static final MessageRouter instance = new MessageRouter();
	
	private final LogManager logger = LogManager.getInstance();
	private final MessageManager messageManager = MessageManager.getInstance();

	private final ThreadPoolExecutor executorService;
	
	public MessageRouter() {
		BlockingQueue<Runnable> arrayBlockingQueue = new ArrayBlockingQueue<>(10);
		executorService = new ThreadPoolExecutor(10, 100, 30, TimeUnit.SECONDS, arrayBlockingQueue);
		// when the blocking queue is full, this tries to put into the queue which blocks
		executorService.setRejectedExecutionHandler(new RejectedExecutionHandler() {
		    @Override
		    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
		        try {
		            // block until there's room
		            executor.getQueue().put(r);
		            // check afterwards and throw if pool shutdown
		            if (executor.isShutdown()) {
		                throw new RejectedExecutionException(
		                    "Task " + r + " rejected from " + executor);
		            }
		        } catch (InterruptedException e) {
		            Thread.currentThread().interrupt();
		            throw new RejectedExecutionException("Producer interrupted", e);
		        }
		    }
		});
		executorService.setThreadFactory(new ThreadFactory() {
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r);
				t.setDaemon(true);
				return t;
			}
			
		});
	}	

	public static MessageRouter getInstance() {
		return instance;
	}

	public void registerClient(String jointype, Object object) {
		messageManager.register(jointype, object);
	}
	
	public void unregisterClient(String jointype) {
		messageManager.unregister(jointype);
	}
	
	public void sendAsyncTo(String[] handles, String message) {
		Observable.fromArray(handles)
			.subscribeOn(Schedulers.from(executorService))
			.subscribe(handle -> messageManager.sendMessage(handle, message));

		/**
		dests.stream().map(dest -> messageMap.get(dest))
			.filter(elem -> elem != null)
			.map(elem -> CompletableFuture.runAsync(()->elem.sendMessage(message), executor));
		**/
	}
	
	public void sendSyncTo(String[] handles, String message) {
		Observable.fromArray(handles)
			.subscribeOn(Schedulers.from(executorService))
			.map(handle -> messageManager.sendMessage(handle, message))
			.onErrorReturnItem(false)
			.blockingSubscribe();
			
		/*
		List<CompletableFuture<Void>> futureList = dests.stream().map(dest -> messageMap.get(dest))
			.filter(elem -> elem != null)
			.map(elem -> CompletableFuture.runAsync(()->elem.sendMessage(message), executor))
			.collect(Collectors.toList());
		
		futureList.stream().map(CompletableFuture::join);
		*/
	}

	@Override
	public boolean sendMessage(String handle, String message) {
		return messageManager.sendMessage(handle, message);
	}
	
	@Override
	public String receiveMessage(String sessionKey) {
		// TODO Auto-generated method stub
		return messageManager.receiveMessage(sessionKey);
	}

	@Override
	public void confirmMessage(String handle) {
		// TODO Auto-generated method stub
		messageManager.confirmMessage(handle);
	}

	@Override
	public String openSession(String jointype, String serverUrl, String jsonParams) {
		return messageManager.openSession(jointype, serverUrl, jsonParams);
	}
	
	@Override
	public boolean closeSession(String sessionKey) {
		// TODO Auto-generated method stub
		return messageManager.closeSession(sessionKey);
	}

	@Override
	public BaseSessModel makeSessModel(String domain, String jsonParams) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean connectServer(BaseSessModel client) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void disconnectServer() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getSessionName(BaseSessModel client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BaseSessModel connectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void disconnectSession(BaseSessModel client) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void closeAllSession() {
		// TODO Auto-generated method stub
		
	}
	
}