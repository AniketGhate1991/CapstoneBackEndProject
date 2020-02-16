package com.upgrad.FoodOrderingApp.service.dao;

import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.CustomerAddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
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
public class AddressDao {
    @PersistenceContext
    private EntityManager entityManager;

    public StateEntity getAddressByStateUid(String uid) {
        try {
            return entityManager.createNamedQuery("addressByuuid" , StateEntity.class).setParameter("uuid" , uid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public CustomerAddressEntity getCustomerId(Integer customerId) {
        try {
            return entityManager.createNamedQuery("customerByid" , CustomerAddressEntity.class).setParameter("customerId" , customerId).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public AddressEntity craeteAddress(AddressEntity addressEntity) {
        entityManager.persist(addressEntity);
        return addressEntity;
    }

    public CustomerAddressEntity craeteAddress1(CustomerAddressEntity customerAddressEntity) {
        entityManager.persist(customerAddressEntity);
        return customerAddressEntity;
    }

    public List<AddressEntity> getAllAddress() {

        try {
            return entityManager.createNamedQuery("getAllAddress", AddressEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public List<StateEntity> getAllStates() {

        try {
            return entityManager.createNamedQuery("getAllStates", StateEntity.class).getResultList();
        } catch (NoResultException nre) {
            return null;
        }
    }

    public AddressEntity getAddressByUid(String uid) {
        try {
            return entityManager.createNamedQuery("addressByuuids" , AddressEntity.class).setParameter("uuid" , uid).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Transactional
    public AddressEntity DeleteAddress(AddressEntity addressEntity){
        entityManager.remove(addressEntity);

        return addressEntity;
    }
    @Transactional
    public CustomerAddressEntity DeleteAddressCustomer(CustomerAddressEntity customerAddressEntity){
        entityManager.remove(customerAddressEntity);

        return customerAddressEntity;
    }
}
