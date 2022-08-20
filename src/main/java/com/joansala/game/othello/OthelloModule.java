package com.joansala.game.othello;

/*
 * Aalina engine.
 * Copyright (c) 2021 Joan Sala Soler <contact@joansala.com>
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


import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import com.google.inject.Provides;

import com.joansala.cli.*;
import com.joansala.engine.*;
import com.joansala.cache.GameCache;
import com.joansala.engine.base.BaseModule;
import com.joansala.engine.negamax.Negamax;
import com.joansala.book.base.BaseRoots;
import static com.joansala.game.othello.Othello.*;


/**
 * Binds together the components of the Othello engine.
 */
public class OthelloModule extends BaseModule {

    /** Shared openings book instance */
    private static Roots<Game> roots;


    /**
     * Command line interface.
     */
    @Command(
      name = "othello",
      version = "1.0.0",
      description = "Othello is a strategy board game"
    )
    private static class OthelloCommand extends MainCommand {

        @Option(names = "--roots", description = "Openings book path")
        private static String roots = OthelloRoots.ROOTS_PATH;

        @Option(names = "--disturbance", description = "disturbance")
        private static double disturbance = ROOT_DISTURBANCE;

        @Option(names = "--threshold", description = "threshold")
        private static double threshold = ROOT_THRESHOLD;
    }


    /**
     * Game module configuration.
     */
    @Override protected void configure() {
        bind(Game.class).to(OthelloGame.class);
        bind(Board.class).to(OthelloBoard.class);
        bind(Engine.class).to(Negamax.class);
        bind(Cache.class).to(GameCache.class);
    }


    /**
     * Openings book provider.
     */
    @Provides @SuppressWarnings("rawtypes")
    public static Roots provideRoots() {
        if (roots instanceof Roots == false) {
            String path = OthelloCommand.roots;

            try {
                OthelloRoots roots = new OthelloRoots(path);
                roots.setDisturbance(OthelloCommand.disturbance);
                roots.setThreshold(OthelloCommand.threshold);
                OthelloModule.roots = roots;
            } catch (Exception e) {
                logger.warning("Cannot open openings book: " + path);
                roots = new BaseRoots();
            }
        }

        return roots;
    }


    /**
     * Executes the command line interface.
     *
     * @param args      Command line parameters
     */
    public static void main(String[] args) throws Exception {
        BaseModule module = new OthelloModule();
        OthelloCommand main = new OthelloCommand();
        System.exit(main.execute(module, args));
    }
}
