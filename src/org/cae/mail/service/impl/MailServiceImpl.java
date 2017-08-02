package org.cae.mail.service.impl;

import java.io.File;
import java.io.IOException;
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
import javax.servlet.ServletContext;

import static org.cae.mail.common.Util.fileReader;
import static org.cae.mail.common.Util.getJsonFile;

import org.cae.mail.entity.Mail;
import org.cae.mail.entity.MailMessage;
import org.cae.mail.entity.MailType;
import org.cae.mail.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("mailService")
public class MailServiceImpl implements IMailService {

	@Autowired
	private ServletContext servletContext;
	@Autowired
	private JavaMailSenderImpl mailSender;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	private Map<MailType, List<String>> receiversMap;

	// 解析XML配置实现热部署功能
	@PostConstruct
	@Scheduled(cron = "* * * * * * ")
	public void init() {
		long startTime = System.currentTimeMillis();
		receiversMap = new HashMap<MailType, List<String>>();
		ObjectMapper mapper = new ObjectMapper();
		File[] jsonFile = getJsonFile(
				this.getClass().getClassLoader().getResource("/").getPath().replaceFirst("/", ""));
		try {
			for (int i = 0; i < jsonFile.length; i++) {
				String json = fileReader(jsonFile[i]);

				JsonNode node = mapper.readTree(json);
				JsonNode caeMailNode = node.get("cae-mail");
				for (int j = 0; j < caeMailNode.size(); j++) {
					String temp = caeMailNode.get(j).get("type").toString();
					temp = temp.substring(1, temp.length() - 1);
					if (!MailType.contains(temp)) {
						continue;
					}
					MailType type = MailType.valueOf(temp);
					JsonNode mailListNode = caeMailNode.get(j).get("mailList");
					ArrayList<String> mailList = new ArrayList<String>();
					for (int k = 0; k < mailListNode.size(); k++) {
						temp = mailListNode.get(k).get("address").toString();
						temp = temp.substring(1, temp.length() - 1);
						mailList.add(temp);
					}
					receiversMap.put(type, mailList);
				}
			}
			receiversMap = Collections.unmodifiableMap(receiversMap);
			long endTime = System.currentTimeMillis();
			System.out.println("程序运行时间：" + (endTime - startTime) + "ms " + "map内容是" + receiversMap);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void sendMailService(MailMessage mailMessage) {
		List<String> receivers = receiversMap.get(mailMessage.getType());
		for (String receiver : receivers) {
			threadPool.execute(new MailTask(mailMessage.getMail(), receiver));
		}
	}

	private class MailTask implements Runnable {

		private Mail mail;
		private String to;

		public MailTask(Mail mail, String to) {
			this.mail = mail;
			this.to = to;
		}

		@Override
		public void run() {
			MimeMessage mimeMessage = mailSender.createMimeMessage();
			MimeMessageHelper mimeMessageHelper;
			try {
				mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
				mimeMessageHelper.setTo(to);
				String nick = javax.mail.internet.MimeUtility.encodeText("cae项目组");
				mimeMessageHelper.setFrom(new InternetAddress(nick + "<kuma_loveliver@163.com>"));
				mimeMessageHelper.setSubject(mail.getTitle());
				mimeMessageHelper.setText(mail.getContent());
				mailSender.send(mimeMessage);
			} catch (MessagingException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

	}

}
