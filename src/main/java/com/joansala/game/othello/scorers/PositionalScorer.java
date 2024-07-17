package com.joansala.game.othello.scorers;

/*
 * Samurai framework.
 * Copyright (C) 2024 Joan Sala Soler <contact@joansala.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation,  either version 3 of the License,  or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not,  see <http://www.gnu.org/licenses/>.
 */

import com.joansala.engine.Scorer;
import com.joansala.game.othello.OthelloGame;
import static com.joansala.game.othello.Othello.*;
import static com.joansala.util.bits.Bits.*;


/**
 * This heuristic function evaluates the position of pieces on a
 * othello board to estimate who has the advantage.
 */
public final class PositionalScorer implements Scorer<OthelloGame> {

    /** Board weights evaluation mask */
    static final long WEIGHTS_MASK = 0XFFE7C38181C3E7FFL;


    /**
     * {@inheritDoc}
     */
    public final int evaluate(OthelloGame game) {
        int score = 0;

        long south = WEIGHTS_MASK & game.state(SOUTH_STONE);

        while (empty(south) == false) {
            final int checker = first(south);
            score += WEIGHTS[checker];
            south ^= bit(checker);
        }

        long north = WEIGHTS_MASK & game.state(NORTH_STONE);

        while (empty(north) == false) {
            final int checker = first(north);
            score -= WEIGHTS[checker];
            north ^= bit(checker);
        }

        return score;
    }


    /** Weight of each owned board checker */
    private static final int[] WEIGHTS = {
        90,  34, 18, 17, 17, 18,  34, 90,
        34, -15, -3,  0,  0, -3, -15, 34,
        18,  -3,  0,  0,  0,  0,  -3, 18,
        17,   0,  0,  0,  0,  0,   0, 17,
        17,   0,  0,  0,  0,  0,   0, 17,
        18,  -3,  0,  0,  0,  0,  -3, 18,
        34, -15, -3,  0,  0, -3, -15, 34,
        90,  34, 18, 17, 17, 18,  34, 90
    };
}
