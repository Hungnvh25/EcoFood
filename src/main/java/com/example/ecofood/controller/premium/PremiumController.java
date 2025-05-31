package com.example.ecofood.controller.premium;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

import com.example.ecofood.DTO.CreatePaymentLinkRequestBody;
import com.example.ecofood.Util.RandomStringGenerator;
import com.example.ecofood.domain.User;
import com.example.ecofood.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import vn.payos.PayOS;
import vn.payos.type.CheckoutResponseData;
import vn.payos.type.ItemData;
import vn.payos.type.PaymentData;
import vn.payos.type.PaymentLinkData;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PremiumController {

    final PayOS payOS;
    UserService userService;
    RandomStringGenerator randomStringGenerator;



    @RequestMapping(value = "/premium")
    public String index(Model model) {
        int daysLeft = 0;
        User user = this.userService.getCurrentUser();
        if (user.getPremium() != null && user.getPremium() && user.getPremiumStartDate() != null) {
            LocalDateTime expiryDate = user.getPremiumStartDate().plusDays(30);
            LocalDateTime now = LocalDateTime.now();
            if (expiryDate.isAfter(now)) {
                daysLeft = (int) ChronoUnit.DAYS.between(now, expiryDate);
            }
        }
        model.addAttribute("daysLeft", daysLeft);
        model.addAttribute("user", user);
        return "client/premium/show";
    }


    @RequestMapping(value = "/premium/success")
    public String Success() {
        User user = this.userService.getCurrentUser();
        user.setPremium(true);
        user.setPremiumStartDate(LocalDateTime.now());

        userService.saveUser(user);
        return "client/premium/success";
    }

    @RequestMapping(value = "/premium/cancel")
    public String Cancel() {
        return "client/premium/cancel";
    }

    @RequestMapping(method = RequestMethod.POST, value = "/create-payment-link", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public void checkout(HttpServletRequest request, HttpServletResponse httpServletResponse) {
        try {
            final String baseUrl = getBaseUrl(request);
            final String productName = "Gói Premium của bạn";
            final String description = this.randomStringGenerator.generateRandomString(6);
            final String returnUrl = baseUrl + "/premium/success";
            final String cancelUrl = baseUrl + "/premium/cancel";
            final int price = 2000;
            // Gen order code
            String currentTimeString = String.valueOf(new Date().getTime());
            long orderCode = Long.parseLong(currentTimeString.substring(currentTimeString.length() - 6));
            ItemData item = ItemData.builder().name(productName).quantity(1).price(price).build();
            PaymentData paymentData = PaymentData.builder().orderCode(orderCode).amount(price).description(description)
                    .returnUrl(returnUrl).cancelUrl(cancelUrl).item(item).build();
            CheckoutResponseData data = this.payOS.createPaymentLink(paymentData);

            String checkoutUrl = data.getCheckoutUrl();

            httpServletResponse.setHeader("Location", checkoutUrl);
            httpServletResponse.setStatus(302);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int serverPort = request.getServerPort();
        String contextPath = request.getContextPath();

        String url = scheme + "://" + serverName;
        if ((scheme.equals("http") && serverPort != 80) || (scheme.equals("https") && serverPort != 443)) {
            url += ":" + serverPort;
        }
        url += contextPath;
        return url;
    }
}