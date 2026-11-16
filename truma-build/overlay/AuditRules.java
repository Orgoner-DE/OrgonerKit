package de.orgoner.trumashow;

import java.util.Locale;

/** Pure Java explanations and validation. Deliberately has no active attack methods. */
public final class AuditRules {
    private AuditRules(){}

    public static String explain(int port) {
        switch(port) {
            case 21: return "FTP: oft unverschlüsselt; sichere Alternative prüfen.";
            case 22: return "SSH: nur für berechtigte Administration freigeben.";
            case 23: return "Telnet: unverschlüsselt; nach Möglichkeit deaktivieren.";
            case 53: return "DNS/TCP: Dienstkonfiguration überprüfen.";
            case 80: return "HTTP: sensible Daten nur über HTTPS übertragen.";
            case 139: case 445: return "SMB: nur im vertrauenswürdigen LAN zulassen.";
            case 443: case 8443: return "HTTPS: Zertifikat und Updates kontrollieren.";
            case 554: return "RTSP: Kamerazugriff und Authentifizierung prüfen.";
            case 1883: return "MQTT: Zugangsschutz und TLS prüfen.";
            case 3389: return "RDP: Zugriff begrenzen und MFA einsetzen.";
            case 5900: return "VNC: Zugang beschränken; Verschlüsselung prüfen.";
            case 8080: return "Alternativer Webport: Admin-Oberfläche schützen.";
            case 8883: return "MQTT/TLS: Zertifikate und Zugriffsrechte prüfen.";
            case 9100: return "Druck-Port: Netz-Zugriff auf berechtigte Geräte begrenzen.";
            default: return "Dienst identifizieren und nur benötigte Ports erlauben.";
        }
    }

    public static boolean validTlsHost(String text) {
        if(text==null || text.isEmpty() || text.length()>253) return false;
        String host=text.toLowerCase(Locale.ROOT);
        if(host.contains("..") || host.startsWith(".") || host.endsWith(".")) return false;
        if(host.endsWith(".local") || host.endsWith(".lan") ||
           host.endsWith(".internal") || host.equals("localhost")) return false;
        String[] labels=host.split("\\.",-1);
        if(labels.length<2) return false;
        for(String part:labels) {
            if(part.length()<1 || part.length()>63 ||
               !part.matches("[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")) return false;
        }
        return labels[labels.length-1].matches("[a-z]{2,63}");
    }
}
