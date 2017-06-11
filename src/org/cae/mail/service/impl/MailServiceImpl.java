package org.cae.mail.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("mailService")
public class MailServiceImpl implements IMailService {

	@Autowired
	private JavaMailSenderImpl mailSender;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	private Map<Integer,List<String>> receiversMap;
	
	//目前先写死,之后再写成xml配置且支持热部署的功能
	@PostConstruct
	public void init(){
		receiversMap=new HashMap<Integer,List<String>>();
		List<String> list=new ArrayList<String>();
		//list.add("709782571@qq.com");
		list.add("callanalysisengine@163.com");
		//list.add("lq664376661@163.com");
		receiversMap.put(IConstant.USER_ADVICE, list);
		receiversMap=Collections.unmodifiableMap(receiversMap);
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
