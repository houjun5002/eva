package com.Alan.eva.ui.activity;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.Alan.eva.R;
import com.Alan.eva.service.BleService;
import com.Alan.eva.service.ServiceUtils;
import com.Alan.eva.tools.SpTools;
import com.Alan.eva.ui.EApp;
import com.Alan.eva.ui.core.AbsActivity;
import com.Alan.eva.ui.dialog.OperateDialog;

public class StartActivity extends AbsActivity {
    private final int GOTO_GUIDE = 0x0001;
    private final int GOTO_MAIN = 0x0002;

    @Override
    public Activity getCurrActivity() {
        return this;
    }

    @Override
    public int getRootViewId() {
        return R.layout.ac_start;
    }

    @Override
    public void findView(View rootView) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*获取屏幕宽度 开始*/
        DisplayMetrics dm = new DisplayMetrics();
        Display display = getCurrActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(dm);
        EApp.getApp().setScreenWidth(dm.widthPixels);
        EApp.getApp().setScreenHeight(dm.heightPixels);
        /*获取屏幕宽度 结束*/
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth == null) {
            showBleErrorDialog();
            return;
        }

        if(ServiceUtils.isServiceRunning(this,"BleService")){
            BleService    bleserver =  new BleService();
            bleserver.stopSelf();
        }
        requestMultiPermission();
        //checkIsFirst();
    }

    private void showBleErrorDialog() {
        OperateDialog dialog = new OperateDialog(getCurrActivity());
        dialog.setCancel("");
        dialog.setOk("退出应用");
        dialog.setOnOk(v -> {
            dialog.dismiss();
            currFinish();
        });
        dialog.setContent("本机不支持蓝牙通信，不能使用。");
        int wid = getCurrActivity().getResources().getDimensionPixelOffset(R.dimen.size_300);
        dialog.create(wid, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show();
    }

    /**
     * 判断是否是首次进入app
     */
    private void checkIsFirst() {
        Handler handler = new Handler(msg -> {
            int what = msg.what;
            if (what == GOTO_GUIDE) {
                guide();
            } else if (what == GOTO_MAIN) {
                gotoActivity(HomeActivity.class);
            }
            currFinish();
            return false;
        });
//        handler.postDelayed(() -> {
//            boolean isFirst = SpTools.getInstance(getCurrActivity()).isFirstStart();
//            handler.sendEmptyMessage(isFirst ? GOTO_GUIDE : GOTO_MAIN);
//            SpTools.getInstance(getCurrActivity()).putFirstStart(false);
//        }, 0);

        boolean isFirst = SpTools.getInstance(getCurrActivity()).isFirstStart();
            handler.sendEmptyMessageDelayed(isFirst ? GOTO_GUIDE : GOTO_MAIN,isFirst ?500:2000);
    }

    private void guide() {
        gotoActivity(HomeActivity.class);
    }
    public void requestMultiPermission(){
        requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH,
                        Manifest.permission.READ_PHONE_STATE,Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.INTERNET},
                new RequestPermissionCallBack() {
                    @Override
                    public void granted() {
                        checkIsFirst();
                        //Toast.makeText(StartActivity.this, "获取权限成功，执行正常操作", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void denied() {
                        Toast.makeText(StartActivity.this, "获取权限失败，正常功能受到影响", Toast.LENGTH_LONG).show();
                        StartActivity.this.finish();
                    }
                });
    }
}
