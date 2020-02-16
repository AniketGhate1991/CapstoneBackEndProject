package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.PaymentResponse;
import com.upgrad.FoodOrderingApp.service.businness.PaymentService;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class PaymentController {
    @Autowired
    private PaymentService paymentService;
    @RequestMapping(method = RequestMethod.GET, path = "/payment", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<List<PaymentResponse>> GetPaymentMethods ()
    {
        final List<PaymentEntity> paymentEntities = paymentService.getAllPayment();

        List<PaymentResponse> lstPay = new ArrayList<>();
        for (PaymentEntity n: paymentEntities){
            PaymentResponse obj = new PaymentResponse();
            obj.setId(UUID.fromString(n.getUuid()));
            obj.setPaymentName(n.getPaymentName());
            lstPay.add(obj);
        }


        return new ResponseEntity<List<PaymentResponse>>(lstPay, HttpStatus.OK);

    }
}
