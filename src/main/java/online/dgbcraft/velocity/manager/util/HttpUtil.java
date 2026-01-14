package online.dgbcraft.velocity.manager.util;

import com.google.common.base.Charsets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    public static String getRequest(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setUseCaches(false);
        connection.setRequestMethod("GET");
        connection.getResponseCode();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
            StringBuilder result = new StringBuilder();
            while (true) {
                String line = reader.readLine();
                if (line != null) {
                    result.append(line);
                } else {
                    String sb = result.toString();
                    reader.close();
                    return sb;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            try {
                BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection.getErrorStream(), Charsets.UTF_8));
                StringBuilder result2 = new StringBuilder();
                while (true) {
                    String line2 = reader2.readLine();
                    if (line2 != null) {
                        result2.append(line2);
                    } else {
                        String sb2 = result2.toString();
                        reader2.close();
                        return sb2;
                    }
                }
            } catch (IOException e2) {
                throw e;
            }
        }
    }

    public static String postRequest(URL url, String payload, String header) throws IOException {
        byte[] postAsBytes = payload.getBytes(Charsets.UTF_8);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        connection.setUseCaches(false);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", header + "; charset=utf-8");
        connection.setRequestProperty("Content-Length", String.valueOf(postAsBytes.length));
        connection.setDoOutput(true);
        OutputStreamWriter writer = new OutputStreamWriter(connection.getOutputStream(), Charsets.UTF_8);
        try {
            writer.write(payload);
            writer.close();
            connection.getResponseCode();
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), Charsets.UTF_8));
                StringBuilder result = new StringBuilder();
                while (true) {
                    String line = reader.readLine();
                    if (line != null) {
                        result.append(line);
                    } else {
                        String sb = result.toString();
                        reader.close();
                        return sb;
                    }
                }
            } catch (IOException e) {
                try {
                    BufferedReader reader2 = new BufferedReader(new InputStreamReader(connection.getErrorStream(), Charsets.UTF_8));
                    StringBuilder result2 = new StringBuilder();
                    while (true) {
                        String line2 = reader2.readLine();
                        if (line2 != null) {
                            result2.append(line2);
                        } else {
                            String sb2 = result2.toString();
                            reader2.close();
                            return sb2;
                        }
                    }
                } catch (IOException e2) {
                    throw e;
                }
            }
        } catch (Throwable th) {
            try {
                writer.close();
            } catch (Throwable th2) {
                th.addSuppressed(th2);
            }
            throw th;
        }
    }
}