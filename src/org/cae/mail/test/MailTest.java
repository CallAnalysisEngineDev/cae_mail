package org.cae.mail.test;

import static org.junit.Assert.*;

import org.cae.mail.service.IMailService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class MailTest {

	private IMailService service;
	
	@Before
	public void init(){
		ApplicationContext ctx=new ClassPathXmlApplicationContext("applicationContext.xml");
		service=(IMailService) ctx.getBean("mailService");
	}
	
	@Test
	public void test() {
		service.sendMailService(null);
	}

}
