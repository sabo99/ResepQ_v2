package com.sabo.resepq_v2.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabo.resepq_v2.DetailActivity;
import com.sabo.resepq_v2.Helper.Common;
import com.sabo.resepq_v2.Helper.EventCloseSearchBar;
import com.sabo.resepq_v2.R;
import com.sabo.resepq_v2.RoomDB.RecipeModel;
import com.squareup.picasso.Picasso;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.ViewHolder> {

    private Context context;
    private List<RecipeModel> recipeModelList;

    public RecipeAdapter(Context context, List<RecipeModel> recipeModelList) {
        this.context = context;
        this.recipeModelList = recipeModelList;
    }

    @NonNull
    @Override
    public RecipeAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeAdapter.ViewHolder holder, int position) {
        RecipeModel list = recipeModelList.get(position);
        Picasso.get().load(list.getItemImage()).into(holder.ivItemImage);
        holder.tvItemName.setText(list.getItemName());
        holder.tvItemIngredient.setText(list.getItemIngredient());
        holder.tvTime.setText(list.getDatetime());

        holder.itemView.setOnClickListener(v -> {
            Common.selectedRecipe = list;
            context.startActivity(new Intent(context, DetailActivity.class));
            EventBus.getDefault().postSticky(new EventCloseSearchBar(true));
        });
    }

    @Override
    public int getItemCount() {
        return recipeModelList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView ivItemImage;
        TextView tvItemName, tvItemIngredient, tvTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvItemName = itemView.findViewById(R.id.tvItemName);
            tvItemIngredient = itemView.findViewById(R.id.tvItemIngredient);
            tvTime = itemView.findViewById(R.id.tvTime);
        }
    }
}
