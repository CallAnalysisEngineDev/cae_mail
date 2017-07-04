package org.cae.mail.controller.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import static org.cae.mail.common.Util.toObject;

import org.cae.mail.controller.IMailController;
import org.cae.mail.entity.MailMessage;
import org.cae.mail.service.IMailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("mailController")
public class MailControllerImpl implements MessageListener,IMailController {

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

	@Override
	public void sendMailController(MailMessage mailMessage) {
		mailService.sendMailService(mailMessage);
	}

}
