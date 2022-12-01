package com.skhynix.repository;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HttpRepository {

	private static final HttpRepository instance = new HttpRepository();
	
	public HttpRepository() {}
	public static HttpRepository getInstance() {
		return instance;
	}
	
	public Single<String> getData(String url) throws IOException, InterruptedException, URISyntaxException {
		HttpRequest request = HttpRequest.newBuilder(new URI(url))
				.version(HttpClient.Version.HTTP_1_1)
				.GET()
				.build();
		
		return Single.fromFuture(HttpClient.newHttpClient().sendAsync(request, HttpResponse.BodyHandlers.ofString()))
				.subscribeOn(Schedulers.io())
				.flatMap(res -> {
					if(res.statusCode() >= 200 && res.statusCode() < 400) return Single.just(res.body());
					else return Single.error(new Throwable("status code:" + res.statusCode()));
				});

	}
}
