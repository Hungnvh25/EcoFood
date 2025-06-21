package com.example.ecofood.service;


import com.example.ecofood.DTO.UserDTO;
import com.example.ecofood.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;



    public void sendRegistrationSuccessNotification(UserDTO user, String confirmPassword) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ecofood.2025@gmail.com"); // Email của bạn
        message.setTo(user.getEmail());
        message.setSubject("Đăng ký tài khoản thành công");
        message.setText(String.format(
                "Chào %s,\n\n" +
                        "Chúc mừng bạn đã đăng ký tài khoản thành công! " +
                        "Bạn có thể đăng nhập vào hệ thống để bắt đầu sử dụng dịch vụ của chúng tôi.\n\n" +
                        "Tài khoản đăng nhập:\n" +
                        "Email: %s\n" +
                        "Mật khẩu: %s\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ EcoFood",
                user.getUserName(), user.getEmail(), confirmPassword));

        sendNotification(message);
    }
    @Async
    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ecofood.2025@gmail.com"); // Email của bạn
        message.setTo(email);
        message.setSubject("Mã OTP để đặt lại mật khẩu");
        message.setText(String.format(
                "Chào bạn,\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản của mình. " +
                        "Mã OTP của bạn là:\n\n" +
                        "%s\n\n" +
                        "Vui lòng sử dụng mã này để xác nhận. Mã OTP có hiệu lực trong 5 phút.\n" +
                        "Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.\n\n" +
                        "Trân trọng,\n" +
                        "Đội ngũ EcoFood",
                otp));

        sendNotification(message);
    }

    public void sendNotification(SimpleMailMessage message) {
        try {
            this.mailSender.send(message);
            System.out.println("Email sent successfully to " +message.getReplyTo());
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
        }
    }

}