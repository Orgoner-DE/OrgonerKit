package de.orgoner.trumashow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.RouteInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.net.InetAddress;
import java.net.URL;
import java.security.cert.Certificate;
import java.util.List;
import javax.net.ssl.HttpsURLConnection;

/** Visible, defensive utilities. No remote execution, exploits or covert access. */
public final class SpecialMenuActivity extends Activity {
    private static final int BG = Color.rgb(8, 11, 10);
    private static final int PANEL = Color.rgb(23, 28, 25);
    private static final int ORANGE = Color.rgb(255, 163, 46);
    private static final int INK = Color.rgb(232, 239, 220);
    private static final int MUTED = Color.rgb(159, 174, 151);
    private LinearLayout root;
    private EditText tlsHost;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(16), dp(24), dp(16), dp(28));
        scroll.addView(root);
        root.addView(label("TRUMA://BLACK-LAB", 27, ORANGE, true));
        root.addView(label("SPECIAL MENU  •  1.1.0  •  DEFENSIVE MODE", 12, MUTED, true));
        root.addView(label("Werkzeuge nur für eigene Geräte und freigegebene Netze.", 13, INK, false));
        addButton(root, "< ZURÜCK ZUM DASHBOARD", () -> finish(), true);

        LinearLayout profiles = group("01  SCAN PROFILE");
        profiles.addView(label("Profile setzen TCP-Prüfports für den autorisierten WLAN-Scan.", 13, MUTED, false));
        addButton(profiles, "[WEB] HTTP / HTTPS", () -> profile("80,443,8080,8443"), false);
        addButton(profiles, "[REMOTE] SSH / RDP / VNC", () -> profile("22,23,3389,5900"), false);
        addButton(profiles, "[IOT] Geräte / Drucker", () -> profile("80,443,554,1883,8883,9100"), false);
        addButton(profiles, "[COMMON] Breiter Überblick", () -> profile("21,22,23,53,80,139,443,445,3389,5900,8080,8443"), false);

        LinearLayout diagnostics = group("02  NETZWERK & SYSTEM");
        addButton(diagnostics, "WLAN / GATEWAY / DNS ANZEIGEN", this::showNetworkReport, false);
        addButton(diagnostics, "VPN / PROXY / DNS-SCHUTZ", this::showSecurityReport, false);
        addButton(diagnostics, "TCP-PORT RISIKOBERICHT", this::showPortReport, false);
        addButton(diagnostics, "GERÄTESTATUS", this::showDeviceReport, false);

        LinearLayout tls = group("03  TLS HEALTH CHECK");
        tls.addView(label("Einzelner HTTPS-Handshake gegen selbst gewählte Domain (Port 443).", 13, MUTED, false));
        tlsHost = new EditText(this);
        tlsHost.setSingleLine(true);
        tlsHost.setTextSize(15);
        tlsHost.setTypeface(Typeface.MONOSPACE);
        tlsHost.setTextColor(INK);
        tlsHost.setHintTextColor(MUTED);
        tlsHost.setHint("example.com");
        tlsHost.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
        tlsHost.setPadding(dp(12),dp(8),dp(12),dp(8));
        GradientDrawable input = new GradientDrawable();
        input.setColor(Color.rgb(32,38,33)); input.setCornerRadius(dp(8));
        tlsHost.setBackground(input);
        LinearLayout.LayoutParams fl = new LinearLayout.LayoutParams(-1, dp(48));
        fl.setMargins(0,dp(10),0,dp(8)); tls.addView(tlsHost,fl);
        addButton(tls, "TLS VERBINDUNG TESTEN", this::tlsCheck, true);

        LinearLayout admin = group("04  PROTOKOLL & NOT-AUS");
        addButton(admin, "BERICHT ANZEIGEN", () -> dialog("SCANBERICHT", ScanStore.summary(this)), false);
        addButton(admin, "BERICHT TEILEN", this::shareReport, false);
        addButton(admin, "AUF UPDATES PRÜFEN", () -> UpdateCheck.check(this), false);
        addButton(admin, "NOT-AUS  •  ALLE SCANS STOPPEN", this::confirmEmergency, true);
        root.addView(label("Keine Passwortsuche • keine Angriffe • keine Bot-Fernsteuerung",12,MUTED,false));
        setContentView(scroll);
    }

    private void profile(String ports) {
        ScanStore.prefs(this).edit().putString("ports", ports).putBoolean("profile_just_set", true).apply();
        Toast.makeText(this,"Profil übernommen. Scan im Hauptmenü starten.",Toast.LENGTH_LONG).show();
    }

    private void showNetworkReport() {
        try {
            ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
            Network n = cm.getActiveNetwork();
            if(n == null) {dialog("NETZWERK","Keine aktive Netzwerkverbindung.");return;}
            LinkProperties lp = cm.getLinkProperties(n);
            if(lp == null) {dialog("NETZWERK","Keine Link-Daten verfügbar.");return;}
            StringBuilder report = new StringBuilder();
            report.append("IP im WLAN: ").append(NetInfo.currentWifiIp(this)).append("\n");
            report.append("Interface: ").append(lp.getInterfaceName()).append("\n");
            report.append("Gateway: ");
            for(RouteInfo route: lp.getRoutes())
                if(route.isDefaultRoute() && route.getGateway()!=null)
                    report.append(route.getGateway().getHostAddress()).append(" ");
            report.append("\nDNS: ");
            for(InetAddress dns: lp.getDnsServers()) report.append(dns.getHostAddress()).append(" ");
            if(Build.VERSION.SDK_INT >= 28)
                report.append("\nPrivate DNS: ").append(lp.isPrivateDnsActive() ? "aktiv" : "nicht aktiv");
            dialog("WLAN / GATEWAY / DNS",report.toString());
        } catch(Exception ex) {dialog("NETZWERK","Diagnose nicht möglich: "+ex.getClass().getSimpleName());}
    }

    private void showSecurityReport() {
        ConnectivityManager cm=(ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        Network n=cm.getActiveNetwork();
        NetworkCapabilities caps=n==null ? null : cm.getNetworkCapabilities(n);
        LinkProperties lp=n==null ? null : cm.getLinkProperties(n);
        String report="VPN-Transport: "+(caps!=null && caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)?"JA":"NEIN / nicht erkennbar")
            +"\nWLAN-Transport: "+(caps!=null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)?"JA":"NEIN")
            +"\nHTTP-Proxy: "+(lp!=null && lp.getHttpProxy()!=null?"konfiguriert":"nicht konfiguriert")
            +"\nPrivate DNS: "+(Build.VERSION.SDK_INT>=28 && lp!=null && lp.isPrivateDnsActive()?"JA":"NEIN / unbekannt")
            +"\n\nDiese Prüfung bewertet Einstellungen, keine Schwachstellen.";
        dialog("SICHERHEITS-CHECK",report);
    }

    private void showPortReport() {
        String prefix=ScanStore.prefs(this).getString("baseline_prefix","");
        List<ScanEngine.Host> hosts=prefix.isEmpty()?null:ScanStore.read(this,prefix);
        if(hosts==null) {dialog("PORT RISIKO","Erst einen autorisierten WLAN-Scan durchführen.");return;}
        StringBuilder sb=new StringBuilder("Offene Ports sind kein Beweis für eine Sicherheitslücke.\n\n");
        int count=0;
        for(ScanEngine.Host host:hosts) {
            for(Integer port:host.ports) {
                if(count++>=45) {sb.append("… weitere Funde im Bericht");break;}
                sb.append(host.ip).append(":").append(port).append("  ");
                sb.append(AuditRules.explain(port)).append("\n");
            }
            if(count>=45)break;
        }
        if(count==0)sb.append("Keine geprüften TCP-Ports offen.");
        dialog("PORT-RISIKOHINWEISE",sb.toString());
    }

    private void showDeviceReport() {
        String report="Android: "+Build.VERSION.RELEASE+" (API "+Build.VERSION.SDK_INT+")"
            +"\nModell: "+Build.MANUFACTURER+" "+Build.MODEL
            +"\nHintergrundscan: "+(ScanStore.prefs(this).getBoolean("bg_enabled",false)?"AKTIV":"AUS")
            +"\nWLAN-Scanfreigabe: "+(ScanStore.prefs(this).getBoolean("authorized",false)?"gesetzt":"nicht gesetzt")
            +"\n\nBerechtigungen bleiben auf die App beschränkt.";
        dialog("GERÄTESTATUS",report);
    }

    private void tlsCheck() {
        String host=tlsHost.getText().toString().trim().toLowerCase(java.util.Locale.ROOT);
        if(!AuditRules.validTlsHost(host)) {
            dialog("TLS","Nur eine vollständige Domain ohne Port, Pfad oder IP-Adresse eingeben.");
            return;
        }
        Toast.makeText(this,"TLS-Prüfung läuft…",Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            HttpsURLConnection conn=null;
            String msg;
            try {
                conn=(HttpsURLConnection)new URL("https://"+host+"/").openConnection();
                conn.setConnectTimeout(5000);conn.setReadTimeout(5000);
                conn.setInstanceFollowRedirects(false);
                conn.setRequestMethod("HEAD");
                conn.connect();
                Certificate[] certs=conn.getServerCertificates();
                msg="Domain: "+host+"\nHTTPS: OK\nCipher: "+conn.getCipherSuite()
                    +"\nZertifikate: "+certs.length
                    +"\nHTTP-Status: "+conn.getResponseCode()
                    +"\n\nNur TLS-Verbindungscheck, kein Pentest.";
            } catch(Exception e) {
                msg="TLS-Verbindung fehlgeschlagen: "+e.getClass().getSimpleName()
                    +"\nMögliche Ursachen: Netzwerk, Zertifikat, DNS oder Server.";
            } finally {if(conn!=null)conn.disconnect();}
            final String out=msg;
            runOnUiThread(()->dialog("TLS HEALTH CHECK",out));
        },"TRUMA-TLS").start();
    }

    private void shareReport() {
        Intent send=new Intent(Intent.ACTION_SEND);
        send.setType("text/plain");
        send.putExtra(Intent.EXTRA_TEXT,ScanStore.summary(this));
        startActivity(Intent.createChooser(send,"TRUMA-Bericht teilen"));
    }

    private void confirmEmergency() {
        new AlertDialog.Builder(this)
            .setTitle("NOT-AUS")
            .setMessage("Alle laufenden App-Scans abbrechen, Hintergrundscan deaktivieren und Freigabe für das WLAN löschen?")
            .setNegativeButton("Abbrechen",null)
            .setPositiveButton("JETZT STOPPEN",(d,w)->{
                MainActivity.EMERGENCY_CANCEL.set(true);
                ScanScheduler.disable(this);
                ScanStore.prefs(this).edit()
                    .putBoolean("authorized",false).remove("approved_prefix").apply();
                dialog("NOT-AUS","Scan abgebrochen. Hintergrundprüfung aus. Netzfreigabe gelöscht.");
            }).show();
    }

    private LinearLayout group(String title) {
        LinearLayout panel=new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(14),dp(14),dp(14),dp(14));
        GradientDrawable bg=new GradientDrawable();
        bg.setColor(PANEL);
        bg.setCornerRadius(dp(7));
        bg.setStroke(dp(1),Color.rgb(79,69,45));
        panel.setBackground(bg);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);
        p.setMargins(0,dp(16),0,0);
        root.addView(panel,p);
        panel.addView(label(title,15,ORANGE,true));
        return panel;
    }

    private TextView label(String value,int size,int color,boolean bold) {
        TextView v=new TextView(this);
        v.setText(value);v.setTextSize(size);v.setTextColor(color);
        v.setTypeface(Typeface.MONOSPACE,bold?Typeface.BOLD:Typeface.NORMAL);
        v.setPadding(0,dp(5),0,dp(6));
        return v;
    }

    private void addButton(LinearLayout parent,String title,Runnable callback,boolean primary) {
        Button button=new Button(this);
        button.setAllCaps(false);button.setText(title);button.setTextSize(13);
        button.setTextColor(primary?BG:INK);
        button.setTypeface(Typeface.MONOSPACE,Typeface.BOLD);
        GradientDrawable d=new GradientDrawable();
        d.setColor(primary?ORANGE:Color.rgb(43,48,40));
        d.setCornerRadius(dp(7));
        if(!primary)d.setStroke(dp(1),Color.rgb(89,94,77));
        button.setBackground(d);
        button.setOnClickListener(v->callback.run());
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(48));
        p.setMargins(0,dp(6),0,dp(6));
        parent.addView(button,p);
    }

    private void dialog(String title,String body) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(body)
            .setPositiveButton("OK",null).show();
    }

    private int dp(float n) {return (int)(n*getResources().getDisplayMetrics().density+0.5f);}
}
