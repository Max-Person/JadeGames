package jade_players.gameplay.battleship;

import jade_players.gameplay.utils.Grid;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Океан - поле для морского боя. Клетки океана содержат состояние {@link OceanState}
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public class Ocean extends Grid<Ocean.OceanState> {
    public static final int OCEAN_DIM = 10;
    
    private Ocean(int dim, OceanState initialState) {
        super(dim, dim, () -> initialState);
    }
    
    public static Ocean friendly(int dim){
        return new Ocean(dim, OceanState.ocean);
    }
    
    public static Ocean enemy(int dim){
        return new Ocean(dim, OceanState.unknown);
    }
    
    /**
     * Состояние клетки океана
     */
    public enum OceanState {
        unknown('.'),
        ocean(' '),
        ship('#'),
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
        System.out.print("   ");
        IntStream.range(0, dimX).forEach(i -> System.out.print(i + "  "));
        System.out.println();
        AtomicInteger colCount = new AtomicInteger();
        asRows().forEach(row -> {
            System.out.print(colCount.getAndIncrement() + "  ");
            row.forEach(cell -> {
                System.out.print(cell.getContent().getTileChar());
                System.out.print("  ");
            });
            System.out.println();
        });
        System.out.println();
    }
}
