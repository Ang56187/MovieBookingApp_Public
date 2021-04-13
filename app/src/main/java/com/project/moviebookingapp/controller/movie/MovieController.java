package com.project.moviebookingapp.controller.movie;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.firebase.ui.firestore.SnapshotParser;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;
import com.project.moviebookingapp.enumeration.BtnType;
import com.project.moviebookingapp.listener.OnFirebaseCallback;
import com.project.moviebookingapp.model.Movie;
import com.project.moviebookingapp.model.Showtime;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static com.project.moviebookingapp.custom.TextViewFormat.getCompareDateTime;
import static com.project.moviebookingapp.custom.TextViewFormat.getLocalDateFormat;

public class MovieController {
    private FirebaseFirestore mFirestore;
    private Context context;
    private Showtime showtime ;
    private SharedPreferences app_preferences;

    public MovieController(Context context){
        this.context = context;
        mFirestore = FirebaseFirestore.getInstance();
        showtime = new Showtime();
        app_preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }//constructor

    ///lesson learned:
    //only use cache if retrieval is not time sensitive
    //as if document changed, cache will keep the old ver

    //link to query directly
    //for home fragment
    public FirestoreRecyclerOptions<Movie> queryNoMovieDocuments(){
        Query query = mFirestore.collection("movies")
                .whereEqualTo(FieldPath.documentId(),"0");
        FirestoreRecyclerOptions<Movie> options =
                new FirestoreRecyclerOptions.Builder<Movie>().setQuery(query, new SnapshotParser<Movie>() {
                    @NonNull
                    @Override
                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Movie movie = setMovieSnapshots(snapshot);
                        movie.setMovieID(snapshot.getId());
                        return movie;
                    }
                }).build();

