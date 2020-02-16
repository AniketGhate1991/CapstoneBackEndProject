package com.upgrad.FoodOrderingApp.api.controller;

import com.upgrad.FoodOrderingApp.api.model.*;
import com.upgrad.FoodOrderingApp.service.businness.AddressService;
import com.upgrad.FoodOrderingApp.service.entity.AddressEntity;
import com.upgrad.FoodOrderingApp.service.entity.StateEntity;
import com.upgrad.FoodOrderingApp.service.exception.*;
import net.minidev.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@CrossOrigin
@RestController
@RequestMapping("/")
public class AddressController {

    @Autowired
    private AddressService addressService;

    @RequestMapping(method = RequestMethod.POST, path = "/address", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SaveAddressResponse> SaveAddress(final SaveAddressRequest saveAddressRequest,@RequestHeader("authorization") final String authorization) throws SaveAddressException, AuthorizationFailedException,AddressNotFoundException {

        String [] bearerToken = authorization.split("Bearer ");

        final AddressEntity addEntity = new AddressEntity();
        final StateEntity stateEntity = new StateEntity();

        addEntity.setUuid(UUID.randomUUID().toString());
        addEntity.setFlatBuilNumber(saveAddressRequest.getFlatBuildingName());
        addEntity.setLocality(saveAddressRequest.getLocality());
        addEntity.setCity(saveAddressRequest.getCity());
        addEntity.setPincode(saveAddressRequest.getPincode());

        stateEntity.setUuid(saveAddressRequest.getStateUuid());

       addEntity.setState(stateEntity);
        final AddressEntity addressEntity = addressService.SaveAddress(addEntity,bearerToken[1]);

        SaveAddressResponse addressResponse = new SaveAddressResponse().id(addressEntity.getUuid()).status("ADDRESS SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SaveAddressResponse>(addressResponse, HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/address/customer", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> GetAllSavedAddresses(@RequestHeader("authorization") final String authorization) throws AuthorizationFailedException
    {
        String [] bearerToken = authorization.split("Bearer ");

        final List<AddressEntity> addressEntities = addressService.getAllAddress(bearerToken[1]);


        List<JSONObject> entities = new ArrayList<JSONObject>();
        for (AddressEntity n : addressEntities) {
            JSONObject Entity = new JSONObject();
            Entity.put("id", n.getUuid());
            Entity.put("flat_building_name", n.getFlatBuilNumber());
            Entity.put("locality", n.getLocality());
            Entity.put("city", n.getCity());
            Entity.put("pincode", n.getPincode());
            Entity.put("state", n.getState());
            entities.add(Entity);
        }

        return new ResponseEntity<Object>(entities, HttpStatus.OK);

    }

    @RequestMapping(method = RequestMethod.DELETE, path = "/address/{address_id}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<DeleteAddressResponse> DeleteSavedAddress(@PathVariable("address_id") final String AddressUUID, @RequestHeader("authorization") final String authorization) throws AuthorizationFailedException, AddressNotFoundException
    {
        String [] bearerToken = authorization.split("Bearer ");

        final AddressEntity addressEntities = addressService.DeleteAddress(AddressUUID,bearerToken[1]);

        DeleteAddressResponse deleteAddressResponse = new DeleteAddressResponse().id(UUID.randomUUID()).status("ADDRESS DELETED SUCCESSFULLY");

        return new ResponseEntity<DeleteAddressResponse>(deleteAddressResponse, HttpStatus.OK);
    }

    @RequestMapping(method = RequestMethod.GET, path = "/states", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<Object> GetAllStates()
    {

        final List<StateEntity> stateEntities = addressService.getAllStates();


        List<JSONObject> entities = new ArrayList<JSONObject>();
        for (StateEntity n : stateEntities) {
            JSONObject Entity = new JSONObject();
            Entity.put("state_name", n.getStateName());
            Entity.put("id", n.getUuid());


            entities.add(Entity);
        }

        return new ResponseEntity<Object>(entities, HttpStatus.OK);

    }
}
