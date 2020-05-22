package com.example.grift.fitnessfiend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class RecipeHistoryFragment extends Fragment {
    private EditText etHistory;
    private Button btnHistory;
    private Button btnClear;
    private RecyclerView rvHistory;
    private DatabaseReference historyReference;
    private RecipeAdapter searchAdapter;
    private List<RecipeListItem> searchList = new ArrayList<>();
    private List<RecipeListItem> recipeList = new ArrayList<>();
    private RecipeAdapter rAdapter;

    public RecipeHistoryFragment() {
        // Required empty public constructor
    }

    public static RecipeHistoryFragment newInstance() {
        RecipeHistoryFragment fragment = new RecipeHistoryFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recipe_history, container, false);
        //Controls
        etHistory = v.findViewById(R.id.etLiked);
        btnHistory = v.findViewById(R.id.btnLiked);
        btnClear = v.findViewById(R.id.btnClear);
        rvHistory = v.findViewById(R.id.rvLiked);
        //Database Setup
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            historyReference = FirebaseDatabase.getInstance().getReference().child("users").child(
                    Objects.requireNonNull(email).substring(0, email.indexOf('@'))).child("recipeHistory");
        }
        //Setup RecyclerView
        rAdapter = new RecipeAdapter(recipeList);
        rvHistory.setAdapter(rAdapter);
        rvHistory.setItemAnimator(new DefaultItemAnimator());
        rvHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        //Search Variables
        searchAdapter = new RecipeAdapter(searchList);
        //Get Database stuff
        historyReference.orderByKey().addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                try {
                    JSONObject r = new JSONObject((Map) dataSnapshot.getValue());
                    JSONArray jarrIngredients = r.getJSONArray("ingredientsList");
                    ArrayList<String> arrIngredients = new ArrayList<>();
                    for (int j = 0; j < jarrIngredients.length(); j++) {
                        arrIngredients.add(jarrIngredients.getString(j));
                    }
                    JSONArray jNutrients = r.getJSONArray("nutrientsList");
                    ArrayList<RecipeListItem.Nutrient> arrNutrients = new ArrayList<>();
                    JSONObject nut = jNutrients.getJSONObject(0);
                    arrNutrients.add(0, addNutrientToArray(nut));
                    nut = jNutrients.getJSONObject(1);
                    arrNutrients.add(1, addNutrientToArray(nut));
                    nut = jNutrients.getJSONObject(2);
                    arrNutrients.add(2, addNutrientToArray(nut));
                    nut = jNutrients.getJSONObject(3);
                    arrNutrients.add(3, addNutrientToArray(nut));
                    RecipeListItem item = new RecipeListItem(
                            r.getString("name"),
                            r.getString("picture"),
                            r.getString("source"),
                            arrNutrients,
                            arrIngredients);
                    recipeList.add(item);
                    rAdapter.notifyDataSetChanged();
                }
                catch (Exception e){e.printStackTrace();}
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        //Clicking on a recipe
        rAdapter.setOnItemClickListener(new RecipeAdapter.RecipeAdapterListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getActivity(),RecipeActivity.class);
                RecipeListItem r = recipeList.get(position);
                //Calling Recipe Activity
                i.putExtra("recipe",r);
                startActivity(i);
            }
        });
        //Clicking the search button
        btnHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 0; i< recipeList.size(); i++){
                    if (recipeList.get(i).getName().contains(etHistory.getText().toString())) {
                        searchList.add(recipeList.get(i));
                    }
                }
                rvHistory.setAdapter(searchAdapter);
                searchAdapter.notifyDataSetChanged();
            }
        });
        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rvHistory.setAdapter(rAdapter);
                rAdapter.notifyDataSetChanged();
            }
        });
        searchAdapter.setOnItemClickListener(new RecipeAdapter.RecipeAdapterListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getActivity(),RecipeActivity.class);
                RecipeListItem r = searchList.get(position);
                //Calling Recipe Activity
                i.putExtra("recipe",r);
                startActivity(i);
            }
        });

        return v;
    }
    public RecipeListItem.Nutrient addNutrientToArray(JSONObject obj){
        try {
            return new RecipeListItem.Nutrient(obj.getString("name"),obj.getDouble("quantity"),obj.getString("unit"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
