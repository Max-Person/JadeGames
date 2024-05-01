package jade_players.gameplay.battleship;

import jade_players.gameplay.utils.Direction;
import jade_players.gameplay.utils.Grid;
import jade_players.gameplay.utils.Point;
import jade_players.gameplay.utils.RandomUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Корабль на поле для морского боя
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public class Ship {
    private final int length;
    private final boolean isHorizontal;
    private final Point tip;
    
    private Ship(int length, boolean isHorizontal, Point tip) {
        this.length = length;
        this.isHorizontal = isHorizontal;
        this.tip = tip;
    }
    
    public Set<Point> getOccupiedPoints() {
        Direction delta = isHorizontal ? Direction.right : Direction.down;
        Point current = tip;
        Set<Point> points = new HashSet<>();
        for (int i = 0; i < length; i++) {
            points.add(current);
            current = current.translate(delta);
        }
        return points;
    }
    
    public List<Grid.Cell<Ocean.OceanState>> getOccupiedCells(Grid<Ocean.OceanState> ocean) {
        return getOccupiedPoints().stream()
            .map(ocean::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
    
    public boolean canPlace(Ocean ocean) {
        List<Grid.Cell<Ocean.OceanState>> occupiedCells = getOccupiedCells(ocean);
        
        if (occupiedCells.size() != length) { //Выходит за пределы поля
            return false;
        }
        
        return occupiedCells.stream()
            .map(Grid.Cell::getNeighborsAndSelf)
            .flatMap(List::stream)
            .allMatch(cell -> cell.getContent() == Ocean.OceanState.ocean);
    }
    
    public boolean place(Ocean ocean) {
        if (canPlace(ocean)) {
            getOccupiedPoints().forEach(p -> ocean.put(p, Ocean.OceanState.ship));
            return true;
        }
        return false;
    }
    
    public void remove(Ocean ocean) {
        getOccupiedPoints().forEach(p -> ocean.put(p, Ocean.OceanState.ocean));
    }
    
    public static Ship createRandomShip(int length, int oceanDimX, int oceanDimY) {
        Random random = RandomUtils.random;
        boolean isHorizontal = random.nextBoolean();
        int tipX = random.nextInt(oceanDimX - (isHorizontal ? (length - 1) : 0));
        int tipY = random.nextInt(oceanDimY - (!isHorizontal ? (length - 1) : 0));
        return new Ship(length, isHorizontal, new Point(tipX, tipY));
    }
}
