package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.SignupCustomerRequest;
import com.upgrad.FoodOrderingApp.api.model.SignupCustomerResponse;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class CustomerController {
    @Autowired
    private CustomerService CustomerBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> userSignup(final SignupCustomerRequest signupUserRequest) throws SignUpRestrictedException, SignUpRestrictedException {

        final CustomerEntity custEntity = new CustomerEntity();

        custEntity.setUuid(UUID.randomUUID().toString());
        custEntity.setFirstname(signupUserRequest.getFirstName());
        custEntity.setLastname(signupUserRequest.getLastName());
        custEntity.setEmail(signupUserRequest.getEmailAddress());
        custEntity.setContactNumber(signupUserRequest.getContactNumber());
        custEntity.setPassword(signupUserRequest.getPassword());


        final CustomerEntity createdcustomerEntity = CustomerBusinessService.signup(custEntity);

        SignupCustomerResponse custResponse = new SignupCustomerResponse().id(createdcustomerEntity.getUuid()).status("CUSTOMER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupCustomerResponse>(custResponse, HttpStatus.CREATED);

    }

}
