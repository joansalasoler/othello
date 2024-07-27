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
 * Othello game by evaluating the board position. This heuristic
 * prioritizes corner occupation.
 *
 * To fine-tune the piece-square table weights, the same method used for
 * the Draughts engine was used with a few modifications:
 *
 * a) Use {@link Montecarlo} to evaluate the training set.
 * b) Augment the training set with the mirrored and rotated versions
 *    of each board. We want a symmetrical heuristic function.
 * c) Create a baseline weights table using the whole training set.
 * d) Create a specific weights table for each different corner
 *    occupation combination. Considering only the positions from the
 *    training set that have from zero to four corners occupied.
 * d) Prevent overfitting by averaging the weights of the specific
 *    corners tables with the baseline table.
 */
public final class CornersScorer implements Scorer<OthelloGame> {

    /** Mask with a bit set for each board corner */
    private long CORNERS_MASK = 0x8100000000000081L;


    /**
     * {@inheritDoc}
     */
    public final int evaluate(OthelloGame game) {
        int score = 0;

        long south = game.state(SOUTH_STONE);
        long north = game.state(NORTH_STONE);

        final int[] SOUTH_WEIGHTS = weights(south);
        final int[] NORTH_WEIGHTS = weights(north);

        while (empty(south) == false) {
            final int checker = first(south);
            score += SOUTH_WEIGHTS[checker];
            south ^= bit(checker);
        }

        while (empty(north) == false) {
            final int checker = first(north);
            score -= NORTH_WEIGHTS[checker];
            north ^= bit(checker);
        }

        return score;
    }


    /**
     * Obtain the weights table for the occupied corners.
     */
    private int[] weights(long bitboard) {
        final long c = bitboard & CORNERS_MASK;
        final long b = (c >> 60) | (c >> 54) | (c >> 6) | c;
        return WEIGHTS[(int) (0xFL & b)];
    }


