package com.example.ecofood.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CreatePaymentLinkRequestBody {
    private String Name;
    private String description;
    private String returnUrl;
    private int price;
    private String cancelUrl;

}