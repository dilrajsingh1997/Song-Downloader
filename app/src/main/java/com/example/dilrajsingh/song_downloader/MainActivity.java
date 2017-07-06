package com.example.dilrajsingh.song_downloader;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Notification.Builder;
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
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    NotificationManager notificationManager;
    ListView listView;
    Button button;
    int[] count = {0, 0, 0, 0, 0};
    EditText editText;
    ProgressBar pBar;
    //Handler handler;
    String url1 = "http://pagalworld.co/?page_file=search&id=";
    String url2 = "&name=";
    String url3 = "&submit=Search";
    ProgressDialog mProgressDialog;
    AlertDialog.Builder downDialog;
    int qwerty = 0;
    ArrayList<String> larray = new ArrayList<>();
    ArrayList<String> narray = new ArrayList<>();
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;
    ExecutorService executorService;
    String[] permiss = {"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"};
    int requestCode = 200;
    String zxc = "", name, format, q;
    String[] formats = {"mp3", "mp4", "jpg", "gif", "3gp", "avi", "jar", "sis", "sisx", "exe", "pdf"};

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    return;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permiss, requestCode);
        }
        pBar = (ProgressBar) findViewById(R.id.tempPr);
        editText = (EditText) findViewById(R.id.editText);
        button = (Button) findViewById(R.id.button);
        listView = (ListView) findViewById(R.id.listView);
        pBar.setVisibility(View.GONE);
        executorService = Executors.newFixedThreadPool(5);
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                ((Vibrator)getSystemService(VIBRATOR_SERVICE)).vibrate(500);
                return true;
            }
        });
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

    private boolean checkPermission(){
        int res1 = getApplicationContext().checkCallingOrSelfPermission(permiss[0]);
        int res2 = getApplicationContext().checkCallingOrSelfPermission(permiss[1]);
        return (res1==PackageManager.PERMISSION_GRANTED) && (res2==PackageManager.PERMISSION_GRANTED);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public void onShow(View view){
        q = editText.getText().toString();
        AlertDialog.Builder al = new AlertDialog.Builder(MainActivity.this);
        al.setTitle("Select format");
        al.setCancelable(true);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, formats);
        al.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                format = formats[which].toString();
                String url = url1 + q.replace(" ", "+") + url2 + format + url3;
                larray.clear();
                narray.clear();
                if (isNetworkAvailable()) {
                    new search().execute(url);
                } else {
                    Toast.makeText(MainActivity.this, "Please check your network connection", Toast.LENGTH_LONG).show();
                }
            }
        });
        al.show();
    }

    public class search extends AsyncTask<String, Void, Void>{

        String count, page;
        int pages;
        boolean run = true;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            run = true;
            larray.clear();
            narray.clear();
            mProgressDialog = new ProgressDialog(MainActivity.this);
            mProgressDialog.setTitle("Loading all songs");
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mProgressDialog.dismiss();
                    run = false;
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            while(run) {
                try {
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
                    } catch (Exception NullPointerException) {
                        pages = 0;
                    }
                    while (pages > 0) {
                        String next = doc.select("a[class=rightarrow]").attr("href");
                        Elements links = doc.select("a[class=touch]");
                        for (Element q : links) {
                            String link = q.attr("href");
                            larray.add(link);
                            narray.add(q.text());
                            qwerty++;
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
                    for (Element q : links) {
                        String link = q.attr("href");
                        larray.add(link);
                        narray.add(q.text());
                        qwerty++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    Toast.makeText(MainActivity.this, "No results found", Toast.LENGTH_LONG).show();
                    larray.clear();
                    narray.clear();
                }
                return null;
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
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file, please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(false);
                pDialog.setButton(DialogInterface.BUTTON_NEUTRAL, "Background", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
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
                    String res = "[ Download File ]";
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
            int pos = -1;
            for(int i=0;i<5;i++){
                if(count[i]==0){
                    count[i] = 1;
                    pos = i;
                    break;
                }
            }
            if(pos==-1){
                Toast.makeText(MainActivity.this, "Maximum 5 downloads allowed", Toast.LENGTH_SHORT).show();
                return;
            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if(!checkPermission()){
                    requestPermissions(permiss, requestCode);
                }
                Toast.makeText(MainActivity.this, "Started", Toast.LENGTH_SHORT).show();
                executorService.execute(new downFile(zxc, pos));
            }
            else{
                Toast.makeText(MainActivity.this, "Started", Toast.LENGTH_SHORT).show();
                executorService.execute(new downFile(zxc, pos));
                //new Thread(new downFile(zxc)).start();
            }
        }
    }

    public class downFile implements Runnable{

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(android.R.drawable.stat_sys_download)
                        .setContentTitle("Downloading")
                        .setContentText(name)
                        .setOngoing(false);
        String name_local, ul;
        int prg = 0, id;
        Handler handler;
        Runnable r;
        long time = SystemClock.currentThreadTimeMillis(), tempTime;

        public downFile(String ul, int id){
            this.name_local = name;
            this.ul = ul;
            this.id = id;
            prg = 0;
            mBuilder.setProgress(100, 0, false);
            notificationManager.notify(id, mBuilder.build());
            /*handler = new Handler();
            r = new Runnable() {
                @Override
                public void run() {
                        mBuilder.setProgress(100, prg, false);
                        notificationManager.notify(0, mBuilder.build());
                        try {
                            sleep(1000);
                        } catch (InterruptedException e) {

                        }
                    handler.post(this);
                }
            };
            handler.post(r);*/
        }

        @Override
        public void run() {
            int countt = 0;
            try {
                Looper.prepare();
                URL url = new URL(ul);
                URLConnection conection = url.openConnection();
                conection.connect();
                int length = conection.getContentLength();
                InputStream in = new BufferedInputStream(url.openStream(), 4096);
                OutputStream ou = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/Download/" + name + "." + format);
                byte data[] = new byte[4096];
                long total = 0;
                int done = -1;
                while ((countt = in.read(data)) != -1) {
                    total += countt;
                    tempTime = SystemClock.uptimeMillis();
                    prg = (int) ((total * 100) / length);
                    ou.write(data, 0, countt);
                    int Done = (int) Math.round(total * 100.0 / length);
                    if ((done != Done) && (tempTime - time >= 1000)) {
                        done = Done;
                        mBuilder.setProgress(100, done, false);
                        notificationManager.notify(id, mBuilder.build());
                        time = SystemClock.uptimeMillis();
                    }
                }
                notificationManager.cancel(id);
                prg = 0;
                count[id] = 0;
                Looper.loop();
                ou.flush();
                ou.close();
                in.close();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*public class downloadFile extends AsyncTask<String, String, String>{

        Runnable r;
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(MainActivity.this)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Downloading")
                        .setContentText(name)
                        .setOngoing(true);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
            handler = new Handler();
            r = new Runnable() {
                @Override
                public void run() {
                    mBuilder.setProgress(100, prg, false);
                    notificationManager.notify(0, mBuilder.build());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {

                    }
                    handler.post(this);
                }
            };
            handler.post(r);
        }

        @Override
        protected String doInBackground(String... params) {
                publishProgress("0");
            //while(run_down) {
                int count = 0;
                try {
                    URL url = new URL(params[0]);
                    URLConnection conection = url.openConnection();
                    conection.connect();
                    int length = conection.getContentLength();
                    InputStream in = new BufferedInputStream(url.openStream(), 4096);
                    OutputStream ou = new FileOutputStream(Environment.getExternalStorageDirectory().toString() + "/Download/" + name + "." + format);
                    byte data[] = new byte[4096];
                    long total = 0;
                    while ((count = in.read(data)) != -1) {
                        total += count;
                        //sleep(1000);
                        publishProgress("" + (int) ((total * 100) / length));
                        ou.write(data, 0, count);
                        if(isCancelled())
                            break;
                    }
                    ou.flush();
                    ou.close();
                    in.close();
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();}
//                catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            return null;
//            }
//            return null;
        }

        protected void onProgressUpdate(String... progress) {
            pDialog.setProgress(Integer.parseInt(progress[0]));
            prg = Integer.parseInt(progress[0]);
        }

        @Override
        protected void onPostExecute(String file_url) {
            dismissDialog(progress_bar_type);
            notificationManager.cancel(0);
            prg = 0;
            handler.removeCallbacks(r);
        }
    }*/

    @Override
    public void onBackPressed() {
        notificationManager.cancelAll();
        executorService.shutdownNow();
        super.onBackPressed();
    }
}