package com.hit.demo.Activity;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hit.demo.Model.Movie;
import com.hit.demo.R;
import com.squareup.picasso.Picasso;

public class MovieDetailActivity extends Activity implements View.OnClickListener {

    private TextView textViewMovieRate, textViewMoviePopularity, textViewMovieName;
    private ImageView imgMoviePoster;
    private Movie movie;
    private RelativeLayout llBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        if (getIntent() != null) {
            movie = (Movie) getIntent().getSerializableExtra("MovieDetail");
        }

        init();
    }

    private void init() {

        imgMoviePoster = findViewById(R.id.imgMoviePoster);
        textViewMovieName = findViewById(R.id.textViewMovieName);
        textViewMovieRate = findViewById(R.id.textViewMovieRate);
        textViewMoviePopularity = findViewById(R.id.textViewMoviePopularity);
        llBack = findViewById(R.id.llBack);
        llBack.setOnClickListener(this);

        textViewMovieName.setText(movie.getTitle());
        textViewMovieRate.setText(movie.getRating());
        textViewMoviePopularity.setText(movie.getPopularity());
        Picasso.with(this).load("https://image.tmdb.org/t/p/w500/" + movie.getPoster()).into(imgMoviePoster);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.llBack:
                finish();
                break;
        }
    }
}
