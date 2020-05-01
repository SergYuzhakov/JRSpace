package com.space.service;

import com.space.controller.ShipOrder;
import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


@Service
@Transactional
public class ShipServiceImpl implements ShipService{

        private final ShipRepository shipRepository;

        @Autowired
        public ShipServiceImpl(ShipRepository shipRepository) {
            this.shipRepository = shipRepository;
        }

        @Override
        public List<Ship> findShips(String name,
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
                                  Double maxRating) {
            final Date afterDate = after == null ? null : new Date(after);
            final Date beforeDate = before == null ? null : new Date(before);
            final List<Ship> list = new ArrayList<>();
            shipRepository.findAll().forEach((ship) -> {
                if (name != null && !ship.getName().contains(name)) return;
                if (planet != null && !ship.getPlanet().contains(planet)) return;
                if (shipType != null && ship.getShipType() != shipType) return;
                if (afterDate != null && ship.getProdDate().before(afterDate)) return;
                if (beforeDate != null && ship.getProdDate().after(beforeDate)) return;
                if (isUsed != null && ship.getUsed().booleanValue() != isUsed.booleanValue()) return;
                if (minSpeed != null && ship.getSpeed().compareTo(minSpeed) < 0) return;
                if (maxSpeed != null && ship.getSpeed().compareTo(maxSpeed) > 0) return;
                if (minCrewSize != null && ship.getCrewSize().compareTo(minCrewSize) < 0) return;
                if (maxCrewSize != null && ship.getCrewSize().compareTo(maxCrewSize) > 0) return;
                if (minRating != null && ship.getRating().compareTo(minRating) < 0) return;
                if (maxRating != null && ship.getRating().compareTo(maxRating) > 0) return;
                list.add(ship);
            });
            return list;
        }



        @Override
        public void deleteShip(Ship ship) {
             shipRepository.delete(ship);
        }

        @Override
        public Ship getShip(Long id) {
            return shipRepository.findById(id).orElse(null);
        }

        @Override
        public Ship updateShip(Ship oldShip, Ship newShip) throws IllegalArgumentException {

            boolean isChangeRating = false;

        final String name = newShip.getName();
        if (name != null) {
            if (isStringValid(name)) {
                oldShip.setName(name);
            } else {
                throw new IllegalArgumentException();
            }
        }

        final String planet = newShip.getPlanet();
        if (planet != null) {
            if (isStringValid(planet)) {
                oldShip.setPlanet(planet);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getShipType() != null) {
            oldShip.setShipType(newShip.getShipType());
        }

        final Date prodDate = newShip.getProdDate();
        if (prodDate != null) {
            if (isProdDateValid(prodDate)) {
                oldShip.setProdDate(prodDate);
                isChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (newShip.getUsed() != null) {
            oldShip.setUsed(newShip.getUsed());
            isChangeRating = true;
        }

        final Double speed = newShip.getSpeed();
        if (speed != null) {
            if (isSpeedValid(speed)) {
                oldShip.setSpeed(speed);
                isChangeRating = true;
            } else {
                throw new IllegalArgumentException();
            }
        }

        final Integer crewSize = newShip.getCrewSize();
        if (crewSize != null) {
            if (isCrewSizeValid(crewSize)) {
                oldShip.setCrewSize(crewSize);
            } else {
                throw new IllegalArgumentException();
            }
        }

        if (isChangeRating) {
            final double rating = computeRating(oldShip.getSpeed(), oldShip.getUsed(), oldShip.getProdDate());
            oldShip.setRating(rating);
        }

                   shipRepository.save(oldShip);
                   return oldShip;

        }

         @Override
         public Ship saveShip(Ship ship) { return shipRepository.save(ship);}

         @Override
         public boolean isShipValid(Ship ship) {
                  return   ship != null
                                && isStringValid(ship.getName())
                                && isStringValid(ship.getPlanet())
                                && isSpeedValid(ship.getSpeed())
                                && isCrewSizeValid(ship.getCrewSize())
                                && isProdDateValid(ship.getProdDate());
             }

         @Override
         public double computeRating(double speed, boolean isUsed, Date prod) {

            final int nowYear = 3019;
            final int prodYear = getYearFromDate(prod);
            final double k = isUsed ? 0.5 : 1.0;
            final double rating = 80 * speed * k / (nowYear - prodYear + 1);

            return round(rating);
    }


        public List<Ship> getPageShips(List<Ship> list, Integer pageNumber, Integer pageSize){

            final Integer pageN = pageNumber == null ? 0 : pageNumber; // если pageNumber не указан – нужно использовать значение 0
            final Integer pageS = pageSize == null ? 3 : pageSize; // если pageSize не указан – нужно использовать значение 3.

            int from = pageN * pageS;
            int to = from + pageS;
            if( to > list.size()) to = list.size(); // предел  subList должен быть не больше размера списка

            return list.subList(from, to);
        }

        public List<Ship> sortListShips(List<Ship> list, ShipOrder order){
            if(order != null){
            list.sort((s1,s2)-> {
                switch (order) {
                    case ID:
                       return s1.getId().compareTo(s2.getId());
                    case DATE:
                       return s1.getProdDate().compareTo(s2.getProdDate());
                    case SPEED:
                       return s1.getSpeed().compareTo(s2.getSpeed());
                    case RATING:
                       return s1.getRating().compareTo(s2.getRating());
                    default:
                        return 0;
                    }
                });
            }
            return list;
        }

    private boolean isCrewSizeValid(Integer crewSize) {
        final int minCrewSize = 1;
        final int maxCrewSize = 9999;
        return crewSize != null && crewSize.compareTo(minCrewSize) >= 0 && crewSize.compareTo(maxCrewSize) <= 0;
    }

    private boolean isSpeedValid(Double speed) {
        final double minSpeed = 0.01;
        final double maxSpeed = 0.99;
        return speed != null && speed.compareTo(minSpeed) >= 0 && speed.compareTo(maxSpeed) <= 0;
    }

    private boolean isStringValid(String value) {
        final int maxStringLength = 50;
        return value != null && !value.isEmpty() && value.length() <= maxStringLength;
    }

    private boolean isProdDateValid(Date prodDate) {
        final Date startProd = getDateForYear(2800);
        final Date endProd = getDateForYear(3019);
        return prodDate != null && prodDate.after(startProd) && prodDate.before(endProd);
    }

    private Date getDateForYear(int year) {
        final Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    private int getYearFromDate(Date date) {
        final Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        return calendar.get(Calendar.YEAR);
    }

    private double round(double value) {
        return Math.round(value * 100) / 100D;
    }


}


