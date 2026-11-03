package com.aaronjwood.portauthority.async;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.aaronjwood.portauthority.R;
import com.aaronjwood.portauthority.response.HostAsyncResponse;
import com.aaronjwood.portauthority.response.MainAsyncResponse;

import java.io.IOException;
import java.lang.ref.WeakReference;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class WanIpAsyncTask extends AsyncTask<Void, Void, String> {

    // Cloudflare's trace endpoint answers with key=value lines, one of them "ip=".
    // The IPv4 literal keeps the answer IPv4 even when the phone also has IPv6.
    private static final String EXTERNAL_IP_SERVICE = "https://1.1.1.1/cdn-cgi/trace";
    private final WeakReference<MainAsyncResponse> delegate;

    /**
     * Constructor to set the delegate
     *
     * @param delegate Called when the external IP has been fetched
     */
    public WanIpAsyncTask(MainAsyncResponse delegate) {
        this.delegate = new WeakReference<>(delegate);
    }

    /**
     * Fetch the external IP address
     *
     * @param params
     * @return External IP address
     */
    @Override
    @SuppressLint("NewApi")
    protected String doInBackground(Void... params) {
        MainAsyncResponse activity = delegate.get();
        Context ctx = (Context) activity;
        OkHttpClient httpClient = new OkHttpClient();
        Request request = new Request.Builder().url(EXTERNAL_IP_SERVICE).build();

        try (Response response = httpClient.newCall(request).execute()) {
            ResponseBody body = response.body();
            if (!response.isSuccessful() || body == null) {
                return ctx.getResources().getString(R.string.errExternIp);
            }

            for (String line : body.string().split("\n")) {
                if (line.startsWith("ip=")) {
                    return line.substring(3).trim();
                }
            }

            return ctx.getResources().getString(R.string.errExternIp);
        } catch (IOException e) {
            return ctx.getResources().getString(R.string.errExternIp);
        }
    }

    /**
     * Calls the delegate when the external IP has been fetched
     *
     * @param result External IP address
     */
    @Override
    protected void onPostExecute(String result) {
        MainAsyncResponse activity = delegate.get();
        if (activity != null) {
            activity.processFinish(result);
        }
    }
}
