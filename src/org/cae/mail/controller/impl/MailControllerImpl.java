package org.cae.mail.controller.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static org.cae.mail.common.Util.toObject;
import org.cae.mail.entity.MailMessage;
import org.cae.mail.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mailController")
public class MailControllerImpl implements MessageListener {

	@Autowired
	private IMailService mailService;
	
	@Override
	public void onMessage(Message message) {
		try {
			TextMessage textMessage=(TextMessage) message;
			MailMessage mailMessage=toObject(textMessage.getText(), MailMessage.class);
			sendMailController(mailMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void sendMailController(MailMessage mailMessage) {
		mailService.sendMailService(mailMessage);
	}

}
