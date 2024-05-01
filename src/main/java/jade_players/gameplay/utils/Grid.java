package jade_players.gameplay.utils;


import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Прямоугольная сетка, состоящая из дискретных ячеек.
 * Имеет размеры dimX на dimY.
 * Логика заполнения ячеек в сетке предполагает, что все они существуют,
 * и изначально заполнены некоторым исходным значением.
 * Все операции с ячейками, выходящими за пределы сетки, не имеют эффекта и возвращают null.
 *
 * @param <T> тип содержимого каждой ячейки сетки.
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public class Grid<T> implements Map<Point, Grid.Cell<T>> {
    
    public final int dimX;
    public final int dimY;
    private final Supplier<T> initCellContent;
    
    private final Map<Point, Cell<T>> contents = new HashMap<>();
    
    public Grid(int dimX, int dimY, Supplier<T> initCellContent) {
        this.dimX = dimX;
        this.dimY = dimY;
        this.initCellContent = initCellContent;
    }
    
    @Override
    public int size() {
        return dimX * dimY;
    }
    
    @Override
    public boolean isEmpty() {
        return false;
    }
    
    @Override
    public boolean containsKey(Object o) {
        return o instanceof Point p && isInBounds(p);
    }
    
    public boolean isInBounds(Point point){
        return isInBounds(0, point.x(), dimX) && isInBounds(0, point.y(), dimY);
    }
    
    private boolean isInBounds(int minInclusive, int check, int maxExclusive){
        return  minInclusive <= check && check < maxExclusive;
    }
    
    @Override
    public boolean containsValue(Object o) {
        if(o instanceof Cell<?> searchCell){
            return Objects.equals(this.get(searchCell.point).getContent(), searchCell.getContent());
        }
        return values().stream().anyMatch(cell -> Objects.equals(cell.getContent(), o));
    }
    
    public Cell<T> get(int x, int y){
        return this.get(new Point(x, y));
    }
    
    @Override
    public Cell<T> get(Object o) {
        if(this.containsKey(o)){
            Point p = (Point) o;
            return contents.computeIfAbsent(p, point -> new Cell<>(this, point, initCellContent.get()));
        }
        return null;
    }
    
    public Cell<T> put(Point point, T content) {
        Cell<T> cell = this.get(point);
        if(cell != null){
            cell.setContent(content);
        }
        return cell;
    }
    
    public Cell<T> put(Cell<T> tCell) {
        return this.put(tCell.point, tCell.content);
    }
    
    @Deprecated
    @Override
    public Cell<T> put(Point point, Cell<T> tCell) {
        if(tCell.point.equals(point)){
            return put(point, tCell.content);
        }
        return null;
    }
    
    @Override
    public Cell<T> remove(Object o) {
        if(this.containsKey(o)){
            return this.put((Point) o, initCellContent.get());
        }
        return null;
    }
    
    @Override
    public void putAll(Map<? extends Point, ? extends Cell<T>> map) {
        map.forEach(this::put);
    }
    
    @Override
    public void clear() {
        contents.clear();
    }
    
    @Override
    public Set<Point> keySet() {
        Set<Point> keys = new HashSet<>();
        IntStream.range(0, dimX).forEach(x -> {
            IntStream.range(0, dimY).forEach(y -> {
                keys.add(new Point(x, y));
            });
        });
        return keys;
    }
    
    @Override
    public Set<Cell<T>> values() {
        return keySet().stream().map(this::get).collect(Collectors.toSet());
    }
    
    @Deprecated
    @Override
    public Set<Entry<Point, Cell<T>>> entrySet() {
        return keySet().stream().map(point -> Map.entry(point, this.get(point))).collect(Collectors.toSet());
    }
    
    public List<List<Cell<T>>> asRows(){
        return IntStream.range(0, dimY).mapToObj(y ->
            IntStream.range(0, dimX).mapToObj(x ->
                this.get(x, y)
            ).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }
    
    public List<List<Cell<T>>> asColumns(){
        return IntStream.range(0, dimX).mapToObj(x ->
            IntStream.range(0, dimY).mapToObj(y ->
                this.get(x, y)
            ).collect(Collectors.toList())
        ).collect(Collectors.toList());
    }
    
    public static class Cell<T>{
        private final Grid<T> grid;
        
        private Point point;
        private T content;
        
        private Cell(Grid<T> grid, Point point, T content) {
            this.grid = grid;
            this.point = point;
            this.content = content;
        }
        
        public Point getPoint() {
            return point;
        }
        
        public T getContent() {
            return content;
        }
        
        public void setContent(T content) {
            this.content = content;
        }
        
        public List<Cell<T>> getNeighborsAndSelf(){
            List<Cell<T>> list = new ArrayList<>(getNeighbors().values());
            list.add(0, this);
            return list;
        }
        
        public Map<Direction, Cell<T>> getNeighbors(){
            return getNeighbors(Direction.values());
        }
        
        public Map<Direction, Cell<T>> getNeighbors(Direction... directions){
            Map<Direction, Point> neighborPoints = point.getNeighbors(directions);
            Map<Direction, Cell<T>> neighborCells = new HashMap<>();
            neighborPoints.forEach((dir, p) -> {
                Cell<T> cell = grid.get(p);
                if(cell != null){
                    neighborCells.put(dir, cell);
                }
            });
            return neighborCells;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Cell<?> cell = (Cell<?>) o;
            return Objects.equals(grid, cell.grid)
                && Objects.equals(point, cell.point)
                && Objects.equals(content, cell.content);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(grid, point, content);
        }
    }
}
