package jade_players.gameplay.utils;

import java.util.Arrays;
import java.util.function.Predicate;

/**
 * TODO Class Description
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public enum Direction {
    up          (0, -1),
    up_right    (1, -1),
    right       (1, 0),
    down_right  (1, 1),
    down        (0, 1),
    down_left   (-1, 1),
    left        (-1, 0),
    up_left     (-1, -1),
    ;
    public final int dx;
    public final int dy;
    
    Direction(int dx, int dy){
        this.dx = dx;
        this.dy = dy;
    }
    
    public boolean isHorizontal(){
        return this == left || this == right;
    }
    
    public boolean isVertical(){
        return this == up || this == down;
    }
    
    public boolean isOrthogonal(){
        return isVertical() || isHorizontal();
    }
    
    public boolean isDiagonal(){
        return !isOrthogonal();
    }
    
    public static Direction[] values(Predicate<Direction> predicate){
        return Arrays.stream(values()).filter(predicate).toArray(Direction[]::new);
    }
}
