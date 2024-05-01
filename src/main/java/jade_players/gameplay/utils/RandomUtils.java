package jade_players.gameplay.utils;

import java.util.*;
import java.util.function.Predicate;

/**
 * Доступ к единому генератору случайных чисел
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public abstract class RandomUtils {
    public static final Random random = new Random(new Date().getTime());
    
    public static <T> Optional<T> chooseAnyRandomly(Collection<T> collection){
        if(collection.isEmpty()) return Optional.empty();
        List<T> list = new ArrayList<>(collection);
        return Optional.of(list.get(random.nextInt(list.size())));
    }
    
    public static Point randomPoint(int xBound, int yBound){
        return new Point(random.nextInt(xBound), random.nextInt(yBound));
    }
    
    public static Point randomPoint(Grid<?> grid){
        return randomPoint(grid.dimX, grid.dimY);
    }
    
    public static <T> Optional<Grid.Cell<T>> randomCellThatMatches(Grid<T> grid, Predicate<Grid.Cell<T>> filter){
        return chooseAnyRandomly(grid.values().stream().filter(filter).toList());
    }
}
