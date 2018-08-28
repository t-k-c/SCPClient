package tk.tkctechnologies.calc.scp;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;


/**
 * Created by codename-tkc on 11/09/2017.
 * This is the class responsible response of data from server
 */

public class DataBaseResponse {
    public static final String ERROR = "An error occurred";

    public static String getPostResponseData(String stringUrl, Uri.Builder builder, Context context) throws IOException {
        String response = ERROR;
        try {
            URL url = new URL(stringUrl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);
            urlConnection.setRequestMethod("GET");
            if (builder != null) {

                String query = builder.build().getEncodedQuery();
                OutputStreamWriter outputStreamWriter = new OutputStreamWriter(urlConnection.getOutputStream());
                outputStreamWriter.write(query);
                outputStreamWriter.flush();
                outputStreamWriter.close();
            }
            urlConnection.connect();
            int response_code = urlConnection.getResponseCode();
            if (response_code == HttpURLConnection.HTTP_OK) {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                bufferedReader.close();
                response = stringBuilder.toString();

            }
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            Log.e("ISItravel LoginManager", "MalformedURLException caught : " + e.getMessage());
        }
        Log.i("ISItravel LoginManager", "Returning result");
        return response;
    }


}
