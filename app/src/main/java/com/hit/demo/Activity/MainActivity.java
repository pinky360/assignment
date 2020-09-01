package com.hit.demo.Activity;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hit.demo.Util.DatabaseHelper;
import com.hit.demo.Model.Movie;
import com.hit.demo.Util.PaginationScrollListener;
import com.hit.demo.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class MainActivity extends Activity {

    private Dialog pDialog;
    private RecyclerView recyclerViewMovie;
    private MovieListAdapter movieListAdapter;
    List<Movie> listMovie, tempMovieList;
    private static final int PAGE_START = 1;
    private int TOTAL_PAGE, CURRENT_PAGE = PAGE_START;
    private boolean isLoading = false, isLastPage = false;
    private DatabaseHelper helper;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {

        helper = new DatabaseHelper(this);

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        recyclerViewMovie = findViewById(R.id.recyclerViewMovie);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerViewMovie.setLayoutManager(layoutManager);
        recyclerViewMovie.setItemAnimator(new DefaultItemAnimator());
        recyclerViewMovie.setHasFixedSize(true);

        recyclerViewMovie.addOnScrollListener(new PaginationScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                CURRENT_PAGE += 1;
                Log.e("total_page", TOTAL_PAGE + "");
                if (CURRENT_PAGE <= TOTAL_PAGE) {
                    movieListAdapter.addLoadingFooter();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            loadNextPage();
                        }
                    }, 1000);
                } else {
                    isLastPage = true;
                }
            }

            @Override
            protected boolean isLastPage() {
                return isLastPage;
            }

            @Override
            protected boolean isLoading() {
                return isLoading;
            }

        });

        if (isNetworkConnected()) {
            getListOfMovies();
        } else {
            listMovie = helper.getMovieData();
            if (listMovie.isEmpty()) {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            } else {
                MovieListAdapter movieListAdapter = new MovieListAdapter(this, listMovie);
                recyclerViewMovie.setAdapter(movieListAdapter);
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isNetworkConnected()) {
            listMovie = helper.getMovieData();
            if (listMovie.isEmpty()) {
                Toast.makeText(this, "No Data", Toast.LENGTH_SHORT).show();
            } else {
                MovieListAdapter movieListAdapter = new MovieListAdapter(this, listMovie);
                recyclerViewMovie.setAdapter(movieListAdapter);
            }
        }
    }

    private void loadNextPage() {
        AsyncHttpClient client = new AsyncHttpClient();

        client.get("https://api.themoviedb.org/3/movie/now_playing?api_key=d9d07d2ad8aee6a0476656fdf668ee21&language=en-US&page=" + CURRENT_PAGE, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                movieListAdapter.removeLoadingFooter();
                isLoading = false;
                boolean isSuc = false;

                try {
                    JSONArray jArrayValue = new JSONArray(response.getString("results"));
                    TOTAL_PAGE = response.getInt("total_pages");
                    isSuc = true;
                    for (int i = 0; i < jArrayValue.length(); i++) {
                        JSONObject jObjectsValue = jArrayValue.getJSONObject(i);
                        Movie map = new Movie();
                        map.setId(jObjectsValue.getString("id"));
                        map.setTitle(jObjectsValue.getString("original_title"));
                        map.setPoster(jObjectsValue.getString("poster_path"));
                        map.setPopularity(jObjectsValue.getString("popularity"));
                        map.setRating(jObjectsValue.getString("vote_average"));
                        listMovie.add(map);
                        tempMovieList = helper.getMovieData();
                        if (tempMovieList.size() == 0) {
                            database = helper.getWritableDatabase();
                            helper.addMovieList(new Movie(jObjectsValue.getString("id"), jObjectsValue.getString("poster_path"), jObjectsValue.getString("original_title"),
                                    jObjectsValue.getString("popularity"), jObjectsValue.getString("vote_average")));
                            database = helper.getWritableDatabase();
                        }

                        for (int j = 0; j < tempMovieList.size(); j++) {
                            if (!jObjectsValue.getString("id").equalsIgnoreCase(tempMovieList.get(j).getId())) {
                                database = helper.getWritableDatabase();
                                helper.addMovieList(new Movie(jObjectsValue.getString("id"), jObjectsValue.getString("poster_path"), jObjectsValue.getString("original_title"),
                                        jObjectsValue.getString("popularity"), jObjectsValue.getString("vote_average")));
                                database = helper.getWritableDatabase();
                            }

                        }
                    }
                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                if (isSuc) {
                    movieListAdapter.notifyDataSetChanged();
                    if (CURRENT_PAGE != TOTAL_PAGE) {
                    } else {
                        isLastPage = true;
                    }
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void getListOfMovies() {

        AsyncHttpClient client = new AsyncHttpClient();
        database = helper.getReadableDatabase();

        listMovie = new ArrayList<>();
        client.get("https://api.themoviedb.org/3/movie/now_playing?api_key=d9d07d2ad8aee6a0476656fdf668ee21&language=en-US&page=1", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.e("Response", response.toString());
                try {

                    JSONArray jArrayValue = new JSONArray(response.getString("results"));
                    TOTAL_PAGE = response.getInt("total_pages");

                    for (int i = 0; i < jArrayValue.length(); i++) {
                        JSONObject jObjectsValue = jArrayValue.getJSONObject(i);
                        Movie map = new Movie();
                        map.setTitle(jObjectsValue.getString("original_title"));
                        map.setPoster(jObjectsValue.getString("poster_path"));
                        map.setPopularity(jObjectsValue.getString("popularity"));
                        map.setRating(jObjectsValue.getString("vote_average"));
                        listMovie.add(map);
                        tempMovieList = helper.getMovieData();
                        if (tempMovieList.size() == 0) {
                            database = helper.getWritableDatabase();
                            helper.addMovieList(new Movie(jObjectsValue.getString("id"), jObjectsValue.getString("poster_path"), jObjectsValue.getString("original_title"),
                                    jObjectsValue.getString("popularity"), jObjectsValue.getString("vote_average")));
                            database = helper.getWritableDatabase();
                        }
                        for (int j = 0; j < tempMovieList.size(); j++) {
                            if (!jObjectsValue.getString("id").equalsIgnoreCase(tempMovieList.get(j).getId())) {
                                database = helper.getWritableDatabase();
                                helper.addMovieList(new Movie(jObjectsValue.getString("id"), jObjectsValue.getString("poster_path"), jObjectsValue.getString("original_title"),
                                        jObjectsValue.getString("popularity"), jObjectsValue.getString("vote_average")));
                                database = helper.getWritableDatabase();
                            }
                        }
                    }

                    if (!listMovie.isEmpty()) {
                        movieListAdapter = new MovieListAdapter(MainActivity.this, listMovie);
                        recyclerViewMovie.setAdapter(movieListAdapter);
                    }

                } catch (JSONException e) {
                    Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String res, Throwable t) {
                Toast.makeText(MainActivity.this, t.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public class MovieListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        List<Movie> categoryList;
        private static final int ITEM = 0;
        private static final int LOADING = 1;
        private boolean isLoadingAdded = false;


        public class MyViewHolder extends RecyclerView.ViewHolder {

            ImageView imgMoviePoster;
            TextView textViewMovieName;

            public MyViewHolder(View view) {
                super(view);
                imgMoviePoster = view.findViewById(R.id.imgMoviePoster);
                textViewMovieName = view.findViewById(R.id.textViewMovieName);
            }
        }

        public MovieListAdapter(Context context, List<Movie> categoryList) {
            this.context = context;
            this.categoryList = categoryList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
            RecyclerView.ViewHolder viewHolder = null;
            LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());

            switch (viewType) {
                case ITEM:
                    viewHolder = getViewHolder(viewGroup, layoutInflater);
                    break;

                case LOADING:
                    View viewLoading = layoutInflater.inflate(R.layout.item_progress_pagination, viewGroup, false);
                    viewHolder = new LoadingVH(viewLoading);
                    break;
            }

            return viewHolder;
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder myViewHolder, final int position) {

            switch (getItemViewType(position)) {
                case ITEM:
                    MyViewHolder holder = (MyViewHolder) myViewHolder;
                    holder.textViewMovieName.setText(categoryList.get(position).getTitle());
                    Picasso.with(context).load("https://image.tmdb.org/t/p/w500/" + categoryList.get(position).getPoster()).into(holder.imgMoviePoster);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            startActivity(new Intent(MainActivity.this,MovieDetailActivity.class)
                            .putExtra("MovieDetail",categoryList.get(position)));
                        }
                    });
                    break;

                case LOADING:
                    //Do nothing
                    break;
            }

        }

        private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater layoutInflater) {
            RecyclerView.ViewHolder viewHolder;

            View view = layoutInflater.inflate(R.layout.row_movie, parent, false);
            viewHolder = new MyViewHolder(view);

            return viewHolder;
        }

        public void addLoadingFooter() {
            isLoadingAdded = true;
            add(new Movie());
        }

        public void add(Movie bestSellingList) {
            categoryList.add(bestSellingList);
            notifyItemInserted(categoryList.size() - 1);
        }

        public void removeLoadingFooter() {
            isLoadingAdded = false;

            int position = categoryList.size() - 1;
            Log.e("pos", String.valueOf(position));
            Movie partsItem = getItem(position);

            if (partsItem != null) {
                categoryList.remove(position);
                notifyItemRemoved(position);
            }
        }


        public boolean isEmpty() {
            return getItemCount() == 0;
        }

        @Override
        public int getItemCount() {
            if (categoryList == null) return 0;
            else return categoryList.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (position == categoryList.size() - 1 && isLoadingAdded) return LOADING;
            else return ITEM;

        }

        public class LoadingVH extends RecyclerView.ViewHolder {
            public LoadingVH(View viewLoading) {
                super(viewLoading);
            }
        }

        public Movie getItem(int position) {
            return categoryList.get(position);
        }

    }

}
