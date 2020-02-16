package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.businness.OrderService;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.*;
import net.minidev.json.JSONObject;
import org.hibernate.internal.CriteriaImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.math.BigDecimal;
import java.util.*;

@CrossOrigin
@RestController
@RequestMapping("/")
public class OrderController {

    @Autowired
    private OrderService orderService;



    @RequestMapping(method = RequestMethod.GET, path = "order/coupon/{coupon_name}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CouponDetailsResponse> GetCouponbyCouponName(@PathVariable("coupon_name") final String coupon_name, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, CouponNotFoundException
    {
         String [] bearerToken = authorization.split("Bearer ");

         CouponEntity couponEntity = orderService.getCoupon(coupon_name,bearerToken[1]);

        CouponDetailsResponse couponDetailsResponse = new CouponDetailsResponse().id(UUID.fromString(couponEntity.getUuid())).couponName(couponEntity.getCouponName()).percent(couponEntity.getPercent());
        return new ResponseEntity<CouponDetailsResponse>(couponDetailsResponse, HttpStatus.CREATED);

    }

    @RequestMapping(method = RequestMethod.POST, path = "/order", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveOrderResponse> SaveOrder(final SaveOrderRequest saveOrderRequest,@RequestHeader("authorization") final String authorization) throws CouponNotFoundException, AuthorizationFailedException,AddressNotFoundException,PaymentMethodNotFoundException,RestaurantNotFoundException,ItemNotFoundException {

        String [] bearerToken = authorization.split("Bearer ");

        final OrdersEntity ordersEntity = new OrdersEntity();
        final CouponEntity couponEntity = new CouponEntity();
        final AddressEntity addressEntity = new AddressEntity();
        final PaymentEntity paymentEntity = new PaymentEntity();
        final RestaurantEntity restaurantEntity = new RestaurantEntity();
        final OrderItemEntity itemEntity = new OrderItemEntity();
        final ItemEntity itemEntity1 = new ItemEntity();

        ordersEntity.setUuid(UUID.randomUUID().toString());
        ordersEntity.setBill(saveOrderRequest.getBill().doubleValue());
        ordersEntity.setDiscount(saveOrderRequest.getDiscount().doubleValue());

        couponEntity.setUuid((saveOrderRequest.getCouponId()).toString());
        addressEntity.setUuid(saveOrderRequest.getAddressId());
        paymentEntity.setUuid(saveOrderRequest.getPaymentId().toString());
        restaurantEntity.setUuid(saveOrderRequest.getRestaurantId().toString());
        List<ItemQuantity> lstQuant = saveOrderRequest.getItemQuantities();

        for (ItemQuantity a: lstQuant){
            itemEntity1.setUuid(a.getItemId().toString());
            itemEntity.setItemId(itemEntity1);
            itemEntity.setQuantity(a.getQuantity());
            itemEntity.setPrice(a.getPrice());
        }

        ordersEntity.setCouponId(couponEntity);
        ordersEntity.setAddressId(addressEntity);
        ordersEntity.setPaymentId(paymentEntity);
        ordersEntity.setRestaurantId(restaurantEntity);

        final OrdersEntity ordersEntity1 = orderService.SaveOrder(ordersEntity,itemEntity,bearerToken[1]);

        SaveOrderResponse saveOrderResponse = new SaveOrderResponse().id(ordersEntity1.getUuid()).status("ORDER SUCCESSFULLY PLACED");
        return new ResponseEntity<SaveOrderResponse>(saveOrderResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/order", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<CustomerOrderResponse> GetPastOrdersofUser(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException{

        String [] bearerToken = authorization.split("Bearer ");
        List<OrdersEntity> ordersEntities = orderService.getOrders(bearerToken[1]);

        List<OrderList> orderLists = new ArrayList<>();
        CustomerOrderResponse customerOrderResponse = new CustomerOrderResponse();
        for (OrdersEntity n:ordersEntities){
           OrderList ord = new OrderList();

           ord.setId(UUID.fromString(n.getUuid()));
            ord.setBill(BigDecimal.valueOf(n.getBill()));
            ord.setDiscount(BigDecimal.valueOf(n.getDiscount()));
            ord.setDate(n.getDate().toString());

            OrderListCoupon orderListCoupon = new OrderListCoupon();
            orderListCoupon.setId(UUID.fromString(n.getCouponId().getUuid()));
            orderListCoupon.setCouponName(n.getCouponId().getCouponName());
            orderListCoupon.setPercent(n.getCouponId().getPercent());

            ord.setCoupon(orderListCoupon);

            OrderListPayment orderListPayment = new OrderListPayment();
            orderListPayment.setId(UUID.fromString(n.getPaymentId().getUuid()));
            orderListPayment.setPaymentName(n.getPaymentId().getPaymentName());
            ord.setPayment(orderListPayment);

            OrderListCustomer orderListCustomer = new OrderListCustomer();
            orderListCustomer.setId(UUID.fromString(n.getCustomerId().getUuid()));
            orderListCustomer.setContactNumber(n.getCustomerId().getContactNumber());
            orderListCustomer.setEmailAddress(n.getCustomerId().getEmail());
            orderListCustomer.setFirstName(n.getCustomerId().getFirstname());
            orderListCustomer.setLastName(n.getCustomerId().getLastname());
            ord.setCustomer(orderListCustomer);

            OrderListAddress orderListAddress = new OrderListAddress();
            orderListAddress.setId(UUID.fromString(n.getAddressId().getUuid()));
            orderListAddress.setCity(n.getAddressId().getCity());
            orderListAddress.setFlatBuildingName(n.getAddressId().getFlatBuilNumber());
            orderListAddress.setLocality(n.getAddressId().getLocality());
            orderListAddress.setPincode(n.getAddressId().getPincode());

            OrderListAddressState orderListAddressState = new OrderListAddressState();
            orderListAddressState.setId(UUID.fromString(n.getAddressId().getState().getUuid()));
            orderListAddressState.setStateName(n.getAddressId().getState().getStateName());

            orderListAddress.setState(orderListAddressState);
            ord.setAddress(orderListAddress);

            OrderItemEntity orderItemEntity = orderService.getOrdersItems(n.getId());

            List<ItemQuantityResponse> itemQuantityResponse = new ArrayList<>();
            ItemQuantityResponse itemQuantityResponse1 = new ItemQuantityResponse();

            ItemEntity itemEntity = orderService.getItems(orderItemEntity.getItemId().getUuid());

            ItemQuantityResponseItem itemQuantityResponseItem = new ItemQuantityResponseItem();

            itemQuantityResponseItem.setId(UUID.fromString(itemEntity.getUuid()));
            itemQuantityResponseItem.setItemName(itemEntity.getItemName());
            itemQuantityResponseItem.setItemPrice(itemEntity.getPrice());


            itemQuantityResponse1.setItem(itemQuantityResponseItem);
            itemQuantityResponse1.setPrice(orderItemEntity.getPrice());
            itemQuantityResponse1.setQuantity(orderItemEntity.getQuantity());

            itemQuantityResponse.add(itemQuantityResponse1);

            ord.setItemQuantities(itemQuantityResponse);


            orderLists.add(ord);
        }

        customerOrderResponse.setOrders(orderLists);

        return new ResponseEntity<CustomerOrderResponse>(customerOrderResponse, HttpStatus.OK);
    }

}
