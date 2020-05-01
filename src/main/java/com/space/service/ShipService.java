package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;

import java.util.Date;
import java.util.List;

public interface ShipService {

   List<Ship> findShips(String name,
                      String planet,
                      ShipType shipType,
                      Long after,
                      Long before,
                      Boolean isUsed,
                      Double minSpeed,
                      Double maxSpeed,
                      Integer minCrewSize,
                      Integer maxCrewSize,
                      Double minRating,
                      Double maxRating);


   List<Ship> sortListShips(List<Ship> list, ShipOrder order);
   List<Ship> getPageShips(List<Ship> list, Integer pageNumber, Integer pageSize);

   Ship getShip(Long id);
   Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException;
   Ship saveShip(Ship ship);

   boolean isShipValid(Ship ship);
   double computeRating(double speed, boolean isUsed, Date prod);
   void deleteShip(Ship ship);

}
