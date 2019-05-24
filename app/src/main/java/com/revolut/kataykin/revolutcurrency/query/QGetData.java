package com.revolut.kataykin.revolutcurrency.query;

import android.content.Context;
import android.os.AsyncTask;

import com.revolut.kataykin.revolutcurrency.Log;
import com.revolut.kataykin.revolutcurrency.db.RateDBOp;
import com.revolut.kataykin.revolutcurrency.model.Currency;
import com.revolut.kataykin.revolutcurrency.model.Rate;
import com.revolut.kataykin.revolutcurrency.view.DoEvent;
import com.revolut.kataykin.revolutcurrency.view.DoEventGetRates;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class QGetData extends AsyncTask<String, Void, List<Rate>> {

    private static boolean isRun;

    public static boolean isRun() {
        return isRun;
    }

    private DoEventGetRates doAfterIfSuccess;
    private DoEvent doIfFault;
    private String sBaseKey;
    private WeakReference<Context> mWeakContext;


    public QGetData(WeakReference<Context> reference, String sBaseKey, DoEventGetRates doAfterIfSuccess, DoEvent doIfFault) {
        this.sBaseKey = sBaseKey != null ? sBaseKey.trim() : null;
        this.doAfterIfSuccess = doAfterIfSuccess;
        this.doIfFault = doIfFault;
        this.mWeakContext = reference;
    }

    public List<Rate> doInBackground(String... urls) {
        if (isRun)
            return null;
        isRun = true;
        try {

            List<String> lKeyBase = new ArrayList<>();
            if (sBaseKey == null) {
                for (Currency.Info ci : Currency.Info.values())
                    lKeyBase.add(ci.getKey());
            } else {
                lKeyBase.add(sBaseKey);
            }

            List<Rate> lResult = new ArrayList<>();
            for (String keyFrom : lKeyBase) {

                InputStream is = null;
                try {
                    URL url = new URL("https://revolut.duckdns.org/latest?base=" + keyFrom);
                    HttpsURLConnection url_con = (HttpsURLConnection) url.openConnection();
                    url_con.setRequestMethod("GET");
                    url_con.setRequestProperty("Content-length", "0");
                    url_con.setUseCaches(false);
                    url_con.setAllowUserInteraction(false);
                    url_con.setConnectTimeout(3000);
                    url_con.setReadTimeout(3000);
                    url_con.connect();
                    int status = url_con.getResponseCode();

                    is = new BufferedInputStream(url_con.getInputStream());

                    if (status != 200 && status != 201)
                        return null;

                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"), 8);
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                        sb.append(line).append("\n");
                    reader.close();

                    String res = "[" + sb.toString() + "]";

                    JSONArray jArray = new JSONArray(res);
                    if (jArray.length() == 0)
                        return null;

                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject joDataMain = jArray.getJSONObject(i);

                        if (joDataMain == null)
                            continue;

                        if (!joDataMain.has("rates")) {
                            continue;
                        }

                        JSONObject jdData = joDataMain.getJSONObject("rates");

                        Iterator<String> iKey = jdData.keys();
                        while (iKey.hasNext()) {
                            String key = iKey.next();
                            if (key != null && !jdData.has(key))
                                continue;
                            Double rate = (Double) jdData.get(key);
                            if (key != null && rate != null && rate > 0) {
                                lResult.add(new Rate(keyFrom, key, rate));
                            }
                        }
                    }

//                    RateDBOp rateDBOp = new RateDBOp(null);
//                    rateDBOp.update(lResult);

                } catch (Exception e) {
                    e.printStackTrace();
                    l(e.toString());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                            l(e.toString());
                        }
                    }
                }
            }

            if (mWeakContext != null) {
                RateDBOp rateDBOp = new RateDBOp();
                rateDBOp.update(mWeakContext.get(), lResult);
            }

            return lResult;
        } finally {
            isRun = false;
        }
    }

    public void onPreExecute() {
    }

    @Override
    public void onCancelled() {
    }

    public void onPostExecute(List<Rate> lRates) {
        if (lRates != null && lRates.size() > 0 && doAfterIfSuccess != null)
            doAfterIfSuccess.run(lRates);
        else if (doIfFault != null)
            doIfFault.run();
    }

    private void l(String log) {
        Log.i(getClass(), log);
    }
}
