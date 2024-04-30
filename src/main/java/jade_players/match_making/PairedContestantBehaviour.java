package jade_players.match_making;

import jade.core.AID;

import java.util.List;

/**
 * {@link ContestantBehaviour} для матчей 1 на 1
 *
 * @author Marat Gumerov
 * @since 30.04.2024
 */
public abstract class PairedContestantBehaviour extends ContestantBehaviour {
    @Override
    protected void onMatchFound(int myIndex, List<AID> otherContestants) {
        onMatchFound(myIndex == 0, otherContestants.get(0));
    }
    
    /**
     * Действия при нахождении матча
     * @param isFirst является ли игрок первым
     * @param opponent AID оппонента
     */
    protected abstract void onMatchFound(boolean isFirst, AID opponent);
}
