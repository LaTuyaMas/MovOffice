package com.movcat.movoffice;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.movcat.movoffice.constants.Constants;
import com.movcat.movoffice.databinding.ActivityManagerBinding;
import com.movcat.movoffice.models.Date;
import com.movcat.movoffice.models.Game;
import com.movcat.movoffice.models.GameComment;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManagerActivity extends AppCompatActivity {
    private ActivityManagerBinding binding;
    private boolean isEdit;
    private Game viewGame;
    private List<Game> gamesList;
    private FirebaseDatabase database;
    private DatabaseReference refGamesList;
    private DatabaseReference refGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityManagerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        gamesList = new ArrayList<>();
        viewGame = new Game();

        database = FirebaseDatabase.getInstance("https://movcatalog-9d20f-default-rtdb.europe-west1.firebasedatabase.app/");
        refGamesList = database.getReference("games");

        refGamesList.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gamesList.clear();
                if (snapshot.exists()) {
                    for ( DataSnapshot gameSnapshot : snapshot.getChildren() ) {
                        Game game = gameSnapshot.getValue(Game.class);
                        gamesList.add(game);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        String gameId = getIntent().getStringExtra(Constants.gameKey);
        if (gameId == null) {
            isEdit = false;
        }
        else {
            isEdit = true;
            refGame = database.getReference("games").child(gameId);
            refGame.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    viewGame = null;
                    if (snapshot.exists()) {
                        viewGame = snapshot.getValue(Game.class);
                        fillInfo();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }

        binding.contentManager.btnConfirmManager.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                Game game = getForm();
                if (isEdit){
                    refGame.setValue(game);
                }
                else {
                    gamesList.add(0, game);
                    refGamesList.setValue(gamesList);
                }
            }
        });
    }

    private void fillInfo(){
        binding.contentManager.txtNameManager.setText(viewGame.getName());
        binding.contentManager.txtIconManager.setText(viewGame.getIcon());
        binding.contentManager.txtBannerManager.setText(viewGame.getBanner());
        binding.contentManager.txtPriceManager.setText(String.valueOf(viewGame.getPrice()));
        binding.contentManager.txtDevManager.setText(viewGame.getDevelopers().toString());
        binding.contentManager.txtPubManager.setText(viewGame.getPublishers().toString());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Game getForm() {
        Game game = new Game();
        game.setName(binding.contentManager.txtNameManager.getText().toString());
        game.setIcon(binding.contentManager.txtIconManager.getText().toString());
        game.setBanner(binding.contentManager.txtBannerManager.getText().toString());
        ArrayList<String> images = new ArrayList<>();
        game.setImages(images);
        game.setPrice(Float.parseFloat(binding.contentManager.txtPriceManager.getText().toString()));
        ArrayList<String> dev = new ArrayList<>();
        dev.add(binding.contentManager.txtDevManager.getText().toString());
        game.setDevelopers(dev);
        ArrayList<String> pub = new ArrayList<>();
        pub.add(binding.contentManager.txtPubManager.getText().toString());
        game.setPublishers(pub);

        ArrayList<String> gen = new ArrayList<>();
        if (binding.contentManager.cbSingleManager.isActivated()){
            gen.add("Singleplayer");
        }
        if (binding.contentManager.cbMultiManager.isActivated()){
            gen.add("Multiplayer");
        }
        if (binding.contentManager.cbPlatManager.isActivated()){
            gen.add("Platformer");
        }
        if (binding.contentManager.cbAdventureManager.isActivated()){
            gen.add("Adventure");
        }
        if (binding.contentManager.cbFantasyManager.isActivated()){
            gen.add("Fantasy");
        }
        if (binding.contentManager.cbRetroManager.isActivated()){
            gen.add("Retro");
        }
        game.setGenres(gen);

        ArrayList<String> con = new ArrayList<>();
        if (binding.contentManager.cbPCManager.isActivated()){
            con.add("PC");
        }
        if (binding.contentManager.cbPCManager.isActivated()){
            con.add("PS4");
        }
        if (binding.contentManager.cbPCManager.isActivated()){
            con.add("Xbox");
        }
        if (binding.contentManager.cbPCManager.isActivated()){
            con.add("Nintendo Switch");
        }
        game.setConsoles(con);

        long selectedDate = binding.contentManager.cvReleaseManager.getDate();
        java.util.Date localDate2 = new java.util.Date(selectedDate);
        Date release = new Date(localDate2.getDay(),localDate2.getMonth(),localDate2.getYear());
        game.setReleaseDate(release);

        LocalDate localdate = LocalDate.now();
        Date post = new Date(localdate.getDayOfMonth(), localdate.getMonthValue(), localdate.getYear());
        game.setPostDate(post);

        ArrayList<GameComment> comments = new ArrayList<>();
        game.setComments(comments);

        return game;
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