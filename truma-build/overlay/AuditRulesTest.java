package de.orgoner.trumashow;

import static org.junit.Assert.*;
import org.junit.Test;

public class AuditRulesTest {
    @Test public void commonPortsExplain() {
        assertTrue(AuditRules.explain(23).contains("unverschlüsselt"));
        assertTrue(AuditRules.explain(3389).contains("MFA"));
        assertNotNull(AuditRules.explain(65535));
    }
    @Test public void validTls() {
        assertTrue(AuditRules.validTlsHost("example.com"));
        assertTrue(AuditRules.validTlsHost("www.example.org"));
        assertFalse(AuditRules.validTlsHost("127.0.0.1"));
        assertFalse(AuditRules.validTlsHost("localhost"));
        assertFalse(AuditRules.validTlsHost("host.local"));
        assertFalse(AuditRules.validTlsHost("example.com:8443"));
        assertFalse(AuditRules.validTlsHost("https://example.com"));
        assertFalse(AuditRules.validTlsHost("abc..def.com"));
        assertFalse(AuditRules.validTlsHost("-name.com"));
        assertFalse(AuditRules.validTlsHost(null));
    }
}
