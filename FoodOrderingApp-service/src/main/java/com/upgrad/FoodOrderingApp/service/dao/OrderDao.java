package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.CustomerNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao {
    @PersistenceContext
    private EntityManager entityManager;

    public CouponEntity getCouponByName(String couponName) {
        try {
            return entityManager.createNamedQuery("getCouponByName" , CouponEntity.class).setParameter("couponName" , couponName).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CouponEntity getCouponByUUID(String UUID) {
        try {
            return entityManager.createNamedQuery("getCouponByUUID" , CouponEntity.class).setParameter("UUID" , UUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public PaymentEntity getPaymentByUUID(String UUID) {
        try {
            return entityManager.createNamedQuery("getPaymentByUUID" , PaymentEntity.class).setParameter("UUID" , UUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public OrdersEntity SaveOrder(OrdersEntity ordersEntity) {
        entityManager.persist(ordersEntity);
        return ordersEntity;
    }

    public OrderItemEntity SaveOrderItem(OrderItemEntity orderItemEntity) {
        entityManager.persist(orderItemEntity);
        return orderItemEntity;
    }

    public List<OrdersEntity> getOrders(Integer UUID) {
        try {
            return entityManager.createNamedQuery("getAllordersByUUID" , OrdersEntity.class).setParameter("UUID" , UUID).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public OrderItemEntity getOrdersItems(Integer orderid) {
        try {
            return entityManager.createNamedQuery("getItemByOrderId" , OrderItemEntity.class).setParameter("orderid" , orderid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}
