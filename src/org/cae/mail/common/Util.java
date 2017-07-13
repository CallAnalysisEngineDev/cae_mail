package org.cae.mail.common;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.util.XMLErrorHandler;
import org.xml.sax.SAXException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

	private static SimpleDateFormat dateSdf=new SimpleDateFormat("yyyy-MM-dd");
	private static SimpleDateFormat timeSdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	public static String toJson(Object target){
		ObjectMapper mapper=new ObjectMapper();
		try{
			return mapper.writeValueAsString(target);
		}catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public static <T> T toObject(String json,Class<T> clazz){
		ObjectMapper mapper=new ObjectMapper();
		try {
			return mapper.readValue(json, clazz);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String date2String(Date date){
		return dateSdf.format(date);
	}
	
	public static String time2String(Date date){
		return timeSdf.format(date);
	}

	public static String getNowDate(){
		return date2String(new Date());
	}
	
	public static String getNowTime(){
		return time2String(new Date());
	}
	
	public static String getBefore(long time){
		return time2String(new Date(System.currentTimeMillis()-time));
	}
	
	public static String getCharId(){
		return getCharId(new String(), 10);
	}
	
	public static String getCharId(int size){
		return getCharId(new String(),size);
	}
	
	public static String getCharId(String pre,int size){
		StringBuffer theResult=new StringBuffer();
		theResult.append(pre);
		String a = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		for(int i=0;i<size-pre.length();i++){
			int rand =(int)(Math.random() * a.length());
			theResult.append(a.charAt(rand));
		}
		return theResult.toString();
	}
	
	public static short getRandom(int randomRange){
		Random random=new Random();
		return (short) random.nextInt(randomRange);
	}
	
	public static boolean isNotNull(Object object){
		boolean result=false;
		if(object==null)
			return result;
		if(object instanceof String){
			String temp=(String) object;
			if(temp!=null&&!temp.equals(""))
				result=true;
			else
				result=false;
		}
		else if(object instanceof List){
			List list=(List) object;
			if(list.size()>0)
				result=true;
			else
				result=false;
		}
		return result;
	}
	
	public static void logStackTrace(Logger logger,StackTraceElement[] stackTrace){
		String stackInfo="";
		for(StackTraceElement element:stackTrace){
			stackInfo+=element+"\n";
		}
		logger.error(stackInfo);
	}
	
	public static String md5(String str){
		try{
			MessageDigest md=MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return new BigInteger(1,md.digest()).toString(16);
		}catch(Exception ex){
			return null;
		}
	}
	/**
	 * 
	 * @param Xml文件的路径
	 * @param Xsd文件的路径
	 * @return 返回校验成功的标志
	 */
	public static boolean validateXML(String xmlFilePath,String xsdFilePath) { 
			  long startTime = System.currentTimeMillis(); 
		        try {
		            SchemaFactory factory = 
		                    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		            Schema schema = factory.newSchema(new File(xsdFilePath));
		            Validator validator = schema.newValidator();
		            validator.validate(new StreamSource(new File(xmlFilePath)));
		        } catch (IOException | SAXException e) {
		            System.out.println("Exception: "+e.getMessage()); 
		            return false;
		        }
		        return true;
		    }
	/**
	 * 
	 * @param xmlDir XML文件所在文件夹的路径
	 * @return	XML文件数组
	 */
	public static File[] getXMLFile(String xmlDir){
		File file=new File(xmlDir);
		if(!file.exists()){
			file.mkdir();
		}
		File[] xmlFiles=new File(xmlDir).listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				String filename = pathname.getName().toLowerCase();
				if(filename.endsWith(".xml")){
					return true;
				}else{
					return false;
				}
			}
		});
		return xmlFiles;
	}
}
