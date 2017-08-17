package com.github.anrtestapp;

import android.app.Application;
import android.util.Log;

import com.github.anrwatchdog.ANRError;
import com.github.anrwatchdog.ANRWatchDog;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

public class ANRWatchdogTestApplication extends Application {

    ANRWatchDog anrWatchDog = new ANRWatchDog(2000);

    private String getCause(Throwable throwable, int level) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < level; i++) {
            sb.append("    ");
        }
        sb.append(throwable.toString());
        sb.append('\n');

        StackTraceElement[] traceElements = throwable.getStackTrace();
        if (traceElements != null) {
            for (StackTraceElement traceElement : traceElements) {
                for (int i = 0; i < level; i++) {
                    sb.append("    ");
                }
                sb.append(traceElement.toString());
                sb.append('\n');
            }
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            String causeStr = getCause(cause, level + 1);
            sb.append(causeStr);
        }

        return sb.toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        anrWatchDog.setANRListener(new ANRWatchDog.ANRListener() {
            @Override
            public void onAppNotResponding(ANRError error) {
                Log.e("ANR-Watchdog", "Detected Application Not Responding!");
                
                Log.e("ANR-Watchdog", "ANR cause:" +  getCause(error, 0));

                // Some tools like ACRA are serializing the exception, so we must make sure the exception serializes correctly
                try {
                    new ObjectOutputStream(new ByteArrayOutputStream()).writeObject(error);
                }
                catch (IOException ex) {
                    throw new RuntimeException(ex);
                }

                Log.i("ANR-Watchdog", "Error was successfully serialized");

                throw error;
            }
        });

        anrWatchDog.start();
    }
}
