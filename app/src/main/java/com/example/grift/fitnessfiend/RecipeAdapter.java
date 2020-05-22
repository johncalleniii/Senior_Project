package com.example.grift.fitnessfiend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {
    private RecipeAdapterListener listener;
    private List<RecipeListItem> recipeList;

    interface RecipeAdapterListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(RecipeAdapterListener listener) {
        this.listener = listener;
    }


    public class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView tvRecipe;
        ImageView ivRecipe;


        public RecipeViewHolder(View view) {
            super(view);
            tvRecipe = view.findViewById(R.id.textViewRecipe);
            ivRecipe = view.findViewById(R.id.ivRecipe);

            view.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                } });

        }
    }


    public RecipeAdapter(List<RecipeListItem> recipeList) {
        this.recipeList = recipeList;
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_recipe, parent, false);

        return new RecipeViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(RecipeViewHolder holder, int position) {
        RecipeListItem recipe = recipeList.get(position);

        holder.tvRecipe.setText(recipe.getName());
        Picasso.get().load(recipe.getPicture()).into(holder.ivRecipe);
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void setData(List<RecipeListItem> recipeList) {
        this.recipeList = recipeList;
        notifyDataSetChanged();
    }

}
