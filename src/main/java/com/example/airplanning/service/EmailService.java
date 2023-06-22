package com.example.airplanning.service;

import com.example.airplanning.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    private final UserService userService;

    @Value("${AdminMail.id}")
    private String mailId;

    /**
     * 메세지 생성 메서드
     * type = 1 => 회원가입 인증 메세지
     * type = 2 => 아이디 찾기 메세지
     * type = 3 => 비밀번호 찾기 메세지
     */
    private MimeMessage createMessage(String email, String userName, String ePw, int type) throws Exception {
        MimeMessage message = mailSender.createMimeMessage();

        message.addRecipients(Message.RecipientType.TO, email);
        if (type == 1) message.setSubject("Air Planning 회원가입 이메일 인증");
        else if (type == 2) message.setSubject("Air Planning 아이디 찾기");
        else message.setSubject("Air Planning 비밀번호 찾기 이메일 인증");

        String msgg = "";
        msgg += "<div style='margin:100px;'>";
        msgg += "<h1> 안녕하세요 Air Planning 입니다. </h1>";
        msgg += "<br>";
        
        if (type == 1) msgg += "<p>아래 코드를 회원가입 창으로 돌아가 입력해주세요<p>";
        else if (type == 2) msgg += "<p>아래의 아이디를 확인해주세요<p>";
        else msgg += "<p>새로운 비밀번호로 로그인 해주세요<p>";
        
        msgg += "<p>감사합니다!<p>";
        msgg += "<br>";
        msgg += "<div align='center' style='border:1px solid black; font-family:verdana';>";
        
        if (type == 1) msgg += "<h3 style='color:blue;'>회원가입 인증 코드입니다.</h3>";
        else if (type == 2) msgg += "<h3 style='color:blue;'>회원님의 Air Planning 아이디 입니다.</h3>";
        else msgg += "<h3 style='color:blue;'>새로운 비밀번호 입니다.</h3>";
        
        msgg += "<div style='font-size:130%'>";
        
        if (type == 1) {
            msgg += "CODE : <strong>";
            msgg += ePw + "</strong><div><br/> ";
        } else if (type == 2) {
            msgg += "아이디 : <strong>";
            msgg += userName + "</strong><div><br/> ";
        } else {
            msgg += "비밀번호 : <strong>";
            msgg += ePw + "</strong><div><br/> ";
        }
        
        msgg += "</div>";
        message.setText(msgg, "utf-8", "html");//내용
        message.setFrom(new InternetAddress(mailId, "Air Planning"));//보내는 사람

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

    // 회원가입 인증 메시지 발송
    public String sendLoginAuthMessage(String email) throws Exception {
        String ePw = createKey();
        MimeMessage message = createMessage(email, null, ePw, 1);
        try {//예외처리
            mailSender.send(message);
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        }
        setDataExpire(email + "_auth", ePw, 60 * 5L);
        return "인증 메일이 발송되었습니다.";
    }

    // 아이디 찾기 아이디 메시지 발송
    public String sendFoundIdMessage(String email) throws Exception {
        String result = "메일로 아이디를 전송했습니다.";
        String userName = userService.findIdByEmail(email);
        MimeMessage message = createMessage(email, userName, null, 2);
        try {    //예외처리
            mailSender.send(message);
        } catch (MailException es) {
            es.printStackTrace();
            throw new IllegalArgumentException();
        } catch (AppException e) {
            result = e.getMessage();
        } catch (Exception e) {
            result = "메일 전송에 실패하였습니다.";
        }
        return result;
    }

    // 비밀번호 찾기 새로운 비밀번호 메시지 발송
    public String sendFoundPasswordMessage(String email, String userName) throws Exception {
        String result = "메일로 새로운 비밀번호를 전송했습니다";
        String foundUserName = userService.findIdByEmail(email);
        if (!userName.equals(foundUserName)) {
            result = "아이디에 해당하는 이메일이 없습니다.";
        } else {
            String ePw = createKey();

            String newPassword = ePw;
            userService.changePassword(foundUserName, newPassword);
            MimeMessage message = createMessage(email, null, ePw, 3);

            try {    //예외처리
                mailSender.send(message);
            } catch (MailException es) {
                es.printStackTrace();
                throw new IllegalArgumentException();
            } catch (AppException e) {
                result = e.getMessage();
            } catch (Exception e) {
                result = "메일 전송에 실패하였습니다.";
            }
        }

        return result;
    }

    // redis
    // 인증번호 확인 하기
    public String getData(String key) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        return valueOperations.get(key);
    }

    public void setDataExpire(String key, String value, long duration) {
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        Duration expireDuration = Duration.ofSeconds(duration);
        valueOperations.set(key, value, expireDuration);
    }

}
