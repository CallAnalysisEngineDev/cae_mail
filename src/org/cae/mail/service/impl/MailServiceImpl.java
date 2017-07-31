package org.cae.mail.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

@Service("mailService")
public class MailServiceImpl implements IMailService {

	@Autowired
	private ServletContext servletContext;
	@Autowired
	private JavaMailSenderImpl mailSender;
	@Autowired
	private ThreadPoolTaskExecutor threadPool;
	private Map<Enum<MailType>, List<String>> receiversMap;

	// 解析XML配置实现热部署功能
	@PostConstruct
	@Scheduled(cron = "* * * * * * ")
	public void init() {
		receiversMap = new HashMap<Enum<MailType>, List<String>>();
		File[] jsonFileArray = getJsonFile(
				this.getClass().getClassLoader().getResource("/").getPath().replaceFirst("/", ""));
		for (int i = 0; i < jsonFileArray.length; i++) {
			try {
				JSONObject mailJSON = new JSONObject(fileReader(jsonFileArray[i]));
				JSONArray mailArray = mailJSON.getJSONArray("cae-mail");
				for (int j = 0; j < mailArray.length(); j++) {
					ArrayList<String> mailList = new ArrayList<String>();
					JSONObject mail = mailArray.getJSONObject(j);
					JSONArray mailListArray = mail.getJSONArray("mailList");
					Enum<MailType> type = MailType.valueOf(mail.getString("type"));
					for (int k = 0; k < mailListArray.length(); k++) {
						String address = mailListArray.getJSONObject(k).getString("address");
						mailList.add(address);
					}
					receiversMap.put(type, mailList);
				}
			} catch (IOException | JSONException | EnumConstantNotPresentException e) {
				e.printStackTrace();
			}
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
