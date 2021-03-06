package com.michaelfotiadis.ukatmdb.network;

import com.google.gson.Gson;
import com.michaelfotiadis.ukatmdb.network.api.AtmApi;
import com.michaelfotiadis.ukatmdb.network.client.RestClient;
import com.michaelfotiadis.ukatmdb.network.client.okhttp.OkHttpFactory;
import com.michaelfotiadis.ukatmdb.network.model.AtmResponse;

import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class NetworkLoader {

    private final ApiStore mApiStore;

    private final RestClient mRestClient;

    public NetworkLoader(final Gson gson, final OkHttpFactory okHttpFactory) {

        mApiStore = new ApiStore();
        mRestClient = new RestClient(gson, okHttpFactory);

    }

    public Single<AtmResponse> getForBank(final Bank bank) {

        final String endpoint = mApiStore.getEndpointForBank(bank);
        final AtmApi atmApi;

        if (bank == Bank.UNDEFINED) {
            throw new IllegalStateException("Unsupported Bank operation");
        } else if (mApiStore.isCertificateStrict(bank)) {
            atmApi = mRestClient.createAcceptAllApi(AtmApi.class, endpoint);
        } else {
            atmApi = mRestClient.createApi(AtmApi.class, endpoint);
        }

        return atmApi.getRx().subscribeOn(Schedulers.io())
                .cache()
                .onErrorResumeNext(new Function<Throwable, SingleSource<? extends AtmResponse>>() {
                    @Override
                    public SingleSource<? extends AtmResponse> apply(final Throwable throwable) throws Exception {
                        return Single.error(throwable);
                    }
                });

    }

}
