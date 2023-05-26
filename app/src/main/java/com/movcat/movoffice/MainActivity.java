package com.movcat.movoffice;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.movcat.movoffice.adapters.HomeAdapter;
import com.movcat.movoffice.databinding.ActivityMainBinding;
import com.movcat.movoffice.models.Game;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.SearchView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private ArrayList<Game> gamesList;
    private ArrayList<Game> backupList;
    private HomeAdapter gamesAdapter;
    private RecyclerView.LayoutManager gamesLM;
    private FirebaseDatabase database;
    private DatabaseReference refGames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);

        database = FirebaseDatabase.getInstance("https://movcatalog-9d20f-default-rtdb.europe-west1.firebasedatabase.app/");
        refGames = database.getReference("games");

        gamesList = new ArrayList<>();
        backupList = new ArrayList<>();
        gamesAdapter = new HomeAdapter(gamesList, R.layout.game_view_holder, this);
        int columnas;
        columnas = getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4;
        gamesLM = new GridLayoutManager(MainActivity.this, columnas);
        binding.contentMain.gamesContainer.setAdapter(gamesAdapter);
        binding.contentMain.gamesContainer.setLayoutManager(gamesLM);

        prepareFirebaseListeners();

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    private void prepareFirebaseListeners(){
        refGames.addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                gamesList.clear();
                if (snapshot.exists()) {
                    for ( DataSnapshot gameSnapshot : snapshot.getChildren() ) {
                        Game game = gameSnapshot.getValue(Game.class);
                        backupList.add(game);
                    }
                    gamesList.addAll(backupList);
                }
                gamesAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        MenuItem item = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) item.getActionView();
        searchView.setQueryHint("Type back to return to normal");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                filterGameBySearch(s);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void filterGameBySearch(String query) {
        if (query.isEmpty() && backupList.size() != 0) {
            binding.contentMain.lblTitleHome.setText("THE ENTIRE LIST");

            gamesList.clear();
            gamesList.addAll(backupList);
            backupList.clear();
            gamesAdapter.notifyDataSetChanged();
        }
        else {
            binding.contentMain.lblTitleHome.setText("SEARCH RESULTS");

            if (backupList.size() == 0) {
                backupList.addAll(gamesList);
                gamesList.clear();
            }
            else {
                gamesList.clear();
                gamesList.addAll(backupList);
            }

            ArrayList<Game> filteredList = new ArrayList<>();
            for (Game g : gamesList) {
                if (g.getName().toLowerCase().contains(query.toLowerCase())) {
                    filteredList.add(g);
                }
            }

            gamesList.clear();
            gamesList.addAll(filteredList);
            gamesAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}