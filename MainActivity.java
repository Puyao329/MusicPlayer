package com.example.musicplayer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    MediaPlayer myplayer;
    Button btn;
    boolean flagPauseMusic=true;
    boolean flagAudioCanPlay;
    SeekBar seekBar ;
    Handler barHandler;
    Runnable barRunnable;
    TextView tvProgress;
    TextView tv_duration;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String autoFilePath = "/mnt/sdcard/b.mp3";
//        btn=findViewById(R.id.btnPlay_or_pause);
//        btn.setOnClickListener(listener);
//        playMusic(autoFilePath);
        //获取控件
        seekBar=(SeekBar)findViewById(R.id.seekBar);
        tvProgress=findViewById(R.id.tv_current_progress);
        tv_duration = findViewById(R.id.tv_duration);
        myplayer=new MediaPlayer();
        //播放音乐
        flagAudioCanPlay=autoPlayMusic(autoFilePath);
        if(flagAudioCanPlay)
            showSeekBarAndTextView();
        else
            Toast.makeText(this,"不存在",Toast.LENGTH_LONG).show();
    }


//    View.OnClickListener listener = new Button.OnClickListener(){
//
//        @Override
//        public void onClick(View view) {
//            if(view.getId()==R.id.btnPlay_or_pause){
//                //标志位取反
//                flagPauseMusic=!flagPauseMusic;
//                if(flagPauseMusic){//为true，暂停播放，按钮变播放
//                    pauseMusic();
//                    btn.setText("播放");
//                }else{
//                    playMusic();
//                    btn.setText("暂停");
//                }
//            }
//        }
//    };
    private boolean autoPlayMusic(String audioFilePath){
        if(new File(audioFilePath).exists()&&myplayer!=null){
            try{
                myplayer.reset();//将信息重置
                myplayer.setDataSource(audioFilePath);//指定音乐文件的路径
                myplayer.prepare();//完成一些预备工作
                myplayer.start();//开始播放
                return true;
            }catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
        else
            return false;
    }
    private void showSeekBarAndTextView(){
        //设置拖动条的最大值
        seekBar.setMax(myplayer.getDuration());
        //显示当前音乐的播放总时长
        tv_duration.setText(FormatConverter.timeFormatConverter(myplayer.getDuration()));
        //构造对象
        barHandler=new Handler();
        barRunnable=new Runnable() {
            @Override
            public void run() {
                //获取当前歌曲播放位置
                seekBar.setProgress(myplayer.getCurrentPosition());
                tvProgress.setText(FormatConverter.timeFormatConverter(myplayer.getCurrentPosition()));
                //100毫秒之后更新拖动进度条
                barHandler.postDelayed(barRunnable,100);
            }
        };
        //启动进度条
        barHandler.post(barRunnable);

    }
    private void playMusic(String audioFilePath){
        if(new File(audioFilePath).exists()){
            myplayer=new MediaPlayer();
            try{
                myplayer.reset();//将信息重置
                myplayer.setDataSource(audioFilePath);//指定音乐文件的路径
                myplayer.prepare();//完成一些预备工作
                myplayer.start();//开始播放
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        else
        {
            Toast.makeText(this,"指定的音乐文件不存在噢",Toast.LENGTH_SHORT).show();
        }
    }
    private void playMusic(){//从暂停处开始播放
        if(myplayer!=null)
            myplayer.start();
    }
    //暂停播放
    private void pauseMusic(){
        if(myplayer!=null)
            myplayer.pause();
    }
}