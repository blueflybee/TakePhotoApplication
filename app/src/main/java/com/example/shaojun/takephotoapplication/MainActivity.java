package com.example.shaojun.takephotoapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.app.TakePhotoActivity;
import com.jph.takephoto.app.TakePhotoImpl;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.InvokeParam;
import com.jph.takephoto.model.TContextWrap;
import com.jph.takephoto.model.TResult;
import com.jph.takephoto.model.TakePhotoOptions;
import com.jph.takephoto.permission.InvokeListener;
import com.jph.takephoto.permission.PermissionManager;
import com.jph.takephoto.permission.TakePhotoInvocationHandler;

import java.io.File;

public class MainActivity extends AppCompatActivity implements TakePhoto.TakeResultListener, InvokeListener {
    private static final String TAG = TakePhotoActivity.class.getName();
    private TakePhoto takePhoto;
    private InvokeParam invokeParam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getTakePhoto().onCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file=new File(Environment.getExternalStorageDirectory(), "/temp/"+System.currentTimeMillis() + ".jpg");
                if (!file.getParentFile().exists())file.getParentFile().mkdirs();
                Uri imageUri = Uri.fromFile(file);

                configCompress(takePhoto);
                configTakePhotoOption(takePhoto);

                takePhoto.onPickFromCaptureWithCrop(imageUri, getCropOptions());
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        getTakePhoto().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        getTakePhoto().onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.TPermissionType type = PermissionManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionManager.handlePermissionsResult(this, type, invokeParam, this);
    }

    /**
     * 获取TakePhoto实例
     *
     * @return
     */
    public TakePhoto getTakePhoto() {
        if (takePhoto == null) {
            takePhoto = (TakePhoto) TakePhotoInvocationHandler.of(this).bind(new TakePhotoImpl(this, this));
        }
        return takePhoto;
    }

    @Override
    public void takeSuccess(TResult result) {
        Log.i(TAG, "takeSuccess：" + result.getImage().getCompressPath());
        Glide.with(this).load(result.getImage().getCompressPath()).into((ImageView) findViewById(R.id.iv));
    }

    @Override
    public void takeFail(TResult result, String msg) {
        Log.i(TAG, "takeFail:" + msg);
    }

    @Override
    public void takeCancel() {
        Log.i(TAG, getResources().getString(com.jph.takephoto.R.string.msg_operation_canceled));
    }

    @Override
    public PermissionManager.TPermissionType invoke(InvokeParam invokeParam) {
        PermissionManager.TPermissionType type = PermissionManager.checkPermission(TContextWrap.of(this), invokeParam.getMethod());
        if (PermissionManager.TPermissionType.WAIT.equals(type)) {
            this.invokeParam = invokeParam;
        }
        return type;
    }

    private void configCompress(TakePhoto takePhoto){
//        if(rgCompress.getCheckedRadioButtonId()!=R.id.rbCompressYes){
//            takePhoto.onEnableCompress(null,false);
//            return ;
//        }
        int maxSize= Integer.parseInt("102400");
        int width= Integer.parseInt("100");
        int height= Integer.parseInt("100");
        boolean showProgressBar=true;
        boolean enableRawFile = true;
        CompressConfig config;
//        if(rgCompressTool.getCheckedRadioButtonId()==R.id.rbCompressWithOwn){
        config=new CompressConfig.Builder()
                .setMaxSize(maxSize)
                .setMaxPixel(width>=height? width:height)
                .enableReserveRaw(enableRawFile)
                .create();
//        }else {
//            LubanOptions option=new LubanOptions.Builder()
//                    .setMaxHeight(height)
//                    .setMaxWidth(width)
//                    .setMaxSize(maxSize)
//                    .create();
//            config=CompressConfig.ofLuban(option);
//            config.enableReserveRaw(enableRawFile);
//        }
        takePhoto.onEnableCompress(config,showProgressBar);


    }

    private void configTakePhotoOption(TakePhoto takePhoto){
        TakePhotoOptions.Builder builder=new TakePhotoOptions.Builder();
//        if(rgPickTool.getCheckedRadioButtonId()==R.id.rbPickWithOwn){
        builder.setWithOwnGallery(true);
//        }
//        if(rgCorrectTool.getCheckedRadioButtonId()==R.id.rbCorrectYes){
        builder.setCorrectImage(true);
//        }
        takePhoto.setTakePhotoOptions(builder.create());

    }

    private CropOptions getCropOptions(){
//        if(rgCrop.getCheckedRadioButtonId()!=R.id.rbCropYes)return null;
        int height= Integer.parseInt("100");
        int width= Integer.parseInt("100");
        boolean withWonCrop=false;

        CropOptions.Builder builder=new CropOptions.Builder();

//        if(rgCropSize.getCheckedRadioButtonId()==R.id.rbAspect){
//            builder.setAspectX(width).setAspectY(height);
//        }else {
        builder.setOutputX(width).setOutputY(height);
//        }
        builder.setWithOwnCrop(withWonCrop);
        return builder.create();
    }
}
