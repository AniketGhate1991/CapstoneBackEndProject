package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.CustomerService;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.util.Base64;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class CustomerController {
    @Autowired
    private CustomerService CustomerBusinessService;

    @RequestMapping(method = RequestMethod.POST, path = "/customer/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupCustomerResponse> customerSignup(final SignupCustomerRequest signupUserRequest) throws SignUpRestrictedException, SignUpRestrictedException {

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

    @RequestMapping(method = RequestMethod.POST, path = "/customer/login", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LoginResponse> customerLogin(@RequestHeader("authorization") final String authorization) throws AuthenticationFailedException {

        byte[] decode = Base64.getDecoder().decode(authorization.split("Basic ")[1]);
        String decodedText = new String(decode);
        String[] decodedArray = decodedText.split(":");

        CustomerAuthEntity userAuthToken = CustomerBusinessService.authenticate(decodedArray[0],decodedArray[1]);

        CustomerEntity user = userAuthToken.getCustomerId();
        String sendMessage = "LOGGED IN SUCCESSFULLY";
        LoginResponse authorizedUserResponse = new LoginResponse().id(UUID.fromString(user.getUuid()).toString()).message(sendMessage).firstName(user.getFirstname()).lastName(user.getLastname()).emailAddress(user.getEmail()).contactNumber(user.getContactNumber());
        HttpHeaders headers = new HttpHeaders();
        headers.add("access_token", userAuthToken.getAccessToken());
        return new ResponseEntity<LoginResponse>(authorizedUserResponse, headers, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/customer/logout", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<LogoutResponse> customerlogout(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException {


        String [] bearerToken = authorization.split("Bearer ");

        CustomerAuthEntity userAuthTokenEntity = CustomerBusinessService.logout(bearerToken[0]);

        LogoutResponse authorizedUserResponse = new LogoutResponse().id(userAuthTokenEntity.getUuid()).message("LOGGED OUT SUCCESSFULLY");

        return new ResponseEntity<LogoutResponse>(authorizedUserResponse,HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdateCustomerResponse> UpdateCustomer (final UpdateCustomerRequest custRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException , UpdateCustomerException {

        String [] bearerToken = authorization.split("Bearer ");

        final CustomerEntity customentity = new CustomerEntity();
        customentity.setFirstname(custRequest.getFirstName());
        customentity.setLastname(custRequest.getLastName());

        final CustomerEntity customerEntity1 = CustomerBusinessService.updatecustomer( customentity,bearerToken[1]);
        UpdateCustomerResponse customResponse = new UpdateCustomerResponse().id(customerEntity1.getUuid()).status("CUSTOMER DETAILS UPDATED SUCCESSFULLY").firstName(customerEntity1.getFirstname()).lastName(customerEntity1.getLastname());

        return new ResponseEntity<UpdateCustomerResponse>(customResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.PUT, path = "/customer/password", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UpdatePasswordResponse> UpdateCustomerPassword (final UpdatePasswordRequest passRequest, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException , UpdateCustomerException {

        String [] bearerToken = authorization.split("Bearer ");

        String OldPass = passRequest.getOldPassword();
        String NewPass = passRequest.getNewPassword();

        final CustomerEntity customerEntity1 = CustomerBusinessService.updatecustomerPassword( NewPass,OldPass,bearerToken[1]);
        UpdatePasswordResponse customResponse = new UpdatePasswordResponse().id(customerEntity1.getUuid()).status("CUSTOMER DETAILS UPDATED SUCCESSFULLY");

        return new ResponseEntity<UpdatePasswordResponse>(customResponse,HttpStatus.CREATED);
    }
}
