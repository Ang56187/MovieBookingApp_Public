package com.project.moviebookingapp.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.project.moviebookingapp.R;
import com.project.moviebookingapp.controller.movie.FavouriteController;
import com.project.moviebookingapp.controller.movie.MovieController;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Favourite;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.ui.account.tabs.FavouritesTab;
import com.project.moviebookingapp.ui.movie.MovieDetailActivity;

import java.util.ArrayList;

import static com.project.moviebookingapp.custom.TextViewFormat.df;
import static com.project.moviebookingapp.custom.TextViewFormat.ratingCountFormat;

//non firebase ver of adapter
//no real time syncing
public class NonFirebaseMovieAdapter extends RecyclerView.Adapter<NonFirebaseMovieAdapter.MovieViewHolder> {
    private ArrayList<Movie> movieDataset;
    private LayoutInflater mInflater;
    private int overallViewId;
    private Activity adapterActivity;
    private Context adapterContext;
    //checks if favouritetab called the adapter
    private Boolean isFav = false;

    private FirebaseStorage mFireStorage;
    private FirebaseFirestore mFirestore;

    private MovieController controller;
    private FavouriteController favouriteController;

    // Constructor for adapter
    public NonFirebaseMovieAdapter(Context context,ArrayList<Movie> mDataset,
                                int viewID) {
        this.mInflater = LayoutInflater.from(context);
        movieDataset = mDataset;
        overallViewId = viewID;
        adapterContext = context;
        adapterActivity = (Activity)context;
        mFireStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        controller = new MovieController(context);
    }

    public NonFirebaseMovieAdapter(Context context,ArrayList<Movie> mDataset,Boolean isFav,
                                   int viewID) {
        this.mInflater = LayoutInflater.from(context);
        this.isFav = isFav;
        movieDataset = mDataset;
        overallViewId = viewID;
        adapterContext = context;
        adapterActivity = (Activity)context;
        mFireStorage = FirebaseStorage.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        controller = new MovieController(context);
        favouriteController = new FavouriteController(context);
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MovieViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView ratingScoreTextView;
        public TextView movieTitleTextView;
        public ImageView posterImageView;
        public LinearLayout moviePosterLinearLayout;
        public TextView ratingNumTextView;
        private ImageButton favouriteButton;

        public MovieViewHolder(View v) {
            super(v);
            ratingScoreTextView = v.findViewById(R.id.ratingScoreTextView);
            moviePosterLinearLayout= v.findViewById(R.id.moviePosterLinearLayout);
            posterImageView = v.findViewById(R.id.posterImageView);
            movieTitleTextView = v.findViewById(R.id.movieTitleTextView);
            ratingNumTextView = v.findViewById(R.id.ratingNumTextView);
            favouriteButton = v.findViewById(R.id.favouriteButton);
        }

    }

