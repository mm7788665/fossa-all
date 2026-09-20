package com.txy.util;



import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通用工具类
 * author:panjianping
 */

@SuppressWarnings("unchecked")
public class CommonUtil {

    // 默认日期格式
    public static final String DATE_FMT = "yyyy-MM-dd"; // 日期

    public static final String TIME_FMT = "HH:mm:ss"; // 时间

    public static final String DATE_TIME_FMT = "yyyy-MM-dd HH:mm:ss"; // 日期时间
    // 验证的正则表达式
    private static final String REG_ALPHA = "^[a-zA-Z]+$";

    private static final String REG_ALPHANUM = "^[a-zA-Z0-9]+$";

    private static final String REG_NUMBER = "^\\d+$";

    private static final String REG_INTEGER = "^[-+]?[1-9]\\d*$|^0$/";

    private static final String REG_FLOAT = "[-\\+]?\\d+(\\.\\d+)?$";

    private static final String REG_PHONE = "^((\\(\\d{2,3}\\))|(\\d{3}\\-))?(\\(0\\d{2,3}\\)|0\\d{2,3}-)?[1-9]\\d{6,7}(\\-\\d{1,4})?$";

    private static final String REG_MOBILE = "^((\\+86)|(86))?(1)\\d{10}$";

    private static final String REG_QQ = "^[1-9]\\d{4,10}$";

    private static final String REG_EMAIL = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";

    private static final String REG_ZIP = "^[1-9]\\d{5}$";

    private static final String REG_IP = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";

    private static final String REG_URL = "^(http|https|ftp):\\/\\/(([A-Z0-9][A-Z0-9_-]*)(\\.[A-Z0-9][A-Z0-9_-]*)+)(:(\\d+))?\\/?/i";

    private static final String REG_CHINESE = "^[\\u0391-\\uFFE5]+$";

    private static final String REG_MONEY = "[\\-\\+]?\\d+(\\.\\d+)?$";

    /**
     * 通过正则表达验证
     */
    public static boolean matchesByRegex(String regex, String value) {
        if (isNull(regex, value)) {
            return false;
        }
        return Pattern.matches(regex, value);
    }

    /**
     * 可以用于判断Object,String,Map,Collection,String,Array是否为空
     */
    public static boolean isNull(Object value) {
        if (value == null) {
            return true;
        } else if (value instanceof String) {
            if (((String) value).trim().replaceAll("\\s", "").equals("")) {
                return true;
            }
        } else if (value instanceof Collection) {
            if (((Collection) value).isEmpty()) {
                return true;
            }
        } else if (value.getClass().isArray()) {
            if (Array.getLength(value) == 0) {
                return true;
            }
        } else if (value instanceof Map) {
            if (((Map) value).isEmpty()) {
                return true;
            }
        } else {
            return false;
        }
        return false;

    }

    public static boolean isNull(Object value, Object... items) {
        if (isNull(value) || isNull(items)) {
            return true;
        }
        for (Object item : items) {
            if (isNull(item)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNotNull(Object value) {

        return !isNull(value);
    }

    public static boolean isNotNull(Object value, Object... items) {

        return !isNull(value, items);
    }

    /**
     * 如果值是NULL 返回 ""空字符串 否则返回自身
     *
     * @param value
     * @return
     */
    public static String toString(String value) {

        return ((value == null) ? "" : value);
    }

    /**
     * 将数组转换成List<Map<String,Object>>对象
     *
     * @param array 要转换的数组
     * @return List<Map<String,Objct>>
     */
    public static List<Map<String, Object>> arrayToList(String[] array) {
        List<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
        for (int i = 0; i < array.length; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("value", i);
            item.put("text", array[i]);
            items.add(item);
        }
        return items;
    }

    /**
     * linwenming Mar 30, 2012
     *
     * @return Map<String,Object>
     */
    public static Map<String, Object> arrayToMap(String[] array) {
        Map<String, Object> maps = new HashMap<String, Object>();
        for (int i = 0; i < array.length; i++) {
            maps.put(String.valueOf(i), array[i]);
        }
        return maps;
    }

    public static Map<String, Object> arrToMap(String[] array) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < array.length; i++) {
            map.put(array[i], array[i]);
        }
        return map;
    }

    public static boolean isAlpha(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_ALPHA, value);
    }

