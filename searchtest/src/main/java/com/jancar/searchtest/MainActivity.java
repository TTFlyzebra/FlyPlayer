package com.jancar.searchtest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jancar.search.IJancarSearch;
import com.jancar.search.JancarSearch;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    IJancarSearch iJancarSearch = JancarSearch.getInstance();
    TextView tvShow;
    EditText etTitle,etSinger,etAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startActivity(new Intent(this,StartActivity.class));
        tvShow = findViewById(R.id.tvshow);
        etTitle = findViewById(R.id.etTitie);
        etSinger = findViewById(R.id.etSinger);
        etAlbum = findViewById(R.id.etAlbum);
        iJancarSearch.register(this);
    }

    @Override
    protected void onDestroy() {
        iJancarSearch.unregister();
        super.onDestroy();
    }

    public void onSingerFind(View view) {
        StringBuilder stringBuffer = new StringBuilder("开始按歌手搜索-->"+etSinger.getText().toString()+"......\n");
        tvShow.setText(stringBuffer.toString());
        long startTime = System.currentTimeMillis();
        List<String> list = iJancarSearch.searchMusicBySinger(etSinger.getText().toString());
        long endTime = System.currentTimeMillis();
        stringBuffer.append("搜索结束，耗时").append(endTime-startTime).append("毫秒\n");
        tvShow.setText(stringBuffer.toString());
        for(String str:list){
           stringBuffer.append(str).append("\n");
        }
        tvShow.setText(stringBuffer.toString());
    }

    public void onTitleFind(View view) {
        StringBuilder stringBuffer = new StringBuilder("开始按歌名搜索-->"+etTitle.getText().toString()+"......\n");
        tvShow.setText(stringBuffer.toString());
        long startTime = System.currentTimeMillis();
        List<String> list = iJancarSearch.searchMusicByTitle(etTitle.getText().toString());
        long endTime = System.currentTimeMillis();
        stringBuffer.append("搜索结束，耗时").append(endTime-startTime).append("毫秒\n");
        tvShow.setText(stringBuffer.toString());
        for(String str:list){
            stringBuffer.append(str).append("\n");
        }
        tvShow.setText(stringBuffer.toString());
    }

    public void onAlbumFind(View view) {
        StringBuilder stringBuffer = new StringBuilder("开始按专辑搜索-->"+etAlbum.getText().toString()+"......\n");
        tvShow.setText(stringBuffer.toString());
        long startTime = System.currentTimeMillis();
        List<String> list = iJancarSearch.searchMusicByAlbum(etAlbum.getText().toString());
        long endTime = System.currentTimeMillis();
        stringBuffer.append("搜索结束，耗时").append(endTime-startTime).append("毫秒\n");
        tvShow.setText(stringBuffer.toString());
        for(String str:list){
            stringBuffer.append(str).append("\n");
        }
        tvShow.setText(stringBuffer.toString());
    }

    public void onTitleSingerFind(View view) {
        StringBuilder stringBuffer = new StringBuilder("开始按歌手歌名搜索-->"+etSinger.getText().toString()+"-"+etTitle.getText().toString()+"......\n");
        tvShow.setText(stringBuffer.toString());
        long startTime = System.currentTimeMillis();
        List<String> list = iJancarSearch.searchMusic(etSinger.getText().toString(),etTitle.getText().toString());
        long endTime = System.currentTimeMillis();
        stringBuffer.append("搜索结束，耗时").append(endTime-startTime).append("毫秒\n");
        tvShow.setText(stringBuffer.toString());
        for(String str:list){
            stringBuffer.append(str).append("\n");
        }
        tvShow.setText(stringBuffer.toString());
    }

    public void onTitleSingerAlbumFind(View view) {
        StringBuilder stringBuffer = new StringBuilder("开始按歌手歌名专辑搜索-->"+etSinger.getText().toString()+"-"+etTitle.getText().toString()+"-"+etAlbum.getText().toString()+"......\n");
        tvShow.setText(stringBuffer.toString());
        long startTime = System.currentTimeMillis();
        List<String> list = iJancarSearch.searchMusic(etSinger.getText().toString(),etTitle.getText().toString(),etAlbum.getText().toString());
        long endTime = System.currentTimeMillis();
        stringBuffer.append("搜索结束，耗时").append(endTime-startTime).append("毫秒\n");
        tvShow.setText(stringBuffer.toString());
        for(String str:list){
            stringBuffer.append(str).append("\n");
        }
        tvShow.setText(stringBuffer.toString());
    }
}
