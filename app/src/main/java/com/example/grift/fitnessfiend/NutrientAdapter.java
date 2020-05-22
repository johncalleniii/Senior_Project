package com.example.grift.fitnessfiend;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NutrientAdapter extends RecyclerView.Adapter<NutrientAdapter.MyViewHolder> {

    private List<RecipeListItem.Nutrient> nutrientList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView tvName;
        public TextView tvQuantity;
        public TextView tvUnit;

        public MyViewHolder(View view) {
            super(view);
            tvName = view.findViewById(R.id.tvName);
            tvQuantity = view.findViewById(R.id.tvQuantity);
            tvUnit = view.findViewById(R.id.tvUnit);

        }
    }

    public NutrientAdapter(List<RecipeListItem.Nutrient> nutrientList) {
        this.nutrientList = nutrientList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_nutrient, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        RecipeListItem.Nutrient nutrient = nutrientList.get(position);

        holder.tvName.setText(nutrient.getName());
        holder.tvQuantity.setText(Integer.toString((int) (nutrient.getQuantity())));
        holder.tvUnit.setText(nutrient.getUnit());
    }

    @Override
    public int getItemCount() {
        return nutrientList.size();
    }

    public void setData(List<RecipeListItem.Nutrient> nutrientList) {
        this.nutrientList = nutrientList;
        notifyDataSetChanged();
    }


}
