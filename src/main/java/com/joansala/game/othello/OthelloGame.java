package com.joansala.game.othello;

/*
 * Aalina engine.
 * Copyright (C) 2021-2024 Joan Sala Soler <contact@joansala.com>
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

import com.joansala.engine.Board;
import com.joansala.engine.Scorer;
import com.joansala.engine.base.BaseGame;
import com.joansala.game.othello.Othello.Player;
import com.joansala.game.othello.scorers.CornersScorer;
import com.joansala.util.hash.ZobristHash;
import static com.joansala.util.bits.Bits.*;
import static com.joansala.game.othello.Othello.*;


/**
 * Represents an Othello game between two players.
 */
public class OthelloGame extends BaseGame {

    /** Recommended score to evaluate draws */
    public static final int CONTEMPT_SCORE = 0;

    /** Player forfeits its turn */
    public static final int FORFEIT_MOVE = BOARD_SIZE;

    /** Capacity of this game object */
    private static final int CAPACITY = 2 * BOARD_SIZE;

    /** Heuristic evaluation function */
    private static final Scorer<OthelloGame> scorer = scoreFunction();

    /** Hash code generator */
    private static final ZobristHash hasher = hashFunction();

    /** Start position and turn */
    private OthelloBoard board;

    /** Current player color */
    private Player player;

    /** Current opponent color */
    private Player rival;

    /** Move generation cursors */
    private int[] cursors;

    /** Hash code history */
    private long[] hashes;

    /** Board states history */
    private long[] states;

    /** Legal moves history */
    private long[] mobilities;

    /** Current position bitboards */
    private long[] state;

    /** Bitboard of legal moves */
    private long mobility;

    /** Set when no player can move */
    private boolean stagnant;

    /** Current move generation cursor */
    private int cursor;


    /**
     * Instantiate a new game on the start state.
     */
    public OthelloGame() {
        super(CAPACITY);
        cursors = new int[CAPACITY];
        hashes = new long[CAPACITY];
        mobilities = new long[CAPACITY];
        states = new long[CAPACITY << 1];
        setStartingBoard(new OthelloBoard());
    }


    /**
     * Initialize the heuristic evaluation function.
     */
    private static Scorer<OthelloGame> scoreFunction() {
        return new CornersScorer();
    }


