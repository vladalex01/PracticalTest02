package ro.pub.cs.systems.eim.practicaltest02.network;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cz.msebera.android.httpclient.HttpEntity;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.NameValuePair;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.ResponseHandler;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.impl.client.BasicResponseHandler;
import cz.msebera.android.httpclient.impl.client.DefaultHttpClient;
import cz.msebera.android.httpclient.message.BasicNameValuePair;
import cz.msebera.android.httpclient.protocol.HTTP;
import cz.msebera.android.httpclient.util.EntityUtils;
import ro.pub.cs.systems.eim.practicaltest02.general.Constants;
import ro.pub.cs.systems.eim.practicaltest02.general.Utilities;
import ro.pub.cs.systems.eim.practicaltest02.model.CurrencyModel;

public class CommunicationThread extends Thread {

    private ServerThread serverThread;
    private Socket socket;

    public CommunicationThread(ServerThread serverThread, Socket socket) {
        this.serverThread = serverThread;
        this.socket = socket;
    }

    @Override
    public void run() {
        if (socket == null) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] Socket is null!");
            return;
        }
        try {
            BufferedReader bufferedReader = Utilities.getReader(socket);
            PrintWriter printWriter = Utilities.getWriter(socket);
            if (bufferedReader == null || printWriter == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Buffered Reader / Print Writer are null!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Waiting for parameters from client (city / information type!");
            String informationType = bufferedReader.readLine();
            if (informationType == null || informationType.isEmpty()) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error receiving parameters from client (city / information type!");
                return;
            }
            Log.i(Constants.TAG, "[COMMUNICATION THREAD] Got currency: " + informationType);
            HashMap<String, CurrencyModel> data = serverThread.getData();
            CurrencyModel currencyModel = null;

            if (data.containsKey(informationType)) {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the cache...");
                currencyModel = data.get(informationType);
            } else {
                Log.i(Constants.TAG, "[COMMUNICATION THREAD] Getting the information from the webservice...");
                HttpClient httpClient = new DefaultHttpClient();
                String pageSourceCode = "";

//              MAKE QUERY
                HttpGet httpGet = new HttpGet(Constants.WEB_SERVICE_ADDRESS);
                HttpResponse httpGetResponse = httpClient.execute(httpGet);
                HttpEntity httpGetEntity = httpGetResponse.getEntity();
                if (httpGetEntity != null) {
                    pageSourceCode = EntityUtils.toString(httpGetEntity);

                }

                if (pageSourceCode == null) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] Error getting the information from the webservice!");
                    return;
                } else {
                    Log.i(Constants.TAG, pageSourceCode );
                }

                // Updated for openweather API
                JSONObject content = new JSONObject(pageSourceCode);

                JSONObject time = content.getJSONObject("time");
                String updated = time.getString("updated");

                JSONObject bpi = content.getJSONObject("bpi");
                JSONObject USD = bpi.getJSONObject("USD");
                JSONObject EUR = bpi.getJSONObject("EUR");

                String usd_rate = USD.getString("rate");
                String eur_rate = EUR.getString("rate");
                Log.i(Constants.TAG, "UPDATED: " + updated + " EUR_RATE: " + eur_rate + " USD_RATE: " + usd_rate);

                currencyModel = new CurrencyModel(
                        updated, eur_rate, usd_rate
                );
                serverThread.setData("EUR", currencyModel);
                serverThread.setData("USD", currencyModel);
            }
            if (currencyModel == null) {
                Log.e(Constants.TAG, "[COMMUNICATION THREAD] Currency Model is null!");
                return;
            }
            String result = null;
            switch(informationType) {
                case "EUR":
                    result = currencyModel.getUpdated() + ": EUR = " + currencyModel.getEur_rate();
                    break;
                case "USD":
                    result = currencyModel.getUpdated() + ": USD = " + currencyModel.getUsd_rate();
                    break;
                default:
                    result = "[COMMUNICATION THREAD] Wrong information type (EUR / USD)!";
            }
            printWriter.println(result);
            printWriter.flush();


//      These stay!
        } catch (IOException ioException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
            if (Constants.DEBUG) {
                ioException.printStackTrace();
            }
        }
        catch (JSONException jsonException) {
            Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + jsonException.getMessage());
            if (Constants.DEBUG) {
                jsonException.printStackTrace();
            }
        }
        finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ioException) {
                    Log.e(Constants.TAG, "[COMMUNICATION THREAD] An exception has occurred: " + ioException.getMessage());
                    if (Constants.DEBUG) {
                        ioException.printStackTrace();
                    }
                }
            }
        }
    }

}
