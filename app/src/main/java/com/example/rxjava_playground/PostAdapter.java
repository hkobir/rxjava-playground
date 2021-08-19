package com.example.rxjava_playground;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.rxjava_playground.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private static final String TAG = "RecyclerAdapter";

    private List<Post> posts = new ArrayList<>();

    @NonNull
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_post_list_item, null, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.ViewHolder holder, int position) {
        holder.bind(posts.get(position));
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void setPosts(List<Post> posts) {
        this.posts = posts;
        notifyDataSetChanged();
    }

    public void updatePost(Post post) {
        posts.set(posts.indexOf(post), post); //updated post again with comment
        notifyItemChanged(posts.indexOf(post));
    }

    public List<Post> getPosts() {
        return posts;
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, numComments;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.titlePostTV);
            numComments = itemView.findViewById(R.id.num_comments);
            progressBar = itemView.findViewById(R.id.progress_bar);
        }

        public void bind(Post post) {
            title.setText(post.getTitle());

            if (post.getComments() == null) {  //when comments not loaded yet
                showProgressBar(true);
                numComments.setText("");
            } else {
                showProgressBar(false);
                numComments.setText(String.valueOf(post.getComments().size()));
            }
        }

        private void showProgressBar(boolean showProgressBar) {
            if (showProgressBar) {
                progressBar.setVisibility(View.VISIBLE);
            } else {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
