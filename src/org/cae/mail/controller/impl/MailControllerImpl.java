package org.cae.mail.controller.impl;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.cae.mail.common.IConstant;
import org.cae.mail.common.Util;
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
			MailMessage mailMessage=Util.toObject(textMessage.getText(), MailMessage.class);
			sendMailController(mailMessage);
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

	private void sendMailController(MailMessage mailMessage) {
		if(mailMessage.getType()==IConstant.USER_ADVICE)
			mailService.sendMailService(mailMessage.getMail());
	}

}
