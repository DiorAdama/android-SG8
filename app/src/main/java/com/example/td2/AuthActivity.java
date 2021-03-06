package com.example.td2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Base64;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class AuthActivity extends Activity {
    boolean http_result;
    @Override
    public void onCreate(Bundle savedInstanceData){
        super.onCreate(savedInstanceData);
        this.setContentView(R.layout.activity_authentication);

        this.setButtonListeners();
    }

    public static String readStream(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader r = new BufferedReader(new InputStreamReader(is),1000);
        for (String line = r.readLine(); line != null; line =r.readLine()){
            sb.append(line);
        }
        is.close();
        return sb.toString();
    }

    private void setButtonListeners(){
        Button button_login = this.findViewById(R.id.button_login);
        button_login.setOnClickListener(ev->{
            new Thread(() -> {
                URL url = null;
                try {
                    url = new URL("https://httpbin.org/basic-auth/bob/sympa");
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    try {
                        String user_name = ((EditText) findViewById(R.id.textfield_username)).getText().toString();
                        String password = ((EditText) findViewById(R.id.textfield_password)).getText().toString();
                        String basicAuth = "Basic " + Base64.encodeToString(
                                (user_name + ":" + password).getBytes(),
                                Base64.NO_WRAP);
                        urlConnection.setRequestProperty ("Authorization", basicAuth);
                        InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                        String s = AuthActivity.readStream(in);
                        JSONObject json = new JSONObject(s);
                        this.http_result = (boolean) json.get("authenticated");
                        runOnUiThread(() -> {
                            TextView httpResp = findViewById(R.id.textview_http_response);
                            String notif;
                            if (this.http_result) notif = "Authentication succeeded";
                            else notif = "Authentication failed";
                            httpResp.setText(notif);
                        });
                    } finally {
                        urlConnection.disconnect();
                    }
                }

                catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        TextView httpResp = findViewById(R.id.textview_http_response);
                        String notif = "Authentication failed";
                        httpResp.setText(notif);
                    });
                }
            }).start();
        });
    }
}
