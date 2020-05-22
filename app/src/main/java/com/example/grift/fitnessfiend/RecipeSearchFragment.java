package com.example.grift.fitnessfiend;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RecipeSearchFragment extends Fragment {
    private Button btnSearch;
    private EditText etSearch;
    private RecyclerView rvRecipes;
    private List<RecipeListItem> recipeList = new ArrayList<>();
    private RecipeAdapter mAdapter;
    private final String apiURL = "https://api.edamam.com/search?q=";
    private final String apiID = "538c2da0";
    private final String apiKey = "be2556300c684c3cb46abed351626842";
    private String q;
    private DatabaseReference historyReference;

    public RecipeSearchFragment() {
        // Required empty public constructor
    }

    public static RecipeSearchFragment newInstance() {
        RecipeSearchFragment fragment = new RecipeSearchFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recipe_search, container, false);
        //Firebase Stuff
        if (FirebaseAuth.getInstance().getCurrentUser() != null){
            String email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            historyReference = FirebaseDatabase.getInstance().getReference().child("users").child(
                    Objects.requireNonNull(email).substring(0, email.indexOf('@'))).child("recipeHistory");
        }
        //RecyclerView Stuff
        etSearch = v.findViewById(R.id.etLiked);
        btnSearch = v.findViewById(R.id.btnLiked);
        rvRecipes = v.findViewById(R.id.rvLiked);
        mAdapter = new RecipeAdapter(recipeList);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        rvRecipes.setLayoutManager(mLayoutManager);
        rvRecipes.setItemAnimator(new DefaultItemAnimator());
        rvRecipes.setAdapter(mAdapter);
        //Performing a Search
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recipeList.clear();
                if (!etSearch.getText().toString().isEmpty()) {
                    try {
                        q = etSearch.getText().toString();
                        run(q);
                    } catch (Exception e){
                        Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        //Going to the recipe
        mAdapter.setOnItemClickListener(new RecipeAdapter.RecipeAdapterListener() {
            @Override
            public void onItemClick(int position) {
                Intent i = new Intent(getActivity(),RecipeActivity.class);
                RecipeListItem r = recipeList.get(position);
                //Putting it into the history tab
                historyReference.child(r.getName()).setValue(r);
                //Calling Recipe Activity
                i.putExtra("recipe",r);
                startActivity(i);
            }
        });
        return v;
    }

    private void run(String q) throws IOException {

        OkHttpClient client = new OkHttpClient();

        HttpUrl.Builder urlBuilder = HttpUrl.parse(apiURL+q).newBuilder();
        urlBuilder.addQueryParameter("app_id",apiID);
        urlBuilder.addQueryParameter("app_key",apiKey);
        urlBuilder.addQueryParameter("from","0");
        urlBuilder.addQueryParameter("to","100");
        String url = urlBuilder.build().toString();

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Toast.makeText(getActivity().getApplicationContext(), "Error", Toast.LENGTH_SHORT).show();
                call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                final String myResponse = response.body().string();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //Getting text into reader
                            JSONObject reader = new JSONObject(myResponse);
                            //Getting Recipe Array into an array
                            JSONArray recipes = reader.getJSONArray("hits");
                            //Putting into the recyclerView
                            for (int i = 0; i < recipes.length(); i++){
                                JSONObject r = recipes.getJSONObject(i).getJSONObject("recipe");
                                JSONArray jarrIngredients = r.getJSONArray("ingredientLines");
                                ArrayList<String> arrIngredients = new ArrayList<>();
                                for (int j = 0; j < jarrIngredients.length(); j++)
                                {
                                    arrIngredients.add(jarrIngredients.getString(j));
                                }
                                JSONObject jNutrients = r.getJSONObject("totalNutrients");
                                ArrayList<RecipeListItem.Nutrient> arrNutrients = new ArrayList<>();
                                JSONObject nut = jNutrients.getJSONObject("ENERC_KCAL");
                                arrNutrients.add(0,addNutrientToArray(nut));
                                nut = jNutrients.getJSONObject("CHOCDF");
                                arrNutrients.add(1,addNutrientToArray(nut));
                                nut = jNutrients.getJSONObject("FAT");
                                arrNutrients.add(2,addNutrientToArray(nut));
                                nut = jNutrients.getJSONObject("PROCNT");
                                arrNutrients.add(3,addNutrientToArray(nut));
                                arrNutrients.get(0).setName("Calories");
                                RecipeListItem item = new RecipeListItem(
                                        r.getString("label"),
                                        r.getString("image"),
                                        r.getString("url"),
                                        arrNutrients,
                                        arrIngredients);
                                recipeList.add(item);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });

            }
        });
    }

    public RecipeListItem.Nutrient addNutrientToArray(JSONObject obj){
        try {
            return new RecipeListItem.Nutrient(obj.getString("label"),obj.getDouble("quantity"),obj.getString("unit"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}