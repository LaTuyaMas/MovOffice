package com.movcat.movoffice.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.movcat.movoffice.GameViewActivity;
import com.movcat.movoffice.R;
import com.movcat.movoffice.constants.Constants;
import com.movcat.movoffice.models.Game;
import com.squareup.picasso.Picasso;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.GameVH>{

    private final List<Game> objects;
    private final int resources;
    private final Context context;

    public HomeAdapter(List<Game> objects, int resources, Context context) {
        this.objects = objects;
        this.resources = resources;
        this.context = context;
    }

    @NonNull
    @Override
    public GameVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GameVH(LayoutInflater.from(context).inflate(resources, null));
    }

    @Override
    public void onBindViewHolder(@NonNull GameVH holder, int position) {
        Game game = objects.get(position);
        holder.lblTitle.setText(game.getName());

        Picasso.get()
                .load(game.getIcon())
                .error(R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgBanner);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GameViewActivity.class);
                intent.putExtra(Constants.gameKey, game.getId());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    public static class GameVH extends RecyclerView.ViewHolder {
        ImageView imgBanner;
        TextView lblTitle;

        public GameVH(@NonNull View itemView) {
            super(itemView);

            imgBanner = itemView.findViewById(R.id.imgBannerCard);
            lblTitle = itemView.findViewById(R.id.lblTitleCard);
        }
    }
}
