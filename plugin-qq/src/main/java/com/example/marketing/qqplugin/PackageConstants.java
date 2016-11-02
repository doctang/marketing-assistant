package com.example.marketing.qqplugin;

import com.example.marketing.common.PackageResources;

import static android.support.test.InstrumentationRegistry.getTargetContext;

public class PackageConstants {

    public static final class Settings {
        public static final String PACKAGE = "com.android.settings";
        public static final PackageResources R = new PackageResources(getTargetContext(), PACKAGE);
    }

    public static final class Stk {
        public static final String PACKAGE = "com.android.stk";
        public static final PackageResources R = new PackageResources(getTargetContext(), PACKAGE);
    }

    public static final class Mms {
        public static final String PACKAGE = "com.android.mms";
        public static final PackageResources R = new PackageResources(getTargetContext(), PACKAGE);
    }

    public static final class MobileQQ {
        public static final String PACKAGE = "com.tencent.mobileqq";
        public static final PackageResources R = new PackageResources(getTargetContext(), PACKAGE);
    }
}
