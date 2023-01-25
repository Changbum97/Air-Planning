package com.example.airplanning.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.mail.Message;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.time.Duration;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;

    public static final String ePw = createKey();

    private MimeMessage createMessage(String to) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);
        message.setSubject("Air Planning 이메일 인증");

        String msgg="";
        msgg+= "<div style='margin:100px;'>";
        msgg+= "<h1> 안녕하세요 Air Planning 입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래 코드를 회원가입 창으로 돌아가 입력해주세요<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다!<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "CODE : <strong>";
        msgg+= ePw+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("skdlfma123@gmail.com","Air Planning"));//보내는 사람

        return message;
    }

    private MimeMessage foundMessage(String to, String userName) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, to);
        message.setSubject("Air Planning 아이디 찾기");

        String msgg="";
        msgg+= "<div style='margin:100px;'>";
        msgg+= "<h1> 안녕하세요 Air Planning 입니다. </h1>";
        msgg+= "<br>";
        msgg+= "<p>아래의 아이디를 확인해주세요<p>";
        msgg+= "<br>";
        msgg+= "<p>감사합니다!<p>";
        msgg+= "<br>";
        msgg+= "<div align='center' style='border:1px solid black; font-family:verdana';>";
        msgg+= "<h3 style='color:blue;'>회원님의 Air Planning 아이디 입니다.</h3>";
        msgg+= "<div style='font-size:130%'>";
        msgg+= "아이디 : <strong>";
        msgg+= userName+"</strong><div><br/> ";
        msgg+= "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress("skdlfma123@gmail.com","Air Planning"));//보내는 사람

        return message;
    }

    public static String createKey() {
        StringBuffer key = new StringBuffer();
        Random rnd = new Random();

        for (int i = 0; i < 8; i++) { // 인증코드 8자리
            int index = rnd.nextInt(3); // 0~2 까지 랜덤

            switch (index) {
                case 0:
                    key.append((char) ((int) (rnd.nextInt(26)) + 97));
                    //  a~z  (ex. 1+97=98 => (char)98 = 'b')
                    break;
                case 1:
                    key.append((char) ((int) (rnd.nextInt(26)) + 65));
                    //  A~Z
                    break;
                case 2:
                    key.append((rnd.nextInt(10)));
                    // 0~9
                    break;
            }
        }
        return key.toString();
    }

    public String sendSimpleMessage(String to) throws Exception {
        to = to.substring(6).replace("%40","@");
        log.info("email : {} ", to);
        MimeMessage message = createMessage(to);
        try{//예외처리
            mailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        setDataExpire(ePw, to, 60*5L);
        return "인증 메일이 발송되었습니다.";
    }

    public String sendFoundUserName(String to, String userName) throws Exception {
        MimeMessage message = foundMessage(to, userName);
        try{//예외처리
            mailSender.send(message);
        }catch(MailException es){
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        return "메일을 확인해 주세요.";
    }

    //redis
    public String getData(String key){
        key = key.substring(5);
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setData(String key, String value) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        valueOperations.set(key, value);
    }

    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }
}
