package com.upgrad.FoodOrderingApp.api.controller;
import com.upgrad.FoodOrderingApp.api.model.ItemListResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemQuantityResponse;
import com.upgrad.FoodOrderingApp.api.model.ItemQuantityResponseItem;
import com.upgrad.FoodOrderingApp.api.model.PaymentResponse;
import com.upgrad.FoodOrderingApp.service.businness.ItemService;
import com.upgrad.FoodOrderingApp.service.businness.PaymentService;
import com.upgrad.FoodOrderingApp.service.entity.PaymentEntity;
import com.upgrad.FoodOrderingApp.service.entity.ItemEntity;
import com.upgrad.FoodOrderingApp.service.exception.RestaurantNotFoundException;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @RequestMapping(method = RequestMethod.GET, path = "/item/restaurant/{restaurant_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<ItemEntity> GetTop5ItemsbyPopularity(@PathVariable("restaurant_id") final String restaurant_id)throws RestaurantNotFoundException
    {
        final ItemEntity itemEntities = itemService.getTopItem(restaurant_id);

        return new ResponseEntity<ItemEntity>(itemEntities, HttpStatus.OK);
    }
}
