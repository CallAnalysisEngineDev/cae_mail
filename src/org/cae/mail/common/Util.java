package org.cae.mail.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	private static Logger logger = Logger.getLogger(Util.class);
	private static SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat timeSdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");

	// 私有构造器防止外部创建新的Util对象
	private Util() {
	}

	public static String toJson(Object target) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.writeValueAsString(target);
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
		}
		return "";
	}

	public static <T> T toObject(String json, Class<T> clazz) {
		ObjectMapper mapper = new ObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return (T) new Object();
	}

	public static String date2String(Date date) {
		return dateSdf.format(date);
	}

	public static String time2String(Date date) {
		return timeSdf.format(date);
	}

	public static String getNowDate() {
		return date2String(new Date());
	}

	public static String getNowTime() {
		return time2String(new Date());
	}

	public static String getBefore(long time) {
		return time2String(new Date(System.currentTimeMillis() - time));
	}

	public static String getCharId() {
		return getCharId(new String(), 10);
	}

	public static String getCharId(int size) {
		return getCharId(new String(), size);
	}

	public static String getCharId(String pre, int size) {
		StringBuffer theResult = new StringBuffer();
		theResult.append(pre);
		String a = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for (int i = 0; i < size - pre.length(); i++) {
			int rand = (int) (Math.random() * a.length());
			theResult.append(a.charAt(rand));
		}
		return theResult.toString();
	}

	public static short getRandom(int randomRange) {
		Random random = new Random();
		return (short) random.nextInt(randomRange);
	}

	public static boolean isNotNull(Object object) {
		boolean result = false;
		if (object == null)
			return result;
		if (object instanceof String) {
			String temp = (String) object;
			if (temp != null && !temp.equals(""))
				result = true;
			else
				result = false;
		} else if (object instanceof List) {
			List list = (List) object;
			if (list.size() > 0)
				result = true;
			else
				result = false;
		}
		return result;
	}

	public static String md5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 
	 * @param jsonDir
	 *            JSON文件所在的目录
	 * @return JSON文件数组
	 */
	public static File[] getJsonFile(String jsonDir) {
		File file = new File(jsonDir);
		if (!file.exists()) {
			file.mkdir();
		}
		File[] jsonFiles = new File(jsonDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String filename = pathname.getName().toLowerCase();
				if (filename.endsWith(".json")) {
					return true;
				} else {
					return false;
				}
			}
		});
		return jsonFiles;
	}

	/**
	 * 
	 * @param file
	 *            文件
	 * @return 文件文本
	 */
	public static String fileReader(File file) {
		try {
			BufferedReader bufferedReader = new BufferedReader(new FileReader(
					file));
			StringBuffer buffer = new StringBuffer();
			String s = null;
			while ((s = bufferedReader.readLine()) != null) {
				buffer.append(s);
			}
			bufferedReader.close();
			return buffer.toString();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}

	public static boolean isEmail(String address) {
		boolean flag = false;
		try {
			String check = "^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
			Pattern regex = Pattern.compile(check);
			Matcher matcher = regex.matcher(address);
			flag = matcher.matches();
		} catch (Exception e) {
			flag = false;
		}
		return flag;
	}
}
