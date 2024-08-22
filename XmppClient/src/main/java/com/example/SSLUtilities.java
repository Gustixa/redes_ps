package com.example;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.http.HttpClient;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtilities {

    public static HttpClient createHttpClientWithPem(String pemFilePath) throws Exception {
        // Cargar el certificado desde el archivo PEM
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate caCert;

        try (FileInputStream fis = new FileInputStream(pemFilePath)) {
            caCert = (X509Certificate) cf.generateCertificate(fis);
        }

        // Crear un KeyStore que contenga nuestro certificado CA
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null); // Inicializa un KeyStore vacío
        keyStore.setCertificateEntry("caCert", caCert);

        // Crear un TrustManager que confíe en el certificado de nuestro KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // Crear un SSLContext que use nuestro TrustManager
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), new java.security.SecureRandom());

        // Crear un HttpClient que use nuestro SSLContext personalizado
        return HttpClient.newBuilder()
            .sslContext(sslContext)
            .build();
    }
}
