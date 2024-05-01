package jade_players.gameplay.battleship;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Генерация кораблей на поле для морского боя
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public abstract class ShipGeneration {
    private static final int SHIP_GEN_MAX_TRIES = 5;
    
    private static final List<Integer> SHIP_LENGTHS = List.of(
        1, 1, 1, 1,
        2, 2, 2,
        3, 3,
        4
    );
    
    public static void main(String[] args) {
        generateShipsInOcean(Ocean.OCEAN_DIM);
    }
    
    public static Ocean generateShipsInOcean(int oceanDim){
        Ocean ocean = new Ocean(oceanDim);
        List<Integer> shipLengths = new ArrayList<>(SHIP_LENGTHS);
        Collections.reverse(shipLengths);
        placeShipRandomly(ocean, shipLengths, 0, SHIP_GEN_MAX_TRIES);
        ocean.print();
        return ocean;
    }
    
    private static boolean placeShipRandomly(
        Ocean ocean,
        List<Integer> lengths,
        int lengthIndex,
        int maxTries
    ){
        if(lengthIndex >= lengths.size()) return true;
        int length = lengths.get(lengthIndex);
        for (int i = 0; i < maxTries; i++) {
            Ship ship = Ship.createRandomShip(length, ocean.dimX, ocean.dimY);
//            System.out.println(repeat("-", lengthIndex) + length);
            if(!ship.place(ocean)){
                continue;
            }
            if(placeShipRandomly(ocean, lengths, lengthIndex + 1, maxTries)){
                return true;
            }
            ship.remove(ocean);
        }
        return false;
    }
    
    
    private static String repeat(String s, int n){
        return new String(new char[n]).replace("\0", s);
    }
}
