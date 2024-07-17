package com.joansala.game.othello.scorers;

/*
 * Samurai framework.
 * Copyright (C) 2024 Joan Sala Soler <contact@joansala.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import com.joansala.engine.Scorer;
import com.joansala.game.othello.OthelloGame;
import static com.joansala.game.othello.Othello.*;
import static com.joansala.util.bits.Bits.*;


/**
 * Evaluate the current state using only a material balance heuristic.
 */
public final class MaterialScorer implements Scorer<OthelloGame> {

    /** Weight of the captured stones difference */
    public static final int TALLY_WEIGHT = 15;


    /**
     * {@inheritDoc}
     */
    public final int evaluate(OthelloGame game) {
        final int south = count(game.state(SOUTH_STONE));
        final int north = count(game.state(NORTH_STONE));

        return TALLY_WEIGHT * (south - north);
    }
}
