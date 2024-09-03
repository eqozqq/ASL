package net.eqozqq.alphastonelauncher;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.widget.TabHost;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Holo_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabHost.TabSpec tabSpecServer = tabHost.newTabSpec("Server");
        tabSpecServer.setIndicator("Server");
        tabSpecServer.setContent(R.id.tab_server);
        tabHost.addTab(tabSpecServer);

        TabHost.TabSpec tabSpecNews = tabHost.newTabSpec("News");
        tabSpecNews.setIndicator("News");
        tabSpecNews.setContent(R.id.tab_news);
        tabHost.addTab(tabSpecNews);

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                Fragment fragment = null;

                if (tabId.equals("Server")) {
                    fragment = new ServerFragment();
                } else if (tabId.equals("News")) {
                    fragment = new NewsFragment();
                }

                if (fragment != null) {
                    FragmentManager fm = getFragmentManager();
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(android.R.id.tabcontent, fragment);
                    ft.commit();
                }
            }
        });

        tabHost.setCurrentTab(0);
        FragmentManager fm = getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(android.R.id.tabcontent, new ServerFragment());
        ft.commit();
    }
}
