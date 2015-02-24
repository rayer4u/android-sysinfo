package net.roybi.SysInfo.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.util.DisplayMetrics;

import com.google.code.microlog4android.Logger;
import com.google.code.microlog4android.LoggerFactory;

// http://blog.csdn.net/wulianghuan/article/details/18400581
public class SignatureGettter {
    public static Signature getAPKSignInfo(String apkPath) {
        String PATH_PackageParser = "android.content.pm.PackageParser";
        try {
            // apk包的文件路径
            // 这是一个Package 解释器, 是隐藏的
            // 构造函数的参数只有一个, apk文件的路径
            // PackageParser packageParser = new PackageParser(apkPath);
            Class pkgParserCls = Class.forName(PATH_PackageParser);
            Class[] typeArgs = new Class[1];
            typeArgs[0] = String.class;
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);
            Object[] valueArgs = new Object[1];
            valueArgs[0] = apkPath;
            Object pkgParser = pkgParserCt.newInstance(valueArgs);
            Logger logger = LoggerFactory.getLogger(SignatureGettter.class.getName());
            logger.debug("pkgParser:" + pkgParser.toString());
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况
            DisplayMetrics metrics = new DisplayMetrics();
            metrics.setToDefaults();
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new
            // File(apkPath), apkPath,
            // metrics, 0);
            typeArgs = new Class[4];
            typeArgs[0] = File.class;
            typeArgs[1] = String.class;
            typeArgs[2] = DisplayMetrics.class;
            typeArgs[3] = Integer.TYPE;
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",
                    typeArgs);
            valueArgs = new Object[4];
            valueArgs[0] = new File(apkPath);
            valueArgs[1] = apkPath;
            valueArgs[2] = metrics;
            valueArgs[3] = PackageManager.GET_SIGNATURES;
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);

            typeArgs = new Class[2];
            typeArgs[0] = pkgParserPkg.getClass();
            typeArgs[1] = Integer.TYPE;
            Method pkgParser_collectCertificatesMtd = pkgParserCls.getDeclaredMethod(
                    "collectCertificates", typeArgs);
            valueArgs = new Object[2];
            valueArgs[0] = pkgParserPkg;
            valueArgs[1] = PackageManager.GET_SIGNATURES;
            pkgParser_collectCertificatesMtd.invoke(pkgParser, valueArgs);
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开
            Field packageInfoFld = pkgParserPkg.getClass().getDeclaredField("mSignatures");
            Signature[] info = (Signature[]) packageInfoFld.get(pkgParserPkg);
            logger.debug("size:" + info.length);
            logger.debug(info[0].toCharsString());
            return info[0];
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Signature getMySignInfo(Context context) {
        return getSignInfo(context, context.getPackageName());
    }
    
    public static Signature getSignInfo(Context context, String packagename) {  
        try {  
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packagename, PackageManager.GET_SIGNATURES);  
            Signature[] signs = packageInfo.signatures;  
            return signs[0]; 
        } catch (Exception e) {  
            e.printStackTrace();  
            return null;
        }  
    }  
    
    public static X509Certificate parseSignature(Signature sign) {  
        try {  
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");  
            return (X509Certificate) certFactory.generateCertificate(new ByteArrayInputStream(sign.toByteArray()));  
//            String pubKey = cert.getPublicKey().toString();  
//            String signNumber = cert.getSerialNumber().toString();  
//            System.out.println("signName:" + cert.getSigAlgName());  
//            System.out.println("pubKey:" + pubKey);  
//            System.out.println("signNumber:" + signNumber);  
//            System.out.println("subjectDN:"+cert.getSubjectDN().toString());  
        } catch (CertificateException e) {  
            e.printStackTrace();  
            return null;
        }  
    }
    
    public static String getFingerprint(byte[] cert) {  
        String hexString = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] publicKey = md.digest(cert);
            hexString = byte2HexFormatted(publicKey);
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
        }
        return hexString;
    }
    
    public static String byte2HexFormatted(byte[] arr) {
        StringBuilder str = new StringBuilder(arr.length * 2);
        for (int i = 0; i < arr.length; i++) {
            String h = Integer.toHexString(arr[i]);
            int l = h.length();
            if (l == 1) h = "0" + h;
            if (l > 2) h = h.substring(l - 2, l);
            str.append(h.toUpperCase());
            if (i < (arr.length - 1)) str.append(':');
        }
        return str.toString();
    }
}
