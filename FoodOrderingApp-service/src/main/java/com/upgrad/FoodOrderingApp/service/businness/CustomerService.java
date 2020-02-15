package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAuthEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerEntity;
import com.upgrad.FoodOrderingApp.service.exception.AuthenticationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.SignUpRestrictedException;
import com.upgrad.FoodOrderingApp.service.exception.UpdateCustomerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service

public class CustomerService {
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private PasswordCryptographyProvider passwordCryptographyProvider;


    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity signup(CustomerEntity customerEntity) throws SignUpRestrictedException {

        CustomerEntity customerEntity1 = customerDao.getCustomerByContactNumber(customerEntity.getContactNumber());
        if (customerEntity1 != null) {
            throw new SignUpRestrictedException("SGR-001", "This contact number is already registered! Try other contact number.");
        }

        if (customerEntity.getFirstname() == null || customerEntity.getEmail() == null || customerEntity.getContactNumber() == null
                || customerEntity.getPassword() == null) {
            throw new SignUpRestrictedException("SGR-005", "Except last name all fields should be filled");
        }

        String emailValidation = "^(.+)@(.+)$";
        Pattern pattern = Pattern.compile(emailValidation);
        Matcher matcher = pattern.matcher(customerEntity.getEmail());
        if (!matcher.matches()) {
            throw new SignUpRestrictedException("SGR-002", "Invalid email-id format!");
        }

        String contactValidation = "[0-9]{10}";
        Pattern pattern1 = Pattern.compile(contactValidation);
        Matcher matcher1 = pattern1.matcher(customerEntity.getContactNumber());
        if (!matcher1.matches()) {
            throw new SignUpRestrictedException("SGR-003", "Invalid contact number!");
        }

        String passwordStrengthValidation = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[#@$%&*!^])(?=\\S+$).{8,}$";
        Pattern pattern2 = Pattern.compile(passwordStrengthValidation);
        Matcher matcher2 = pattern2.matcher(customerEntity.getPassword());
        if (!matcher2.matches()) {
            throw new SignUpRestrictedException("SGR-004", "Weak Password");
        }

        String[] encryptedText = passwordCryptographyProvider.encrypt(customerEntity.getPassword());
        customerEntity.setSalt(encryptedText[0]);
        customerEntity.setPassword(encryptedText[1]);

        return customerDao.createCustomer(customerEntity);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity authenticate(String username,String password) throws AuthenticationFailedException {

        CustomerEntity customerEntity = customerDao.getCustomerByContactNumber(username);

        if (customerEntity == null) {
            throw new AuthenticationFailedException("ATH-001", "This contact number has not been registered!");
        }

        final String encryptedPassword = PasswordCryptographyProvider.encrypt(password, customerEntity.getSalt());

        if (encryptedPassword.equals(customerEntity.getPassword())) {
            JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
            CustomerAuthEntity customerAuthEntity = new CustomerAuthEntity();
            customerAuthEntity.setUuid(UUID.randomUUID().toString());
            customerAuthEntity.setCustomerId(customerEntity);

            ZonedDateTime now = ZonedDateTime.now();
            ZonedDateTime expiresAt = now.plusHours(8);

            customerAuthEntity.setLoginAt(ZonedDateTime.now());
            customerAuthEntity.setExpiresAt(expiresAt);
            customerAuthEntity.setAccessToken(jwtTokenProvider.generateToken(customerEntity.getUuid(), now, expiresAt));


            return customerDao.createCustomerAuth(customerAuthEntity);
        } else {
            throw new AuthenticationFailedException("ATH-002", "Invalid Credentials");
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerAuthEntity logout(String authorization) throws AuthorizationFailedException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerByAccessToken(authorization);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        ZonedDateTime expireTime = customerAuthEntity.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now();

        if (expireTime.isBefore(currentTime)) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }


        ZonedDateTime logoutAtTime = customerAuthEntity.getLogoutAt();
        if (logoutAtTime != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        }

        customerAuthEntity.setLogoutAt(ZonedDateTime.now());
        customerDao.updateCustomerAuth(customerAuthEntity);

        return customerAuthEntity;


    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updatecustomer(CustomerEntity customerEntity,String authorization) throws AuthorizationFailedException,UpdateCustomerException {

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerByAccessToken(authorization);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        if (customerEntity.getFirstname() == null) {
            throw new UpdateCustomerException("UCR-002", "First name field should not be empty");
        }

        ZonedDateTime expireTime = customerAuthEntity.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now();

        if (expireTime.isBefore(currentTime)) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        ZonedDateTime logoutAtTime = customerAuthEntity.getLogoutAt();
        if (logoutAtTime != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        }

        CustomerEntity customen = new CustomerEntity();
        customen = customerAuthEntity.getCustomerId();
        customen.setFirstname(customerEntity.getFirstname());
        customen.setLastname(customerEntity.getLastname());


        customerDao.updateCustomer(customen);


        return customen;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public CustomerEntity updatecustomerPassword(String NewPassword,String OldPassword,String authorization) throws AuthorizationFailedException,UpdateCustomerException {

        if (NewPassword == null || OldPassword == null) {
            throw new UpdateCustomerException("UCR-003", "No field should be empty.");
        }

        CustomerAuthEntity customerAuthEntity = customerDao.getCustomerByAccessToken(authorization);

        if (customerAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "Customer is not Logged in.");
        }

        ZonedDateTime expireTime = customerAuthEntity.getExpiresAt();
        ZonedDateTime currentTime = ZonedDateTime.now();

        if (expireTime.isBefore(currentTime)) {
            throw new AuthorizationFailedException("ATHR-003", "Your session is expired. Log in again to access this endpoint.");
        }

        ZonedDateTime logoutAtTime = customerAuthEntity.getLogoutAt();
        if (logoutAtTime != null) {
            throw new AuthorizationFailedException("ATHR-002", "Customer is logged out. Log in again to access this endpoint.");

        }

        String passwordStrengthValidation = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[#@$%&*!^])(?=\\S+$).{8,}$";
        Pattern pattern2 = Pattern.compile(passwordStrengthValidation);
        Matcher matcher2 = pattern2.matcher(NewPassword);
        if (!matcher2.matches()) {
            throw new UpdateCustomerException("UCR-001", "Weak password!");
        }

        CustomerEntity customen = new CustomerEntity();
        customen = customerAuthEntity.getCustomerId();

        final String encryptedPassword = PasswordCryptographyProvider.encrypt(OldPassword, customen.getSalt());

        if (encryptedPassword.equals(customen.getPassword())) {
            String[] encryptedText = passwordCryptographyProvider.encrypt(NewPassword);
            customen.setSalt(encryptedText[0]);
            customen.setPassword(encryptedText[1]);

            return customerDao.updateCustomer(customen);
        } else {
            throw new UpdateCustomerException("UCR-004", "Incorrect old password!");
        }



    }
}
