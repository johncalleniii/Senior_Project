package com.example.grift.fitnessfiend;

import android.content.Intent;
import android.os.Bundle;
import android.text.util.Linkify;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RecipeActivity extends AppCompatActivity {
    private TextView tvRecipeName;
    private TextView tvLink;
    private ImageView ivRecipe;
    private RecyclerView rvIngredients;
    private RecyclerView rvNutrients;
    private RecipeListItem r;
    private List<String> ingredientList = new ArrayList<>();
    private List<RecipeListItem.Nutrient> nutrientList = new ArrayList<>();
    private IngredientAdapter ingAdapter;
    private NutrientAdapter nutAdapter;
    private CheckBox cbLiked;
    private DatabaseReference likedReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);
        //Controls
        tvRecipeName = findViewById(R.id.tvRecipeName);
        tvLink = findViewById(R.id.tvLink);
        ivRecipe = findViewById(R.id.ivRecipe);
        rvIngredients = findViewById(R.id.rvIngredients);
        rvNutrients = findViewById(R.id.rvNutrients);
        cbLiked = findViewById(R.id.cbLiked);
        //Database Stuff
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            likedReference = FirebaseDatabase.getInstance().getReference().child("users").child(
                    Objects.requireNonNull(email).substring(0, email.indexOf('@'))).child("recipeLiked");
        }
        //getting recipe
        Intent i = getIntent();
        r = ( RecipeListItem) i.getParcelableExtra("recipe");
        //Easy Variable Stuff
        tvRecipeName.setText(r.getName());
        Picasso.get().load(r.getPicture()).into(ivRecipe);
        tvLink.setText(r.getSource());
        ingAdapter = new IngredientAdapter(ingredientList);
        //Ingredient RecyclerView
        for (int j = 0; j <r.getIngredientsList().size(); j++){
            ingredientList.add(r.getIngredient(j));
        }
        ingAdapter =  new IngredientAdapter(ingredientList);
        RecyclerView.LayoutManager ingLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvIngredients.setLayoutManager(ingLayoutManager);
        rvIngredients.setItemAnimator(new DefaultItemAnimator());
        rvIngredients.setAdapter(ingAdapter);
        ingAdapter.notifyDataSetChanged();
        //Nutrient RecyclerView
        for (int j = 0; j<r.getNutrientsList().size(); j++){
            nutrientList.add(r.getNutrient(j));
        }
        nutAdapter = new NutrientAdapter(nutrientList);
        RecyclerView.LayoutManager nutLayoutManager = new LinearLayoutManager(getApplicationContext());
        rvNutrients.setLayoutManager(nutLayoutManager);
        rvNutrients.setItemAnimator(new DefaultItemAnimator());
        rvNutrients.setAdapter(nutAdapter);
        nutAdapter.notifyDataSetChanged();
        //Link Stuff
        Linkify.addLinks(tvLink,Linkify.ALL);
        //Checking if recipe is liked on opening screen
        likedReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(r.getName())) cbLiked.setChecked(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Adding a liked history
        cbLiked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    likedReference.child(r.getName()).setValue(r);
                }
                else {
                    likedReference.child(r.getName()).removeValue();
                }
            }
        });
    }
}