        return options;
    }


    //get all movies available
    public FirestoreRecyclerOptions<Movie> queryMovieDocuments(){
        Query query = mFirestore.collection("movies");
        FirestoreRecyclerOptions<Movie> options =
                new FirestoreRecyclerOptions.Builder<Movie>().setQuery(query, new SnapshotParser<Movie>() {
                    @NonNull
                    @Override
                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Movie movie = setMovieSnapshots(snapshot);
                        movie.setMovieID(snapshot.getId());
                        return movie;
                    }
                }).build();

        return options;
    }

    //overlapped
    public void queryMovieDocuments(OnFirebaseCallback callback){
        mFirestore.collection("movies").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            ArrayList<Movie> movieList = new ArrayList<>();
                            for(QueryDocumentSnapshot doc: task.getResult()){
                                movieList.add(setMovieSnapshots(doc));
                            }
                            callback.firebaseCallBack(movieList);
                        }
                    }
                });

    }



    //for home fragment
    public FirestoreRecyclerOptions<Movie> queryLatestMovies(OnFirebaseCallback callback){
        Query query = mFirestore.collection("movies")
                .whereLessThanOrEqualTo("startDate",new Timestamp(new Date()));

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> movieIDList = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                    if(doc.getTimestamp("startDate").toDate().compareTo(new Date()) <= 0
                            && doc.getTimestamp("endDate").toDate().compareTo(new Date()) >= 0
                            && movieIDList.size()<6) {
                        movieIDList.add(doc.getId());
                    }
                }//end for

                Query secondQuery;
                if(movieIDList.size()>0){
                    secondQuery =  mFirestore.collection("movies")
                            .whereIn(FieldPath.documentId(),movieIDList)
                            .orderBy("startDate", Query.Direction.DESCENDING)
                            .limit(5);
                }
                else{
                    secondQuery =  mFirestore.collection("movies")
                            .whereIn(FieldPath.documentId(), Arrays.asList("o")).limit(5);
                }

                FirestoreRecyclerOptions<Movie> options =
                        new FirestoreRecyclerOptions.Builder<Movie>().setQuery(secondQuery,
                                new SnapshotParser<Movie>() {
                                    @NonNull @Override
                                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                                        Movie movie = setMovieSnapshots(snapshot);
                                        movie.setMovieID(snapshot.getId());
                                        return movie;
                                    }
                                }).build();
                callback.firebaseCallBack(options);
            }
        });

        FirestoreRecyclerOptions<Movie> options =
                new FirestoreRecyclerOptions.Builder<Movie>().setQuery(query, new SnapshotParser<Movie>() {
                    @NonNull @Override
                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Movie movie = setMovieSnapshots(snapshot);
                        movie.setMovieID(snapshot.getId());
                        return movie;
                    }
                }).build();

        return options;
    }



    //for home fragment
    public FirestoreRecyclerOptions<Movie> queryShowingNowMovies(OnFirebaseCallback callback){
        Query query = mFirestore.collection("movies")
                .whereLessThanOrEqualTo("startDate",new Timestamp(new Date()));

        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<String> movieIDList = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                        if(doc.getTimestamp("startDate").toDate().compareTo(new Date()) <= 0
                                && doc.getTimestamp("endDate").toDate()
                                .compareTo(new Date()) >= 0 && movieIDList.size()<10) {
                            movieIDList.add(doc.getId());
                        }
                }//end for

                Query secondQuery;
                if(movieIDList.size()>0){
                    secondQuery =  mFirestore.collection("movies")
                            .whereIn(FieldPath.documentId(),movieIDList).limit(10);
                }
                else{
                    secondQuery =  mFirestore.collection("movies")
                            .whereIn(FieldPath.documentId(), Arrays.asList("o")).limit(10);
                }

                FirestoreRecyclerOptions<Movie> options =
                        new FirestoreRecyclerOptions.Builder<Movie>().setQuery(secondQuery,
                                new SnapshotParser<Movie>() {
                                    @NonNull
                                    @Override
                                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                                        Movie movie = setMovieSnapshots(snapshot);
                                        movie.setMovieID(snapshot.getId());
                                        return movie;
                                    }
                                }).build();
                callback.firebaseCallBack(options);
            }
        });

        FirestoreRecyclerOptions<Movie> options =
                new FirestoreRecyclerOptions.Builder<Movie>().setQuery(query, new SnapshotParser<Movie>() {
                    @NonNull
                    @Override
                    public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                        Movie movie = setMovieSnapshots(snapshot);
                        movie.setMovieID(snapshot.getId());
                        return movie;
                    }
                }).build();

        return options;
    }


    ///////////////////////////////for profiles
    private int movieIDCount = 0;
    public void queryMoviesByStatus(OnFirebaseCallback callback){
        Query query = mFirestore.collection("tickets")
                    .whereEqualTo("accountID",
                            app_preferences.getString("accountID", " "));

        //first query (get tickets to get movie id "key")
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<Movie> movieList = new ArrayList<>();
                    //store all movies
                    ArrayList<String> movieIDList = new ArrayList<>();
                    //store movies that are already seen
                    ArrayList<String> usedMovieIDList = new ArrayList<>();

                    int arrSize = task.getResult().size();

                    //seperate tickets by isUsed status
                    for(QueryDocumentSnapshot doc:task.getResult()){
                        if(doc.getBoolean("isUsed")){
                            movieIDList.add(doc.getString("movieID"));
                            usedMovieIDList.add(doc.getString("movieID"));
                        }
                        else{
                            movieIDList.add(doc.getString("movieID"));
                        }
                    }//end for

                    List<String> distinctMovieIDList = movieIDList.stream().distinct().
                            collect(Collectors.toList());
                    List<String> distinctUsedMovieIDList = usedMovieIDList.stream().distinct().
                            collect(Collectors.toList());

                    movieIDCount = 0;
                    //first add movies booked but not yet seen
                    for(String id: distinctMovieIDList){
                        //second query (find movie docs that matches the movie id)
                        mFirestore.collection("movies")
                                .document(id)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            Movie movie = setMovieSnapshots(task.getResult());
                                            //for currently booked, it removes tickets that are
                                            // already used by user to prevent duplication of movies
                                            //(if movie appeared at "last seen", movie wont be shown
                                            // at "currently shown" again)
                                            if(distinctUsedMovieIDList.contains(id)){
                                                movie.setUsed(true);
                                            }
                                            else{
                                                movie.setUsed(false);
                                            }
                                            movieList.add(movie);
                                            if(movieIDCount == (distinctMovieIDList.size()-1)){
                                                callback.firebaseCallBack(movieList);
                                            }
                                            movieIDCount++;
                                        }else{
                                            Toast.makeText(context, "Error getting documents: "
                                                            + task.getException(),
                                                    Toast.LENGTH_LONG).show();
                                            Log.d("Error", "Error getting documents: ",
                                                    task.getException());
                                        }
                                    }
                                });
                    }//end first loop

                }else{
                    try {
                        throw task.getException();
                    } catch (FirebaseNetworkException e) {
                        Toast.makeText(context, "Network connection interrupted.",
                                Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }
                }
            }//end if
        });
    }


    private int favouriteMovieCount = 0;
    public void queryFavouriteMovies(OnFirebaseCallback callback){
        //first query, find all favourites by user first
        favouriteMovieCount = 0;
        mFirestore.collection("favourites")
                .whereEqualTo("accountID",app_preferences.getString("accountID"," "))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<Movie> movieList = new ArrayList<>();

                    //checks if no results, then 0
                    if(task.getResult().size() == 0 ){
                        callback.firebaseCallBack(movieList);
                    }

                    for(DocumentSnapshot doc:task.getResult()){
                        mFirestore.collection("movies")
                                .document(doc.getString("movieID")).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            Movie movie = setMovieSnapshots(task.getResult());
                                            movieList.add(movie);
                                            if(favouriteMovieCount == movieList.size()-1){
                                                callback.firebaseCallBack(movieList);
                                            }
                                            favouriteMovieCount++;
                                        }else{
                                            Toast.makeText(context, "Error getting documents: "
                                                            + task.getException(),
                                                    Toast.LENGTH_LONG).show();
                                            Log.d("Error",
                                                    "Error getting documents: ",
                                                    task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }

    private int reviewedMovieCount = 0;
    public void queryReviewedMovies(OnFirebaseCallback callback) {
        //first query, find all favourites by user first
        reviewedMovieCount = 0;
        mFirestore.collection("ratings")
                .whereEqualTo("accountID",app_preferences.getString("accountID"," "))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    ArrayList<Movie> movieList = new ArrayList<>();

                    //checks if no results, then 0
                    if(task.getResult().size() == 0 ){
                        callback.firebaseCallBack(movieList);
                    }

                    for(DocumentSnapshot doc:task.getResult()){
                        mFirestore.collection("movies")
                                .document(doc.getString("movieID")).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            Movie movie = setMovieSnapshots(task.getResult());
                                            movieList.add(movie);
                                            if(reviewedMovieCount == movieList.size()-1){
                                                callback.firebaseCallBack(movieList);
                                            }
                                            reviewedMovieCount++;
                                        }else{
                                            Toast.makeText(context, "Error getting documents: "
                                                            + task.getException(),
                                                    Toast.LENGTH_LONG).show();
                                            Log.d("Error",
                                                    "Error getting documents: ",
                                                    task.getException());
                                        }
                                    }
                                });
                    }
                }
            }
        });
    }


    public void querySearchMovieDocuments(String searchInput, BtnType btntype,
                                                 ArrayList<String> genreList,
                                                 OnFirebaseCallback callback) {
        Query query = setGenreSearchQuery(searchInput,genreList);


        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Movie> movieList = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                    switch(btntype){
                        case SHOWINGNOW:
                            if(doc.getTimestamp("startDate").toDate()
                                    .compareTo(new Date()) <= 0 && doc.getTimestamp("endDate")
                                    .toDate().compareTo(new Date()) >= 0) {
                                movieList.add(setMovieSnapshots(doc));
                            }
                            break;
                        case COMINGSOON:
                            if(doc.getTimestamp("startDate").toDate().compareTo(new Date())>0){
                                movieList.add(setMovieSnapshots(doc));
                            }
                            break;
                        case NOTSHOWING:
                            if(doc.getTimestamp("endDate").toDate().compareTo(new Date())<0){
                                movieList.add(setMovieSnapshots(doc));
                            }
                            break;
                        default:
                            break;
                    }
                }//end for
                callback.firebaseCallBack(movieList);
            }
        });
    }//end queryDefaultSearchMovieDocuments


    //overlapped for date select
    private Date selectedTime;
    public void querySearchMovieDocuments(
            String searchInput, ArrayList<String> genreList,
            Date selectedDate,OnFirebaseCallback callback){
        String dateString = getLocalDateFormat().format(selectedDate);
        selectedTime = selectedDate;

        //get showtimes in that date
        try {
            selectedTime = getCompareDateTime().parse(dateString + " 00:00:00");
        }
        catch (ParseException e){
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }
        Query query = setGenreSearchQuery(searchInput,genreList);

        query.get(Source.CACHE).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                ArrayList<Movie> movieList = new ArrayList<>();
                for(QueryDocumentSnapshot doc: task.getResult()){
                    if(doc.getTimestamp("startDate").toDate().compareTo(selectedTime) <= 0 &&
                    doc.getTimestamp("endDate").toDate().compareTo(selectedTime) >= 0){
                        movieList.add(setMovieSnapshots(doc));
                    }
                }
                callback.firebaseCallBack(movieList);
            }
        });

    }


    //get all dates
    //to show movie showtime date toggle
    public FirestoreRecyclerOptions<Showtime> queryShowDateDocuments(String movieID,
                                                                     ArrayList<String> dateList){

        Query query = mFirestore.collection("showtimes")
                .whereEqualTo("movieID",movieID)
                .whereGreaterThan("startTime",new Timestamp(new Date()));

        dateList.clear();
        FirestoreRecyclerOptions<Showtime> options
                =  new FirestoreRecyclerOptions.Builder<Showtime>().setQuery(query,
                new SnapshotParser<Showtime>() {
            @NonNull
            @Override
            public Showtime parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                String dateString = getLocalDateFormat().format(snapshot.getTimestamp("startTime").toDate());
                if(dateList.contains(dateString)){
                    showtime = new Showtime();
                }
                else{
                    dateList.add(dateString);
                    showtime = new Showtime((String)snapshot.get("hallID"),
                            (String)snapshot.get("movieID"),
                            (Timestamp)snapshot.get("startTime"),
                            (Double)snapshot.getLong("price").doubleValue());
                }
                return showtime;
            }
        }).build();
        return options;
    }

    //to check if movie have showdates
    public void retrieveShowdates(String movieID,OnFirebaseCallback callback){
        Query query = mFirestore.collection("showtimes")
                .whereEqualTo("movieID",movieID)
                .whereGreaterThan("startTime",new Timestamp(new Date()));
        query.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                callback.firebaseCallBack(queryDocumentSnapshots.getDocuments().size());
            }
        });
    }

    //to show movie showtime time toggle
    public FirestoreRecyclerOptions<Showtime> queryShowTimeDocuments(String movieID,Timestamp time){
        String dateString = getLocalDateFormat().format(time.toDate());
        Date startTime = time.toDate();
        Date endTime = time.toDate();

        try {
            startTime = getLocalDateFormat().parse(dateString);
            endTime = getCompareDateTime().parse(dateString + " 23:59:59");
        }
        catch (ParseException e){
            Toast.makeText(context,e.toString(),Toast.LENGTH_LONG).show();
        }

        Query query;
        //checks for showtimes that are before the current date
        if(startTime.compareTo(new Date()) <= 0){
            query = mFirestore.collection("showtimes")
                    .whereEqualTo("movieID",movieID)
                    .whereGreaterThan("startTime",new Timestamp(new Date()))
                    .whereLessThanOrEqualTo("startTime",new Timestamp(endTime));
        }
        else{
            query = mFirestore.collection("showtimes")
                    .whereEqualTo("movieID",movieID)
                    .whereGreaterThan("startTime",new Timestamp(startTime))
                    .whereLessThan("startTime",new Timestamp(endTime));
        }

        FirestoreRecyclerOptions<Showtime> options
                =  new FirestoreRecyclerOptions.Builder<Showtime>().setQuery(query,
                Showtime.class).build();
        return options;
    }

    //to get recommended related movies for each movie
    public void queryRecommendedMovies(String collectionName,String id,OnFirebaseCallback callback){
        mFirestore.collection(collectionName).document(id).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        //ensure only users with recommendations query it
                        if((documentSnapshot.get("recommendedList") == null)) {
                            callback.firebaseCallBack(null);
                        }
                        else if(((List<String>)documentSnapshot.get("recommendedList")).isEmpty()){
                            callback.firebaseCallBack(null);
                        }
                        else{
                            List<String> recommendedList = (List<String>)
                                    documentSnapshot.get("recommendedList");
                            Query query = mFirestore.collection("movies")
                                    .whereIn(FieldPath.documentId(),recommendedList);
                            FirestoreRecyclerOptions<Movie> options =
                                    new FirestoreRecyclerOptions.Builder<Movie>().setQuery(query,
                                            new SnapshotParser<Movie>() {
                                                @NonNull @Override
                                                public Movie parseSnapshot(@NonNull DocumentSnapshot snapshot) {
                                                    Movie movie = setMovieSnapshots(snapshot);
                                                    movie.setMovieID(snapshot.getId());
                                                    return movie;
                                                }
                                            }).build();
                            callback.firebaseCallBack(options);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(context,e.toString(),Toast.LENGTH_SHORT).show();
                        Log.d("Error",e.toString());
                    }
                });
    }

    ////uses callbacks
    //get all ratings
    public void retrieveRatings(String movieID , OnFirebaseCallback callback){
        mFirestore.collection("ratings").whereEqualTo("movieID",
                movieID).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            ArrayList<Double> ratingScoreList = new ArrayList<>();
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                ratingScoreList.add(document.getDouble("ratingScore"));
                            }
                            callback.firebaseCallBack(ratingScoreList);
                        } else {
                            Log.d("Doc ", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }//end firebaseoperation



    public Query setGenreSearchQuery(String searchInput,ArrayList<String> genreList){
        Query query;
        if (genreList.size() > 0) {
            query = mFirestore.collection("movies")
                    .whereArrayContainsAny("genre", genreList)
                    .orderBy("movieName_lowerCase")
                    .startAt(searchInput)
                    .endAt(searchInput + "\uf8ff");
        } else {
            query = mFirestore.collection("movies")
                    .orderBy("movieName_lowerCase")
                    .startAt(searchInput)
                    .endAt(searchInput + "\uf8ff");
        }
        return query;
    }

    public Movie setMovieSnapshots(DocumentSnapshot snapshot){
        Movie movie = new Movie((List<String>) snapshot.get("actor"),
                (List<String>) snapshot.get("genre"), snapshot.getString("director"),
                snapshot.getTimestamp("startDate"), snapshot.getTimestamp("endDate"),
                snapshot.getString("movieName"), snapshot.getString("moviePosterURL"),
                ((Long) snapshot.get("runtime")).intValue(), snapshot.getString("synopsis"),
                snapshot.getString("trailerURL"));
        movie.setMovieID(snapshot.getId());
        return movie;
    }
}
