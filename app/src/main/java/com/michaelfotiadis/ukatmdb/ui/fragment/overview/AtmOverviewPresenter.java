package com.michaelfotiadis.ukatmdb.ui.fragment.overview;

import com.michaelfotiadis.ukatmdb.injection.Injector;
import com.michaelfotiadis.ukatmdb.model.AtmDetails;
import com.michaelfotiadis.ukatmdb.network.Bank;
import com.michaelfotiadis.ukatmdb.network.NetworkLoader;
import com.michaelfotiadis.ukatmdb.network.model.AtmResponse;
import com.michaelfotiadis.ukatmdb.network.model.Datum;
import com.michaelfotiadis.ukatmdb.utils.AppLog;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

public class AtmOverviewPresenter {

    private Single<AtmResponse> mSingle;
    private final AtmOverviewView mView;
    private final Bank mBank;
    private final DisposableSingleObserver<List<AtmDetails>> mDisposable;

    @Inject
    NetworkLoader mNetworkLoader;

    /*package*/  AtmOverviewPresenter(final Bank bank, final AtmOverviewView mainView) {

        Injector.getComponentStore().getAndroidAwareComponent().inject(this);

        mBank = bank;
        mView = mainView;

        mDisposable = new DisposableSingleObserver<List<AtmDetails>>() {
            @Override
            public void onSuccess(@NonNull final List<AtmDetails> details) {
                AppLog.d("Received " + details.size() + " details");
                mView.showContent(details);
            }

            @Override
            public void onError(@NonNull final Throwable e) {
                mView.showError(e);
            }
        };

    }

    protected void loadData() {

        mSingle.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .cache()
                .map(this::convertDetails)
                .subscribe(mDisposable);

    }

    @NonNull
    private List<AtmDetails> convertDetails(final AtmResponse atmResponse) {
        final List<AtmDetails> details = new ArrayList<>();

        for (final Datum datum : atmResponse.getData()) {
            details.add(new AtmDetails(datum));
        }

        return details;
    }

    protected void onStart() {
        mSingle = mNetworkLoader.getForBank(mBank);
    }

    protected void onStop() {
        mDisposable.dispose();
        mSingle.unsubscribeOn(AndroidSchedulers.mainThread());
    }

}