    /** Weights of each checker grouped by occupied corners */
    private static final int[][] WEIGHTS = {{
        119,  -18,    4,    3,    3,    4,  -18,  119 ,
        -18,  -38,   -4,   -4,   -4,   -4,  -38,  -18 ,
          4,   -4,    2,   -4,   -4,    2,   -4,    4 ,
          3,   -4,   -4,   -2,   -2,   -4,   -4,    3 ,
          3,   -4,   -4,   -2,   -2,   -4,   -4,    3 ,
          4,   -4,    2,   -4,   -4,    2,   -4,    4 ,
        -18,  -38,   -4,   -4,   -4,   -4,  -38,  -18 ,
        119,  -18,    4,    3,    3,    4,  -18,  119
    }, {
        119,   27,   -2,    9,    4,    6,  -20,  119 ,
         27,   -7,   -6,   -4,   -5,   -7,  -34,  -18 ,
         -2,   -6,   -2,   -2,   -2,    0,   -6,    3 ,
          9,   -4,   -2,    0,    0,   -4,   -4,    2 ,
          4,   -5,   -2,    0,   -2,   -2,   -4,    2 ,
          6,   -7,    0,   -4,   -2,    1,   -6,    3 ,
        -20,  -34,   -6,   -4,   -4,   -6,  -36,  -18 ,
        119,  -18,    3,    2,    2,    3,  -18,  119 ,
    }, {
        119,  -20,    6,    4,    9,   -2,   27,  119 ,
        -18,  -34,   -7,   -5,   -4,   -6,   -7,   27 ,
          3,   -6,    0,   -2,   -2,   -2,   -6,   -2 ,
          2,   -4,   -4,    0,    0,   -2,   -4,    9 ,
          2,   -4,   -2,   -2,    0,   -2,   -5,    4 ,
          3,   -6,    1,   -2,   -4,    0,   -7,    6 ,
        -18,  -36,   -6,   -4,   -4,   -6,  -34,  -20 ,
        119,  -18,    3,    2,    2,    3,  -18,  119 ,
    }, {
        119,   16,   -3,    6,    6,   -3,   16,  119 ,
         25,   -8,   -4,   -3,   -3,   -4,   -8,   25 ,
         -2,   -4,   -4,   -2,   -2,   -4,   -4,   -2 ,
          7,   -3,   -2,   -1,   -1,   -2,   -3,    7 ,
          4,   -5,   -4,    0,    0,   -4,   -5,    4 ,
          4,   -8,    0,   -4,   -4,    0,   -8,    4 ,
        -18,  -34,   -6,   -4,   -4,   -6,  -34,  -18 ,
        119,  -16,    2,    3,    3,    2,  -16,  119 ,
    }, {
        119,  -18,    3,    2,    2,    3,  -18,  119 ,
        -20,  -34,   -6,   -4,   -4,   -6,  -36,  -18 ,
          6,   -7,    0,   -4,   -2,    1,   -6,    3 ,
          4,   -5,   -2,    0,   -2,   -2,   -4,    2 ,
          9,   -4,   -2,    0,    0,   -4,   -4,    2 ,
         -2,   -6,   -2,   -2,   -2,    0,   -6,    3 ,
         27,   -7,   -6,   -4,   -5,   -7,  -34,  -18 ,
        119,   27,   -2,    9,    4,    6,  -20,  119 ,
    }, {
        119,   25,   -2,    7,    4,    4,  -18,  119 ,
         16,   -8,   -4,   -3,   -5,   -8,  -34,  -16 ,
         -3,   -4,   -4,   -2,   -4,    0,   -6,    2 ,
          6,   -3,   -2,   -1,    0,   -4,   -4,    3 ,
          6,   -3,   -2,   -1,    0,   -4,   -4,    3 ,
         -3,   -4,   -4,   -2,   -4,    0,   -6,    2 ,
         16,   -8,   -4,   -3,   -5,   -8,  -34,  -16 ,
        119,   25,   -2,    7,    4,    4,  -18,  119 ,
    }, {
        119,  -19,    2,    4,    8,   -3,   22,  119 ,
        -19,  -28,   -6,   -4,   -4,   -4,   -8,   22 ,
          2,   -6,    0,   -3,   -4,   -3,   -4,   -3 ,
          4,   -4,   -3,    1,    0,   -4,   -4,    8 ,
          8,   -4,   -4,    0,    1,   -3,   -4,    4 ,
         -3,   -4,   -3,   -4,   -3,    0,   -6,    2 ,
         22,   -8,   -4,   -4,   -4,   -6,  -28,  -19 ,
        119,   22,   -3,    8,    4,    2,  -19,  119 ,
    }, {
        119,    2,   -4,    0,   -2,   -3,    2,  119 ,
          2,   -6,    0,    0,   -3,   -1,   -5,    4 ,
         -4,    0,   -4,   -2,    0,   -6,   -2,   -1 ,
          0,    0,   -2,    1,   -2,   -3,   -5,    2 ,
         -2,   -3,    0,   -2,   -2,   -1,   -4,    2 ,
         -3,   -1,   -6,   -3,   -1,    0,   -8,    2 ,
          2,   -5,   -2,   -5,   -4,   -8,  -21,  -21 ,
        119,    4,   -1,    2,    2,    2,  -21,  119 ,
    }, {
        119,  -18,    3,    2,    2,    3,  -18,  119 ,
        -18,  -36,   -6,   -4,   -4,   -6,  -34,  -20 ,
          3,   -6,    1,   -2,   -4,    0,   -7,    6 ,
          2,   -4,   -2,   -2,    0,   -2,   -5,    4 ,
          2,   -4,   -4,    0,    0,   -2,   -4,    9 ,
          3,   -6,    0,   -2,   -2,   -2,   -6,   -2 ,
        -18,  -34,   -7,   -5,   -4,   -6,   -7,   27 ,
        119,  -20,    6,    4,    9,   -2,   27,  119 ,
    }, {
        119,   22,   -3,    8,    4,    2,  -19,  119 ,
         22,   -8,   -4,   -4,   -4,   -6,  -28,  -19 ,
         -3,   -4,   -3,   -4,   -3,    0,   -6,    2 ,
          8,   -4,   -4,    0,    1,   -3,   -4,    4 ,
          4,   -4,   -3,    1,    0,   -4,   -4,    8 ,
          2,   -6,    0,   -3,   -4,   -3,   -4,   -3 ,
        -19,  -28,   -6,   -4,   -4,   -4,   -8,   22 ,
        119,  -19,    2,    4,    8,   -3,   22,  119 ,
    }, {
        119,  -18,    4,    4,    7,   -2,   25,  119 ,
        -16,  -34,   -8,   -5,   -3,   -4,   -8,   16 ,
          2,   -6,    0,   -4,   -2,   -4,   -4,   -3 ,
          3,   -4,   -4,    0,   -1,   -2,   -3,    6 ,
          3,   -4,   -4,    0,   -1,   -2,   -3,    6 ,
          2,   -6,    0,   -4,   -2,   -4,   -4,   -3 ,
        -16,  -34,   -8,   -5,   -3,   -4,   -8,   16 ,
        119,  -18,    4,    4,    7,   -2,   25,  119 ,
    }, {
        119,    2,   -3,   -2,    0,   -4,    2,  119 ,
          4,   -5,   -1,   -3,    0,    0,   -6,    2 ,
         -1,   -2,   -6,    0,   -2,   -4,    0,   -4 ,
          2,   -5,   -3,   -2,    1,   -2,    0,    0 ,
          2,   -4,   -1,   -2,   -2,    0,   -3,   -2 ,
          2,   -8,    0,   -1,   -3,   -6,   -1,   -3 ,
        -21,  -21,   -8,   -4,   -5,   -2,   -5,    2 ,
        119,  -21,    2,    2,    2,   -1,    4,  119 ,
    }, {
        119,  -16,    2,    3,    3,    2,  -16,  119 ,
        -18,  -34,   -6,   -4,   -4,   -6,  -34,  -18 ,
          4,   -8,    0,   -4,   -4,    0,   -8,    4 ,
          4,   -5,   -4,    0,    0,   -4,   -5,    4 ,
          7,   -3,   -2,   -1,   -1,   -2,   -3,    7 ,
         -2,   -4,   -4,   -2,   -2,   -4,   -4,   -2 ,
         25,   -8,   -4,   -3,   -3,   -4,   -8,   25 ,
        119,   16,   -3,    6,    6,   -3,   16,  119 ,
    }, {
        119,    4,   -1,    2,    2,    2,  -21,  119 ,
          2,   -5,   -2,   -5,   -4,   -8,  -21,  -21 ,
         -3,   -1,   -6,   -3,   -1,    0,   -8,    2 ,
         -2,   -3,    0,   -2,   -2,   -1,   -4,    2 ,
          0,    0,   -2,    1,   -2,   -3,   -5,    2 ,
         -4,    0,   -4,   -2,    0,   -6,   -2,   -1 ,
          2,   -6,    0,    0,   -3,   -1,   -5,    4 ,
        119,    2,   -4,    0,   -2,   -3,    2,  119 ,
    }, {
        119,  -21,    2,    2,    2,   -1,    4,  119 ,
        -21,  -21,   -8,   -4,   -5,   -2,   -5,    2 ,
          2,   -8,    0,   -1,   -3,   -6,   -1,   -3 ,
          2,   -4,   -1,   -2,   -2,    0,   -3,   -2 ,
          2,   -5,   -3,   -2,    1,   -2,    0,    0 ,
         -1,   -2,   -6,    0,   -2,   -4,    0,   -4 ,
          4,   -5,   -1,   -3,    0,    0,   -6,    2 ,
        119,    2,   -3,   -2,    0,   -4,    2,  119 ,
    }, {
        119,    6,    6,    0,    0,    6,    6,  119 ,
          6,   -6,   -4,    1,    1,   -4,   -6,    6 ,
          6,   -4,    3,   -2,   -2,    3,   -4,    6 ,
          0,    1,   -2,    2,    2,   -2,    1,    0 ,
          0,    1,   -2,    2,    2,   -2,    1,    0 ,
          6,   -4,    3,   -2,   -2,    3,   -4,    6 ,
          6,   -6,   -4,    1,    1,   -4,   -6,    6 ,
        119,    6,    6,    0,    0,    6,    6,  119 ,
    }};
}
