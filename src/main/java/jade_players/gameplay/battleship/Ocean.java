package jade_players.gameplay.battleship;

import jade_players.gameplay.utils.Grid;
import jade_players.gameplay.utils.Point;

import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Океан - поле для морского боя. Клетки океана содержат состояние {@link OceanState}
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public class Ocean extends Grid<Ocean.OceanState> {
    public static final int OCEAN_DIM = 10;
    
    public Ocean(int dim) {
        super(dim, dim, () -> OceanState.ocean);
    }
    
    /**
     * Состояние клетки океана
     */
    public enum OceanState {
        ocean('~'),
        ship('S'),
        sunken_ship('X'),
        ;
        
        private final Character tileChar;
        
        OceanState(Character tileChar) {
            this.tileChar = tileChar;
        }
        
        public Character getTileChar() {
            return tileChar;
        }
    }
    
    /**
     * Вывести океан в консоль
     */
    public void print(){
        asRows().forEach(row -> {
            row.forEach(cell -> {
                System.out.print(cell.getContent().getTileChar());
                System.out.print("  ");
            });
            System.out.println();
        });
        System.out.println();
    }
}
