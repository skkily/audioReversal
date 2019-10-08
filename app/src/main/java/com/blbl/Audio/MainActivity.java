package com.blbl.Audio;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;



import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.microshow.rxffmpeg.RxFFmpegInvoke;
import io.microshow.rxffmpeg.RxFFmpegSubscriber;
import io.reactivex.annotations.NonNull;

public class MainActivity extends AppCompatActivity {

    private MediaRecorder mediaRecorder=null;//创建一个空的MediaRecorder对象
    Button button_record=null;
    Button button_rever1=null;
    TextView textView=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {//6.0才用动态权限
            initPermission();
        }

        button_record=findViewById(R.id.button_record);
        button_rever1=findViewById(R.id.button_reversal1);
        //button_rever2=findViewById(R.id.button_reversal2);
        textView=findViewById(R.id.text_time);

        textView.setText(timePlay());

        checkButton();

        button_record.setOnClickListener(new ButtonListener());
        button_rever1.setOnClickListener(new ButtonListener());
        //button_rever2.setOnClickListener(new ButtonListener());
    }


    //申请两个权限，录音和文件读写
    //1、首先声明一个数组permissions，将需要的权限都放在里面
    String[] permissions = new String[]{Manifest.permission.RECORD_AUDIO,};
    //2、创建一个mPermissionList，逐个判断哪些权限未授予，未授予的权限存储到mPerrrmissionList中
    List<String> mPermissionList = new ArrayList<>();

    private final int mRequestCode = 100;//权限请求码


    //权限判断和申请
    public void initPermission() {

        mPermissionList.clear();//清空没有通过的权限

        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }

        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
            //说明权限都已经通过，可以做你想做的事情去
        }
    }


    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                showPermissionDialog();//跳转到系统设置权限页面，或者直接关闭页面，不让他继续访问
            }else{
                //全部权限通过，可以进行下一步操作。。。

            }
        }

    }


    /**
     * 不再提示权限时的展示对话框
     */
    AlertDialog mPermissionDialog;


    private void showPermissionDialog() {
        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请授予")
                    .setPositiveButton("去授予", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            initPermission();
                        }
                    }).create();
        }
        mPermissionDialog.show();
    }



    class ButtonListener implements View.OnClickListener {

        public void onClick(View v) {
            if(v.getId() == R.id.button_record){
                Log.d("test", "cansal button ---> click1");

                if(button_record.getText().toString().equals("开始录音")) {
                    textView.setText("录音中");
                    startRecord();
                    button_record.setText("停止录音");
                }else if(button_record.getText().toString().equals("停止录音")){

                    stopRecord();
                    button_record.setText("清除录音");
                    textView.setText(timePlay());
                }else if(button_record.getText().toString().equals("清除录音"))
                {
                    cleanRecord("/data/data/com.blbl.Audio/recording");
                    textView.setText("无录音");
                    button_record.setText("开始录音");
                }
            }else if(v.getId() == R.id.button_reversal1){
                Log.d("test", "cansal button ---> click2");
                starPplay("02.mp3");
            }
        }



    }

    /**
     * 开始录音的方法
     */
    public void startRecord()
    {
        //获取录音文件路径
        String path=getRecordFilePath();//获取录音文件路径
        if(!"".equals(path))
        {
            mediaRecorder=new MediaRecorder();//实例化MediaRecorder
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置记录的媒体文件的输出转换格式。
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            // 设置音频记录的编码格式。
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            if (Build.VERSION.SDK_INT >= 10) {
                mediaRecorder.setAudioSamplingRate(44100);
                mediaRecorder.setAudioEncodingBitRate(96000);
            } else {
                // older version of Android, use crappy sounding voice codec
                mediaRecorder.setAudioSamplingRate(8000);
                mediaRecorder.setAudioEncodingBitRate(12200);
            }

            mediaRecorder.setOutputFile(path);//设置输出路径


        }
        //文件录制错误监听
        mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

            @Override
            public void onError(MediaRecorder arg0, int arg1, int arg2) {
                if(mediaRecorder!=null)
                {
//解除资源与mediaRecorder的赋值关系,让资源可以为其他程序利用
                    mediaRecorder.release();
                    Toast.makeText(MainActivity.this,"record error",Toast.LENGTH_SHORT).show();
                }
            }
        });
        try
        {
            mediaRecorder.prepare();//准备
            mediaRecorder.start();//开始录音

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * 停止录音的方法
     */
    public void stopRecord()
    {
        if(mediaRecorder!=null)
        {
            mediaRecorder.stop();//停止录音
            mediaRecorder.release();//释放资源
        }

        audioRevel("ffmpeg -i /data/data/com.blbl.Audio/recording/01.mp3 -vf reverse -af areverse -preset superfast /data/data/com.blbl.Audio/recording/02.mp3");
    }

    public boolean cleanRecord(String dir){

        // 如果dir不以文件分隔符结尾，自动添加文件分隔符
        if (!dir.endsWith(File.separator))
            dir = dir + File.separator;
        File dirFile = new File(dir);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if ((!dirFile.exists()) || (!dirFile.isDirectory())) {
            System.out.println("删除目录失败：" + dir + "不存在！");
            return false;
        }
        boolean flag = true;
        // 删除文件夹中的所有文件包括子目录
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag)
                    break;
            }
        }
        if (!flag) {
            System.out.println("删除失败！");
            return false;
        }
        // 删除当前目录
        if (dirFile.delete()) {
            System.out.println("删除" + dir + "成功！");
            return true;
        } else {
            return false;
        }

    }

    /**
     * 删除单个文件
     *
     * @param fileName
     *            要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public boolean deleteFile(String fileName) {
        File file = new File(fileName);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                System.out.println("删除单个文件" + fileName + "成功！");
                return true;
            } else {
                System.out.println("删除单个文件" + fileName + "失败！");
                return false;
            }
        } else {
            System.out.println("删除单个文件失败：" + fileName + "不存在！");
            return false;
        }
    }


    public boolean checkButton(){
        try
        {
            File f=new File("/data/data/com.blbl.Audio/recording/01.mp3");
            if(!f.exists())
            {
                return false;
            }

        }
        catch (Exception e)
        {
            return false;
        }
        button_record.setText("清除录音");
        return true;
    }
    /**
     * 获取录音文件的路径
     * @return
     */
    public String getRecordFilePath()
    {
        String filePath="";//声明文件路径

        String sdCardPath="/data/data/com.blbl.Audio";
        File dirFile=new File(sdCardPath+File.separator+"recording");//自定义的录音文件File文件对象
        if(!dirFile.exists())
        {
            dirFile.mkdir();//不存在创建文件夹
        }
        try
        {
            //创建一个前缀为test后缀为.amr的录音文件，使用createTempFile方法来创建是为了避免文件冲突
            filePath=sdCardPath+"/recording"+"/01.mp3";
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        return filePath;//返回录音文件路径
    }

    //播放音频
    private void starPplay(String s) {

        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource("/data/data/com.blbl.Audio/recording/"+s);
            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
            Log.e("test", "播放失败");
            Toast.makeText(MainActivity.this,"先录个音吧？",Toast.LENGTH_SHORT).show();
        }

    }

    private String timePlay(){
        MediaPlayer mPlayer = new MediaPlayer();
        try {
            mPlayer.setDataSource("/data/data/com.blbl.Audio/recording/01.mp3");
            mPlayer.prepare();
            Float a=(float) mPlayer.getDuration()/1000;
            return a.toString()+"s";
        }catch (IOException e){

        }

        return "无录音";

    }

    public void audioRevel(String text){



        String[] commands = text.split(" ");

        RxFFmpegInvoke.getInstance().runCommandRxJava(commands).subscribe(new RxFFmpegSubscriber() {
            @Override
            public void onFinish() {

            }

            @Override
            public void onProgress(int progress, long progressTime) {

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(String message) {

            }
        });
    }


}
