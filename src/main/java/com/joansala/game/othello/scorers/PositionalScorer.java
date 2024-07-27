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
 * This heuristic function estimates the advantage of a player in an
 * Othello game by evaluating the board position.
 *
 * To fine-tune the piece-square table weights, the same method used for
 * the Draughts engine was used with two modifications:
 *
 * a) Use {@link Montecarlo} to evaluate the training set.
 * b) Augment the training set with the mirrored and rotated versions
 *    of each board. We want a symmetrical heuristic function.
 */
public final class PositionalScorer implements Scorer<OthelloGame> {

    /**
     * {@inheritDoc}
     */
    public final int evaluate(OthelloGame game) {
        int score = 0;

        long south = game.state(SOUTH_STONE);
        long north = game.state(NORTH_STONE);

        while (empty(south) == false) {
            final int checker = first(south);
            score += WEIGHTS[checker];
            south ^= bit(checker);
        }

        while (empty(north) == false) {
            final int checker = first(north);
            score -= WEIGHTS[checker];
            north ^= bit(checker);
        }

        return score;
    }


    /** Weight of each owned board checker */
    private static final int[] WEIGHTS = {
        119,  -11,    6,    6,    6,    6,  -11,  119,
        -11,  -38,  -10,   -6,   -6,  -10,  -38,  -11,
          6,  -10,    7,   -3,   -3,    7,  -10,    6,
          6,   -6,   -3,    2,    2,   -3,   -6,    6,
          6,   -6,   -3,    2,    2,   -3,   -6,    6,
          6,  -10,    7,   -3,   -3,    7,  -10,    6,
        -11,  -38,  -10,   -6,   -6,  -10,  -38,  -11,
        119,  -11,    6,    6,    6,    6,  -11,  119
    };
}
