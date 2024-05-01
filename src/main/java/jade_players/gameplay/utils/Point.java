package jade_players.gameplay.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Точка в дискретном пространстве
 * Используется в {@link Grid} и {@link Grid.Cell} для хранения позиции ячеек
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public record Point(int x, int y) {
    
    /**
     * Получить новую точку, смещенную относительно текущей на dx, dy
     */
    public Point translate(int dx, int dy) {
        return new Point(x + dx, y + dy);
    }
    
    /**
     * Получить новую точку, смещенную относительно текущей в заданном направлении
     */
    public Point translate(Direction direction){
        return translate(direction.dx, direction.dy);
    }
    
    /**
     * Получить соседние позиции по всем направлениям (включая диагональные)
     */
    public Map<Direction, Point> getNeighbors(){
        return getNeighbors(Direction.values());
    }
    
    /**
     * Получить соседние позиции по ортогональным направлениям (не диагональные)
     */
    public Map<Direction, Point> getOrthogonalNeighbors(){
        return getNeighbors(Direction.values(Direction::isOrthogonal));
    }
    
    /**
     * Получить соседние позиции по заданным направлениям
     */
    public Map<Direction, Point> getNeighbors(Direction... directions){
        return Arrays.stream(directions).collect(Collectors.toMap(
            Function.identity(),
            this::translate
        ));
    }
}
