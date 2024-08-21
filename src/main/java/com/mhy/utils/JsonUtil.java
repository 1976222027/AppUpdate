package com.mhy.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONWriter;
import com.mhy.appupdate.UpdateInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * fastjson2 JSON.toJSONString功能Feature
 * QuoteFieldNames	输出key时是否使用双引号,默认为true
 * WriteMapNullValue	是否输出值为null的Map,默认为false
 * WriteNulls	是否输出值为null的字段,默认为false
 * WriteNonStringValueAsString	不是字符串也按字符串输出,默认为false
 * WriteNullNumberAsZero	数值字段如果为null,输出为0,而非null
 * WriteNullListAsEmpty	List字段如果为null,输出为[],而非null
 * WriteNullStringAsEmpty	字符类型字段如果为null,输出为”“,而非null
 * WriteNullBooleanAsFalse	Boolean字段如果为null,输出为false,而非null
 */
public class JsonUtil {
    /**
     * json写入文件
     */
    public static boolean createJsonFile(Object jsonData, String filePath) {

//        String content = JSON.toJSONString(jsonData, JSONWriter.Feature.WriteMapNullValue);//输出值为null的字段
        String content = JSON.toJSONString(jsonData);
        try {
            File file = new File(filePath);
            // 创建上级目录
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            // 如果文件存在，则删除文件
            if (file.exists()) {
                file.delete();
            }
            // 创建文件
            file.createNewFile();
            // 写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
            write.write(content);
            write.flush();
            write.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
