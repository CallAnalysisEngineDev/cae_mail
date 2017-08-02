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
import static org.cae.mail.common.Util.isEmail;

import org.apache.log4j.Logger;
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

	private Logger logger = Logger.getLogger(getClass());
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
		receiversMap = new HashMap<MailType, List<String>>();
		ObjectMapper mapper = new ObjectMapper();
		File[] jsonFiles = getJsonFile(this.getClass().getClassLoader()
				.getResource("/").getPath().replaceFirst("/", ""));
		if (jsonFiles.length == 0) {
			logger.info("当前没有mail配置文件");
			return;
		}
		try {
			for (File jsonFile : jsonFiles) {
				String json = fileReader(jsonFile);
				JsonNode node = mapper.readTree(json);
				JsonNode caeMailNodes = node.get("cae-mail");
				if (caeMailNodes == null) {
					logger.error("没有找到<cae-mail>节点",
							new IllegalArgumentException("没有找到<cae-mail>节点"));
					continue;
				}

				for (int i = 0; i < caeMailNodes.size(); i++) {
					JsonNode caeMailNode = caeMailNodes.get(i).get("type");
					if (caeMailNode == null) {
						logger.error("第" + (i + 1) + "个<cae-mail>节点中"
								+ "没有找到<type>节点", new IllegalArgumentException(
								"第" + (i + 1) + "个<cae-mail>节点中"
										+ "没有找到<type>节点"));
						break;
					}

					String caeMail = caeMailNode.toString();
					caeMail = caeMail.substring(1, caeMail.length() - 1);
					if (!MailType.contains(caeMail)) {
						logger.error("没有" + caeMail + "类型的收件人列表",
								new IllegalArgumentException("没有" + caeMail
										+ "类型的收件人列表"));
						continue;
					}
					MailType type = MailType.valueOf(caeMail);
					JsonNode mailListNode = caeMailNodes.get(i).get("mailList");
					if (mailListNode == null) {
						logger.error(
								"第" + (i + 1) + "个<cae-mail>节点中"
										+ "没有找到<mailList>节点",
								new IllegalArgumentException("第" + (i + 1)
										+ "个<cae-mail>节点中" + "没有找到<mailList>节点"));
						break;
					}

					String mailAddress = "";
					JsonNode mailAdressNode = null;
					ArrayList<String> mailList = new ArrayList<String>();
					for (int j = 0; j < mailListNode.size(); j++) {
						mailAdressNode = mailListNode.get(j).get("address");
						if (mailAdressNode == null) {
							logger.error("第" + (i + 1) + "个<mailList>节点中"
									+ "没有找到<address>节点",
									new IllegalArgumentException("第" + (j + 1)
											+ "个<mailList>节点中"
											+ "没有找到<address>节点"));
							break;
						}
						mailAddress = mailAdressNode.toString();
						mailAddress = mailAddress.substring(1,
								mailAddress.length() - 1);
						if (!isEmail(mailAddress)) {
							logger.error("第" + (i + 1) + "个<cae-mail>节点中的"
									+ "第" + (j + 1) + "个<address>的电子邮件不合法",
									new IllegalArgumentException("第" + (i + 1)
											+ "个<cae-mail>节点中的" + "第" + (j + 1)
											+ "个<address>的电子邮件不合法"));
							break;
						}
						mailList.add(mailAddress);
					}
					receiversMap.put(type, mailList);
				}
			}
			receiversMap = Collections.unmodifiableMap(receiversMap);
		} catch (JsonProcessingException e) {
			logger.error(e.getMessage(), e);
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
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
				mimeMessageHelper = new MimeMessageHelper(mimeMessage, true,
						"UTF-8");
				mimeMessageHelper.setTo(to);
				String nick = javax.mail.internet.MimeUtility
						.encodeText("cae项目组");
				mimeMessageHelper.setFrom(new InternetAddress(nick
						+ "<kuma_loveliver@163.com>"));
				mimeMessageHelper.setSubject(mail.getTitle());
				mimeMessageHelper.setText(mail.getContent());
				mailSender.send(mimeMessage);
			} catch (MessagingException | UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}

	}

}
