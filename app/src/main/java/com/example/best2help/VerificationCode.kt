package com.example.best2help

import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.MessagingException
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

class VerificationCode
{
    companion object {
        fun sendEmail(
            senderEmail: String,
            receiverEmail: String?,
            password: String,
            verificationCode: String
        ) {
            val stringHost = "smtp.gmail.com"

            val properties = System.getProperties()

            properties.put("mail.smtp.host", stringHost)
            properties.put("mail.smtp.port", "465")
            properties.put("mail.smtp.ssl.enable", "true")
            properties.put("mail.smtp.auth", "true")

            val session = Session.getInstance(properties, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(senderEmail, password)
                }
            })

            val mimeMessage = MimeMessage(session)
            mimeMessage.setRecipient(Message.RecipientType.TO, InternetAddress(receiverEmail))

            mimeMessage.subject = "Account Verification"

            val emailContent = """
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Email Verification</title>
    <style>
        body {
            font-family: 'Helvetica Neue', Arial, sans-serif;
            background-color: #f4f4f4;
            margin: 0;
            padding: 0;
            text-align: center;
        }

        .container {
            background-color: #ffffff;
            max-width: 600px;
            margin: 20px auto;
            padding: 20px;
            border-radius: 8px;
            box-shadow: 0 0 10px rgba(0, 0, 0, 0.1);
            animation: fadeIn 1s ease-in-out;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
            }

            to {
                opacity: 1;
            }
        }

        h1 {
            color: #3498db;
        }

        p {
            color: #555555;
            line-height: 1.6;
        }

        .verification-code {
            background-color: #3498db;
            color: #ffffff;
            font-size: 32px;
            padding: 15px 30px;
            border-radius: 4px;
            margin: 20px 0;
            letter-spacing: 2px;
        }

        .footer {
            margin-top: 20px;
            color: #888888;
        }
    </style>
</head>

<body>
    <div class="container">
        <h1>Email Verification</h1>
        <p style="color: #666;">Dear $receiverEmail,</p>
        <p style="color: #777;">Thank you for choosing our service. To complete your registration, please use the following verification code:</p>
        <div class="verification-code">
            <strong>$verificationCode</strong>
        </div>
        <p style="color: #777;">If you did not request this verification, please ignore this email.</p>
        <p style="color: #555;">Best regards,<br>Best2Help</p>
        <div class="footer">
            <p style="color: #888;">This is an automated message. Please do not reply.</p>
        </div>
    </div>
</body>

</html>

            """.trimIndent()


            mimeMessage.setText(emailContent, "UTF-8", "html")

            Thread {
                try {
                    Transport.send(mimeMessage)
                } catch (e: MessagingException) {
                    e.printStackTrace()
                }
            }.start()
        }
    }
}