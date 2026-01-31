package com.sumver.go;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class hackvip implements IXposedHookLoadPackage {

    private static final String TAG = "GoHook";
    private static final String TARGET_PACKAGE_NAME = "com.lvxingetch.goplayer";

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) throws Throwable {
        if (!TARGET_PACKAGE_NAME.equals(lpparam.packageName)) {
            return; // 如果包名不匹配，直接返回，不执行后续逻辑
        }

        XposedBridge.log(TAG + ": Hook目标： " + lpparam.packageName);

        // 直接hook开屏oncreate
        String launcherActivityClassName = "com.lvxingetch.goplayer.launcher.LauncherActivity";
        String onCreateMethodName = "onCreate";
        Class<?>[] bundleParamType = {Bundle.class}; // onCreate(Bundle savedInstanceState)

        Class<?> launcherActivityClass = XposedHelpers.findClassIfExists(launcherActivityClassName, lpparam.classLoader); // 使用 findClassIfExists 更安全
        if (launcherActivityClass != null) {
            XposedBridge.log(TAG + ": Found class " + launcherActivityClassName);

            // Hook onCreate(Bundle savedInstanceState) 方法，在方法执行完毕后 (after) 立即跳转
            XposedHelpers.findAndHookMethod(launcherActivityClass, onCreateMethodName, bundleParamType[0],
                    new XC_MethodHook() {
                        @Override
                        protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                            Log.d(TAG, "[Xposed] Hooked LauncherActivity.onCreate()已成功");

                            // 获取当前 LauncherActivity 实例
                            android.app.Activity launcherActivityInstance = (android.app.Activity) param.thisObject;

                            try {
                                // 创建跳转到 MainActivity 的 Intent
                                Class<?> mainActivityClass = XposedHelpers.findClass("com.lvxingetch.goplayer.MainActivity", lpparam.classLoader);
                                Intent intent = new Intent(launcherActivityInstance, mainActivityClass);

                                // 启动 MainActivity
                                launcherActivityInstance.startActivity(intent);
                                Log.d(TAG, "[Xposed] 启动 MainActivity 成功.");

                                // 直接结束LauncherActivity，不然它会接收数据使得在MainActivity右上角显示开会员等标识
                                launcherActivityInstance.finish();
                                Log.d(TAG, "[Xposed] 关闭LauncherActivity");

                            } catch (Exception e) {
                                Log.e(TAG, "[Xposed] Error: " + e.getMessage(), e);
                            }
                        }
                    });
        } else {
            XposedBridge.log(TAG + ": Class " + launcherActivityClassName + " 在com.lvxingetch.goplayer中没找到");
        }
    }
}