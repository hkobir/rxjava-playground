package com.example.rxjava_playground;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.example.rxjava_playground.models.Comment;
import com.example.rxjava_playground.models.Post;
import com.example.rxjava_playground.remote.PostService;

import java.util.List;
import java.util.Random;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.POST;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    //ui
    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.postRV);
        disposable = new CompositeDisposable();
        initRecyclerView();

        //return updated observable post with comments
        getPostObservable()
                .subscribeOn(Schedulers.io())
                .flatMap(new Function<Post, ObservableSource<Post>>() {  //linking between comments data
                    @Override
                    public ObservableSource<Post> apply(Post post) throws Exception {
                        return getCommentsObservable(post);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Post>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(Post post) {
                        updatePost(post);  //updated post again with comment
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private Observable<Post> getPostObservable() {
        return PostService
                .getRequestApi()
                .getPosts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<List<Post>, ObservableSource<Post>>() {
                    @Override
                    public ObservableSource<Post> apply(List<Post> posts) throws Exception {
                        adapter.setPosts(posts);
                        return Observable.fromIterable(posts)
                                .subscribeOn(Schedulers.io());
                    }
                });
    }

    private Observable<Post> getCommentsObservable(Post post) {
        return PostService
                .getRequestApi()
                .getComments(post.getId())
                .map(new Function<List<Comment>, Post>() {
                    @Override
                    public Post apply(List<Comment> comments) throws Exception {
                        //create  random delay comments
                        int delay = (new Random().nextInt(5) + 1) * 1000;
                        Thread.sleep(delay);
                        Log.d(TAG, "apply id " + post.getId() + ": sleeping thread: " + Thread.currentThread().getName() + " for" +
                                String.valueOf(delay) + " ms");
                        post.setComments(comments);
                        return post;
                    }
                })
                .subscribeOn(Schedulers.io());
    }

    private void updatePost(Post post) {
        adapter.updatePost(post);
    }

    private void initRecyclerView() {
        adapter = new PostAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    public void goSearch(View view) {
        startActivity(new Intent(this, SearchViewActivity.class));
    }
}