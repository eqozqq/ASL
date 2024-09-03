package net.eqozqq.alphastonelauncher;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;

public class NewsFragment extends Fragment {
    private static final String TAG = "NewsFragment";
    private ListView newsListView;
    private ArrayAdapter<NewsItem> adapter;
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private static final String NEWS_URL = "http://lmc.nostalgiaforum.xyz/news.json";
    private boolean isErrorDialogShown = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);
        newsListView = view.findViewById(R.id.news_listview);

        adapter = new NewsAdapter(getActivity());
        newsListView.setAdapter(adapter);

        fetchNews();

        return view;
    }

    private void fetchNews() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://lmc.nostalgiaforum.xyz/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NewsApi newsApi = retrofit.create(NewsApi.class);
        Call<List<NewsItem>> call = newsApi.getNews();

        call.enqueue(new Callback<List<NewsItem>>() {
            @Override
            public void onResponse(Call<List<NewsItem>> call, Response<List<NewsItem>> response) {
                if (response.isSuccessful()) {
                    List<NewsItem> newsItems = response.body();
                    if (newsItems != null) {
                        updateUI(newsItems);
                        isErrorDialogShown = false;
                    } else {
                        showError("No news data available.");
                    }
                } else {
                    showError("Failed to fetch news. Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<NewsItem>> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void updateUI(List<NewsItem> newsItems) {
        mainHandler.post(() -> {
            if (isAdded()) {
                adapter.clear();
                adapter.addAll(newsItems);
                adapter.notifyDataSetChanged();
            }
        });
    }

    private void showError(String message) {
        if (!isErrorDialogShown) {
            isErrorDialogShown = true;
            mainHandler.post(() -> {
                if (isAdded()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), android.R.style.Theme_Holo_Dialog);
                    builder.setTitle("Error")
                            .setMessage(message)
                            .setPositiveButton("OK", (dialog, which) -> {
                                dialog.dismiss();
                                isErrorDialogShown = false;
                            })
                            .create()
                            .show();
                }
            });
        }
    }

    interface NewsApi {
        @GET("news.json")
        Call<List<NewsItem>> getNews();
    }

    static class NewsItem {
        String title;
        String time;
        String text;

        @Override
        public String toString() {
            return "NewsItem{" +
                    "title='" + title + '\'' +
                    ", time='" + time + '\'' +
                    ", text='" + text + '\'' +
                    '}';
        }
    }

    private static class NewsAdapter extends ArrayAdapter<NewsItem> {
        private final LayoutInflater inflater;

        NewsAdapter(Context context) {
            super(context, 0);
            inflater = LayoutInflater.from(context);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.news_item, parent, false);
            }

            NewsItem newsItem = getItem(position);

            if (newsItem != null) {
                TextView titleView = convertView.findViewById(R.id.news_title);
                TextView timeView = convertView.findViewById(R.id.news_time);
                TextView textView = convertView.findViewById(R.id.news_text);

                titleView.setText(newsItem.title);
                timeView.setText(newsItem.time);
                textView.setText(newsItem.text);
            }

            return convertView;
        }
    }
}
