package com.twitter_backend.services;

import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.twitter_backend.exceptions.EmailFailedToSendException;

import jakarta.mail.Session;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

    private final Gmail gmail;

    @Autowired
    public MailService(Gmail gmail) {
        this.gmail = gmail;
    }

    public void sendGmail(String toAdress, String subject, String content) throws Exception {

        Properties properties = new Properties();

        Session session = Session.getDefaultInstance(properties, null);

        MimeMessage email = new MimeMessage(session);
        try {
            email.setFrom(new InternetAddress(System.getenv("MAIL_SERVICE_ADDRESS")));
            email.addRecipient(jakarta.mail.Message.RecipientType.TO, new InternetAddress(toAdress));
            email.setSubject(content);
            email.setText(content);

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();

            email.writeTo(buffer);
            byte[] rawMessageBytes = buffer.toByteArray();
            String encodedEmail = Base64.encodeBase64String(rawMessageBytes);

            Message message = new Message();
            message.setRaw(encodedEmail);
            message = gmail.users().messages().send("me", message).execute();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EmailFailedToSendException();

        }

    }

}