    /**
     * Initialize the hash code generator.
     */
    private static ZobristHash hashFunction() {
        return new ZobristHash(RANDOM_SEED, PIECE_COUNT, BOARD_SIZE);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int turn() {
        return player.turn;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Board getStartingBoard() {
        return board;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartingBoard(Board board) {
        setStartingBoard((OthelloBoard) board);
    }


    /**
     * {@see #setStartingBoard(Board)}
     */
    public void setStartingBoard(OthelloBoard board) {
        this.index = -1;
        this.board = board;
        this.move = NULL_MOVE;
        this.stagnant = false;
        this.state = board.position();
        setTurn(board.turn());
        this.hash = computeHash();
        computeMobility();
        resetCursor();
    }


    /**
     * Sets the current player to move.
     *
     * @param turn      {@code SOUTH} or {@code NORTH}
     */
    protected void setTurn(int turn) {
        if (turn == SOUTH) {
            player = Player.SOUTH;
            rival = Player.NORTH;
        } else {
            player = Player.NORTH;
            rival = Player.SOUTH;
        }
    }


    /**
     * Toggles the player to move.
     */
    private void switchTurn() {
        Player player = this.player;
        this.player = rival;
        this.rival = player;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public OthelloBoard getCurrentBoard() {
        return new OthelloBoard(state, turn);
    }


    /**
     * Obtain the current bitboard value on the given index.
     *
     * @return      Bitboard value
     */
    public final long state(int index) {
        return state[index];
    }


    /**
     * Current game state reference.
     *
     * @return      A game state
     */
    protected long[] state() {
        return state;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int contempt() {
        return CONTEMPT_SCORE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isLegal(int move) {
        return contains(mobility, bit(move)) || move == FORFEIT_MOVE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int toCentiPawns(int score) {
        return (int) (score * 100.0 / STONE_SCORE);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasEnded() {
        return stagnant;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int outcome() {
        final int south = count(state[SOUTH_STONE]);
        final int north = count(state[NORTH_STONE]);
        if (south < north) return -MAX_SCORE;
        if (south > north) return MAX_SCORE;
        return DRAW_SCORE;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int score() {
        return scorer.evaluate(this);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int getCursor() {
        return cursor;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setCursor(int cursor) {
        this.cursor = cursor;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void resetCursor() {
        if (stagnant) {
            cursor = NULL_MOVE;
        } else if (empty(mobility)) {
            cursor = FORFEIT_MOVE;
        } else {
            cursor = BOARD_SIZE - 1;
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void makeMove(int move) {
        pushState();
        movePieces(move);
        switchTurn();
        computeMobility();
        this.move = move;
        resetCursor();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unmakeMove() {
        popState(index);
        switchTurn();
        index--;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void unmakeMoves(int length) {
        if (length > 0) {
            index -= length;
            setTurn((length & 1) == 0 ? turn() : -turn());
            popState(1 + index);
        }
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public int nextMove() {
        if (cursor == FORFEIT_MOVE) {
            cursor = NULL_MOVE;
            return FORFEIT_MOVE;
        }

        if (cursor >= 0 && mobility != 0L) {
            long mask = mobility & ((2L << cursor) - 1);

            if (empty(mask) == false) {
                final int move = last(mask);
                cursor = move - 1;
                return move;
            }
        }

        return NULL_MOVE;
    }


    /**
     * Places a stone of the current player on the given checker and
     * flips the resulting captured stones if any.
     *
     * @param move       Checker where to place the stone
     */
    private void movePieces(int move) {
        final long checker = bit(move);
        final long rivals = state[rival.color];
        final long players = state[player.color];

        // Toggle the hash sign

        hash ^= rival.sign;
        hash ^= player.sign;

        // Player may have forfeit the turn

        if (move == FORFEIT_MOVE) {
            return;
        }

        // Find all the stones that must be flipped

        long captures = 0x00L;

        for (int direction = 0; direction < 8; direction++) {
            final long rays = rays(rivals, checker, direction);

            if ((players & shiftd(rays, direction)) != 0L) {
                captures |= rays;
            }
        }

        // Update the checkerboards

        state[player.color] ^= checker;
        state[player.color] ^= captures;
        state[rival.color] ^= captures;

        // Update the Zobrist hash

        hash = hasher.insert(hash, move, player.color);

        while (empty(captures) == false) {
            final int index = first(captures);
            hash = hasher.remove(hash, index, rival.color);
            hash = hasher.insert(hash, index, player.color);
            captures ^= bit(index);
        }
    }


    /**
     * Bitboard of legal moves for the current player.
     */
    private void computeMobility() {
        stagnant = false;

        if (empty(mobility = computeMobility(player, rival))) {
            stagnant = empty(computeMobility(rival, player));
        }
    }


    /**
     * Bitboard of legal moves for the given player.
     */
    private long computeMobility(Player player, Player rival) {
        final long rivals = state[rival.color];
        final long players = state[player.color];
        final long free = ~(players | rivals);

        long mobility = 0x00L;

        for (int direction = 0; direction < 8; direction++) {
            final long rays = rays(rivals, players, direction);
            mobility |= free & shiftd(rays, direction);
        }

        return mobility;
    }


    /**
     * Store game state on the history.
     */
    private void pushState() {
        index++;
        moves[index] = move;
        hashes[index] = hash;
        mobilities[index] = mobility;
        cursors[index] = cursor;
        System.arraycopy(state, 0, states, index << 1, PIECE_COUNT);
    }


    /**
     * Retrieve the current game state from the history.
     */
    private void popState(int index) {
        System.arraycopy(states, index << 1, state, 0, PIECE_COUNT);
        move = moves[index];
        hash = hashes[index];
        cursor = cursors[index];
        mobility = mobilities[index];
        stagnant = false;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    protected long computeHash() {
        return computeHash(player, state);
    }


    /**
     * Computes hash code for the given position and turn.
     *
     * @param state     Position array
     * @param player    Player to move
     * @return          Hash code for the position
     */
    protected static long computeHash(Player player, long[] state) {
        long hash = player.sign;

        for (int piece = 0; piece < PIECE_COUNT; piece++) {
            long pieces = state[piece];

            while (empty(pieces) == false) {
                final int index = first(pieces);
                hash = hasher.insert(hash, index, piece);
                pieces ^= bit(index);
            }
        }

        return hash;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void ensureCapacity(int size) {
        // Capacity is fixed for this game
    }
}
