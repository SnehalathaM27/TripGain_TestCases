package com.tripgain.common;

import java.io.File;
import java.util.Map;
import java.util.Properties;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

public class EmailUtils {

    public static void sendReportByEmail(String reportPath, String toEmail, String[] ccEmails,
                                         int totalTests, int passedTests, int failedTests) {

        final String fromEmail = "arun.kumar@tripgain.com";     // Sender's email
        final String password = "nshlltgljgzbqpyn";            // App password or Outlook auth token

        // Email properties for Outlook SMTP
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.office365.com");
        props.put("mail.smtp.port", "587");

        // Create email session with authentication
        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });

        try {
            // Compose the email message
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));

            // Optional: Add CC recipients
            if (ccEmails != null && ccEmails.length > 0) {
                InternetAddress[] ccAddresses = new InternetAddress[ccEmails.length];
                for (int i = 0; i < ccEmails.length; i++) {
                    ccAddresses[i] = new InternetAddress(ccEmails[i]);
                }
                message.setRecipients(Message.RecipientType.CC, ccAddresses);
            }

            message.setSubject("TripGain Automation Test Report for Hotels");

            // Email body with test summary
            String body = String.format(
                    "Hi Team,\n\n" +
                            "Please find attached the TripGain test execution report for Hotels.\n\n" +
                            "Test Summary:\n" +
                            "Total Test Cases Count: %d\n" +
                            "Passed Count: %d\n" +
                            "Failed Count: %d\n\n" +
                            "Regards,\nQA Team",
                    totalTests, passedTests, failedTests
            );

            // Body part with text
            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(body);

            // Attachment part
            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.attachFile(new File(reportPath));

            // Combine parts
            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);

            // Set the content and send
            message.setContent(multipart);
            Transport.send(message);

            System.out.println("✅ Report emailed successfully to: " + toEmail);

        } catch (Exception e) {
            System.err.println("❌ Failed to send email: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
