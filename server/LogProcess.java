package com.teehalf.pocketpill.updatePackage;

import com.j256.ormlite.dao.RuntimeExceptionDao;
import com.teehalf.pocketpill.db.OrmliteHelper;
import com.teehalf.pocketpill.entities.ContentVersion;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by archit on 18/9/16.
 */
public class LogProcess {

    public List<ContentVersion> processUpdate(JSONArray response) {
        try {
            List<ContentVersion> updateListAll = new ArrayList<ContentVersion>();
            for (int i = 0; i < response.length(); i++) {
                JSONObject jsonObject = response.getJSONObject(i);
                ContentVersion contentVersion = new ContentVersion();
                contentVersion.id = jsonObject.getLong("id");
                String ar = jsonObject.getString("link");
                //   contentVersion.filePath=ar.replace("9000","8002");
                // contentVersion.filePath=ar;
                contentVersion.filePath = ar;
                updateListAll.add(contentVersion);
            }
            //  Log.d("ALL UPDATES",updateListAll.toString());
            return updateListAll;


        } catch (Exception e) {

            return null;

        }
    }

    public String content(String filePath){
        try{
            String content="";
            BufferedReader br=new BufferedReader(new FileReader(filePath));
            String temp="";
            while((temp=br.readLine())!=null)
                content+=temp;
            return content;
        }
        catch(Exception e)
        {
            return "";
        }
    }

}