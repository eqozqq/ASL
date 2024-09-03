package net.eqozqq.alphastonelauncher;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class StatisticsActivity extends Activity {

    private static final String TAG = "StatisticsActivity";
    private ListView playersListView;
    private ArrayAdapter<String> adapter;
    private List<PlayerStats> playerStatsList;
    private static final String STATS_URL = "http://lmc.nostalgiaforum.xyz/player_statistics.json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Holo_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        getActionBar().setTitle("Players statistics");

        playersListView = findViewById(R.id.players_listview);
        adapter = new ArrayAdapter<>(this, R.layout.item_player, R.id.player_name);
        playersListView.setAdapter(adapter);

        fetchPlayerStatistics();

        playersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                PlayerStats selectedPlayer = playerStatsList.get(position);
                showPlayerDetails(selectedPlayer);
            }
        });
    }

    private void fetchPlayerStatistics() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder().url(STATS_URL).build();

                try (Response response = client.newCall(request).execute()) {
                    if (response.isSuccessful()) {
                        String jsonData = response.body().string();
                        Type listType = new TypeToken<List<PlayerStats>>() {}.getType();
                        playerStatsList = new Gson().fromJson(jsonData, listType);
                        updateUI();
                    } else {
                        Log.e(TAG, "Failed to fetch player statistics. HTTP code: " + response.code());
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error fetching player statistics", e);
                }
            }
        }).start();
    }

    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.clear();
                if (playerStatsList != null) {
                    for (PlayerStats stats : playerStatsList) {
                        adapter.add(stats.username);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showPlayerDetails(PlayerStats stats) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Dialog);
        builder.setTitle(stats.username)
                .setMessage("Placed blocks: " + stats.placed_blocks + "\n" +
                        "Destroyed blocks: " + stats.destroyed_blocks + "\n" +
                        "Playtime: " + stats.playtime + " seconds")
                .setPositiveButton("Close", null)
                .create()
                .show();
    }

    private static class PlayerStats {
        String username;
        int placed_blocks;
        int destroyed_blocks;
        int playtime;
    }
}
