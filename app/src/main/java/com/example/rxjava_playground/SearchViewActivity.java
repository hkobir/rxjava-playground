package com.example.rxjava_playground;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SearchViewActivity extends AppCompatActivity {
    private SearchView searchView;
    private CompositeDisposable disposable;
    private long timeSinceLastRequest;
    private ProgressBar progressBar;
    private TextView nameTV;
    private final String TAG = "SearchViewActivity";
    private String names[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_view);
        searchView = findViewById(R.id.searchView);
        disposable = new CompositeDisposable();
        timeSinceLastRequest = System.currentTimeMillis();
        progressBar = findViewById(R.id.progressBar);
        nameTV = findViewById(R.id.nameTV);
        names = new String[]{"Kobir", "sumon", "rafi", "remon"};

        Observable<String> observableText = Observable
                .create(new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                            @Override
                            public boolean onQueryTextSubmit(String query) {
                                return false;
                            }

                            @Override
                            public boolean onQueryTextChange(String newText) {
                                if (!emitter.isDisposed()) {
                                    emitter.onNext(newText);
                                }
                                return false;
                            }
                        });
                    }
                })
                .debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());


        observableText.subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                disposable.add(d);
            }

            @Override
            public void onNext(String s) {
                try {
                    sendRequestServer(s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d(TAG, "onNext: time last request: " + (System.currentTimeMillis() - timeSinceLastRequest));
                Log.d(TAG, "onNext: query text: " + s);
                timeSinceLastRequest = System.currentTimeMillis();
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Query execution completed");
            }
        });
    }

    //Debounce operator is used to search query after specific milliseconds(i.e 500ms) of typing break
    public void sendRequestServer(String query) throws InterruptedException {
        //do remote search query with given string


        for (int i = 0; i < names.length; i++) {
            if (query.equals(names[i])) {
                nameTV.setText(names[i]);
            }
        }

    }


    private void showProgressBar(boolean showProgressBar) {
        if (showProgressBar) {
            progressBar.setVisibility(View.VISIBLE);
            nameTV.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
            nameTV.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }
}