    public static boolean isAlphanum(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_ALPHANUM, value);
    }

    public static boolean isInteger(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_INTEGER, value);
    }

    public static boolean isFloat(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_FLOAT, value);
    }

    public static boolean isMoney(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_MONEY, value);
    }

    public static boolean isPhone(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_PHONE, value);
    }

    public static boolean isMobile(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_MOBILE, value);
    }

    public static boolean isEmail(String value) {
        if (isNull(value)) return false;
        return Pattern.matches(REG_EMAIL, value);
    }

    public static boolean isQQ(String value) {

        return Pattern.matches(REG_QQ, value);
    }

    public static boolean isZip(String value) {

        return Pattern.matches(REG_ZIP, value);
    }

    public static boolean isIP(String value) {

        return Pattern.matches(REG_IP, value);
    }

    public static boolean isURL(String value) {

        return Pattern.matches(REG_URL, value);
    }

    public static boolean isChinese(String value) {

        return Pattern.matches(REG_CHINESE, value);
    }

    /**
     * 验证是否为合法身份证
     */
    public static boolean isIdcard(String value) {
        value = value.toUpperCase();
        if (!(Pattern.matches("^\\d{17}(\\d|X)$", value) || Pattern.matches("\\d{15}$", value))) {
            return false;
        }
        int provinceCode = Integer.parseInt(value.substring(0, 2));
        if (provinceCode < 11 || provinceCode > 91) {
            return false;
        }
        return true;
    }

    public static boolean isDate(String value) {
        if (value == null || value.isEmpty())
            return false;
        try {
            new SimpleDateFormat().parse(value);
            return true;
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static String formatDate(String fmt, Date date) {
        if (isNull(fmt) || isNull(date)) {
            return null;
        }
        String temp = new SimpleDateFormat(fmt).format(date);

        return temp;
    }


    public static String formatNumber(String fmt, Object value) {
        if (isNull(fmt) || isNull(value)) {
            return null;
        }
        String temp = new DecimalFormat(fmt).format(value);

        return temp;
    }

    /**
     * MD5加密
     */
    public static String md5(String value) {
        StringBuilder result = new StringBuilder();

        try {
            // 实例化MD5加载类
            MessageDigest md5 = MessageDigest.getInstance("MD5");

            // 得到字节数据
            byte[] data = md5.digest(value.getBytes("UTF-8"));

            result.append(byte2hex(data));

        } catch (Exception e) {
            e.printStackTrace();
        }

        // 返回结果
        return result.toString().toUpperCase();
    }

    public static String byte2hex(byte[] data) {

        StringBuilder result = new StringBuilder();

        for (byte b : data) {
            // 将二进制转换成字符串
            String temp = Integer.toHexString(b & 0XFF);
            // 追加加密后的内容
            if (temp.length() == 1) { // 判断字符长度
                result.append("0").append(temp);
            } else {
                result.append(temp);
            }
        }

        return result.toString();
    }

    public static String substring(String str, int len) {

        return substring(str, len, null);
    }

    public static String substring(String str, int len, String replaceChar) {

        return substring(str, 0, len, replaceChar);
    }

    public static String substring(String str, int startIndex, int len, String replaceChar) {
        String temp = str;

        if (!isNull(str) && str.length() > len) {
            temp = str.substring(startIndex, len + startIndex) + (isNull(replaceChar) ? "" : replaceChar);
        }

        return temp;
    }

    public static String htmlEncode(String value) {
        String result = "";
        if (!isNull(value)) {
            result = value.replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;").replaceAll(" ", "&nbsp;").replaceAll("\r?\n", "<br/>");
        }
        return result;
    }

    public static String htmlDecode(String value) {
        String result = "";
        if (!isNull(value)) {
            result = value.replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("&quot;", "\"").replace("&nbsp;", " ");
        }
        return result;
    }

    /**
     * 字符串编码(默认使用UTF-8)
     */
    public static String stringEncode(String value) {
        return stringEncode(value, "UTF-8");
    }

    public static String stringEncode(String value, String encoding) {
        String result = null;
        if (!isNull(value)) {
            try {
                if (isNull(encoding)) {
                    encoding = "UTF-8";
                }
                result = new String(value.getBytes("ISO-8859-1"), encoding);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 格式式化字符串
     * 允许使用{0}{1}...作为为占位符
     *
     * @param value 要格式化的字符串
     * @param args  点位符的值
     * @return 格式化后的字符串
     */
    public static String stringFormat(String value, Object... args) {
        // 判断是否为空
        if (isNull(value) || isNull(args)) {
            return value;
        }
        String result = value;
        Pattern p = Pattern.compile("\\{(\\d+)\\}");
        Matcher m = p.matcher(value);
        while (m.find()) {
            // 获取{}里面的数字作为匹配组的下标取值
            int index = Integer.parseInt(m.group(1));
            // 这里得考虑数组越界问题，{1000}也能取到值么？？
            if (index < args.length) {
                // 替换，以{}数字为下标，在参数数组中取值
                result = result.replace(m.group(), args[index].toString());
            }
        }
        return result;
    }

    public static String leftPad(String value, int len, char c) {
        if (isNull(value, len, c)) {
            return value;
        }
        int v = len - value.length();
        for (int i = 0; i < v; i++)
            value = c + value;
        return value;
    }

    public static String rightPad(String value, int len, char c) {
        if (isNull(value, len, c)) {
            return value;
        }
        int v = len - value.length();
        for (int i = 0; i < v; i++)
            value += c;
        return value;
    }

    /**
     * 处理对象的String类型的属性值进行html编码
     */
    public static void objectHtmlEncode(Object object) {
        if (!isNull(object)) {
            Method[] mList = object.getClass().getMethods();
            for (Method method : mList) {
                // 方法名
                String mName = method.getName();
                // 得到方法的方法值类型
                String mRetrunType = method.getReturnType().getSimpleName();
                // 得到方法的参数个数
                int mParamSize = method.getParameterTypes().length;
                // 判断方法值是否是String并参数个数为0
                if (mRetrunType.equals("String") && mParamSize == 0) {
                    // 判断方法是否是以get开头
                    if (mName.startsWith("get")) {
                        // 得到相对应的set方法
                        Method setMethod = null;
                        String setMethodName = "set" + mName.substring(3);
                        // 只有一个String类型的参数
                        Class[] paramClass = {String.class};
                        try {
                            setMethod = object.getClass().getMethod(setMethodName, paramClass);
                            // 判断set方法的返回值是否为空
                            if (!setMethod.getReturnType().getSimpleName().equals("void")) {
                                continue; // 查看下一个方法
                            }
                        } catch (SecurityException e) {
                            continue; // 查看下一个方法
                        } catch (NoSuchMethodException e) {
                            continue; // 查看下一个方法
                        }
                        Object[] params = null;
                        try {
                            // 得到方法的值
                            String mValue = method.invoke(object, params).toString();
                            // 对值进行html编码
                            mValue = htmlEncode(mValue);
                            // 编码后重新赋值
                            params = new Object[]{mValue};
                            setMethod.invoke(object, params);
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        } catch (InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    /**
     * 根据属性名得到属性值(entity中必需存在get,set相应方法)
     */
    public static Object getPropValue(Object entity, String propName) {
        Object result = null;
        //判断对象和属性名是否为空
        if (isNull(entity) || isNull(propName)) {
            return result;
        } else {
            try {
                //调用方法得到get方法值
                Method getMethod = entity.getClass().getMethod(propName.trim());
                result = getMethod.invoke(entity);
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 生成随机UUID
     */
    public static String genUUID() {
        UUID uuid = UUID.randomUUID();
        String temp = uuid.toString();
        return temp.replaceAll("-", "").toUpperCase();
    }

    /**
     * 随机数字
     */
    public static String getRandNum(int len) {
        String result = "";
        String temp = genUUID().replaceAll("[A-Z]+", "");

        int tempLen = temp.length();
        if (len > 32) {
            len = 32;
        }
        if (tempLen < len) {
            result = rightPad(temp, len, '0');
        } else {
            for (int i = 0; i < len; i++) {
                int rnd = new Random().nextInt(tempLen);
                result += temp.charAt(rnd);
            }
        }

        return result;
    }

    /**
     * 使用正则表达查询字符串
     */
    public static List<String> findStr(Object target, String regex) {
        if (isNull(target, regex)) {
            return null;
        }
        Pattern pattern = Pattern.compile(regex);        //正则表达式
        Matcher matcher = pattern.matcher(target.toString());    //操作的字符串
        List<String> tmp = new ArrayList<String>();
        while (matcher.find()) {
            tmp.add(matcher.group());
        }
        return tmp;
    }

    /**
     * 直接删除非空目录
     *
     * @param dir File对象
     */
    public static void deleteDirectory(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory())
            return; // 检查参数
        for (File file : dir.listFiles()) {
            if (file.isFile())
                file.delete(); // 删除所有文件
            else if (file.isDirectory())
                deleteDirectory(file); // 递规的方式删除文件夹
        }
        dir.delete();// 删除目录本身
    }

    /**
     * 直接删除非空目录
     *
     * @param dirPath 要删除的目录的绝对路径
     */
    public static void deleteDirectory(String dirPath) {
        File dir = new File(dirPath);
        deleteDirectory(dir);
    }


    /**
     * 隐藏部分验证码数据
     */
    public static String hideCode(String isHide, String target) {
        // 0.不隐藏 1.隐藏
        if (!"0".equals(isHide)) {
            return target;
        }

        int code_len = 0;
        if (target != null) {
            target = target.replaceAll("\\s", "");
            code_len = target.length();
        } else {
            return "";
        }

        if (code_len > 8) {
            String s1 = target.substring(0, 4);
            String s2 = target.substring(4, code_len - 4).replaceAll("\\w", "*");
            String s3 = target.substring(code_len - 4, code_len);
            if (s2.length() > 6) {
                s2 = s2.substring(0, 6);
            }
            String code = s1 + s2 + s3;

            return code;
        } else {
            return target.replaceAll("\\w", "*");
        }
    }

    /**
     * 隐藏部分验证码数据
     */
    public static String hideCode(String target) {
        return hideCode("0", target);
    }

    /**
     * 通过文件相对路径得到Web项目下的绝对路径
     */
    public static String getPath(String path) {
        URL url = CommonUtil.class.getResource(path);
        if (url != null) {
            String abs_path = CommonUtil.class.getResource(path).toString();
            abs_path = abs_path.substring(abs_path.indexOf("/") + 1);
            return abs_path;
        }
        return null;
    }

    /**
     * 获取项目物理路径
     */
    public static String getProjectPath() {
        String path = CommonUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        if (path.indexOf("WEB-INF") > 0) {
            path = path.substring(0, path.indexOf("/WEB-INF"));
        }
        if (!path.endsWith("/")) {
            path = path + "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path;
    }

    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    public static Double formatDouble(Double value) {
        if (value == null)
            return null;
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    /**
     * 保留两位小数
     *
     * @param value
     * @return
     */
    public static Double formatDoubleDefaultZero(Double value) {
        if (value == null)
            return 0D;
        BigDecimal bg = new BigDecimal(value);
        return bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    /**
     * 检查两个日期是否是同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        return (cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * 版本比较
     *
     * @param currentVersion 最新系统版本(V1.1.1格式)
     * @param comparaVer     待比较版本(V1.1.1格式)
     * @return 0:没有最新版本,1：有最新版
     */
    public static int comparaVersion(String currentVersion, String comparaVer) {
        int reVal = 0;
        if (isNull(currentVersion) || isNull(comparaVer)) {
            return reVal;
        }
        currentVersion = currentVersion.substring(1, currentVersion.length());
        comparaVer = comparaVer.substring(1, comparaVer.length());

        String[] cArr = currentVersion.split("\\.");
        String[] oArr = comparaVer.split("\\.");

        int c1 = Integer.parseInt(cArr[0]);
        int c2 = Integer.parseInt(cArr[1]);

        int o1 = Integer.parseInt(oArr[0]);
        int o2 = Integer.parseInt(oArr[1]);

        if (c1 > o1) {//有新版本
            reVal = 1;
        }
        if (c1 == o1 && c2 > o2) {//有新版本
            reVal = 1;
        }
        return reVal;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V>
    sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }


    /**
     * 计算倒计时字符串
     *
     * @return
     */
    public static String getCountDown(long millseconds) {

        long ts = (millseconds) / 1000;

        long dd = (ts / 60 / 60 / 24);//计算剩余的天数
        long hh = (ts / 60 / 60 % 24);//计算剩余的小时数
        long mm = (ts / 60 % 60);//计算剩余的分钟数
        long ss = (ts % 60);//计算剩余的秒数

        String countDown = cdStr(dd) + "天" + cdStr(hh) + ": " + cdStr(mm) + ": " + cdStr(ss);
        return countDown;

    }

    private static String cdStr(long t) {
        String ts;
        if (t < 10) {
            ts = "0" + t;
        } else {
            ts = t + "";
        }
        return ts;
    }

    /**
     * 构建一个支持排序的临时表， 升序和ids 的顺序一样
     *
     * @param tableName 表名
     * @param colName   字段名
     * @param sortName  排序字段名 值就是 ids集合里值对应的下标
     * @param ids
     * @return
     */
    public static String buildSortTempTableQuerySqlForLong(String tableName, String colName, String sortName, List<Long> ids) {
        if (StringUtils.isEmpty(tableName) || StringUtils.isEmpty(colName)
                || StringUtils.isEmpty(sortName) || CommonUtil.isNull(ids)) {
            return null;
        }
        StringBuffer sql = new StringBuffer("SELECT ");
        sql.append(tableName).append(".").append(colName).append(" , ").append(tableName).append(".").append(sortName).append(" FROM (");
        int i = 0;
        for (Long id : ids) {
            if (i == 0) {
                sql.append(" SELECT ").append(id).append(" AS ").append(colName).append(" ,").append(i).append(" AS ").append(sortName);
            } else {
                sql.append(" UNION ALL SELECT ").append(id).append(" ,").append(i).append(" ");
            }
            i++;
        }
        sql.append(" ) ").append(tableName);

        return sql.toString();
    }

    /**
     * 根据String List集合转换成逗号分隔字符串
     *
     * @param lists 门店集合
     * @return 逗号分割的字符串
     */
    public static String tranListToStrings(List<String> lists) {
        String idStr = "";
        if (CommonUtil.isNotNull(lists)) {
            for (int i = lists.size() - 1; i >= 0; i--) {
                idStr += "'" + lists.get(i) + "'";
                if (i != 0) {
                    idStr += ",";
                }
            }
        }
        return idStr;
    }

    /**
     * 将一个list分隔成多个list
     *
     * @param list      需要分隔的list
     * @param splitSize 分隔大小
     * @return 分隔后的多个list放在这个map的value中，key从0开始，如果分隔后的list为3个，那么key分别为0、1、2
     */
    public static <T> Map<Integer, List<T>> splitList(List<T> list, int splitSize) {
        Map<Integer, List<T>> listBatchMap = new HashMap<Integer, List<T>>();

        int listSize = list.size();
        int batchSize = listSize / splitSize;      // 总批数
        if (listSize % splitSize > 0) {
            batchSize += 1;
        }

        for (int i = 0; i < batchSize; i++) {
            int start = i * splitSize;
            int end = (i + 1) * splitSize;
            if (end > listSize) {
                end = listSize;
            }
            List batchList = list.subList(start, end);
            listBatchMap.put(i, batchList);
        }
        return listBatchMap;
    }

    public static boolean isNumber(String value) {
        if (isNull(value)) return false;
        return NumberUtils.isNumber(value);
    }

    public static boolean isEmojiCharacter(char codePoint) {
        return (codePoint == 0x0) ||

                (codePoint == 0x9) ||

                (codePoint == 0xA) ||

                (codePoint == 0xD) ||

                ((codePoint >= 0x20) && (codePoint <= 0xD7FF)) ||

                ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) ||

                ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));

    }

    public static Long intToLong(Integer num) {
        if (num == null) {
            return null;
        }
        return Long.parseLong(String.valueOf(num));
    }

    /**
     * 格式化日期
     */
    public static String formatDateTime(Date date) {

        return formatDateTime(DATE_TIME_FMT, date);
    }

    public static String formatDateTime(String fmt, Date date) {
        if (isNull(fmt) || isNull(date)) {
            return null;
        }
        String temp = new SimpleDateFormat(fmt).format(date);

        return temp;
    }

    public static String dateToDateStr(Date date) {

        String temp = new SimpleDateFormat(DATE_TIME_FMT).format(date);

        return temp;
    }

    public static String dateToDateStr(String fmt, Date date) {

        String temp = new SimpleDateFormat(fmt).format(date);

        return temp;
    }

    /**
     * 获取日期
     */
    public static Date getCurrentDateTime() {

        return getCurrentDateTime(DATE_TIME_FMT);
    }

    public static Date getCurrentDateTime(String fmt) {

        return dateStrToDate(fmt, getCurrentDateTimeStr(fmt));
    }

    public static String getCurrentDateTimeStr(String fmt) {

        String temp = new SimpleDateFormat(fmt).format(new Date());

        return temp;
    }

    public static Date dateStrToDate(String fmt, String date) {
        Date temp = null;
        try {
            temp = new SimpleDateFormat(fmt).parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return temp;
    }

    /**
     * 设置年、月、日
     */
    public static Date setYMD(Date date, int year, int month, int day) {

        return setYear(setMonth(setDate(date, day), month), year);
    }

    public static Date setYear(Date date, int num) {
        return setDateTime(date, 1, num);
    }

    public static Date setMonth(Date date, int num) {
        return setDateTime(date, 2, num);
    }

    public static Date setDate(Date date, int num) {
        return setDateTime(date, 3, num);
    }

    /**
     * 设置时、分、秒
     */
    public static Date setHMS(Date date, int hour, int minute, int second) {

        return setHour(setMinute(setSecond(date, second), minute), hour);
    }

    public static Date setHour(Date date, int num) {
        return setDateTime(date, 4, num);
    }

    public static Date setMinute(Date date, int num) {
        return setDateTime(date, 5, num);
    }

    public static Date setSecond(Date date, int num) {
        return setDateTime(date, 6, num);
    }

    /**
     * 添加年、月、日、时、分、秒
     */
    public static Date addYear(Date date, int num) {
        return addDateTime(date, 1, num);
    }

    public static Date addMonth(Date date, int num) {
        return addDateTime(date, 2, num);
    }

    public static Date addDate(Date date, int num) {
        return addDateTime(date, 3, num);
    }

    /**
     * 添加年、月、日
     */
    public static Date addYMD(Date date, int year, int month, int day) {

        return addYear(addMonth(addDate(date, day), month), year);
    }

    public static Date addHour(Date date, int num) {
        return addDateTime(date, 4, num);
    }

    public static Date addMinute(Date date, int num) {
        return addDateTime(date, 5, num);
    }

    public static Date addSecond(Date date, int num) {
        return addDateTime(date, 6, num);
    }

    /**
     * 添加时、分、秒
     */
    public static Date addHMS(Date date, int hour, int minute, int second) {

        return addHour(addMinute(addSecond(date, second), minute), hour);
    }

    //设置日期时间
    private static Date setDateTime(Date date, int type, int num) {
        if (date == null) {
            return null;
        }
        //初始化日历对象
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        //根据类型添加
        switch (type) {
            case 1:        //添加年
                cal.set(Calendar.YEAR, num);
                break;
            case 2:        //添加月
                cal.set(Calendar.MONTH, num);
                break;
            case 3:        //添加日
                cal.set(Calendar.DATE, num);
                break;
            case 4:        //添加时
                cal.set(Calendar.HOUR_OF_DAY, num);
                break;
            case 5:        //添加分
                cal.set(Calendar.MINUTE, num);
                break;
            case 6:        //添加秒
                cal.set(Calendar.SECOND, num);
                break;
        }

        //返回操作结果
        return cal.getTime();
    }

    /**
     * 比较两个日期相差的天数
     */
    public static int compareDay(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return 0;
        Calendar d1 = Calendar.getInstance();
        d1.setTime(date1);
        Calendar d2 = Calendar.getInstance();
        d2.setTime(date2);
        if (d1.after(d2)) {
            Calendar swap = d1;
            d1 = d2;
            d2 = swap;
        }
        /*
		 * 经过上面的处理，保证d2在d1之后
		 * 下面这个days可能小于0，因为d2和d1可能不在同一年里，这样的话虽然d1的年份小，但其在一年中的"第几天"却可能比d2大。
		 */
        int days = d2.get(Calendar.DAY_OF_YEAR) - d1.get(Calendar.DAY_OF_YEAR);
        int y2 = d2.get(Calendar.YEAR);
        if (d1.get(Calendar.YEAR) != y2) {//如果不在同一年
            d1 = (Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
				/*
				 * 给定此 Calendar 的时间值，返回指定日历字段可能拥有的最大值。
				 * 例如，在某些年份中，MONTH 字段的实际最大值是 12，而在希伯来日历系统的其他年份中，该字段的实际最大值是 13。
				 * DAY_OF_YEAR：闰年366？
				 */
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != y2);
        }
        return days;
    }

    /**
     * 比较两个日期相差的秒数
     */
    public static long compareTime(Date date1, Date date2) {
        if (date1 == null || date2 == null)
            return 0;

        Calendar c = Calendar.getInstance();

        c.setTime(date1);
        long l1 = c.getTimeInMillis();

        c.setTime(date2);
        long l2 = c.getTimeInMillis();

        return (l2 - l1) / 1000;
    }


    //设置时间
    private static Date addDateTime(Date date, int type, int num) {
        if (date == null) {
            return null;
        }
        //初始化日历对象
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        //根据类型添加
        switch (type) {
            case 1:        //添加年
                cal.set(Calendar.YEAR, cal.get(Calendar.YEAR) + num);
                break;
            case 2:        //添加月
                cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + num);
                break;
            case 3:        //添加日
                cal.set(Calendar.DATE, cal.get(Calendar.DATE) + num);
                break;
            case 4:        //添加时
                cal.set(Calendar.HOUR, cal.get(Calendar.HOUR) + num);
                break;
            case 5:        //添加分
                cal.set(Calendar.MINUTE, cal.get(Calendar.MINUTE) + num);
                break;
            case 6:        //添加秒
                cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + num);
                break;
        }

        //返回操作结果
        return cal.getTime();
    }

}
