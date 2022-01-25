package com.example.i_music;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;


import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        listView=findViewById(R.id.listView);
        Dexter.withContext(getBaseContext())
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        ArrayList<File> mysongs=fetchSongs(Environment.getExternalStorageDirectory());
                        String[] items=new String[mysongs.size()];
                        for(int i=0;i<mysongs.size();i++){
                            items[i]=mysongs.get(i).getName().replace(".mp3","");
                        }
                        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,items);
                        listView.setAdapter(adapter);
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                Intent in=new Intent(getApplicationContext(),PlaySong.class);
                                String currentSong=listView.getItemAtPosition(i).toString();
                                in.putExtra("songList",mysongs);
                                in.putExtra("currentSong",currentSong);
                                in.putExtra("position",i);
                                startActivity(in);
                            }
                        });
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {

                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {
                        permissionToken.continuePermissionRequest();
                    }
                })
                .check();
    }
    public ArrayList<File> fetchSongs(File file){
        ArrayList<File> arrayList=new ArrayList<File>();
        File [] songs=file.listFiles();
        if(songs!=null){
            for(File myFile:songs){
                if(!myFile.isHidden()&&myFile.isDirectory()){
                    Log.d("mytag",myFile.getName());
                    arrayList.addAll(fetchSongs(myFile));
                }
                else{
                    if(myFile.getName().endsWith(".mp3")&& !myFile.getName().startsWith(".")){
                        Log.d("mytag1",myFile.getName());
                        arrayList.add(myFile);
                    }
                }
            }
        }
        return arrayList;
    }
}