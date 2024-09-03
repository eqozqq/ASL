package net.eqozqq.alphastonelauncher;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.app.ActionBar;

public class FAQActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(android.R.style.Theme_Holo_Light);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faq);

        getActionBar().setTitle("Server plugins and their commands");

        WebView webView = findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/faq.html");
    }
}
