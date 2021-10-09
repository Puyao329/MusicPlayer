package com.example.musicplayer_two;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Integer.parseInt;

public class MusicActivity extends AppCompatActivity implements View.OnClickListener{
    private static SeekBar sb;
    private static TextView tv_progress,tv_total,name_song;
    private static Button play,playmode;
    private static ImageView music_pic;
    public int change=0;//记录下标的变化值
    private ObjectAnimator animator;
    private MusicService.MusicControl musicControl;
    String name;
    Intent intent1,intent2;
    MyServiceConn conn;
    private boolean isUnbind =false;//记录服务是否被解绑
    boolean flag;
    int count=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //绑定布局文件
        setContentView(R.layout.activity_music);
        //获得意图
        intent1=getIntent();
        //初始化
        init();
    }
    private void init(){
        tv_progress=(TextView)findViewById(R.id.tv_progress);
        tv_total=(TextView)findViewById(R.id.tv_total);
        sb=(SeekBar)findViewById(R.id.sb);
        name_song=(TextView)findViewById(R.id.song_name);
        play=findViewById(R.id.btn_play);
        playmode=findViewById(R.id.playmode);

        playmode.setOnClickListener(this);
        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_exit).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        findViewById(R.id.btn_pre).setOnClickListener(this);

        name=intent1.getStringExtra("name");
        name_song.setText(name);
        intent2=new Intent(this,MusicService.class);//创建意图对象
        conn=new MyServiceConn();//创建服务连接对象
        bindService(intent2,conn,BIND_AUTO_CREATE);//绑定服务
        //为滑动条添加事件监听
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //进度条改变时，会调用此方法
                if (progress==seekBar.getMax()){//当滑动条到末端时，结束动画
                    animator.pause();//停止播放动画
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {//滑动条开始滑动时调用
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {//滑动条停止滑动时调用
                //根据拖动的进度改变音乐播放进度
                int progress=seekBar.getProgress();//获取seekBar的进度
                musicControl.seekTo(progress);//改变播放进度
            }
        });
        ImageView iv_music=(ImageView)findViewById(R.id.iv_music);
        String position= intent1.getStringExtra("position");
        int i=parseInt(position);
        iv_music.setImageResource(frag1.icons[i]);


        animator=ObjectAnimator.ofFloat(iv_music,"rotation",0f,360.0f);
        animator.setDuration(10000);//动画旋转一周的时间为10秒
        animator.setInterpolator(new LinearInterpolator());//匀速
        animator.setRepeatCount(-1);//-1表示设置动画无限循环
    }


    public static Handler handler=new Handler(){//创建消息处理器对象
        //在主线程中处理从子线程发送过来的消息
        @Override
        public void handleMessage(Message msg){
            Bundle bundle=msg.getData();//获取从子线程发送过来的音乐播放进度
            int duration=bundle.getInt("duration");
            int currentPosition=bundle.getInt("currentPosition");
            sb.setMax(duration);
            sb.setProgress(currentPosition);
            //歌曲总时长
            int minute=duration/1000/60;
            int second=duration/1000%60;
            String strMinute=null;
            String strSecond=null;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+"";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+"";
            }
            tv_total.setText(strMinute+":"+strSecond);
            //歌曲当前播放时长
            minute=currentPosition/1000/60;
            second=currentPosition/1000%60;
            if(minute<10){//如果歌曲的时间中的分钟小于10
                strMinute="0"+minute;//在分钟的前面加一个0
            }else{
                strMinute=minute+" ";
            }
            if (second<10){//如果歌曲中的秒钟小于10
                strSecond="0"+second;//在秒钟前面加一个0
            }else{
                strSecond=second+" ";
            }
            tv_progress.setText(strMinute+":"+strSecond);
        }
    };
    class MyServiceConn implements ServiceConnection{//用于实现连接服务
        @Override
        public void onServiceConnected(ComponentName name, IBinder service){
            musicControl=(MusicService.MusicControl) service;
        }
        @Override
        public void onServiceDisconnected(ComponentName name){

        }
    }
    private void unbind(boolean isUnbind){
        if(!isUnbind){//判断服务是否被解绑
            musicControl.pausePlay();//暂停播放音乐
            unbindService(conn);//解绑服务
        }
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onClick(View v) {
        String position = intent1.getStringExtra("position");
        String[] musicName = frag1.name;
        int i=parseInt(position);
        music_pic=findViewById(R.id.iv_music);
        switch (v.getId()) {
            case R.id.btn_play://播放按钮点击事件
                count++;
                flag = !flag;
               if(count==1) {
                   musicControl.play(i);
                   animator.start();
                   play.setText("暂停");
               }else if(!flag) {
                   musicControl.pausePlay();
                   animator.pause();
                   play.setText("播放");
               }else{
                   musicControl.continuePlay();
                   animator.start();
                   play.setText("暂停");
               }
                break;

//            case R.id.btn_pause://暂停按钮点击事件
//               flag = !flag;
//                if(flag) {
//                    musicControl.pausePlay();
//                    animator.pause();
//                    pause.setText("播放");
//                }else{
//                    musicControl.continuePlay();
//                    animator.start();
//                    pause.setText("暂停");
//                }
//                break;
//            case R.id.btn_continue_play://继续播放按钮点击事件
//                musicControl.continuePlay();
//                animator.start();
//                break;
            case R.id.btn_exit://退出按钮点击事件
                unbind(isUnbind);
                isUnbind=true;
                count=0;
                finish();
                break;
            case R.id.playmode:
                if(playmode.getText().equals("顺序")){

                }else if(playmode.getText().equals("one")){
                    change=0;
                }
                break;
            case R.id.btn_pre://播放上一首
                if((i+change)<1){
                Toast.makeText(MusicActivity.this,"已经是第一首了",Toast.LENGTH_SHORT).show();
                return;
            }else {
                change--;
                play.setText("暂停");
                music_pic.setImageResource(frag1.icons[i+change]);
                name_song.setText(musicName[i+change]);
                musicControl.play(i+change);
                animator.start();
                break;
            }
            case R.id.btn_next://播放下一首
                if((i+change)==musicName.length-1) {//这里musicName.length-1表示的最后一首歌的下标
                    Toast.makeText(MusicActivity.this, "已经是最后一首了", Toast.LENGTH_SHORT).show();
                    return;
                }
                else {
                    change++;
                    play.setText("暂停");
                    music_pic.setImageResource(frag1.icons[i+change]);
                    name_song.setText(musicName[i+change]);
                    musicControl.play(i+change);
                    animator.start();
                    break;
                }
        }
    }
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbind(isUnbind);//解绑服务
    }
}