package com.example.marketing.qqplugin;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Point;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.marketing.common.Authentication;
import com.example.marketing.plugin.Automator;
import com.example.marketing.plugin.PluginIntent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.support.test.InstrumentationRegistry.getArguments;
import static android.support.test.InstrumentationRegistry.getContext;
import static com.example.marketing.plugin.AutomatorHelper.WAIT_TIME;
import static com.example.marketing.qqplugin.PackageConstants.Mms;
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

    public static final String KEY_PHONE_NUMBER_1 = "phone_number_1";
    public static final String KEY_PHONE_NUMBER_2 = "phone_number_2";
    public static final String KEY_ADD_CONTACTS_COUNT = "add_contacts_count";
    public static final String KEY_ADD_CONTACTS_GREETING1 = "add_contacts_greeting_1";
    public static final String KEY_ADD_CONTACTS_GREETING2 = "add_contacts_greeting_2";
    public static final String KEY_ADD_CONTACTS_GREETING3 = "add_contacts_greeting_3";
    public static final String KEY_ADD_CONTACTS_GREETING4 = "add_contacts_greeting_4";
    public static final String KEY_ADD_CONTACTS_FROM_FIRST = "add_contacts_from_first";
    public static final String KEY_ADDED_ACCOUNT_LIST = "added_account_list";
    public static final String KEY_ADDED_CONTACTS_LIST = "added_contacts_list";

    private Authentication mAuthentication;

    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mAuthentication = Authentication.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mAuthentication = null;
        }
    };

    @Before
    @Override
    public void setUp() {
        super.setUp();

        Intent intent = new Intent(PluginIntent.ACTION_AUTHENTICATE);
        intent.setPackage("com.example.marketing.assistant");
        getContext().bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        sleep();
    }

    @Test
    @Override
    public void run() {
        // 检查权限
        boolean allowed = false;
        if (mAuthentication != null) {
            try {
                allowed = mAuthentication.checkPermission();
            } catch (RemoteException e) {
                // Nothing to do
            }
        }
        if (!allowed) {
            return;
        }

        // 解析参数
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        Bundle args = getArguments();

        // 获取电话号码参数
        String phoneNumber1 = args.getString(KEY_PHONE_NUMBER_1);
        if (TextUtils.isEmpty(phoneNumber1)) {
            phoneNumber1 = sp.getString(KEY_PHONE_NUMBER_1, "");
        }
        String phoneNumber2 = args.getString(KEY_PHONE_NUMBER_2);
        if (TextUtils.isEmpty(phoneNumber2)) {
            phoneNumber2 = sp.getString(KEY_PHONE_NUMBER_2, "");
        }
        final String[] phoneNumbers = { phoneNumber1, phoneNumber2 };
        final Pattern phoneNumberPattern = Pattern.compile("^1\\d{10}$");
        for (int i = 0; i < phoneNumbers.length; i++) {
            String phoneNumber = phoneNumbers[i];
            assertThat(String.format("卡%d电话号码无效", i + 1),
                    phoneNumber != null && phoneNumberPattern.matcher(phoneNumber).find(), is(true));
        }

        // 获取好友验证发送数量参数
        String count = sp.getString(
                KEY_ADD_CONTACTS_COUNT, getContext().getString(R.string.pref_default_add_contacts_count));
        int addContactsCount = 0;
        try {
            addContactsCount = Integer.parseInt(args.getString(KEY_ADD_CONTACTS_COUNT, count));
        } catch (NumberFormatException e) {
            Log.e(TAG, e.getMessage());
        }
        assertThat("好友验证发送数量无效", addContactsCount, is(greaterThan(0)));

        // 获取添加好友问候语参数
        String greeting1 = args.getString(KEY_ADD_CONTACTS_GREETING1);
        if (TextUtils.isEmpty(greeting1)) {
            greeting1 = sp.getString(KEY_ADD_CONTACTS_GREETING1, "");
        }
        String greeting2 = args.getString(KEY_ADD_CONTACTS_GREETING2);
        if (TextUtils.isEmpty(greeting2)) {
            greeting2 = sp.getString(KEY_ADD_CONTACTS_GREETING2, "");
        }
        String greeting3 = args.getString(KEY_ADD_CONTACTS_GREETING3);
        if (TextUtils.isEmpty(greeting3)) {
            greeting3 = sp.getString(KEY_ADD_CONTACTS_GREETING3, "");
        }
        String greeting4 = args.getString(KEY_ADD_CONTACTS_GREETING4);
        if (TextUtils.isEmpty(greeting4)) {
            greeting4 = sp.getString(KEY_ADD_CONTACTS_GREETING4, "");
        }
        final String[] greetings = { greeting1, greeting2, greeting3, greeting4 };

        // 获取是否从第一个联系人开始添加好友参数
        String value = args.getString(KEY_ADD_CONTACTS_FROM_FIRST);
        boolean addContactsFromFirst;
        if (value != null) {
            addContactsFromFirst = "true".equals(value);
        } else {
            addContactsFromFirst = sp.getBoolean(KEY_ADD_CONTACTS_FROM_FIRST, false);
        }
        if (addContactsFromFirst) {
            sp.edit().remove(KEY_ADDED_ACCOUNT_LIST).apply();
            sp.edit().remove(KEY_ADDED_CONTACTS_LIST).apply();
            sp.edit().putBoolean(KEY_ADD_CONTACTS_FROM_FIRST, false).apply();
        }

        // 初始卡
        int slot = 0;
        // 当前已添加过的QQ号码
        Set<String> addedAccountList = sp.getStringSet(KEY_ADDED_ACCOUNT_LIST, new HashSet<String>());
        // 当前已添加过的联系人
        Set<String> addedContactsList = sp.getStringSet(KEY_ADDED_CONTACTS_LIST, new HashSet<String>());
        // 切换到初始卡
        switchSimCard(slot);

        while (true) {
            // 遍历所有已登录的QQ号码
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

            // 点击添加手机联系人
            UiObject2 addContacts = mDevice.wait(Until.findObject(By.text("添加手机联系人")), WAIT_TIME);
            assertThat("没有找到添加手机联系人按钮", addContacts, notNullValue());
            addContacts.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            sleep();

            // 未启用情况下启用通讯录
            UiObject2 enable = mDevice.findObject(By.text("启用"));
            if (enable != null) {
                // 点击启用
                enable.clickAndWait(Until.newWindow(), WAIT_TIME);
                mDevice.waitForIdle();
                sleep();

                // 清空短信
                clearMms();

                // 输入手机号码
                UiObject2 phoneNumber = mDevice.wait(
                        Until.findObject(By.text("请输入你的手机号码")), WAIT_TIME);
                assertThat("没有找到输入手机号码框", phoneNumber, notNullValue());
                phoneNumber.setText(phoneNumbers[slot % 2]);
                mDevice.waitForIdle();
                sleep();

                // 点击下一步请求验证
                UiObject2 next1 = mDevice.findObject(By.text("下一步").enabled(true));
                assertThat("没有找到下一步按钮", next1, notNullValue());
                if (next1.clickAndWait(Until.newWindow(), WAIT_TIME)) {
                    mDevice.waitForIdle();
                    sleep();
                    UiObject2 ok = mDevice.findObject(By.text("确定"));
                    if (ok != null) {
                        if (ok.clickAndWait(Until.newWindow(), WAIT_TIME)) {
                            mDevice.waitForIdle();
                            sleep();
                        } else {
                            playAlertRingtone();
                            return;
                        }
                    }
                } else {
                    playAlertRingtone();
                    return;
                }

                // 从通知栏获取验证码
                String verifyCode = getVerifyCode();
                if (TextUtils.isEmpty(verifyCode)) {
                    playAlertRingtone();
                    return;
                }

                // 输入验证码
                UiObject2 code = mDevice.wait(Until.findObject(By.text("请输入验证码")), WAIT_TIME);
                assertThat("没有找到验证码输入框", code, notNullValue());
                code.setText(verifyCode);
                mDevice.waitForIdle();
                sleep();

                // 点击完成开始验证
                UiObject2 next2 = mDevice.findObject(By.text("完成").enabled(true));
                assertThat("没有找到完成按钮", next2, notNullValue());
                if (next2.clickAndWait(Until.newWindow(), WAIT_TIME)
                        && !mDevice.hasObject(By.text("请求失败"))) {
                    mDevice.waitForIdle();
                    sleep();
                } else {
                    playAlertRingtone();
                    return;
                }

                // 启用手机联系人
                if (mDevice.hasObject(By.text("匹配手机通讯录"))) {
                    UiObject2 ok = mDevice.findObject(By.text("好"));
                    assertThat("没有找到好按钮", ok, notNullValue());
                    ok.clickAndWait(Until.newWindow(), WAIT_TIME);
                    mDevice.wait(Until.hasObject(By.text("正在发送请求")), WAIT_TIME);
                }
            }

            // 获取标题栏位置以便将上次不能添加的联系人拖动至此防止再次添加
            BySelector selector = By.res(PACKAGE, "ivTitleName").text("手机联系人");
            UiObject2 phone = mDevice.wait(Until.findObject(selector), WAIT_TIME * 6);
            final Point point = phone.getVisibleCenter();
            // 当前联系人
            String name = null;

            for (int i = 0; i < addContactsCount; i++) {
                // 添加指定的好友验证数量
                // 将上次不能添加的联系人隐藏
                if (name != null) {
                    UiObject2 source = mDevice.findObject(By.text(name));
                    if (source != null) {
                        source.drag(point, 500);
                        sleep();

                        // 添加到已添加过的联系人列表中
                        addedContactsList.add(name);
                        sp.edit().remove(KEY_ADDED_CONTACTS_LIST).apply();
                        sp.edit().putStringSet(KEY_ADDED_CONTACTS_LIST, addedContactsList).apply();
                    }
                }

                // 开始添加无需回答问题的联系人直到达到数量或无联系人可添加
                UiObject2 button = mDevice.wait(
                        Until.findObject(By.clazz(Button.class).text("添加")), WAIT_TIME);
                if (button != null) {
                    // 获取联系人名称
                    name = button.getParent().findObject(By.clazz(TextView.class)).getText();
                    if (addedContactsList.contains(name)) {
                        // 如果之前已添加过则隐藏并继续添加下一个
                        UiObject2 source = mDevice.findObject(By.text(name));
                        source.drag(point, 500);
                        sleep();
                        i--;
                        continue;
                    }
                    button.clickAndWait(Until.newWindow(), WAIT_TIME * 3);
                    mDevice.waitForIdle();
                    sleep();
                    if (mDevice.hasObject(By.text("必填"))) {
                        // 返回手机联系人
                        UiObject2 back = mDevice.wait(
                                Until.findObject(By.res(PACKAGE, "ivTitleBtnLeft")), WAIT_TIME);
                        assertThat("没有找到返回手机联系人按钮", back, notNullValue());
                        back.click();
                        mDevice.wait(Until.hasObject(selector), WAIT_TIME);
                        i--;
                    } else {
                        BySelector nextSelector = By.res(PACKAGE, "ivTitleBtnRightText").text("下一步");
                        if (mDevice.hasObject(nextSelector)) {
                            // 设置随机问候语
                            String greeting = greetings[new Random().nextInt(greetings.length)];
                            UiObject2 input = mDevice.wait(
                                    Until.findObject(By.clazz(EditText.class)), WAIT_TIME);
                            assertThat("没有找到问候语输入框", input, notNullValue());
                            input.setText(greeting);
                            mDevice.waitForIdle();
                            sleep();

                            // 点击下一步
                            UiObject2 next = mDevice.wait(Until.findObject(nextSelector), WAIT_TIME);
                            assertThat("没有找到下一步按钮", next, notNullValue());
                            next.clickAndWait(Until.newWindow(), WAIT_TIME);
                            mDevice.waitForIdle();
                            sleep();
                        }

                        // 点击发送
                        UiObject2 send = mDevice.findObject(
                                By.res(PACKAGE, "ivTitleBtnRightText").text("发送"));
                        if (send != null) {
                            send.click();
                            mDevice.wait(Until.hasObject(selector), WAIT_TIME);
                            mDevice.wait(Until.gone(By.text(name)), WAIT_TIME);
                        }
                    }

                    // 添加到已添加过的联系人列表中
                    addedContactsList.add(name);
                    sp.edit().remove(KEY_ADDED_CONTACTS_LIST).apply();
                    sp.edit().putStringSet(KEY_ADDED_CONTACTS_LIST, addedContactsList).apply();
                    sleep();
                } else {
                    break;
                }
            }

            // 切卡及数据业务
            switchSimCard(++slot % 2);

            // 再次启动QQ应用
            mHelper.launchApp(PACKAGE);
            mDevice.waitForIdle();
            sleep();

            // 打开抽屉
            while (true) {
                UiObject2 head = mDevice.wait(
                        Until.findObject(By.res(PACKAGE, "conversation_head")), WAIT_TIME);
                if (head != null) {
                    head.click();
                    if (mDevice.wait(Until.hasObject(By.res(PACKAGE, "nickname")), WAIT_TIME)) {
                        mDevice.waitForIdle();
                        sleep();
                        break;
                    }
                }
            }

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
            sp.edit().remove(KEY_ADDED_ACCOUNT_LIST).apply();
            sp.edit().putStringSet(KEY_ADDED_ACCOUNT_LIST, addedAccountList).apply();

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
            if (addedAccountList.contains(last.getText())) {
                break;
            } else {
                last.click();
                mDevice.wait(Until.hasObject(By.text("关联QQ号，同时接收多个帐号的消息。")), WAIT_TIME);
            }
        }
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();

        getContext().unbindService(mServiceConnection);
    }

    private void switchSimCard(int slot) {
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

    private void clearMms() {
        mHelper.launchApp(Mms.PACKAGE);
        mDevice.waitForIdle();
        sleep();
        if (mDevice.hasObject(By.res(Mms.PACKAGE, "mark_sms").enabled(true))) {
            UiObject2 more = mDevice.findObject(By.res(Mms.PACKAGE, "more_menu"));
            assertThat("没有找到更多按钮", more, notNullValue());
            more.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            UiObject2 edit = mDevice.findObject(By.text("编辑"));
            assertThat("没有找到编辑菜单", edit, notNullValue());
            edit.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            UiObject2 multi = mDevice.findObject(
                    By.res(Mms.PACKAGE, "multi_delete_header_checkbox").checked(false));
            if (multi != null) {
                multi.click();
                multi.wait(Until.checked(true), WAIT_TIME);
            }
            UiObject2 delete = mDevice.wait(Until.findObject(
                    By.res(Mms.PACKAGE, "footer_delete_btn").enabled(true)), WAIT_TIME);
            assertThat("没有找到删除按钮", delete, notNullValue());
            delete.clickAndWait(Until.newWindow(), WAIT_TIME);
            mDevice.waitForIdle();
            UiObject2 confirm = mDevice.findObject(By.res(Mms.PACKAGE, "negative"));
            assertThat("没有找到确认按钮", confirm, notNullValue());
            confirm.click();
            mDevice.wait(Until.hasObject(By.res(Mms.PACKAGE, "title_view")), WAIT_TIME);
        }
        mDevice.pressBack();
        mDevice.waitForIdle();
    }

    private String getVerifyCode() {
        mHelper.launchApp(Mms.PACKAGE);
        mDevice.waitForIdle();
        sleep();
        String verifyCode = null;
        Pattern p = Pattern.compile("^(10010008|106575580252304)");
        UiObject2 title = mDevice.wait(Until.findObject(By.res(Mms.PACKAGE, "from").text(p)), 60000);
        if (title != null) {
            UiObject2 layout = title.getParent().getParent();
            UiObject2 subject = layout.findObject(By.res(Mms.PACKAGE, "subject"));
            Matcher m = Pattern.compile("^(.*?(\\d{4,6}).*)$").matcher(subject.getText());
            if (m.find()) {
                verifyCode = m.group(2);
            }
        }
        mDevice.pressBack();
        mDevice.waitForIdle();
        return verifyCode;
    }

    private void sleep() {
        sleep(2000);
    }

    private void sleep(long ms) {
        SystemClock.sleep(ms);
    }
}
