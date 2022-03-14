package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;
import java.util.stream.Collectors;

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
			Set<Move> singleMoves = ImmutableSet.copyOf(makeSingleMoves(this.setup, this.detectives, this.mrX, mrX.location()));
			if(mrX.has(ScotlandYard.Ticket.DOUBLE) && setup.moves.size() > 1) {
				Set<Move> doubleMoves = ImmutableSet.copyOf(makeDoubleMoves(this.setup, this.detectives, this.mrX, mrX.location()));
				this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).addAll(doubleMoves).build();
			} else {
				this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).build();
			}
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

		private static Set<Move> makeSingleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			Set<Move> singleMoves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)) {

				// Generates single moves for normal tickets (Taxi, Bus, Train)
				for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					if(player.has(t.requiredTicket()) && !checkNodeOccupied(detectives, destination)) {
						Move.SingleMove move = new Move.SingleMove(player.piece(), source, t.requiredTicket(), destination);
						singleMoves.add(move);
					}
				}

				// Generates single moves for secret tickets
				for(ScotlandYard.Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()) ) {
					if(player.has(ScotlandYard.Ticket.SECRET) && !checkNodeOccupied(detectives, destination)) {
						Move.SingleMove move = new Move.SingleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination);
						singleMoves.add(move);
					}
				}
			}

			return singleMoves;
		}

		private static boolean checkNodeOccupied(List<Player> detectives, int node) {
			for(int i = 0; i < detectives.size(); i++) {
				if(detectives.get(i).location() == node) return true;
			}
			return false;
		}

		private static Set<Move> makeDoubleMoves(GameSetup setup, List<Player> detectives, Player player, int source){

			Set<Move> doubleMoves = new HashSet<>();

			for(int destination1 : setup.graph.adjacentNodes(source)) {

				if(!checkNodeOccupied(detectives, destination1)) {

					for(int destination2 : setup.graph.adjacentNodes(destination1)) {

						if(!checkNodeOccupied(detectives, destination2)) {

							for(ScotlandYard.Transport t1 : setup.graph.edgeValueOrDefault(source, destination1, ImmutableSet.of())) {
								if(player.has(t1.requiredTicket()) || player.has(ScotlandYard.Ticket.SECRET)) {

									for(ScotlandYard.Transport t2 : setup.graph.edgeValueOrDefault(destination1, destination2, ImmutableSet.of())) {
										if(player.has(t2.requiredTicket()) || player.has(ScotlandYard.Ticket.SECRET)) {

											if(t1.requiredTicket().equals(t2.requiredTicket()) && player.hasAtLeast(t1.requiredTicket(), 2)) {
												Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, t1.requiredTicket(), destination1, t2.requiredTicket(), destination2);
												doubleMoves.add(doubleMove);
											} else if (!t1.requiredTicket().equals(t2.requiredTicket())) {
												Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, t1.requiredTicket(), destination1, t2.requiredTicket(), destination2);
												doubleMoves.add(doubleMove);
											}

											if(player.hasAtLeast(ScotlandYard.Ticket.SECRET, 2)) {
												Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination1, ScotlandYard.Ticket.SECRET, destination2);
												doubleMoves.add(doubleMove);
											}

											if(player.has(t1.requiredTicket()) && player.has(ScotlandYard.Ticket.SECRET)) {
												Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, t1.requiredTicket(), destination1, ScotlandYard.Ticket.SECRET, destination2);
												doubleMoves.add(doubleMove);
											}

											if(player.has(ScotlandYard.Ticket.SECRET) && player.has(t2.requiredTicket())) {
												Move.DoubleMove doubleMove = new Move.DoubleMove(player.piece(), source, ScotlandYard.Ticket.SECRET, destination1, t2.requiredTicket(), destination2);
												doubleMoves.add(doubleMove);
											}
										}
									}
								}
							}
						}
					}
				}
			}

			return doubleMoves;
		}

		@Nonnull
		@Override
		public ImmutableSet<Move> getAvailableMoves() {
			return this.moves;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {

			// stores a new log entry with the added move
			ImmutableList<LogEntry> logEntryFinal = ImmutableList.of();
			List<LogEntry> logEntry = List.of();

			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			Move.Visitor<Integer> getDestination = new Move.FunctionalVisitor<>((x -> x.destination), (x -> x.destination2));
			int destination = move.accept(getDestination);

			// gets ticket used for the move currently only focusing on single moves
			Move.Visitor<ScotlandYard.Ticket> getTicket = new Move.FunctionalVisitor<>((x -> x.ticket), (x ->x.ticket1));
			ScotlandYard.Ticket ticketUsed = move.accept(getTicket);

			Move.Visitor<Boolean> ifIsDouble = new Move.FunctionalVisitor<>((x -> false), (x -> true));
			boolean isDouble = move.accept(ifIsDouble);

			if(move.commencedBy().isMrX()) {
				// adds all current moves to the log
				for(int i =0; i<getMrXTravelLog().size(); i++) {
					logEntry.add(this.log.get(i));
				}
				// want to check if it is Mr X's turn to surface
				// do this using travel log size
				// then want to create new log entry and add this to log
				if(!isDouble && ScotlandYard.REVEAL_MOVES.contains(getMrXTravelLog().size())) {
					logEntry.add(LogEntry.reveal(ticketUsed, destination));
				}
				else if(!isDouble) { logEntry.add(LogEntry.hidden(ticketUsed));}
				logEntryFinal = (ImmutableList<LogEntry>) logEntry;
				// takes used ticket away from Mr X
				Player newMrXUsedTicket  = mrX.use(ticketUsed);
				// moves Mr X to their new destination
				Player newMrXChangedLoc =  mrX.at(destination);
				// returns a new game state and swaps to the detective turn
				return new MyGameState(setup, ImmutableSet.of((Piece) detectives.stream().map(Player::piece).collect(Collectors.toSet())), logEntryFinal, newMrXChangedLoc, detectives);
			}

			/*if(isDouble) {

			}

			if(move.commencedBy().isMrX()) {
				if(setup.moves.get(0) == true) {
					LogEntry entry = LogEntry.reveal(move.tickets(), destination);
				}

				Player nextMrX = new Player(mrX.piece(), mrX., destination);
			}

			// Update the location of the moved piece to the destination of the move
			// Update the counts of relevant tickets (subtract however many required for move)

			 */
			return new MyGameState(setup, ImmutableSet.of(Piece.MrX.MRX), logEntryFinal, mrX, detectives);
			//GameState newState = build(this.setup, this.mrX, ImmutableList.copyOf(this.detectives));
			//return newState;

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
