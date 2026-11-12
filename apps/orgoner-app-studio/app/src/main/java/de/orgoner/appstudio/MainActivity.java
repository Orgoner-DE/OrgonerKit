package de.orgoner.appstudio;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;
import android.graphics.Color;
import android.os.Build;

import org.json.JSONObject;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;

public class MainActivity extends Activity {
    private static final int PICK_FILE = 1001;
    private static final int SAVE_FILE = 1002;
    private WebView web;
    private ValueCallback<Uri[]> fileCallback;
    private String pendingFilename;
    private String pendingContent;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16, 20, 33));
        getWindow().setNavigationBarColor(Color.rgb(16, 20, 33));
        web = new WebView(this);
        setContentView(web);
        WebSettings settings = web.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowFileAccessFromFileURLs(false);
        settings.setAllowUniversalAccessFromFileURLs(false);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
        if (Build.VERSION.SDK_INT >= 26) settings.setSafeBrowsingEnabled(true);
        web.addJavascriptInterface(new AndroidBridge(), "AndroidBridge");
        web.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView view, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                try {
                    startActivityForResult(intent, PICK_FILE);
                    return true;
                } catch(Exception ex) {
                    fileCallback = null;
                    callback.onReceiveValue(null);
                    return true;
                }
            }
        });
        web.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if ("file".equals(uri.getScheme()) && "/android_asset/index.html".equals(uri.getPath())) return false;
                if ("https".equals(uri.getScheme())) {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch(Exception ignored) {}
                }
                return true;
            }
        });
        web.loadUrl("file:///android_asset/index.html");
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE) {
            if (fileCallback != null) {
                Uri uri = resultCode == RESULT_OK && data != null ? data.getData() : null;
                fileCallback.onReceiveValue(uri == null ? null : new Uri[]{uri});
                fileCallback = null;
            }
        } else if (requestCode == SAVE_FILE) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                try (OutputStream out = getContentResolver().openOutputStream(data.getData())) {
                    if (out == null || pendingContent == null) throw new Exception("Datei nicht verfügbar");
                    out.write(pendingContent.getBytes(StandardCharsets.UTF_8));
                    saveResult(true, "Datei gespeichert: " + pendingFilename);
                } catch(Exception ex) {
                    saveResult(false, "Speichern fehlgeschlagen: " + ex.getMessage());
                }
            } else saveResult(false, "Speichern abgebrochen.");
            pendingFilename = null;
            pendingContent = null;
        }
    }

    private void saveResult(boolean ok, String message) {
        web.evaluateJavascript("window.onNativeSaveResult("+ok+","+JSONObject.quote(message)+")", null);
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override public void onBackPressed() {
        if (web.canGoBack()) web.goBack();
        else super.onBackPressed();
    }
    @Override protected void onDestroy() {
        web.removeJavascriptInterface("AndroidBridge");
        web.destroy();
        super.onDestroy();
    }

    public final class AndroidBridge {
        @JavascriptInterface public void saveTextFile(String filename, String content) {
            runOnUiThread(() -> {
                pendingFilename = filename.replaceAll("[^A-Za-z0-9_.-]", "_");
                pendingContent = content;
                Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
                intent.setType("text/plain");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.putExtra(Intent.EXTRA_TITLE, pendingFilename);
                try { startActivityForResult(intent, SAVE_FILE); }
                catch(Exception ex) { saveResult(false, "Dateidialog nicht verfügbar."); }
            });
        }
        @JavascriptInterface public void openHttps(String textUrl) {
            try {
                Uri uri = Uri.parse(textUrl);
                if (!"https".equals(uri.getScheme()) || uri.getHost() == null) return;
                runOnUiThread(() -> {
                    try { startActivity(new Intent(Intent.ACTION_VIEW, uri)); } catch(Exception ignored) {}
                });
            } catch(Exception ignored) {}
        }
        @JavascriptInterface public void checkUpdates(String textUrl) {
            new Thread(() -> {
                JSONObject result = new JSONObject();
                HttpURLConnection conn = null;
                try {
                    URL url = new URL(textUrl);
                    if (!"https".equals(url.getProtocol())) throw new Exception("Nur HTTPS erlaubt");
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setConnectTimeout(7000);
                    conn.setReadTimeout(7000);
                    conn.setInstanceFollowRedirects(false);
                    conn.setRequestProperty("Accept", "application/json");
                    if (conn.getResponseCode() != 200) throw new Exception("HTTP " + conn.getResponseCode());
                    try (InputStream in = conn.getInputStream(); ByteArrayOutputStream buf = new ByteArrayOutputStream()) {
                        byte[] part = new byte[4096];
                        int n;
                        while ((n = in.read(part)) != -1) {
                            if (buf.size() + n > 32768) throw new Exception("Update-Datei zu groß");
                            buf.write(part, 0, n);
                        }
                        result = new JSONObject(buf.toString("UTF-8"));
                    }
                } catch(Exception ex) {
                    try { result.put("error", ex.getMessage() == null ? "Netzwerkfehler" : ex.getMessage()); } catch(Exception ignored) {}
                } finally {
                    if (conn != null) conn.disconnect();
                }
                String json = JSONObject.quote(result.toString());
                runOnUiThread(() -> web.evaluateJavascript("window.onNativeUpdateResult(" + json + ")", null));
            }).start();
        }
    }
}
