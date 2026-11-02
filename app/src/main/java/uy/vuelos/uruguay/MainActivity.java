package uy.vuelos.uruguay;

import android.app.Activity;
import android.content.Intent;
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
import android.webkit.CookieManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private static final int NAVY = Color.rgb(9, 24, 42);
    private static final int DEEP = Color.rgb(17, 40, 62);
    private static final int MINT = Color.rgb(50, 214, 176);
    private static final int WHITE = Color.WHITE;
    private static final String[] NAMES = {"LLEGADAS", "SALIDAS", "PRÓXIMOS DÍAS", "RASTREO"};
    private static final String[] SOURCES = {"Aeropuerto de Carrasco · oficial", "Avionio · informativo", "Flight.info · programación", "FlightAware · seguimiento"};
    private static final String[] URLS = {
        "https://aeropuertodecarrasco.com.uy/panel-de-vuelos/",
        "https://www.avionio.com/es/airport/MVD/departures",
        "https://www.flight.info/MVD/arrivals",
        "https://es.flightaware.com/live/airport/SUMU"
    };
    private WebView browser;
    private Button[] tabs;
    private TextView sourceText;
    private ProgressBar progress;
    private int current = 0;
    private boolean loadError = false;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(NAVY);
        buildInterface();
        if (state != null) {
            current = Math.max(0, Math.min(3, state.getInt("currentTab", 0)));
            browser.restoreState(state);
            renderSelection();
            if (browser.getUrl() == null) openTab(current);
        } else {
            openTab(0);
        }
    }

    private int dp(float value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView t = new TextView(this);
        t.setText(value);
        t.setTextColor(color);
        t.setTextSize(sp);
        if (bold) t.setTypeface(null, android.graphics.Typeface.BOLD);
        return t;
    }

    private void buildInterface() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(WHITE);

        LinearLayout header = new LinearLayout(this);
        header.setBackgroundColor(NAVY);
        header.setPadding(dp(18), dp(13), dp(12), dp(12));
        header.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout heading = new LinearLayout(this);
        heading.setOrientation(LinearLayout.VERTICAL);
        TextView h1 = text("✈  VUELOS URUGUAY", 19, WHITE, true);
        TextView h2 = text("Carrasco (MVD)  ·  Horario local UY", 12, 0xFFB8CED9, false);
        heading.addView(h1);
        heading.addView(h2);
        header.addView(heading, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        Button refresh = utilityButton("↻", "Actualizar");
        refresh.setOnClickListener(v -> browser.reload());
        header.addView(refresh);
        Button outside = utilityButton("↗", "Abrir en navegador");
        outside.setOnClickListener(v -> {
            try {
                String u = browser.getUrl() == null ? URLS[current] : browser.getUrl();
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(u)));
            } catch (Exception ignored) {}
        });
        header.addView(outside);
        root.addView(header);

        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        scroll.setBackgroundColor(DEEP);
        LinearLayout nav = new LinearLayout(this);
        nav.setPadding(dp(10), dp(5), dp(10), dp(7));
        tabs = new Button[NAMES.length];
        for (int i=0; i<NAMES.length; i++) {
            final int index = i;
            Button b = new Button(this);
            b.setAllCaps(false);
            b.setText(NAMES[i]);
            b.setTextSize(11);
            b.setSingleLine(true);
            b.setMinWidth(0);
            b.setMinimumWidth(0);
            b.setMinHeight(0);
            b.setMinimumHeight(0);
            b.setPadding(dp(12), dp(7), dp(12), dp(7));
            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, dp(39));
            p.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(b, p);
            tabs[i] = b;
            b.setOnClickListener(v -> openTab(index));
        }
        scroll.addView(nav);
        root.addView(scroll);

        LinearLayout info = new LinearLayout(this);
        info.setGravity(Gravity.CENTER_VERTICAL);
        info.setPadding(dp(16), dp(9), dp(16), dp(9));
        info.setBackgroundColor(0xFFF0F6F8);
        sourceText = text("Cargando…", 12, 0xFF31506A, true);
        info.addView(sourceText, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        TextView hint = text("Internet", 11, 0xFF587387, false);
        info.addView(hint);
        root.addView(info);

        progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(MINT));
        progress.setIndeterminate(false);
        root.addView(progress, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(3)));

        browser = new WebView(this);
        browser.setBackgroundColor(WHITE);
        WebSettings settings = browser.getSettings();
        settings.setJavaScriptEnabled(true); // Required by the source flight-board websites.
        settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(false);
        settings.setAllowContentAccess(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(browser, true);
        browser.setWebChromeClient(new WebChromeClient() {
            @Override public void onProgressChanged(WebView view, int value) {
                progress.setProgress(value);
                progress.setVisibility(value >= 100 ? View.GONE : View.VISIBLE);
            }
        });
        browser.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (!request.isForMainFrame()) return false;
                String scheme = request.getUrl().getScheme();
                if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme)) return false;
                try { startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl())); } catch (Exception ignored) {}
                return true;
            }
            @Override public void onPageStarted(WebView view, String url, android.graphics.Bitmap favicon) {
                loadError = false;
                sourceText.setText(SOURCES[current] + "  ·  Actualizando…");
            }
            @Override public void onPageFinished(WebView view, String url) {
                if (!loadError) sourceText.setText(SOURCES[current]);
            }
            @Override public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                if (request.isForMainFrame()) {
                    loadError = true;
                    sourceText.setText("Sin conexión o fuente no disponible. Tocá ↻ para reintentar.");
                }
            }
        });
        root.addView(browser, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        TextView footer = text("Fuentes externas  ·  Consultas gratuitas  ·  No afiliada a aeropuertos", 10, 0xFF647C8A, false);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(dp(5), dp(8), dp(5), dp(9));
        footer.setBackgroundColor(0xFFF0F6F8);
        root.addView(footer);
        setContentView(root);
    }

    private Button utilityButton(String symbol, String description) {
        Button b = new Button(this);
        b.setText(symbol);
        b.setTextColor(WHITE);
        b.setTextSize(21);
        b.setAllCaps(false);
        b.setContentDescription(description);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(DEEP));
        b.setMinWidth(0);
        b.setMinimumWidth(0);
        b.setPadding(dp(8), 0, dp(8), 0);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(dp(42), dp(42));
        p.setMargins(dp(4), 0, 0, 0);
        b.setLayoutParams(p);
        return b;
    }

    private void renderSelection() {
        for (int i = 0; i < tabs.length; i++) {
            boolean active = i == current;
            tabs[i].setTextColor(active ? NAVY : WHITE);
            tabs[i].setBackgroundTintList(android.content.res.ColorStateList.valueOf(active ? MINT : DEEP));
        }
        sourceText.setText(SOURCES[current]);
    }

    private void openTab(int index) {
        current = index;
        renderSelection();
        browser.loadUrl(URLS[index]);
    }

    @Override protected void onSaveInstanceState(Bundle out) {
        out.putInt("currentTab", current);
        browser.saveState(out);
        super.onSaveInstanceState(out);
    }

    @Override public void onBackPressed() {
        if (browser != null && browser.canGoBack()) browser.goBack();
        else super.onBackPressed();
    }

    @Override protected void onDestroy() {
        if (browser != null) browser.destroy();
        super.onDestroy();
    }
}
