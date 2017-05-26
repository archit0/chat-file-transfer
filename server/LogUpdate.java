package com.teehalf.pocketpill.updatePackage;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.teehalf.pocketpill.R;
import com.teehalf.pocketpill.db.OrmliteHelper;
import com.teehalf.pocketpill.entities.ContentVersion;
import com.teehalf.pocketpill.util.SharedPrefs;
import com.teehalf.pocketpill.util.Util;
import com.teehalf.pocketpill.util.Utils;
import org.json.JSONArray;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
public class LogUpdate extends AsyncTask<String, Integer, Boolean> {
    public LogUpdate(Context context){
        this.context=context;
        logProcess=new LogProcess();
        tempFolder = context.getString(R.string.database_src_floder) + "temp";
        new File(tempFolder).mkdirs();
        Log.d("Archit","HERE");
        updateUrl = context.getString(R.string.logUpdateUrl);
        masterUrl=context.getString(R.string.masterUrl);

    }
    Context context;
    ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    String updateUrl;
    String tempFolder;
    String masterUrl;
    LogProcess logProcess;
    OrmliteHelper dbHelper;
    SQLiteDatabase sqLiteDatabase;

    private final int INTERNET_FAILED=1419;
    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new ProgressDialog(context);
        progressDialog.setMessage("Checking for update..Please wait");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.show();
    }
    @Override
    protected Boolean doInBackground(String... params) {
        update();
        return null;
    }
    @Override
    public void onProgressUpdate(Integer... params) {
        progressDialog.setProgress(params[0]);

    }

    public void notDown() {
        String t = "";
        if (Util.yourstatus(context) == false) {
            t = "please check your internet connection and retry...";
        } else {
            t = "Apologies, we are having difficulty connecting to our servers..Please check back in 30 mins.";
        }
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setMessage(t);

        alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                update();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();

    }


    public void finish(boolean a) {
        try {
            if(a==false)
                notDown();
            progressDialog.dismiss();
            Toast.makeText(context,"Download Complete",Toast.LENGTH_LONG);
            Utils.deleteDir(new File(tempFolder));
        } catch (Exception e) {
        }

        //Toast.makeText(context,a?"FINISHED":"NOT PROCESSED",Toast.LENGTH_LONG).show();
    }


    //Start 1

    private void update(){
        try {
            requestQueue = Volley.newRequestQueue(context);
            JsonArrayRequest jsObjRequest = new JsonArrayRequest
                    (Request.Method.GET, updateUrl, new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            List<ContentVersion> available=logProcess.processUpdate(response);
                            List<String> inMobile= SharedPrefs.getAll(context);
                            List<ContentVersion> toDownload=new ArrayList<ContentVersion>();

                            for(ContentVersion contentVersion:available){
                                if(inMobile.contains(contentVersion.id+"")==false){
                                    toDownload.add(contentVersion);
                                }
                            }
                            if(inMobile.size()==0)
                            {
                                ContentVersion master=new ContentVersion();
                                master.id=0;
                                master.filePath=masterUrl;
                                toDownload=new ArrayList<ContentVersion>();
                                toDownload.add(master);
                            }
                            //DownloadTask downloadTask = new DownloadTask(toDownload, tempFolder);
                            //downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                            prompToDownload(toDownload);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            notDown();
                        }
                    });
            requestQueue.add(jsObjRequest);
        }
        catch (Exception e){
                e.printStackTrace();
        }
    }
    //End 1




    //Start 2

    private void prompToDownload(final List<ContentVersion> contentVersions){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        if(contentVersions.size()==1&&contentVersions.get(0).id==0)
        {
            alertDialogBuilder.setMessage("Master available updates");
        }
        else{
            alertDialogBuilder.setMessage(contentVersions.size()+" available updates");
        }

        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                DownloadTask downloadTask = new DownloadTask(contentVersions, tempFolder);
                downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
            }
        });
        alertDialogBuilder.setNegativeButton("Later", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                finish(true);
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
    //End 2



    //Start 3


    DownloadTask downloadTask;

    private class DownloadTask extends AsyncTask<String, Integer, Boolean> {

        List<ContentVersion> downloadList = null;
        String tempFolder;

        public DownloadTask(List<ContentVersion> donwloadList, String tempFolder) {
            this.downloadList = donwloadList;
            this.tempFolder = tempFolder;
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {

                for (int x = 0; x < downloadList.size(); x++) {
                    ContentVersion cv = downloadList.get(x);
                    String u = cv.filePath;
                    URL url = new URL(u);
                    File file = new File(tempFolder + "/" + cv.id + ".zip");
                    URLConnection connection = url.openConnection();
                    connection.connect();
                    int lenghtOfFile = connection.getContentLength();
                    InputStream is = url.openStream();
                    FileOutputStream fos = new FileOutputStream(file);
                    byte data[] = new byte[1024];
                    int count = 0;
                    long total = 0;
                    int progress = 0;
                    while ((count = is.read(data)) != -1) {
                        total += count;
                        int progress_temp = (int) total * 100 / lenghtOfFile;
                        if (progress_temp % 10 == 0 && progress != progress_temp) {
                            publishProgress(progress_temp);
                            progress = progress_temp;

                        }
                        fos.write(data, 0, count);
                    }
                    is.close();
                    fos.close();
                }
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (aBoolean == true) {
                if(downloadList.size()==1&&downloadList.get(0).id==0)
                {
                    MasterProcessTask ob = new MasterProcessTask(downloadList.get(0));
                    ob.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                }
                else {
                    ProcessTask ob = new ProcessTask(downloadList);
                    ob.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
                }
            } else {
               finish(false);
            }
        }
    }

    //End 3





    //Start 4

    private class ProcessTask extends AsyncTask<String, Integer, Boolean> {
        List<ContentVersion> downloadList;

        public ProcessTask(List<ContentVersion> downloadList) {
            this.downloadList = downloadList;
        }
        public boolean processAfterDownload(List<ContentVersion> downloadList) {
            try {
                try {
                    dbHelper = OpenHelperManager.getHelper(context, OrmliteHelper.class);
                    sqLiteDatabase = dbHelper.getWritableDatabase();

                } catch (Exception e) {
                }
                for(ContentVersion contentVersion:downloadList){
                    String zipFile = tempFolder + "/" + contentVersion.id + ".zip";
                    String extractFolder = tempFolder + "/" + contentVersion.id;
                    boolean t = Utils.extractFolder(zipFile, extractFolder);
                    String content=logProcess.content(extractFolder+"/log.txt");

                    try{
                        sqLiteDatabase.beginTransaction();
                        String sp[]=content.split("<break>");
                        for(String x:sp) {
                            if(x.trim().equals("")==false)
                            sqLiteDatabase.execSQL(x);
                        }
                        sqLiteDatabase.setTransactionSuccessful();

                        sqLiteDatabase.endTransaction();
                    }
                    catch (Exception e){
                        String ea=e.getMessage();
                        String tl=ea;
                        e.printStackTrace();
                    }
                    SharedPrefs.add(context,contentVersion.id+"");
                }

                return true;
            } catch (Exception e) {
                String t = e.getMessage();
                e.printStackTrace();
                Log.e("app", e.getMessage());
            }
            return false;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            return processAfterDownload(downloadList);

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            finish(aBoolean);
        }
    }

    //End 4



    //Start Master Update





    private class MasterProcessTask extends AsyncTask<String, Integer, Boolean> {
        ContentVersion contentVersion;

        public MasterProcessTask(ContentVersion contentVersion) {
            this.contentVersion = contentVersion;
        }
        public boolean processAfterDownload(ContentVersion contentVersion) {
            try {
                try {
                    dbHelper = OpenHelperManager.getHelper(context, OrmliteHelper.class);
                    sqLiteDatabase = dbHelper.getWritableDatabase();

                } catch (Exception e) {
                }

                    String zipFile = tempFolder + "/" + contentVersion.id + ".zip";
                    String extractFolder = tempFolder + "/" + contentVersion.id;
                    boolean t = Utils.extractFolder(zipFile, extractFolder);
                    String json=logProcess.content(extractFolder+"/master.txt");
                    LogDTO logDTO=new Gson().fromJson(json,LogDTO.class);
                    int size=logDTO.map.size();
                    for(int i=1;i<=size;i++) {
                        try {

                            String content=logDTO.map.get(i+"");

                            sqLiteDatabase.beginTransaction();
                            String sp[] = content.split("<break>");
                            for (String x : sp) {
                                if (x.trim().equals("") == false)
                                    sqLiteDatabase.execSQL(x);
                            }
                            sqLiteDatabase.setTransactionSuccessful();

                            sqLiteDatabase.endTransaction();
                        } catch (Exception e) {
                            String ea = e.getMessage();
                            String tl = ea;
                            e.printStackTrace();
                        }
                        SharedPrefs.add(context,i + "");
                    }

                return true;
            } catch (Exception e) {
                String t = e.getMessage();
                e.printStackTrace();
                Log.e("app", e.getMessage());
            }
            return false;
        }


        @Override
        protected Boolean doInBackground(String... params) {
            return processAfterDownload(contentVersion);

        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            finish(aBoolean);
        }
    }


     class LogDTO{
        Map<String,String> map;
        public LogDTO(){
            map=new HashMap<String,String>();
        }
        public void add(String key,String value){
            map.put(key, value);
        }
    }


    //End Master Update

}