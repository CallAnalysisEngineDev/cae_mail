package org.cae.mail.service.impl;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.cae.mail.common.IConstant;
import org.cae.mail.entity.Mail;
import org.cae.mail.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service("mailService")
public class MailServiceImpl implements IMailService {

	@Autowired
	private JavaMailSenderImpl mailSender;
	private Map<Integer,List<String>> receiversMap;
	
	//目前先写死,之后再写成xml配置且支持热部署的功能
	@PostConstruct
	public void init(){
		receiversMap=new HashMap<Integer,List<String>>();
		List<String> list=new ArrayList<String>();
		list.add("709782571@qq.com");
		receiversMap.put(IConstant.USER_ADVICE, list);
	}
	
	@Override
	public void sendMailService(Mail mail) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		MimeMessageHelper mimeMessageHelper;
		try {
			mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
			mimeMessageHelper.setTo("709782571@qq.com");
			String nick = javax.mail.internet.MimeUtility.encodeText("cae项目组");
	        mimeMessageHelper.setFrom(new InternetAddress(nick + "<callanalysisengine@163.com>"));
	        mimeMessageHelper.setSubject("测试邮件主题");
	        mimeMessageHelper.setText("测试邮件内容");
	        mailSender.send(mimeMessage);
		} catch (MessagingException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
}
