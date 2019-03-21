package com.example.macbookpro.testsourceandroid;
import android.app.Application;
import com.parse.Parse;
public class MyParseApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("TdgH827olWojXWNIW78aNJOB")
                // if defined
                .clientKey("XYqxT7dKubhNeWN2TMISsskw")
                .server("http://39.106.196.116:1337/api/1")
                .build()
        );
    }
}
