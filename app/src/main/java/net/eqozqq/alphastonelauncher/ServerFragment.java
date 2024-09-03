package net.eqozqq.alphastonelauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class ServerFragment extends Fragment {
    private static final String TAG = "ServerFragment";
    private TextView playersOnlineTextView;
    private ListView playersOnlineListView;
    private ArrayAdapter<String> adapter;
    private static final int UPDATE_INTERVAL = 1000;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private boolean isErrorDialogShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server, container, false);

        playersOnlineTextView = view.findViewById(R.id.players_online_textview);
        playersOnlineListView = view.findViewById(R.id.players_online_listview);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1);
        playersOnlineListView.setAdapter(adapter);

        fetchServerData();

        Button statisticsButton = view.findViewById(R.id.button_statistics);
        statisticsButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StatisticsActivity.class);
            startActivity(intent);
        });

        Button faqButton = view.findViewById(R.id.button_faq);
        faqButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), FAQActivity.class);
            startActivity(intent);
        });

        Button launchGameButton = view.findViewById(R.id.button_launch_game);
        launchGameButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setClassName("com.mojang.minecraftpe", "com.mojang.minecraftpe.MainActivity");
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Minecraft PE is not installed", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void fetchServerData() {
        Log.d(TAG, "Fetching server data");

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Log.d(TAG, "Sending request: " + request.url());
                    okhttp3.Response response = chain.proceed(request);
                    Log.d(TAG, "Received response: " + response.code());
                    return response;
                })
                .build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://lmc.nostalgiaforum.xyz/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        ServerApi serverApi = retrofit.create(ServerApi.class);
        Call<ServerData> call = serverApi.getServerData();

        call.enqueue(new Callback<ServerData>() {
            @Override
            public void onResponse(Call<ServerData> call, Response<ServerData> response) {
                Log.d(TAG, "Server response received");
                if (response.isSuccessful()) {
                    ServerData serverData = response.body();
                    Log.d(TAG, "Server data: " + (serverData != null ? serverData.toString() : "null"));
                    if (serverData != null) {
                        updateUI(serverData);
                        isErrorDialogShown = false;
                    } else {
                        Log.e(TAG, "Server data is null");
                        showError("Received empty data from server");
                    }
                } else {
                    Log.e(TAG, "Server response not successful: " + response.code());
                    try {
                        Log.e(TAG, "Error body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showError("Server error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ServerData> call, Throwable t) {
                Log.e(TAG, "Failed to fetch server data", t);
                String errorMessage = "Network error: " + t.getMessage();
                showError(errorMessage);
            }
        });
    }

    private void updateUI(final ServerData serverData) {
        mainHandler.post(() -> {
            if (serverData != null && isAdded()) {
                playersOnlineTextView.setText("Players online (" + serverData.online + "/" + serverData.max + "):");
                adapter.clear();
                adapter.addAll(serverData.players);
                adapter.notifyDataSetChanged();
                Log.d(TAG, "UI updated with server data");
            }
        });
    }

    private void showError(final String message) {
        if (!isErrorDialogShown) {
            isErrorDialogShown = true;
            mainHandler.post(() -> {
                if (isAdded()) {
                    if (message.contains("failed to connect") && message.contains("port 80")) {

                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Dialog);
                        builder.setTitle("Error")
                                .setMessage(message)
                                .setPositiveButton("Copy", (dialog, which) -> {
                                    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                                    android.content.ClipData clip = android.content.ClipData.newPlainText("Error", message);
                                    clipboard.setPrimaryClip(clip);
                                    Toast.makeText(getActivity(), "Error copied", Toast.LENGTH_SHORT).show();
                                })
                                .setNegativeButton("Close", (dialog, which) -> {
                                    dialog.dismiss();
                                    isErrorDialogShown = false;
                                })
                                .create()
                                .show();
                    }
                }
            });
        }
    }

    interface ServerApi {
        @GET("players.json")
        Call<ServerData> getServerData();
    }

    static class ServerData {
        int online;
        int max;
        List<String> players;

        @Override
        public String toString() {
            return "ServerData{" +
                    "online=" + online +
                    ", max=" + max +
                    ", players=" + players +
                    '}';
        }
    }

    private Handler handler = new Handler();
    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            fetchServerData();
            handler.postDelayed(this, UPDATE_INTERVAL);
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(updateRunnable, UPDATE_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
    }
}
