package com.mhy.utils;

import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.ApkMeta;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

/**
 * 获取apk的包名、版本名、版本号、图标等信息
 */
public class ApkUtil {

    /**
     * @param apkPath apk的绝对地址
     */
    public static ApkMeta getApkInfo(String apkPath) {
        File file = new File(apkPath);
        if (file.exists() && file.isFile()) {
            try {
                ApkFile apkFile = new ApkFile(file);
                ApkMeta apkMeta = apkFile.getApkMeta();
                System.out.println("应用名称   :" + apkMeta.getLabel());
                System.out.println("包名       :" + apkMeta.getPackageName());
                System.out.println("版本号     :" + apkMeta.getVersionName());
                System.out.println("图标       :" + apkMeta.getIcon());
                //System.out.println("v2签名       :" + apkFile.getApkV2Singers());
                System.out.println("大小       :" + (double) (file.length() * 100 / 1024 / 1024) / 100 + " MB");
                System.out.println("全部       :===============================");
                System.out.println(apkMeta.toString());
                //  拷贝出的icon文件名 根据需要可以随便改
//                String name = apkMeta.getLabel();
//                //  拷贝图标
//                saveBit(apkPath, apkMeta.getIcon(),"D:/out/icon.png");
                String md5 = getFileMD5(apkPath);
                //用这个字段承载文件大小
                apkMeta.setTargetSdkVersion(String.valueOf(getFileSize(file)));
                //用这个字段承载md5
                apkMeta.setInstallLocation(md5);
                return apkMeta;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //  拷贝图标的存放位置 "E:\\" + name + ".png";
    //  拷贝图标
    public static void saveBit(String apk, String Icon, String fileName) throws IOException {
        ZipInputStream zin = null;

        try {
            //  访问apk 里面的文件
            ZipFile zf = new ZipFile(apk);
            InputStream in = new BufferedInputStream(new FileInputStream(apk));
            zin = new ZipInputStream(in);
            ZipEntry ze;
            while ((ze = zin.getNextEntry()) != null) {
                if (ze.getName().equals(Icon)) {
                    //  拷贝出图标
                    System.out.println("拷贝开始");
                    InputStream inStream = zf.getInputStream(ze);

                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                    //创建一个Buffer字符串
                    byte[] buffer = new byte[1024];
                    //每次读取的字符串长度，如果为-1，代表全部读取完毕
                    int len = 0;
                    //使用一个输入流从buffer里把数据读取出来
                    while ((len = inStream.read(buffer)) != -1) {
                        //用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                        outStream.write(buffer, 0, len);
                    }
                    //关闭输入流
                    inStream.close();
                    //把outStream里的数据写入内存

                    //得到图片的二进制数据，以二进制封装得到数据，具有通用性
                    byte[] data = outStream.toByteArray();
                    //new一个文件对象用来保存图片，默认保存当前工程根目录
                    File imageFile = new File(fileName);
                    //创建输出流
                    FileOutputStream fileOutStream = new FileOutputStream(imageFile);
                    //写入数据
                    fileOutStream.write(data);
                    System.out.println("拷贝结束");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            zin.closeEntry();
        }
    }

//    public static String getMD5(String path) {
//        String md5Hex = "";
//        try {
//            md5Hex = DigestUtils.md5Hex(new FileInputStream(path));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return md5Hex;
//    }

    public static String getFileMD5(String file) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md5.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md5.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getFileMD5(File file) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int length = -1;
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md5.update(buffer, 0, length);
            }
            BigInteger bigInt = new BigInteger(1, md5.digest());
            return bigInt.toString(16);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 获取文件长度 字节
     *
     * @param file
     */
    public static long getFileSize(File file) {
        if (file.exists() && file.isFile()) {
            //String fileName = file.getName();
            return file.length();
        }
        return 0L;
    }

    //第二种方式:使用BuffredReader和BufferedWriter apkMd5
    public static void writeFile(String msg, File outFile) {
        try {
            //如果文件不存在，创建文件
            if (!outFile.exists())
                outFile.createNewFile();
            //创建BufferedWriter对象并向文件写入内容
            BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
            //向文件中写入内容
            bw.write(msg);
            bw.flush();
            bw.close();
        } catch (Exception e) {
        }
    }

    public static String readFile(File file) {
        try {
            StringBuilder builder = new StringBuilder();
            //创建BufferedReader读取文件内容
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                builder.append(line);
            }
            br.close();
            return builder.toString();
        } catch (Exception e) {
            return "";
        }
    }

}
