package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
		if(setup.graph.nodes().size() == 0) throw new IllegalArgumentException();
		if(setup.moves.isEmpty()) throw new IllegalArgumentException();
		if(detectives.equals(null)) throw new NullPointerException();
		if(mrX.equals(null)) throw new NullPointerException();

		for(int i=0; i<detectives.size()-1; i++) {
			for(int j =i+1; j<detectives.size(); j++) {
				if(detectives.get(i).piece().equals(detectives.get(j).piece())) {
					throw new IllegalArgumentException();
				}
				if(detectives.get(i).location()==(detectives.get(j).location())) {
					throw new IllegalArgumentException();
				}
			}
		}
		for(int i = 0; i < detectives.size(); i++) {
			if(detectives.get(i).has(ScotlandYard.Ticket.DOUBLE) || (detectives.get(i).has(ScotlandYard.Ticket.SECRET))) {
				throw new IllegalArgumentException();
			}
		}
		return new GameState() {
			@Nonnull
			@Override
			public GameState advance(Move move) {
				if(move.equals(null)) throw new IllegalArgumentException();
				return null;
			}

			@Nonnull
			@Override
			public GameSetup getSetup() {
				return setup;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getPlayers() {
				Set<Piece> playersMutable = new HashSet<>();
				playersMutable.add(mrX.piece());
				for(int i =0; i<detectives.size(); i++) {
					playersMutable.add(detectives.get(i).piece());
				}
				return ImmutableSet.copyOf(playersMutable);
			}

			@Nonnull
			@Override
			public Optional<Integer> getDetectiveLocation(Piece.Detective detective) {
				for(int i =0; i<detectives.size(); i++) {
					if(detectives.get(i).piece().equals(detective)) {
						return Optional.of(detectives.get(i).location());
					}
				}
				return Optional.ofNullable(null);
			}

			@Nonnull
			@Override
			public Optional<TicketBoard> getPlayerTickets(Piece piece) {
				ImmutableMap<ScotlandYard.Ticket, Integer> tickets = null;
				if(mrX.piece().equals(piece)) {
					tickets = mrX.tickets();
				}
				for(int i =0; i<detectives.size(); i++) {
					if(detectives.get(i).piece().equals(piece)) {
						tickets = detectives.get(i).tickets();
					}
				}
				if(tickets == null) {return Optional.ofNullable(null);}
				ImmutableMap<ScotlandYard.Ticket, Integer> finalTickets = tickets;
				return Optional.of(new TicketBoard() {
					@Override
					public int getCount(@Nonnull ScotlandYard.Ticket ticket) {
						return finalTickets.get(ticket);
					}
				});
			}

			@Nonnull
			@Override
			public ImmutableList<LogEntry> getMrXTravelLog() {
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Piece> getWinner() {
				Set<Piece> winners = new HashSet<>();
				for(int i =0; i< detectives.size(); i++) {
					if (detectives.get(i).hasAtLeast(ScotlandYard.Ticket.UNDERGROUND, 4) ||
							detectives.get(i).hasAtLeast(ScotlandYard.Ticket.BUS, 8) ||
							detectives.get(i).hasAtLeast(ScotlandYard.Ticket.TAXI, 11)) {
						return ImmutableSet.copyOf(winners);
					}
				}
				return null;
			}

			@Nonnull
			@Override
			public ImmutableSet<Move> getAvailableMoves() {
				return null;
			}
		};
	}

}
