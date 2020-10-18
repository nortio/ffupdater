package de.marmaro.krt.ffupdater.metadata.fetcher;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.time.Duration;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

import de.marmaro.krt.ffupdater.utils.ParamRuntimeException;


/**
 * Consume a REST-API from the internet.
 */
public class ApiConsumer {
    private static final String GZIP = "gzip";

    <T> T consume(URL url, Class<T> clazz) {
        try {
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setRequestProperty("Accept-Encoding", GZIP);
            urlConnection.setConnectTimeout((int) Duration.ofSeconds(10).toMillis());
            Preconditions.checkArgument(urlConnection instanceof HttpsURLConnection);

            final String contentEncoding = urlConnection.getContentEncoding();
            final boolean gzipped = GZIP.equals(contentEncoding);
            Preconditions.checkArgument(gzipped || contentEncoding == null);

            try (InputStream original = urlConnection.getInputStream();
                 InputStream decompressed = gzipped ? new GZIPInputStream(original) : original;
                 BufferedReader buffered = new BufferedReader(new InputStreamReader(decompressed))) {
                return new Gson().fromJson(buffered, clazz);
            }
        } catch (IOException e) {
            throw new ParamRuntimeException(e, "can't consume API interface %s", url);
        }
    }
}
