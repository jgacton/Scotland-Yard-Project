package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

<<<<<<< HEAD
import java.util.Optional;
import java.util.ArrayList;
=======
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.TreeSet;
>>>>>>> e2950e0 (12 tests passing)

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		// TODO
		if(setup.moves.isEmpty()) throw new IllegalArgumentException();
		if(detectives.equals(null)) throw new NullPointerException();
<<<<<<< HEAD
=======
		if(mrX.equals(null)) throw new NullPointerException();
		for(int i=0; i<detectives.size(); i++) {
			for(int j =0; j<detectives.size(); j++) {
				if(detectives.get(i).piece().equals(detectives.get(j).piece())) {
					throw new IllegalArgumentException();
				}
				if(detectives.get(i).location()==(detectives.get(j).location())) {
					throw new IllegalArgumentException();
				}
			}
		}
>>>>>>> e2950e0 (12 tests passing)
		for(int i = 0; i < detectives.size(); i++) {
			if(detectives.get(i).has(ScotlandYard.Ticket.DOUBLE)) {
				throw new IllegalArgumentException();
			}
		}
<<<<<<< HEAD
		if()
=======
>>>>>>> e2950e0 (12 tests passing)
		return new GameState() {
			@Nonnull
			@Override
			public GameState advance(Move move) {
				if(move.equals(null)) throw new IllegalArgumentException();
				return null;
<<<<<<< HEAD
=======

>>>>>>> e2950e0 (12 tests passing)
			}

			@Nonnull
			@Override
			public GameSetup getSetup() {
				return setup;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getPlayers() {
<<<<<<< HEAD
=======

>>>>>>> e2950e0 (12 tests passing)
				return null;
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				return Optional.empty();
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return null;
			}

			@Nonnull
			@Override
<<<<<<< HEAD
			public ImmutableSet<Piece> getWinner() {
				return new ArrayList<Piece>();
=======
			public ImmutableSet<Piece> getWinner(){
				return null;
>>>>>>> e2950e0 (12 tests passing)
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {
<<<<<<< HEAD
=======
				ArrayList<Integer> locations = new ArrayList<Integer>();
				for(int i =0; i < detectives.size(); i++) {
					System.out.println(detectives.get(i).location());
					locations.add(detectives.get(i).location());
				}
				for(int i =0; i < locations.size(); i++) {

				}
>>>>>>> e2950e0 (12 tests passing)
				return null;
			}
		};

	}

}
