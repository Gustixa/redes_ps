package com.example;

import javax.net.ssl.*;
import java.net.http.HttpClient;
import java.security.cert.X509Certificate;

public class SSLUtilities {

    public static HttpClient createInsecureHttpClient() throws Exception {
        // Crear un TrustManager que confía en todos los certificados
        TrustManager[] trustAllCerts = new TrustManager[] {
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() { return null; }
                public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                public void checkServerTrusted(X509Certificate[] certs, String authType) { }
            }
        };

        // Configurar el contexto SSL para usar el TrustManager que confía en todos los certificados
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        // Crear un HttpClient que use este SSLContext
        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .build();
    }
}
