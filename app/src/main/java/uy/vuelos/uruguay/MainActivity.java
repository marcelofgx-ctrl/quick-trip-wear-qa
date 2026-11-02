package uy.vuelos.uruguay;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class MainActivity extends Activity {
    private static final int NAVY = 0xFF081C30;
    private static final int TEAL = 0xFF36D4AD;
    private static final int PALE = 0xFFF0F6F8;
    private static final String ARRIVALS = "https://www.avionio.com/widget/es/mvd/arrivals?style=3";
    private static final String DEPARTURES = "https://www.avionio.com/widget/es/mvd/departures?style=3";
    private int mode = 0;
    private Button btnArrivals, btnDepartures;
    private TextView title, clock, status, error;
    private ProgressBar progress;
    private WebView panel;

    @Override public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        createUI();
        mode = bundle == null ? 0 : bundle.getInt("mode", 0);
        choose(mode);
    }

    private int dp(int n) {
        return Math.round(n * getResources().getDisplayMetrics().density);
    }

    private TextView text(String content, int sp, int color, boolean bold) {
        TextView x = new TextView(this);
        x.setText(content);
        x.setTextSize(sp);
        x.setTextColor(color);
        if (bold) x.setTypeface(null, android.graphics.Typeface.BOLD);
        return x;
    }

    private Button button(String caption) {
        Button b = new Button(this);
        b.setText(caption);
        b.setAllCaps(false);
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setMinHeight(0);
        b.setMinimumHeight(0);
        b.setTextSize(14);
        b.setPadding(dp(9), 0, dp(9), 0);
        return b;
    }

    private void createUI() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        LinearLayout heading = new LinearLayout(this);
        heading.setBackgroundColor(NAVY);
        heading.setGravity(Gravity.CENTER_VERTICAL);
        heading.setPadding(dp(17), dp(17), dp(12), dp(14));
        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        title = text("✈  LLEGADAS · CARRASCO", 19, Color.WHITE, true);
        clock = text("Montevideo · MVD · Uruguay", 12, 0xFFB3CCD5, false);
        clock.setPadding(0, dp(5), 0, 0);
        titles.addView(title);
        titles.addView(clock);
        heading.addView(titles, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        Button refresh = button("↻");
        refresh.setTextSize(24);
        refresh.setTextColor(Color.WHITE);
        refresh.setContentDescription("Actualizar información de vuelos");
        refresh.setBackgroundTintList(ColorStateList.valueOf(0xFF20415B));
        refresh.setOnClickListener(v -> { error.setVisibility(View.GONE); panel.reload(); });
        heading.addView(refresh, new LinearLayout.LayoutParams(dp(48), dp(43)));
        root.addView(heading);

        LinearLayout tabs = new LinearLayout(this);
        tabs.setBackgroundColor(NAVY);
        tabs.setPadding(dp(12), 0, dp(12), dp(12));
        btnArrivals = button("↓  Llegadas");
        btnDepartures = button("↑  Salidas");
        tabs.addView(btnArrivals, new LinearLayout.LayoutParams(0, dp(43), 1));
        LinearLayout.LayoutParams second = new LinearLayout.LayoutParams(0, dp(43), 1);
        second.setMargins(dp(9), 0, 0, 0);
        tabs.addView(btnDepartures, second);
        btnArrivals.setOnClickListener(v -> choose(0));
        btnDepartures.setOnClickListener(v -> choose(1));
        root.addView(tabs);

        TextView info = text("HORARIOS LOCALES · ELEGÍ LA FECHA DENTRO DEL PANEL", 11, 0xFF116F5E, true);
        info.setPadding(dp(14), dp(10), dp(14), dp(10));
        info.setBackgroundColor(0xFFEAF6F1);
        root.addView(info);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(ColorStateList.valueOf(TEAL));
        root.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        panel = new WebView(this);
        panel.setBackgroundColor(Color.WHITE);
        WebSettings web = panel.getSettings();
        web.setJavaScriptEnabled(true);
        web.setDomStorageEnabled(true);
        web.setLoadWithOverviewMode(true);
        web.setUseWideViewPort(true);
        web.setBuiltInZoomControls(true);
        web.setDisplayZoomControls(false);
        web.setAllowFileAccess(false);
        web.setAllowContentAccess(false);
        web.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        panel.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        panel.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest req) {
                if (!req.isForMainFrame()) return false;
                Uri url = req.getUrl();
                String host = url.getHost(), path = url.getPath();
                if ("https".equalsIgnoreCase(url.getScheme()) &&
                    "www.avionio.com".equalsIgnoreCase(host) &&
                    path != null && path.startsWith("/widget/es/mvd/")) return false;
                if ("https".equalsIgnoreCase(url.getScheme()) || "http".equalsIgnoreCase(url.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, url)); }
                    catch (Exception ignored) { }
                }
                return true;
            }
            @Override public void onPageStarted(WebView v, String url, android.graphics.Bitmap icon) {
                error.setVisibility(View.GONE);
                status.setText("Consultando vuelos…");
            }
            @Override public void onPageFinished(WebView v, String url) {
                status.setText("Fuente: Avionio · Datos orientativos");
            }
            @Override public void onReceivedError(WebView v, WebResourceRequest req, WebResourceError err) {
                if (req.isForMainFrame()) {
                    error.setText("No se pudo consultar la cartelera. Revisá Internet y tocá ↻.");
                    error.setVisibility(View.VISIBLE);
                    status.setText("Fuente no disponible");
                }
            }
        });
        root.addView(panel, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        error = text("", 12, 0xFFAE2933, true);
        error.setGravity(Gravity.CENTER);
        error.setBackgroundColor(0xFFFFF1F0);
        error.setPadding(dp(14), dp(9), dp(14), dp(9));
        error.setVisibility(View.GONE);
        root.addView(error);

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(dp(12), dp(10), dp(12), dp(10));
        footer.setBackgroundColor(PALE);
        status = text("Fuente: Avionio · Datos orientativos", 11, 0xFF526D7A, false);
        footer.addView(status, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView attribution = text("Avionio.com", 11, 0xFF146C93, true);
        attribution.setOnClickListener(v -> {
            try { startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.avionio.com/es"))); }
            catch (Exception ignored) { }
        });
        footer.addView(attribution);
        root.addView(footer);
        setContentView(root);
    }

    private void choose(int newMode) {
        mode = newMode == 1 ? 1 : 0;
        title.setText(mode == 0 ? "✈  LLEGADAS · CARRASCO" : "✈  SALIDAS · CARRASCO");
        SimpleDateFormat f = new SimpleDateFormat("dd MMM · HH:mm", new Locale("es", "UY"));
        f.setTimeZone(TimeZone.getTimeZone("America/Montevideo"));
        clock.setText("Montevideo · " + f.format(new Date()) + " · MVD");
        btnArrivals.setTextColor(mode == 0 ? NAVY : Color.WHITE);
        btnDepartures.setTextColor(mode == 1 ? NAVY : Color.WHITE);
        btnArrivals.setBackgroundTintList(ColorStateList.valueOf(mode == 0 ? TEAL : 0xFF20415B));
        btnDepartures.setBackgroundTintList(ColorStateList.valueOf(mode == 1 ? TEAL : 0xFF20415B));
        error.setVisibility(View.GONE);
        panel.loadUrl(mode == 0 ? ARRIVALS : DEPARTURES);
    }

    @Override protected void onSaveInstanceState(Bundle state) {
        state.putInt("mode", mode);
        super.onSaveInstanceState(state);
    }
    @Override public void onBackPressed() {
        if (panel.canGoBack()) panel.goBack(); else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        if (panel != null) panel.destroy();
        super.onDestroy();
    }
}
