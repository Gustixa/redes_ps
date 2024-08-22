package com.example;

import java.io.InputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class CertificateLoader {

    public static X509Certificate loadCertificate() throws Exception {
        try (InputStream certInputStream = CertificateLoader.class.getClassLoader().getResourceAsStream("alumchat_lol_cert.pem")) {
            if (certInputStream == null) {
                throw new RuntimeException("Certificate file not found");
            }
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            return (X509Certificate) certificateFactory.generateCertificate(certInputStream);
        }
    }
}
