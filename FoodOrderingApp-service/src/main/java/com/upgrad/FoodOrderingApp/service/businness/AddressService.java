package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.AddressDao;
import com.upgrad.FoodOrderingApp.service.dao.CustomerDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AddressService {

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private CustomerDao customerDao;

    @Transactional(propagation = Propagation.REQUIRED)
    public AddressEntity SaveAddress(AddressEntity addressEntity, String authorization) throws SaveAddressException, AuthorizationFailedException,AddressNotFoundException {


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



           if (addressEntity.getFlatBuilNumber() == null || addressEntity.getLocality() == null || addressEntity.getCity() == null || addressEntity.getPincode() == null || addressEntity.getState().getUuid() == null )
           {
               throw new SaveAddressException("SAR-001", "No field can be empty");
           }


           String picode = addressEntity.getPincode();


        boolean number = picode.matches("-?\\d+(\\.\\d+)?");
        if (!number) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        Pattern digitPattern = Pattern.compile("\\d{6}");
        Matcher matchers = digitPattern.matcher(addressEntity.getPincode());
        if (!matchers.matches()) {
            throw new SaveAddressException("SAR-002", "Invalid pincode");
        }

        StateEntity stateEntity = addressDao.getAddressByStateUid(addressEntity.getState().getUuid());

        if (stateEntity == null){
            throw new AddressNotFoundException("ANF-002", "No state by this id");
        }

        addressEntity.setState(stateEntity);
        addressEntity.setActive(1);

        AddressEntity addressEntity1 = addressDao.craeteAddress(addressEntity);

        CustomerAddressEntity customerAddressEntity = new CustomerAddressEntity();


        customerAddressEntity.setAddressId(addressEntity1);

        customerAddressEntity.setCustomerId(customerAuthEntity.getCustomerId());

        addressDao.craeteAddress1(customerAddressEntity);

        return addressEntity1;


    }

    public List<AddressEntity> getAllAddress(final String authorization) throws AuthorizationFailedException{
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


        return addressDao.getAllAddress();

    }

    public AddressEntity DeleteAddress(final String AddressUUID, final String authorization) throws AuthorizationFailedException, AddressNotFoundException {
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

        if (AddressUUID == null){
            throw new AddressNotFoundException("ANF-005", "Address id can not be empty");
        }

        CustomerEntity customen = customerAuthEntity.getCustomerId();
        CustomerAddressEntity customerAddressEntity = addressDao.getCustomerId(customen.getId());

        AddressEntity addressEntity = addressDao.getAddressByUid(AddressUUID);

        if (customerAddressEntity.getAddressId().getId() != addressEntity.getId()){
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address ");
        }

        if (addressEntity == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        addressDao.DeleteAddressCustomer(customerAddressEntity);
        return addressDao.DeleteAddress(addressEntity);
    }

    public List<StateEntity> getAllStates(){

        return addressDao.getAllStates();

    }
}
