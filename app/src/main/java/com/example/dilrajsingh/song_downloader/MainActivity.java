package com.example.dilrajsingh.song_downloader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;

public class MainActivity extends AppCompatActivity {

    ListView listView;
    Button button;
    EditText editText;
    String url1 = "http://pagalworld.co/?page_file=search&id=";
    String url2 = "&name=mp3&submit=Search";
    ProgressDialog mProgressDialog;
    AlertDialog.Builder downDialog;
    int qwerty = 0;
    ArrayList<String> larray = new ArrayList<>();
    ArrayList<String> narray = new ArrayList<>();
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    String[] permiss = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    int requestCode = 200;
    String zxc = "", name;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new downloadFile().execute(zxc);
                } else {
                    Toast.makeText(MainActivity.this, "Please grant permission", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        listView = (ListView) findViewById(R.id.listView);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                downDialog = new AlertDialog.Builder(MainActivity.this);
                downDialog.setMessage(String.valueOf(parent.getItemAtPosition(position)));
                downDialog.setTitle("Do you want to download this song?");
                downDialog.setPositiveButton("Download", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(isNetworkAvailable()) {
                            new download().execute(larray.get(position));
                        }
                        else{
                            Toast.makeText(MainActivity.this, "Please check your network connection", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                downDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                downDialog.setCancelable(false);
                downDialog.show();
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onShow(View view){
        String q = editText.getText().toString();
        String url = url1 + q.replace(" ", "+") + url2;
        larray.clear();
        narray.clear();
        if(isNetworkAvailable()){
            new search().execute(url);
        }
        else{
            Toast.makeText(MainActivity.this, "Please check your network connection", Toast.LENGTH_LONG).show();
        }
    }

    public class search extends AsyncTask<String, Void, Void>{

        String count, page;
        int pages;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Loading all songs");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog.dismiss();
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            try{
                Document doc = Jsoup.connect(params[0])
                        .followRedirects(true)
                        .ignoreContentType(true)
                        .timeout(12000) // optional
                        .header("Accept-Language", "pt-BR,pt;q=0.8") // missing
                        .header("Accept-Encoding", "gzip,deflate,sdch") // missing
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36") // missing
                        .referrer("http://www.google.com") // optional
                        .execute()
                        .parse();
                try {
                    Element x = doc.select("div[class=pag]").first();
                    count = x.select("center").first().select("b").first().text().substring(16, 17);
                    pages = new Integer(count) - 1;
                }
                catch (Exception NullPointerException){
                    pages = 0;
                }
                while (pages>0){
                    String next = doc.select("a[class=rightarrow]").attr("href");
                    Elements links = doc.select("a[class=touch]");
                    for(Element q : links){
                        String link = q.attr("href");
                        larray.add(link);
                        narray.add(q.text());
                        qwerty ++;
                    }
                    doc = Jsoup.connect(next)
                            .followRedirects(true)
                            .ignoreContentType(true)
                            .timeout(12000) // optional
                            .header("Accept-Language", "pt-BR,pt;q=0.8") // missing
                            .header("Accept-Encoding", "gzip,deflate,sdch") // missing
                            .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36") // missing
                            .referrer("http://www.google.com") // optional
                            .execute()
                            .parse();
                    pages -= 1;
                }
                Elements links = doc.select("a[class=touch]");
                for(Element q : links){
                    String link = q.attr("href");
                    larray.add(link);
                    narray.add(q.text());
                    qwerty ++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (NullPointerException e){
                Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_LONG).show();
                larray.clear();
                narray.clear();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            mProgressDialog.dismiss();
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, narray);
            listView.setAdapter(adapter);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type: // we set this to 0
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file, Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Toast.makeText(MainActivity.this, "Download cancelled", Toast.LENGTH_LONG).show();
                    }
                });
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    public class download extends AsyncTask<String, Void, Void>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(MainActivity.this, "Downloading", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(String... params) {
            String url = params[0];
            try {
                Document doc = Jsoup.connect(url)
                        .followRedirects(true)
                        .ignoreContentType(true)
                        .timeout(12000) // optional
                        .header("Accept-Language", "pt-BR,pt;q=0.8") // missing
                        .header("Accept-Encoding", "gzip,deflate,sdch") // missing
                        .userAgent("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.107 Safari/537.36") // missing
                        .referrer("http://www.google.com") // optional
                        .execute()
                        .parse();
                name = doc.select("div[class=content]").first().select("h2").first().text();
                Elements links = doc.select("a[class=touch]");
                for(Element q : links){
                    String result = "[ Download File ]";
                    if(q.text().equals(result)){
                        zxc = q.attr("href");
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Toast.makeText(MainActivity.this, "Started", Toast.LENGTH_SHORT).show();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(permiss, requestCode);
            }
            else{
                new downloadFile().execute(zxc);
            }
        }
    }

    public class downloadFile extends AsyncTask<String, String, String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... params) {
            int count = 0;
            try {
                URL url = new URL(params[0]);
                URLConnection conection = url.openConnection();
                conection.connect();
                int length = conection.getContentLength();
                InputStream in = new BufferedInputStream(url.openStream(), 8192);
                OutputStream ou = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/" + name + ".mp3");
                byte data[] = new byte[1024];
                long total = 0;
                while((count = in.read(data)) != -1){
                    total += count;
                    publishProgress("" + (int) ((total * 100) / length));
                    ou.write(data , 0, count);
                }
                ou.flush();
                ou.close();
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);
        }
    }

}
