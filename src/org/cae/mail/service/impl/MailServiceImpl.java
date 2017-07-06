package org.cae.mail.service.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.cae.mail.common.IConstant;
import org.cae.mail.entity.Mail;
import org.cae.mail.entity.MailMessage;
import org.cae.mail.service.IMailService;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import static org.cae.mail.common.Util.validateXML;
@Service("mailService")
public class MailServiceImpl implements IMailService {

	@Autowired
	private JavaMailSenderImpl mailSender;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	private Map<Integer,List<String>> receiversMap;
	
	//解析XML配置实现热部署功能
	@PostConstruct
	public void init(){
		receiversMap=new HashMap<Integer,List<String>>();
		SAXReader reader = new SAXReader(); 
	if(validateXML("src/mail.xml", "src/schema.xsd")){
		try{
			Document document=reader.read(new File("src/mail.xml"));
			List<Element> mail =document.getRootElement().element("mails").elements("mail");
			for(Iterator<Element> iterator=mail.iterator();iterator.hasNext();){
				Integer type;
				Element element =(Element) iterator.next();
				List<String> mailList=new ArrayList<String>();
				Attribute attribute=element.attribute("type");
				type=Integer.valueOf(attribute.getData().toString());
				List<Element> address =element.elements("address");
				for(Iterator<Element> iterator2=address.iterator();iterator2.hasNext();){
					element=(Element)iterator2.next();
					mailList.add(element.getText());
				}
				receiversMap.put(type, mailList);
			}
			receiversMap=Collections.unmodifiableMap(receiversMap);
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	}
	
	@Override
	public void sendMailService(MailMessage mailMessage) {
		List<String> receivers=receiversMap.get(mailMessage.getType());
		for(String receiver:receivers){
			threadPool.execute(new MailTask(mailMessage.getMail(),receiver));
		}
	}
	
	private class MailTask implements Runnable{

		private Mail mail;
		private String to;
		
		public MailTask(Mail mail,String to){
			this.mail=mail;
			this.to=to;
		}
		
		@Override
		public void run() {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper;
			try {
				mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				mimeMessageHelper.setTo(to);
				String nick = javax.mail.internet.MimeUtility.encodeText("cae项目组");
		        mimeMessageHelper.setFrom(new InternetAddress(nick+"<kuma_loveliver@163.com>"));
		        mimeMessageHelper.setSubject(mail.getTitle());
		        mimeMessageHelper.setText(mail.getContent());
		        mailSender.send(mimeMessage);
			} catch (MessagingException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		
	}
	
}
