package com.example.marketing.qqplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.By;
import android.support.test.uiautomator.BySelector;
import android.support.test.uiautomator.UiObject2;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiScrollable;
import android.support.test.uiautomator.UiSelector;
import android.support.test.uiautomator.Until;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.example.marketing.common.CommonInterface;
import com.example.marketing.plugin.Automator;
import com.example.marketing.plugin.PluginIntent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Pattern;

import static android.support.test.InstrumentationRegistry.getArguments;
import static android.support.test.InstrumentationRegistry.getContext;
import static com.example.marketing.plugin.AutomatorHelper.WAIT_TIME;
import static com.example.marketing.qqplugin.PackageConstants.MobileQQ.PACKAGE;
import static com.example.marketing.qqplugin.PackageConstants.Settings;
import static com.example.marketing.qqplugin.PackageConstants.Stk;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddContacts extends Automator {

    private static final String TAG = "AddContacts";

    private static final int SLOT_1 = 0;
    private static final int SLOT_2 = 1;

    public static final String KEY_ADD_CONTACT_COUNT = "add_contact_count";
    public static final String KEY_ADD_CONTACT_INTERVAL = "add_contact_interval";
    public static final String KEY_ADD_CONTACT_GREETING1 = "add_contact_greeting_1";
    public static final String KEY_ADD_CONTACT_GREETING2 = "add_contact_greeting_2";
    public static final String KEY_ADD_CONTACT_GREETING3 = "add_contact_greeting_3";
    public static final String KEY_ADD_CONTACT_GREETING4 = "add_contact_greeting_4";
    public static final String KEY_RESET_ADDED_ACCOUNT_LIST = "reset_added_account_list";
    public static final String KEY_ADDED_ACCOUNT_LIST = "added_account_list";
    public static final String KEY_PHONE_NUMBER_LIST = "phone_number_list";

    private SharedPreferences mPref = PreferenceManager.getDefaultSharedPreferences(getContext());
    private Set<String> mPhoneNumberList;

    private CommonInterface mInterface;
    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mInterface = CommonInterface.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mInterface = null;
        }
    };

    private int mSlot = 0;

    @Before
    @Override
    public void setUp() {
        super.setUp();

        Intent intent = new Intent(PluginIntent.ACTION_COMMON_INTERFACE);
        intent.setPackage("com.example.marketing.assistant");
        getContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        sleep();
    }

    @Test
    @Override
    public void run() {
        // 检查权限
        boolean allowed = false;
        if (mInterface != null) {
            try {
                allowed = mInterface.checkPermission();
            } catch (RemoteException e) {
                // Nothing to do
            }
        }
        if (!allowed) {
            return;
        }

        // 获取好友验证发送数量参数
        Bundle args = getArguments();
        String count = mPref.getString(
                KEY_ADD_CONTACT_COUNT, getContext().getString(R.string.pref_default_add_contact_count));
        int addContactCount = 0;
        try {
            addContactCount = Integer.parseInt(args.getString(KEY_ADD_CONTACT_COUNT, count));
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        assertThat("好友验证发送数量无效", addContactCount, is(greaterThan(0)));

        // 获取好友添加间隔参数
        String interval = mPref.getString(KEY_ADD_CONTACT_INTERVAL,
                getContext().getString(R.string.pref_default_add_contact_interval));
        int addContactInterval = 0;
        try {
            addContactInterval = Integer.parseInt(args.getString(KEY_ADD_CONTACT_INTERVAL, interval));
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        assertThat("添加好友间隔无效", addContactInterval, is(greaterThan(0)));

        // 获取添加好友问候语参数
        String greeting1 = args.getString(KEY_ADD_CONTACT_GREETING1);
        if (TextUtils.isEmpty(greeting1)) {
            greeting1 = mPref.getString(KEY_ADD_CONTACT_GREETING1, "");
        }
        String greeting2 = args.getString(KEY_ADD_CONTACT_GREETING2);
        if (TextUtils.isEmpty(greeting2)) {
            greeting2 = mPref.getString(KEY_ADD_CONTACT_GREETING2, "");
        }
        String greeting3 = args.getString(KEY_ADD_CONTACT_GREETING3);
        if (TextUtils.isEmpty(greeting3)) {
            greeting3 = mPref.getString(KEY_ADD_CONTACT_GREETING3, "");
        }
        String greeting4 = args.getString(KEY_ADD_CONTACT_GREETING4);
        if (TextUtils.isEmpty(greeting4)) {
            greeting4 = mPref.getString(KEY_ADD_CONTACT_GREETING4, "");
        }
        final String[] greetings = { greeting1, greeting2, greeting3, greeting4 };

        // 获取是否重置已添加过账号列表参数
        String value = args.getString(KEY_RESET_ADDED_ACCOUNT_LIST);
        boolean resetAddedAccountList;
        if (value != null) {
            resetAddedAccountList = "true".equals(value);
        } else {
            resetAddedAccountList = mPref.getBoolean(KEY_RESET_ADDED_ACCOUNT_LIST, false);
        }
        if (resetAddedAccountList) {
            mPref.edit().remove(KEY_PHONE_NUMBER_LIST).apply();
            mPref.edit().remove(KEY_ADDED_ACCOUNT_LIST).apply();
            mPref.edit().putBoolean(KEY_RESET_ADDED_ACCOUNT_LIST, false).apply();
        }

        // 获取当前已添加过的QQ账号列表
        Set<String> addedAccountList = mPref.getStringSet(KEY_ADDED_ACCOUNT_LIST, new HashSet<String>());

        // 获取当前可用的电话号码列表
        if (mPref.contains(KEY_PHONE_NUMBER_LIST)) {
            mPhoneNumberList = mPref.getStringSet(KEY_PHONE_NUMBER_LIST, new HashSet<String>());
        } else {
            List<String> list;
            try {
                list = mInterface.queryPhoneNumber();
            } catch (RemoteException e) {
                list = new ArrayList<>();
            }
            mPhoneNumberList = new HashSet<>(list);
        }

        // 切换到初始卡
        switchSimCard();

        // 遍历所有已登录的QQ账号
        while (true) {
            // 启动QQ应用
            mHelper.launchApp(PACKAGE);
            mDevice.waitForIdle();
            sleep();

            // 点击联系人页面
            UiObject2 tabs = mDevice.wait(Until.findObject(By.res("android:id/tabs")), WAIT_TIME);
            assertThat("没有找到底部导航", tabs, notNullValue());
            tabs.click();
            mDevice.waitForIdle();
            sleep();

            // 点击添加
            UiObject2 add = mDevice.wait(
                    Until.findObject(By.res(PACKAGE, "ivTitleBtnRightText").text("添加")), WAIT_TIME);
            assertThat("没有找到添加按钮", add, notNullValue());
            add.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            sleep();

            // 点击搜索栏
            UiObject2 box = mDevice.findObject(By.descStartsWith("搜索栏"));
            assertThat("没有找到搜索栏", box, notNullValue());
            box.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();

            for (int i = 0; i < addContactCount; i++) {
                // 防止频繁添加
                if (i > 0) {
                    sleep(addContactInterval * 1000);
                }

                // 从可用电话号码列表中获取一个电话号码
                String phoneNumber;
                Iterator<String> it = mPhoneNumberList.iterator();
                if (it.hasNext()) {
                    phoneNumber = it.next();
                } else {
                    playAlertRingtone();
                    return;
                }

                // 输入电话号码搜索
                UiObject2 keyword = mDevice.findObject(By.res(PACKAGE, "et_search_keyword"));
                assertThat("没有找到关键字输入框", keyword, notNullValue());
                keyword.setText(phoneNumber);
                mDevice.waitForIdle();
                UiObject2 search = mDevice.findObject(By.res(PACKAGE, "btn_cancel_search"));
                assertThat("没有找到搜索按钮", search, notNullValue());
                search.click();
                BySelector searching = By.textStartsWith("正在搜索");
                mDevice.wait(Until.hasObject(searching), WAIT_TIME);
                mDevice.wait(Until.gone(searching), WAIT_TIME);

                // 如果没有找到结果则继续搜索下一个电话号码
                if (mDevice.hasObject(By.desc("没有找到相关结果"))) {
                    removePhoneNumber(phoneNumber);
                    //i--;
                    continue;
                } else {
                    UiObject2 person = mDevice.findObject(By.res(PACKAGE, "title").text("人"));
                    if (person != null) {
                        UiObject2 list = person.getParent().getParent();
                        UiObject2 item = list.findObject(By.clazz(RelativeLayout.class));
                        item.clickAndWait(Until.newWindow(), WAIT_TIME);
                        mDevice.waitForIdle();
                    }
                }

                // 点击加好友按钮
                UiObject2 addHailFellow = mDevice.wait(Until.findObject(By.text("加好友")), WAIT_TIME);
                if (addHailFellow==null)
                {
                    //assertThat("没有找到加好友按钮", addHailFellow, notNullValue());
                    removePhoneNumber(phoneNumber);
                    continue;
                }

                addHailFellow.clickAndWait(Until.newWindow(), WAIT_TIME);
                mDevice.waitForIdle();

                // 拉取验证消息失败时播放警告铃声
                UiObject2 input = mDevice.wait(Until.findObject(By.clazz(EditText.class)), WAIT_TIME);
                //if (input == null) {
                    //playAlertRingtone();
                    //return;
                //    continue;
                //}

                // 根据是否有必填来判断是否需要问答验证问题
                if ("必填".equals(input.getText())) {
                    // 从可用电话号码中去掉该号码
                    removePhoneNumber(phoneNumber);

                    // 返回搜索
                    UiObject2 back1 = mDevice.findObject(By.res(PACKAGE, "ivTitleBtnLeft").text("返回"));
                    assertThat("没有找到返回按钮", back1, notNullValue());
                    back1.clickAndWait(Until.newWindow(), WAIT_TIME);
                    mDevice.waitForIdle();
                    UiObject2 back2 = mDevice.findObject(By.res(PACKAGE, "ivTitleBtnLeft").text("返回"));
                    assertThat("没有找到返回按钮", back2, notNullValue());
                    back2.clickAndWait(Until.newWindow(), WAIT_TIME);
                    mDevice.waitForIdle();
                    i--;
                    continue;
                } ////else {

                //}

                // 点击下一步
                UiObject2 next = mDevice.findObject(By.res(PACKAGE, "ivTitleBtnRightText").text("下一步"));
                if (next!=null) {
                    // 输入随机问候语

                    if (input!=null) {

                        input.setText(greetings[new Random().nextInt(greetings.length)]);
                        sleep();
                    }
                        next.clickAndWait(Until.newWindow(), WAIT_TIME);
                        mDevice.waitForIdle();

                }
                sleep();

                // 点击发送
                UiObject2 send = mDevice.findObject(By.res(PACKAGE, "ivTitleBtnRightText").text("发送"));
                assertThat("没有找到发送按钮", send, notNullValue());
                send.clickAndWait(Until.newWindow(), WAIT_TIME);
                mDevice.waitForIdle();

                // 从可用电话号码中去掉该号码
                removePhoneNumber(phoneNumber);

                // 点击返回
                UiObject2 back = mDevice.findObject(By.res(PACKAGE, "ivTitleBtnLeft").text("返回"));
                //assertThat("没有找到返回按钮", back, notNullValue());
                if (back==null) {
                    mDevice.pressBack();
                    mDevice.waitForIdle();
                    sleep(10000);
                    mDevice.pressBack();
                    mDevice.waitForIdle();
                    sleep(10000);
                    mDevice.pressBack();
                    mDevice.waitForIdle();
                    sleep(10000);
                }
                if (back!=null) {
                    back.clickAndWait(Until.newWindow(), WAIT_TIME);
                    mDevice.waitForIdle();
                }
            }

            // 切卡及数据业务
            switchSimCard();

            // 再次启动QQ应用
            mHelper.launchApp(PACKAGE);
            mDevice.waitForIdle();
            sleep();

            // 打开抽屉
            if (mDevice.wait(Until.hasObject(By.clazz(ProgressBar.class)), WAIT_TIME)) {
                mDevice.wait(Until.gone(By.clazz(ProgressBar.class)), WAIT_TIME * 3);
            }
            if (mDevice.hasObject(By.text("身份过期"))) {
                playAlertRingtone();
                return;
            }
            if (mDevice.wait(Until.hasObject(
                    By.text(Pattern.compile("^(绑定手机通讯录|通讯录)$"))), WAIT_TIME)) {
                UiObject2 msg = mDevice.findObject(
                        By.res(Pattern.compile("^.*(ivTitleBtnLeft|ivTitleBtnLeftButton)$")));
                assertThat("没有找到退出按钮", msg, notNullValue());
                msg.clickAndWait(Until.newWindow(), WAIT_TIME);
                mDevice.waitForIdle();
            }
            UiObject2 head = mDevice.findObject(By.res(PACKAGE, "conversation_head"));
            assertThat("没有找到头像", head, notNullValue());
            head.click();
            mDevice.wait(Until.hasObject(By.res(PACKAGE, "nickname")), WAIT_TIME);
            mDevice.waitForIdle();

            // 打开设置
            UiObject2 settings = mDevice.wait(Until.findObject(By.text("设置")), WAIT_TIME);
            assertThat("没有找到设置按钮", settings, notNullValue());
            settings.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            sleep();

            // 打开账号管理
            mHelper.openMenu("帐号管理");
            mDevice.waitForIdle();
            sleep();

            // 获取当前登录的QQ号码
            UiObject2 check = mDevice.wait(Until.findObject(By.res(PACKAGE, "check")), WAIT_TIME);
            assertThat("没有找到当前账号", check, notNullValue());
            UiObject2 account = check.getParent().findObject(By.res(PACKAGE, "account"));
            assertThat("没有找到账号名称", account, notNullValue());
            addedAccountList.add(account.getText());
            mPref.edit().remove(KEY_ADDED_ACCOUNT_LIST).apply();
            mPref.edit().putStringSet(KEY_ADDED_ACCOUNT_LIST, addedAccountList).apply();

            // 滚动到底部
            UiScrollable list = new UiScrollable(new UiSelector().scrollable(true));
            try {
                list.scrollToEnd(4);
            } catch (UiObjectNotFoundException e) {
                e.printStackTrace();
            }
            mDevice.waitForIdle();
            sleep();

            // 选择最后一个账号切换
            List<UiObject2> icons = mDevice.findObjects(By.res(PACKAGE, "icon"));
            assertThat("没有找到账号", icons.size() > 0, is(true));
            int index = icons.size() - 1;

            // 若账号遍历完成则结束添加
            UiObject2 last = icons.get(index).getParent().findObject(By.res(PACKAGE, "account"));
            assertThat("没有找到最后一个账号", last, notNullValue());
            if (!addedAccountList.contains(last.getText())) {
                last.click();
                mDevice.wait(Until.hasObject(By.text("关联QQ号，同时接收多个帐号的消息。")), WAIT_TIME);
            } else {
                //break;
                sleep(170000);//一轮qq走完，休息接近半个小时
                addedAccountList.clear();
            }
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();

        getContext().unbindService(mServiceConnection);
    }

    private void removePhoneNumber(String phoneNumber) {
        mPhoneNumberList.remove(phoneNumber);
        mPref.edit().remove(KEY_PHONE_NUMBER_LIST).apply();
        mPref.edit().putStringSet(KEY_PHONE_NUMBER_LIST, mPhoneNumberList).apply();
    }

    private void switchSimCard() {
        int slot = mSlot++ % 2;
        mHelper.launchApp(Settings.PACKAGE);
        mDevice.waitForIdle();
        sleep();
        mHelper.openMenu("双卡设置");
        mDevice.waitForIdle();
        sleep();
        BySelector selector = By.checkable(true).enabled(true).clickable(true);
        UiObject2 switch1 = mDevice.wait(Until.findObjects(selector), WAIT_TIME).get(SLOT_1);
        boolean checked1 = switch1.isChecked();
        if (slot == SLOT_1 && !checked1 || slot == SLOT_2 && checked1) {
            switch1.click();
            mDevice.waitForIdle();
            sleep();
            if (!checked1) {
                clickStkDialog();
            }
        }
        UiObject2 switch2 = mDevice.wait(Until.findObjects(selector), WAIT_TIME).get(SLOT_2);
        boolean checked2 = switch2.isChecked();
        if (slot == SLOT_1 && checked2 || slot == SLOT_2 && !checked2) {
            switch2.click();
            mDevice.waitForIdle();
            sleep();
            if (!checked2) {
                clickStkDialog();
            }
        }
        mHelper.launchApp(Settings.PACKAGE);
        mDevice.waitForIdle();
        sleep();
        mHelper.openMenu("移动网络");
        mDevice.waitForIdle();
        sleep();
        UiObject2 tabs = mDevice.wait(Until.findObject(By.res("android:id/tabs")), WAIT_TIME);
        assertThat("没有找到顶部导航", tabs, notNullValue());
        UiObject2 tab = tabs.getChildren().get(slot);
        tab.click();
        tab.wait(Until.selected(true), WAIT_TIME);
        sleep();
        UiObject2 checkbox = mDevice.wait(Until.findObject(selector), WAIT_TIME);
        if (!checkbox.isChecked()) {
            checkbox.click();
            clickStkDialog();
        }
    }

    private void clickStkDialog() {
        BySelector selector = By.res(Stk.PACKAGE, "button_ok");
        UiObject2 ok = mDevice.wait(Until.findObject(selector), 60000);
        if (ok != null) {
            ok.click();
            mDevice.wait(Until.gone(selector), WAIT_TIME);
            sleep();
        }
    }

    private void playAlertRingtone() {
        Uri uri = Uri.parse("android.resource://" + getContext().getPackageName() + "/" + R.raw.alert);
        Ringtone r = RingtoneManager.getRingtone(getContext(), uri);
        r.play();
        sleep(10000);
    }

    private void sleep() {
        sleep(2000);
    }

    private void sleep(long ms) {
        SystemClock.sleep(ms);
    }
}