    // Create new views
    @Override
    public NonFirebaseMovieAdapter.MovieViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = mInflater.inflate(overallViewId, parent, false);
        if(overallViewId == R.layout.component_movieposter_alt){
            view.findViewById(R.id.moviePosterLinearLayout).setLayoutParams(
                    new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        return new NonFirebaseMovieAdapter.MovieViewHolder(view);
    }

    // set values for contents of a view
    @Override
    public void onBindViewHolder(NonFirebaseMovieAdapter.MovieViewHolder holder, int position) {
        Movie movie = movieDataset.get(position);

//        if(movie.getMovieID() == null){
//            holder.moviePosterLinearLayout.setVisibility(View.GONE);
//            holder.moviePosterLinearLayout.setLayoutParams(new LinearLayout
//                    .LayoutParams(0,0));
//            holder.moviePosterLinearLayout.setClickable(false);
//            return;
//        }

        if(isFav){
            holder.favouriteButton.setVisibility(View.VISIBLE);

            holder.favouriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    favouriteController.tapOnFavourite(movie.getMovieID(), true,
                            new OnFirebaseCallback() {@Override
                            public void firebaseCallBack(Object object) {
                                controller.queryFavouriteMovies(new OnFirebaseCallback() {
                                    @Override
                                    public void firebaseCallBack(Object object) {
                                        movieDataset.clear();
                                        if(((ArrayList<Movie>)object).size() != 0) {
                                            movieDataset.addAll((ArrayList<Movie>) object);
                                        }
                                        notifyDataSetChanged();
                                    }
                                });
                            }
                    });
                }
            });
        }

        Intent intent = new Intent(adapterContext, MovieDetailActivity.class);
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        holder.ratingScoreTextView.setText("0.0");
        holder.movieTitleTextView.setText(movie.getMovieName());

        //calculate average review score of movie
        controller.retrieveRatings(movie.getMovieID(), new OnFirebaseCallback() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void firebaseCallBack(Object object) {
                ArrayList<Double> list = (ArrayList<Double>)object;
                Double average = list.stream().mapToDouble(val -> val).average().orElse(0.0);
                if(list.size()>0){
                    holder.ratingScoreTextView.setText(df.format(average));
                    holder.ratingNumTextView.setText(ratingCountFormat(list));
                }else{
                    holder.ratingScoreTextView.setText("0.0");
                    holder.ratingNumTextView.setText("(0)");
                }
                intent.putExtra("ratingScoreList",list);

                //send data to movie detail page
                //intents to pass from any page with poster to movie detail
                holder.moviePosterLinearLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //pass all movie info to next page
                        intent.putExtra("movieID",movie.getMovieID());
                        intent.putExtra("movieName",movie.getMovieName());
                        intent.putExtra("moviePosterURL",movie.getMoviePosterURL());
                        intent.putExtra("trailerURL",movie.getTrailerURL());
                        intent.putExtra("synopsis",movie.getSynopsis());
                        intent.putStringArrayListExtra("genres", (ArrayList<String>) movie.getGenre());
                        intent.putStringArrayListExtra("actors", (ArrayList<String>) movie.getActor());
                        intent.putExtra("runtime",movie.getRuntime());

                        //to check if user run already started or ended
                        intent.putExtra("startDateSeconds",movie.getStartDate().getSeconds());
                        intent.putExtra("endDateSeconds",movie.getEndDate().getSeconds());

                        if(movie.getMoviePosterURL().isEmpty() || movie.getMovieID().isEmpty() ||
                                movie.getMovieName().isEmpty() || movie.getTrailerURL().isEmpty() ||
                                movie.getSynopsis().isEmpty() || movie.getGenre().isEmpty() ||
                                movie.getActor().isEmpty() || movie.getRuntime() == 0){
                            Toast.makeText(adapterContext,"Movie not yet fully loaded.",Toast.LENGTH_LONG).show();
                        }
                        else {
                            adapterContext.startActivity(intent);
                            adapterActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                        }

                        //ensures that if user goes to another movie detail page, it wont keep old detail page
                        // or else it would be confusing
                        if(adapterContext instanceof MovieDetailActivity){
                            adapterActivity.finish();
                        }
                    }
                });
            }
        });

        StorageReference imageRef = mFireStorage.getReference(movie.getMoviePosterURL());
        imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                String imageURL = uri.toString();
                Glide.with(adapterContext).load(imageURL)
                        .override(227,336)
                        .into(holder.posterImageView);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Toast.makeText(adapterContext, "Error retrieving images.", Toast.LENGTH_SHORT).show();
                Log.d("Eror", "Firebase storage error: " + exception);
            }
        });
    }

    // Return the size of your dataset
    @Override
    public int getItemCount() {
        return movieDataset.size();
    }

}

//        final long ONE_MEGABYTE = 227 * 336;
//        imageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
//            @Override
//            public void onSuccess(byte[] bytes) {
//                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                DisplayMetrics dm = new DisplayMetrics();
//                adapterActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//                holder.posterImageView.setMinimumHeight(dm.heightPixels);
//                holder.posterImageView.setMinimumWidth(dm.widthPixels);
//                holder.posterImageView.setImageBitmap(bm);
//            }
//        }).addOnFailureListener(new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(adapterContext, "Error retrieving images.", Toast.LENGTH_SHORT).show();
//            }
//        });
