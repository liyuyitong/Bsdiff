package com.liyu.bisdiffdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.migu.bisdiffdemo.R;
import com.liyu.bisdiffdemo.utils.PatchUtils;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView tvVersion = findViewById(R.id.tv_version);
        tvVersion.setText("versionCode:"+getVersionName());
        findViewById(R.id.btn_update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAPK();
            }
        });

        // 运行时权限申请
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String perms[] = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
            if (checkSelfPermission(perms[0]) == PackageManager.PERMISSION_DENIED) {
                requestPermissions(perms, 1000);
            }
        }
    }

    /**
     * Desc:版本升级
     * <p>
     * Author: [李豫]
     * Date: 2022-04-02
     */
    private void updateAPK() {
        new AsyncTask<Void, Void, File>() {
            @Override
            protected File doInBackground(Void... voids) {
                File oldFile = new File(Environment.getExternalStorageDirectory(), "1.0.apk");
                Log.e(MainActivity.class.getSimpleName(), "oldFile是否存在:"+oldFile.exists());
                String oldFilePath = oldFile.getAbsolutePath();

                File patchFile = new File(Environment.getExternalStorageDirectory(), "out.patch");
                Log.e(MainActivity.class.getSimpleName(), "patchFile是否存在:"+patchFile.exists());
                String patchFilePath = patchFile.getAbsolutePath();
                String outPath = createNewFile().getAbsolutePath();
                PatchUtils.bsPatch(oldFilePath, patchFilePath, outPath);
                return new File(outPath);
            }

            @Override
            protected void onPostExecute(File file) {
                super.onPostExecute(file);

                if (file != null) {
                    if (!file.exists()) return;
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Uri fileUri = FileProvider.getUriForFile(MainActivity.this, MainActivity.this.getApplicationInfo().packageName + ".fileprovider", file);
                        intent.setDataAndType(fileUri, "application/vnd.android.package-archive");
                    } else {
                        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
                    }
                    MainActivity.this.startActivity(intent);
                } else {
                    Toast.makeText(MainActivity.this, "差分包不存在！", Toast.LENGTH_LONG).show();
                }

            }
        }.execute();
    }

    private File createNewFile() {
        StringBuffer sb = new StringBuffer();
        String path = getApplicationContext().getExternalCacheDir().getAbsolutePath();
        sb.append(path);
        File outFile = new File(sb.toString(),"bsdiff.apk");
        File parentFile = outFile.getParentFile();
        if (!parentFile.exists()){
            parentFile.mkdirs();
        }
        if (outFile.exists()){
            outFile.delete();
        }
        try {
            boolean succ = outFile.createNewFile();

            File outFile1 = new File(sb.toString(),"bs.txt");
            outFile1.createNewFile();
            Log.e(MainActivity.class.getSimpleName(), "createNewFile:"+outFile.getAbsolutePath()+"succ:"+succ);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outFile;
    }

    public String getVersionName()
    {
        // 获取packagemanager的实例
        PackageManager packageManager = getPackageManager();
        // getPackageName()是你当前类的包名，0代表是获取版本信息
        PackageInfo packInfo = null;
        try {
            packInfo = packageManager.getPackageInfo(getPackageName(),0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        String version = packInfo.versionName;
        return version;
    }
}