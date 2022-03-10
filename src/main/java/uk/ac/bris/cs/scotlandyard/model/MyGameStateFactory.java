package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {
		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private final Player mrX;
		private final List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives) {

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

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
			return this.log;
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

		private static Set<Move.SingleMove> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			Set<Move.SingleMove> singleMoves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {
				// TODO find out if destination is occupied by a detective
				//  if the location is occupied, don't add to the collection of moves to return
				boolean occupied = false;
				for(int i = 0; i < detectives.size(); i++) {
					if(detectives.get(i).location() == destination) occupied = true;
				}


				for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					// TODO find out if the player has the required tickets
					//  if it does, construct a SingleMove and add it the collection of moves to return
					if(!player.has(t.requiredTicket())) {
						Move.SingleMove move = new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination);
						singleMoves.add(move);
					}
				}

				// TODO consider the rules of secret moves here
				//  add moves to the destination via a secret ticket if there are any left with the player
			}

			return singleMoves;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			this.moves = ImmutableSet.copyOf(makeSingleMoves(this.setup, this.detectives, this.mrX, mrX.location()));
			return this.moves;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {
			if(move.equals(null)) throw new IllegalArgumentException();
			return null;
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

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

		return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

}
