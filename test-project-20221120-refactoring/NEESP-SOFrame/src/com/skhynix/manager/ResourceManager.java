package com.skhynix.manager;

import java.io.IOException;
import java.net.URISyntaxException;

import com.skhynix.decl.Joinable;
import com.skhynix.decl.BaseManager;
import com.skhynix.repository.ASRepository;
import com.skhynix.repository.DBRepository;
import com.skhynix.repository.FileRepository;
import com.skhynix.repository.HttpRepository;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class ResourceManager extends BaseManager {
	private static final ResourceManager instance = new ResourceManager();
	private final ASRepository ASRepo = ASRepository.getInstance();
	private final DBRepository DBRepo = DBRepository.getInstance();
	private final FileRepository FileRepo = FileRepository.getInstance();
	private final HttpRepository HttpRepo = HttpRepository.getInstance();
	
	private final MetaDataManager metaDataManager = MetaDataManager.getInstance();

	public ResourceManager() {
		// TODO Auto-generated constructor stub
		initRepos();
	}
	public static ResourceManager getInstance() {
		return instance;
	}
	
	public void initRepos() {
		
//		Single.just(metaDataManager.getServerInfo(ASRepo.getConnectableType()))
//			.subscribeOn(Schedulers.io())
//			.filter(info -> info.isPresent())
//			.subscribe(info -> ASRepo.openSession(getDomain(), info.get()));

		/** 
		metaDataManager.getServerInfo(key).ifPresent(info -> {
			((AbstractConnectable)clazz).openSession(info);
			messageMap.put(key, clazz);
		});
		**/
	}
	
	public void GetMeta(String key) throws IOException, InterruptedException, URISyntaxException {
		HttpRepo.getData("http://localhost:9999/meta")
			.observeOn(Schedulers.computation())
			.subscribe(data -> {
				System.out.println(data);
			}, error -> {
				System.out.println(error);
			});
	}
	@Override
	public String getDomain() {
		// TODO Auto-generated method stub
		return "resource";
	}
	@Override
	public Joinable createMember(String jointype) {
		// TODO Auto-generated method stub
		switch(jointype)  {
			case "resource:as" : return new ASRepository();
			default: break;
		}
		return null;
	}
}
