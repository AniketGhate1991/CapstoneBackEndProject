package com.upgrad.FoodOrderingApp.service.businness;
import com.upgrad.FoodOrderingApp.service.dao.ItemDao;
import com.upgrad.FoodOrderingApp.service.dao.RestaurantDao;
import com.upgrad.FoodOrderingApp.service.entity.*;
import com.upgrad.FoodOrderingApp.service.exception.AuthorizationFailedException;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ItemService {
    @Autowired
    private ItemDao itemDao;

    @Autowired
    private RestaurantDao restaurantDao;

    public ItemEntity getTopItem(String restaurant) throws RestaurantNotFoundException {

        RestaurantEntity restaurantEntity = restaurantDao.restaurantsByRestaurantId(restaurant);
        if (restaurantEntity == null){
            throw new RestaurantNotFoundException("RNF-001", "No restaurant by this id");
        }
         ItemEntity obj = new ItemEntity();

        return obj;

    }
}
