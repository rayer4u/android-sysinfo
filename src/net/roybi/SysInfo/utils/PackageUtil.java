package net.roybi.SysInfo.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ComponentInfo;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseArray;

public class PackageUtil {
    /**
     * parsePackage
     * @param archiveFilePath
     * @return  PackageParser.Package
     */
    public static Object parsePackage(String archiveFilePath) {
        Object ret = null;
        try {
            Class<?> cls_PackageParser = Class.forName("android.content.pm.PackageParser");
            
            Constructor<?> constructor = cls_PackageParser.getDeclaredConstructor(String.class);
            Object pp = constructor.newInstance(new Object[] { archiveFilePath });
            
            Method mtd_parsePackage = cls_PackageParser.getDeclaredMethod("parsePackage", File.class, String.class, DisplayMetrics.class, int.class);
            mtd_parsePackage.setAccessible(true);
            File sourceFile = new File(archiveFilePath);
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            ret = mtd_parsePackage.invoke(pp, sourceFile, archiveFilePath, metrics, 0);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }  

        return ret;
    }
    
    public static ArrayList<Entry<ServiceInfo, ArrayList<? extends IntentFilter>>> getServiceIntentFilter(Object apackage) {
        ArrayList<Entry<ServiceInfo, ArrayList<? extends IntentFilter>>> ret = new ArrayList<Entry<ServiceInfo, ArrayList<? extends IntentFilter>>>();
        try {
            Class<?> cls_Package = Class.forName("android.content.pm.PackageParser$Package");
            Field fld_activities = cls_Package.getField("services");
            Class<?> cls_Activity = Class.forName("android.content.pm.PackageParser$Service");
            Field fld_intents = cls_Activity.getField("intents");
            Field fld_info = cls_Activity.getField("info");
            ArrayList<?> activities = (ArrayList<?>) fld_activities.get(apackage);
            if (activities != null) {
                for (int i = 0; i < activities.size(); i++) {
                    Object activity = activities.get(i);
                    ServiceInfo info = (ServiceInfo) fld_info.get(activity);
                    ArrayList<? extends IntentFilter> intents = (ArrayList<? extends IntentFilter>) fld_intents.get(activity);
                    if (intents == null) {
                        intents = new ArrayList<IntentFilter>();
                    }
                    ret.add(new AbstractMap.SimpleEntry<ServiceInfo, ArrayList<? extends IntentFilter>>(info, intents));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public static ArrayList<Entry<ProviderInfo, ArrayList<? extends IntentFilter>>> getProviderIntentFilter(Object apackage) {
        ArrayList<Entry<ProviderInfo, ArrayList<? extends IntentFilter>>> ret = new ArrayList<Entry<ProviderInfo, ArrayList<? extends IntentFilter>>>();
        try {
            Class<?> cls_Package = Class.forName("android.content.pm.PackageParser$Package");
            Field fld_activities = cls_Package.getField("providers");
            Class<?> cls_Activity = Class.forName("android.content.pm.PackageParser$Provider");
            Field fld_intents = cls_Activity.getField("intents");
            Field fld_info = cls_Activity.getField("info");
            ArrayList<?> activities = (ArrayList<?>) fld_activities.get(apackage);
            if (activities != null) {
                for (int i = 0; i < activities.size(); i++) {
                    Object activity = activities.get(i);
                    ProviderInfo info = (ProviderInfo) fld_info.get(activity);
                    ArrayList<? extends IntentFilter> intents = (ArrayList<? extends IntentFilter>) fld_intents.get(activity);
                    if (intents == null) {
                        intents = new ArrayList<IntentFilter>();
                    }
                    ret.add(new AbstractMap.SimpleEntry<ProviderInfo, ArrayList<? extends IntentFilter>>(info, intents));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public static ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>> getActivityIntentFilter(Object apackage) {
        ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>> ret = new ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>>();
        try {
            Class<?> cls_Package = Class.forName("android.content.pm.PackageParser$Package");
            Field fld_activities = cls_Package.getField("activities");
            Class<?> cls_Activity = Class.forName("android.content.pm.PackageParser$Activity");
            Field fld_intents = cls_Activity.getField("intents");
            Field fld_info = cls_Activity.getField("info");
            ArrayList<?> activities = (ArrayList<?>) fld_activities.get(apackage);
            if (activities != null) {
                for (int i = 0; i < activities.size(); i++) {
                    Object activity = activities.get(i);
                    ActivityInfo info = (ActivityInfo) fld_info.get(activity);
                    ArrayList<? extends IntentFilter> intents = (ArrayList<? extends IntentFilter>) fld_intents.get(activity);
                    if (intents == null) {
                        intents = new ArrayList<IntentFilter>();
                    }
                    ret.add(new AbstractMap.SimpleEntry<ActivityInfo, ArrayList<? extends IntentFilter>>(info, intents));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
    
    public static ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>> getReceiverIntentFilter(Object apackage) {
        ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>> ret = new ArrayList<Entry<ActivityInfo, ArrayList<? extends IntentFilter>>>();
        try {
            Class<?> cls_Package = Class.forName("android.content.pm.PackageParser$Package");
            Field fld_activities = cls_Package.getField("receivers");
            Class<?> cls_Activity = Class.forName("android.content.pm.PackageParser$Activity");
            Field fld_intents = cls_Activity.getField("intents");
            Field fld_info = cls_Activity.getField("info");
            ArrayList<?> activities = (ArrayList<?>) fld_activities.get(apackage);
            if (activities != null) {
                for (int i = 0; i < activities.size(); i++) {
                    Object activity = activities.get(i);
                    ActivityInfo info = (ActivityInfo) fld_info.get(activity);
                    ArrayList<? extends IntentFilter> intents = (ArrayList<? extends IntentFilter>) fld_intents.get(activity);
                    if (intents == null) {
                        intents = new ArrayList<IntentFilter>();
                    }
                    ret.add(new AbstractMap.SimpleEntry<ActivityInfo, ArrayList<? extends IntentFilter>>(info, intents));
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        
        return ret;
    }
}
