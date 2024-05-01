package jade_players.gameplay.battleship;

import jade_players.gameplay.PlayerAgent;
import jade_players.gameplay.utils.Direction;
import jade_players.gameplay.utils.Grid;
import jade_players.gameplay.utils.Point;
import jade_players.gameplay.utils.RandomUtils;

import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Агент-игрок в морской бой
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public class BattleshipAgent extends PlayerAgent {
    private static final int OCEAN_DIM = Ocean.OCEAN_DIM;
    
    private final Ocean myOcean = ShipGeneration.generateFriendlyOcean(OCEAN_DIM);
    private int myShipCellsCount = ShipGeneration.getShipCellsCount();
    private final Ocean enemyOcean = Ocean.enemy(Ocean.OCEAN_DIM);
    private int enemyShipCellsCount = ShipGeneration.getShipCellsCount();
    
    private boolean log = false;
    
    @Override
    protected void setup() {
        super.setup();
        log = hasArgument("log");
    }
    
    private enum HitResult{
        miss,
        hit,
        sink,
        repeat_sink,
        ;
    }
    
    private Point myLastTurn;
    private boolean iSkipTurn;
    
    @Override
    protected double turnPauseInSeconds() {
        return 0.2;
    }
    
    @Override
    protected TurnResult processOpponentsTurn(String opponentTurnMessageContent) {
        Deque<String> lines = new ArrayDeque<>(List.of(opponentTurnMessageContent.split("\n")));
        boolean iHit = false;
        if(!iSkipTurn && myLastTurn != null){ //Проверить результат моего предыдущего хода
            HitResult response = HitResult.valueOf(lines.pollFirst());
            if(log){
                System.out.println(response + "!");
            }
            switch (response){
                case sink -> sinkEnemyShip(myLastTurn);
                case repeat_sink -> {}
                case hit -> enemyOcean.put(myLastTurn, Ocean.OceanState.ship);
                case miss -> enemyOcean.put(myLastTurn, Ocean.OceanState.ocean);
            }
            iHit = response == HitResult.hit || response == HitResult.sink;
            if(iHit){
                enemyShipCellsCount -= 1;
            }
            if(enemyShipCellsCount == 0){
                return TurnResult.iWon();
            }
        }
        if(!iHit){ //Если мой предыдущий ход не попал, значит оппонент делает свой
            int x = Integer.parseInt(lines.pollFirst());
            int y = Integer.parseInt(lines.pollFirst());
            Point hitPoint = new Point(x, y);
            Grid.Cell<Ocean.OceanState> hitCell = myOcean.get(hitPoint);
            HitResult hitResult;
            if(hitCell.getContent() == Ocean.OceanState.ship){
                hitCell.setContent(Ocean.OceanState.sunken_ship);
                hitResult = checkMyShipIsSunk(hitPoint) ? HitResult.sink : HitResult.hit;
                iSkipTurn = true;
                myShipCellsCount -= 1;
                if(myShipCellsCount == 0){
                    return TurnResult.iLost(hitResult.name());
                }
            }
            else {
                hitResult = HitResult.miss;
                iSkipTurn = false;
            }
            return TurnResult.gameContinues(hitResult.name());
        }
        return TurnResult.gameContinues();
    }
    
    private boolean checkMyShipIsSunk(Point start){
        return getShipCells(start, myOcean).stream()
            .noneMatch(cell -> cell.getContent() == Ocean.OceanState.ship);
    }
    
    private void sinkEnemyShip(Point start){
        getShipCells(start, enemyOcean).forEach(cell -> {
            cell.setContent(Ocean.OceanState.sunken_ship);
            cell.getNeighbors().values().forEach(neighbor -> {
                if(neighbor.getContent() == Ocean.OceanState.unknown){
                    neighbor.setContent(Ocean.OceanState.ocean);
                }
            });
        });
    }
    
    private List<Grid.Cell<Ocean.OceanState>> getShipCells(Point start, Ocean ocean){
        Set<Grid.Cell<Ocean.OceanState>> shipCells = new HashSet<>();
        shipCells.add(ocean.get(start));
        while (true){
            Set<Grid.Cell<Ocean.OceanState>> additionalCells = shipCells.stream()
                .map(Grid.Cell::getNeighborsAndSelf)
                .flatMap(List::stream)
                .filter(cell ->
                    !shipCells.contains(cell)
                        && (cell.getContent() == Ocean.OceanState.ship
                        || cell.getContent() == Ocean.OceanState.sunken_ship)
                )
                .collect(Collectors.toSet());
            if(additionalCells.isEmpty()) break;
            shipCells.addAll(additionalCells);
        }
        return shipCells.stream()
            .sorted(
                Comparator.<Grid.Cell<?>, Integer>comparing(cell -> cell.getPoint().x())
                    .thenComparing(cell -> cell.getPoint().y())
            )
            .collect(Collectors.toList());
    }
    
    @Override
    protected TurnResult takeYourTurn(TurnResult opponentsTurnResult) {
        if(iSkipTurn) return opponentsTurnResult;
        Point attackPoint = chooseAttackPoint(enemyOcean);
        myLastTurn = attackPoint;
        if(log){
            System.out.println("Game state on " + getAID().getLocalName() + "'s turn : ");
            myOcean.print();
            enemyOcean.print();
            System.out.println("Attacking " + attackPoint);
        }
        return TurnResult.gameContinues(
            opponentsTurnResult.messageToOpponent()
            + attackPoint.x() + "\n"
            + attackPoint.y() + "\n"
        );
    }
    
    protected Point chooseAttackPoint(Ocean enemyOcean) {
        Optional<Grid.Cell<Ocean.OceanState>> shipCellOpt = enemyOcean.values().stream()
            .filter(cell -> cell.getContent() == Ocean.OceanState.ship)
            .findFirst();
        if(shipCellOpt.isPresent()){ //Если уже подбили какой-то корабль - атакуем его
            List<Grid.Cell<Ocean.OceanState>> shipCells = getShipCells(shipCellOpt.get().getPoint(), enemyOcean);
            Predicate<Direction> directionsFilter = Direction::isOrthogonal;
            if(shipCells.size() >= 2){
                directionsFilter = shipCells.get(0).getPoint().x() == shipCells.get(1).getPoint().x()
                    ? Direction::isVertical
                    : Direction::isHorizontal;
            }
            Direction[] possibleHitDirections = Direction.values(directionsFilter);
            Set<Grid.Cell<Ocean.OceanState>> possibleAttackCells = Stream.of(
                    shipCells.get(0),
                    shipCells.get(shipCells.size() - 1)
                )
                .map(cell -> cell.getNeighbors(possibleHitDirections).values())
                .flatMap(Collection::stream)
                .filter(cell -> cell.getContent() == Ocean.OceanState.unknown)
                .collect(Collectors.toSet());
            return possibleAttackCells.stream().findFirst().orElseThrow().getPoint();
        }
        else { //Иначе стреляем в случайные неизвестные клетки
            return RandomUtils.randomCellThatMatches(
                enemyOcean,
                cell -> cell.getContent() == Ocean.OceanState.unknown
            ).get().getPoint();
        }
    }
}
