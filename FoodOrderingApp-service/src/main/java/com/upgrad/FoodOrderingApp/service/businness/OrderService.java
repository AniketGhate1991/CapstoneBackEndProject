package com.upgrad.FoodOrderingApp.service.businness;

import com.upgrad.FoodOrderingApp.service.dao.*;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.jaxb.SpringDataJaxb;
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
public class OrderService {
    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AddressDao addressDao;

    @Autowired
    private RestaurantDao restaurantDao;

    @Autowired
    private ItemDao itemDao;
    @Transactional(propagation = Propagation.REQUIRED)
    public CouponEntity getCoupon(final String CouponName, String authorization) throws AuthorizationFailedException, CouponNotFoundException {

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

       if (CouponName == null){
           throw new CouponNotFoundException("CPF-002", "Coupon name field should not be empty");
       }

       CouponEntity couponEntity = orderDao.getCouponByName(CouponName);
       if (couponEntity == null){
           throw new CouponNotFoundException("CPF-001", "No coupon by this name");
       }

        return couponEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrdersEntity SaveOrder(OrdersEntity ordersEntity, OrderItemEntity orderItemEntity, String authorization) throws CouponNotFoundException, AuthorizationFailedException,AddressNotFoundException,PaymentMethodNotFoundException,RestaurantNotFoundException,ItemNotFoundException {

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

        CouponEntity couponEntity = orderDao.getCouponByUUID(ordersEntity.getCouponId().getUuid());
        if (couponEntity == null){
            throw new CouponNotFoundException("CPF-002", "No coupon by this id");
        }

        AddressEntity addressEntity = addressDao.getAddressByUid(ordersEntity.getAddressId().getUuid());
        if (addressEntity == null){
            throw new AddressNotFoundException("ANF-003", "No address by this id");
        }

        CustomerEntity customen = customerAuthEntity.getCustomerId();
        CustomerAddressEntity customerAddressEntity = addressDao.getCustomerId(customen.getId());

        if (customerAddressEntity.getAddressId().getId() != addressEntity.getId()){
            throw new AuthorizationFailedException("ATHR-004", "You are not authorized to view/update/delete any one else's address ");
        }

        PaymentEntity paymentEntity = orderDao.getPaymentByUUID(ordersEntity.getPaymentId().getUuid());
        if(paymentEntity == null){
            throw new PaymentMethodNotFoundException("PNF-002", "No payment method found by this id ");
        }

        RestaurantEntity restaurantEntity = restaurantDao.restaurantsByRestaurantId(ordersEntity.getRestaurantId().getUuid());
        if (restaurantEntity == null){
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id ");
        }

        ItemEntity itemEntity = itemDao.getItemById(orderItemEntity.getItemId().getUuid());
        if (itemEntity == null){
            throw new ItemNotFoundException("INF-003", "No item by this id exist ");
        }

        ordersEntity.setCouponId(couponEntity);
        ordersEntity.setRestaurantId(restaurantEntity);
        ordersEntity.setPaymentId(paymentEntity);
        ordersEntity.setCustomerId(customen);
        ordersEntity.setAddressId(addressEntity);
        ordersEntity.setDate(ZonedDateTime.now());

        OrdersEntity ordersEntity1 = orderDao.SaveOrder(ordersEntity);

        orderItemEntity.setOrderId(ordersEntity1);
        orderItemEntity.setItemId(itemEntity);

        orderDao.SaveOrderItem(orderItemEntity);

       return ordersEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public List<OrdersEntity> getOrders(String authorization) throws AuthorizationFailedException{

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

        CustomerEntity customen = customerAuthEntity.getCustomerId();

        List<OrdersEntity> lstOrders = orderDao.getOrders(customen.getId());


        return lstOrders;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public OrderItemEntity getOrdersItems(Integer orderid){

        OrderItemEntity orderItemEntity = orderDao.getOrdersItems(orderid);
        return orderItemEntity;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public ItemEntity getItems(String itemid){

        ItemEntity itemEntity = itemDao.getItemById(itemid);
        return itemEntity;
    }
}
