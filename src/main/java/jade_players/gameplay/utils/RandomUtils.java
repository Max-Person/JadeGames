package jade_players.gameplay.utils;

import java.util.Date;
import java.util.Random;

/**
 * Доступ к единому генератору случайных чисел
 *
 * @author Marat Gumerov
 * @since 01.05.2024
 */
public abstract class RandomUtils {
    public static final Random random = new Random(new Date().getTime());
}
