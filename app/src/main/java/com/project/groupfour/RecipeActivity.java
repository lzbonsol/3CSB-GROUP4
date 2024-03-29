package com.project.groupfour;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class RecipeActivity extends AppCompatActivity {
    private ImageView recipeImg;
    private TextView recipeName;
    private RatingBar recipeRating;
    private TextView  prepTime;
    private TextView recipeIngred;
    private TextView recipe;
    private Button editRecipe;

    String RecipeKey;
    String img;
    String rName;
    String rate;
    String pTime;
    String ingred;
    String recip;

    ArrayList<String> recipeData = new ArrayList<>();

    Toolbar toolbar;
    private TextView toolName;
    String favItem;
    boolean checkFav;

    DatabaseReference RecipeRef;
    DatabaseReference UserRef;
    FirebaseAuth mAuth;

    DatabaseReference ref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        //setup toolbar
        toolbar = (Toolbar) findViewById(R.id.recipe_toolbar);
        setSupportActionBar(toolbar);
        toolName = findViewById(R.id.recipe_toolbar_name);

        //adding the back arrow in toolbar
        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        recipeImg = findViewById(R.id.recipe_image_view);
        recipeName = findViewById(R.id.txt_recipe_name);
        recipeRating = findViewById(R.id.recipe_rating_view);
        prepTime = findViewById(R.id.txt_prep_time);
        recipeIngred = findViewById(R.id.txt_ingredview);
        recipe = findViewById(R.id.txt_recipe_procedure);
        editRecipe = findViewById(R.id.edit_button);
        SharedPreferences sp = getSharedPreferences("prefs", MODE_PRIVATE);

        if(sp.getString("role", "").equals("Admin")){
            editRecipe.setVisibility(View.VISIBLE);
        }

        RecipeRef = FirebaseDatabase.getInstance().getReference().child("Recipes");
        UserRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mAuth = FirebaseAuth.getInstance();

        RecipeKey = getIntent().getStringExtra("RecipeKey");

        RecipeRef.child(RecipeKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    img = dataSnapshot.child("imageUrl").getValue().toString();
                    rName = dataSnapshot.child("recipeName").getValue().toString();
                    rate = dataSnapshot.child("recipeRating").getValue().toString();
                    pTime = dataSnapshot.child("prepTime").getValue().toString();
                    ingred = dataSnapshot.child("ingredients").getValue().toString();
                    recip = dataSnapshot.child("recipe").getValue().toString();

                    String tb = rName;
                    favItem = rName;

                    Picasso.get().load(img).into(recipeImg);
                    recipeName.setText(rName);
                    recipeRating.setRating(Float.parseFloat(rate));
                    prepTime.setText(pTime);
                    recipeIngred.setText(ingred);
                    recipe.setText(recip);

                    toolName.setText(tb);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void addToFavorites(View v) {
        UserRef.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String UserKey = getIntent().getStringExtra("RecipeKey");
                checkFav = dataSnapshot.child("Favorites").hasChild(UserKey);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        String UserKey = getIntent().getStringExtra("RecipeKey");
        HashMap<String, Object> map = new HashMap<>();
        //map.put(favItem, UserKey);
        map.put("recipeID", UserKey);
        map.put("recipeName", favItem);
        map.put("imageUrl", img);
        if(!checkFav){
            Toast.makeText(this, "Added to favorites", Toast.LENGTH_SHORT).show();
            UserRef.child(mAuth.getCurrentUser().getUid()).child("Favorites").child(UserKey).updateChildren(map);
        }else{
            Toast.makeText(this, "Removed from favorites", Toast.LENGTH_SHORT).show();
            UserRef.child(mAuth.getCurrentUser().getUid()).child("Favorites").child(UserKey).removeValue();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
        }
        return super.onOptionsItemSelected(item);
    }

    public void editRecipe(View v){
        //Toast.makeText(this, "Hi!", Toast.LENGTH_SHORT).show();
        recipeData.add(RecipeKey);
        recipeData.add(img);
        recipeData.add(rName);
        recipeData.add(rate);
        recipeData.add(pTime);
        recipeData.add(ingred);
        recipeData.add(recip);
        Intent i = new Intent(this, AddRecipeActivity.class);
        i.putExtra("FROM_ACTIVITY", "RecipeActivity");
        i.putExtra("RECIPE_DATA", recipeData);
        startActivity(i);
    }
}