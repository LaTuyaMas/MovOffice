package com.movcat.movoffice;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.movcat.movoffice.adapters.GameViewAdapter;
import com.movcat.movoffice.adapters.TagsAdapter;
import com.movcat.movoffice.constants.Constants;
import com.movcat.movoffice.databinding.ActivityGameViewBinding;
import com.movcat.movoffice.models.Game;
import com.movcat.movoffice.models.GameComment;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

public class GameViewActivity extends AppCompatActivity {
    private ActivityGameViewBinding binding;
    private Game viewGame;
    private NumberFormat priceFormat;
    private List<GameComment> commentsList;
    private GameViewAdapter commentsAdapter;
    private RecyclerView.LayoutManager commentsLM;
    private List<String> tagsList;
    private TagsAdapter tagsAdapter;
    private RecyclerView.LayoutManager tagsLM;
    private FirebaseDatabase database;
    private DatabaseReference refGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityGameViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        String gameId = getIntent().getStringExtra(Constants.gameKey);
        commentsList = new ArrayList<>();
        tagsList = new ArrayList<>();
        priceFormat = NumberFormat.getCurrencyInstance();
        priceFormat.setCurrency(Currency.getInstance("EUR")); // Set currency symbol (e.g., USD)
        priceFormat.setMaximumFractionDigits(2);

        if (gameId != null) {
            database = FirebaseDatabase.getInstance("https://movcatalog-9d20f-default-rtdb.europe-west1.firebasedatabase.app/");
            refGame = database.getReference("games").child(gameId);

            commentsAdapter = new GameViewAdapter(commentsList, R.layout.comment_view_holder, this);
            int columnas;
            columnas = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 1 : 2;
            commentsLM = new GridLayoutManager(GameViewActivity.this, columnas);
            prepareFirebaseListeners();
            binding.contentGame.commentsContainer.setAdapter(commentsAdapter);
            binding.contentGame.commentsContainer.setLayoutManager(commentsLM);

            tagsAdapter = new TagsAdapter(tagsList, R.layout.tag_button_view_holder, this);
            tagsLM = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
            binding.contentGame.tagsContainer.setAdapter(tagsAdapter);
            binding.contentGame.tagsContainer.setLayoutManager(tagsLM);

            binding.contentGame.btnDeleteView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDeletion().show();
                }
            });

            binding.contentGame.btnEditView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(GameViewActivity.this, ManagerActivity.class);
                    intent.putExtra(Constants.gameKey, viewGame.getId());
                    startActivity(intent);
                }
            });
        }
    }

    private void prepareFirebaseListeners(){
        refGame.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                viewGame = null;
                commentsList.clear();
                tagsList.clear();
                if (snapshot.exists()) {
                    viewGame = snapshot.getValue(Game.class);
                    if (viewGame != null && viewGame.getComments() != null) {
                        commentsList.addAll(viewGame.getComments());
                        tagsList.addAll(viewGame.getGenres());
                    }
                    commentsAdapter.notifyDataSetChanged();
                    tagsAdapter.notifyDataSetChanged();
                    updateInfo();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void updateInfo() {
        binding.contentGame.lblNameView.setText(viewGame.getName());
        Picasso.get()
                .load(viewGame.getBanner())
                .error(R.drawable.ic_launcher_background)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(binding.contentGame.imgBannerView);

        binding.contentGame.lblDevelopersView.setText("Developers: ");
        for ( String d : viewGame.getDevelopers() ) {
            binding.contentGame.lblDevelopersView.append(d+" ");
        }

        binding.contentGame.lblPublishersView.setText("Publishers: ");
        for ( String p : viewGame.getPublishers() ) {
            binding.contentGame.lblPublishersView.append(p+" ");
        }

        binding.contentGame.lblDateView.setText(viewGame.getReleaseDate().getDay()+"/"+viewGame.getReleaseDate().getMonth()+"/"+viewGame.getReleaseDate().getYear());

        String formattedPrice = priceFormat.format(viewGame.getPrice());
        binding.contentGame.lblPriceView.setText(formattedPrice);

        binding.contentGame.lblScoreView.setText(String.valueOf(getAverageScore()));
    }

    private int getAverageScore() {
        if (commentsList.isEmpty()) {
            return 0;
        }

        int sum = 0;
        for ( GameComment c : commentsList ) {
            sum += c.getScore();
        }
        return sum / commentsList.size();
    }

    @SuppressLint("SetTextI18n")
    private AlertDialog confirmDeletion() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(GameViewActivity.this);

        builder.setCancelable(false);
        TextView mensaje = new TextView(GameViewActivity.this);
        mensaje.setText("ARE YOU SURE YOU WANT TO DELETE THIS GAME?");
        mensaje.setTextSize(20);
        mensaje.setTextColor(Color.RED);
        mensaje.setPadding(50,100,50,100);
        builder.setView(mensaje);

        builder.setNegativeButton("NO", null);
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                refGame.removeValue();
                finish();
            }
        });
        return builder.create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_game, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.back){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}