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
				List<Player> detectives) {

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;

			if(remaining.contains(mrX.piece())) {
				Set<Move> singleMoves = ImmutableSet.copyOf(makeSingleMoves(this.setup, this.detectives, this.mrX, mrX.location()));
				if(mrX.has(ScotlandYard.Ticket.DOUBLE) && setup.moves.size() > 1) {
					Set<Move> doubleMoves = ImmutableSet.copyOf(makeDoubleMoves(this.setup, this.detectives, this.mrX, mrX.location()));
					this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).addAll(doubleMoves).build();
				} else {
					this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).build();
				}
			} else {
				Set<Move> singleMoves = new HashSet<>();
				for(Player detective : detectives) {
					if(remaining.contains(detective.piece())) {
						singleMoves.addAll(makeSingleMoves(this.setup, this.detectives, detective, detective.location()));
					}
				}
				this.moves = ImmutableSet.<Move>builder().addAll(singleMoves).build();
			}
			System.out.println(remaining.toString());
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

			for (Player detective : detectives) {
				playersMutable.add(detective.piece());
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
			for (Player detective : detectives) {
				if (detective.hasAtLeast(ScotlandYard.Ticket.UNDERGROUND, 4) ||
						detective.hasAtLeast(ScotlandYard.Ticket.BUS, 8) ||
						detective.hasAtLeast(ScotlandYard.Ticket.TAXI, 11)) {
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

		private List<LogEntry> LogMrXMove(List<LogEntry> logEntry, ScotlandYard.Ticket ticketUsed, int destination) {
			// adds a new log entry to the log based on if move hidden or not
			// is not what houses moves
			List<LogEntry> checking = new ArrayList<>(logEntry);
			System.out.println(this.setup.moves);
			System.out.println(getMrXTravelLog().size());
			System.out.println(this.setup.moves.get(getMrXTravelLog().size()));
			boolean revealMove = this.setup.moves.get(checking.size());
			LogEntry myNewLogEntry;
			if(revealMove) {
				myNewLogEntry = LogEntry.reveal(ticketUsed, destination);
				//logEntry.add(LogEntry.reveal(ticketUsed, destination));
			}
			else {
				myNewLogEntry = LogEntry.hidden(ticketUsed);
			}
			checking.add(myNewLogEntry);
			for (LogEntry entry : checking) {
				System.out.println(entry);
			}
			return checking;
		}

		@Nonnull
		@Override
		public GameState advance(Move move) {

			// stores a new log entry with the added move
			//ImmutableList<LogEntry> logEntryFinal = ImmutableList.of();
			//List<LogEntry> logEntry = List.copyOf(this.log);

			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);

			// gets the final destination , for single moves this is just x.destination
			// for double it is x.destination2
			Move.Visitor<Integer> getDestinationFinal = new Move.FunctionalVisitor<>((x -> x.destination), (x -> x.destination2));
			int destinationFinal = move.accept(getDestinationFinal);

			// gets the final tickets
			Move.Visitor<ScotlandYard.Ticket> getTicketFinal = new Move.FunctionalVisitor<>((x -> x.ticket), (x ->x.ticket2));
			ScotlandYard.Ticket ticketUsedFinal = move.accept(getTicketFinal);

			// gets if a move made is a double move or not
			Move.Visitor<Boolean> ifIsDouble = new Move.FunctionalVisitor<>((x -> false), (x -> true));
			boolean isDouble = move.accept(ifIsDouble);
			if(move.commencedBy().isMrX()) {
				// stores a new log entry with the added move
				ImmutableList<LogEntry> logEntryFinal = ImmutableList.of();
				List<LogEntry> logEntry = List.copyOf(this.log);
				Player newMrXChangedLoc;
				if(!isDouble) {
					// enter the move into the log
					logEntryFinal = ImmutableList.copyOf(LogMrXMove(logEntry, ticketUsedFinal, destinationFinal));
					System.out.println("after log entry final done and not double");
					System.out.println(logEntryFinal);
					// takes used ticket away from Mr X by returning a new Mr X without this ticket
					// Q - what happens to old Mr X?
					Player newMrXUsedTicket  = mrX.use(ticketUsedFinal);
					// moves Mr X to their new destination by returning a new Mr X at this destination
					// Q - what happens to old Mr Xs?
					newMrXChangedLoc =  newMrXUsedTicket.at(destinationFinal);
				}
				else {
					// gets the intermediate destination
					Move.Visitor<Integer> getDestinationIntermediate = new Move.FunctionalVisitor<>((x -> x.destination), (x -> x.destination1));
					int destinationIntermediate = move.accept(getDestinationIntermediate);

					// gets the intermediate tickets
					Move.Visitor<ScotlandYard.Ticket> getTicketIntermediate = new Move.FunctionalVisitor<>((x -> x.ticket), (x ->x.ticket1));
					ScotlandYard.Ticket ticketUsedIntermediate = move.accept(getTicketIntermediate);

					// double move of Mr X
					List<LogEntry> logEntryFirstMove;

					// add their first move into the log
					logEntryFirstMove = LogMrXMove(logEntry, ticketUsedIntermediate, destinationIntermediate);

					// add their second move into the log
					logEntryFinal = ImmutableList.copyOf(LogMrXMove(logEntryFirstMove,ticketUsedFinal, destinationFinal));

					// create new Mr X objects as previously
					// Q - what happens to the old Mr X objects?
					Player newMrXUsedTicket1  = mrX.use(ticketUsedIntermediate);
					Player newMrXUsedTicket2  = newMrXUsedTicket1.use(ticketUsedFinal);
					Player newMrXUsedDouble = newMrXUsedTicket2.use(ScotlandYard.Ticket.DOUBLE);
					newMrXChangedLoc =  newMrXUsedDouble.at(destinationFinal);
				}
				System.out.println(newMrXChangedLoc.tickets());
				// change to detectives turn means remove Mr X from remaining
				Set<Piece> remainingUpdated = remaining.stream().filter(d -> !d.isMrX()).collect(Collectors.toSet());
				//remainingUpdated.add(detectives.get(0).piece());
				for(Player detective : detectives) {
					remainingUpdated.add(detective.piece());
				}
				// little confused on this part
				System.out.println("after log entry final done and not double and before returning");
				System.out.println(logEntryFinal);
				return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdated), logEntryFinal, newMrXChangedLoc, detectives);
				// returns a new game state and swaps to the detective turn
			}

			else {
				// get the given detective piece
				Optional<Player> currentDetectiveTurn = detectives.stream().filter(d -> d.piece().equals(move.commencedBy())).findFirst();
				// checking ifPresent lost on
				Player currentDetective = currentDetectiveTurn.get();
				// returns detective at the new location
				Player currentDetectiveNewLoc = currentDetective.at(destinationFinal);
				// returns detective with one less ticket
				Player currentDetectiveTickLost = currentDetectiveNewLoc.use(ticketUsedFinal);
				// returns a Mr X with the ticket
				Player newMrX = mrX.give(ticketUsedFinal);
				// ensuring particular detective does not move again in this round
				Set<Piece> remainingUpdated;
				// update rU to not have cD
				remainingUpdated = remaining.stream().filter(d -> !d.equals(currentDetectiveTickLost.piece())).collect(Collectors.toSet());
				// checks if no moves left for detective, then it is mrX turn
				if(remainingUpdated.isEmpty()) {
					remainingUpdated.add(mrX.piece());
					for(Player detective : detectives) {
						remainingUpdated.add(detective.piece());
					}
				}
				// update detectives and replace cD with cDTL
				List<Player> detectivesUpdated = detectives.stream().filter(d -> !d.equals(currentDetective)).collect(Collectors.toList());
				detectivesUpdated.add(currentDetectiveTickLost);
				return new MyGameState(setup, ImmutableSet.copyOf(remainingUpdated), this.log, newMrX, detectivesUpdated);
			}
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

		Set<Piece> remaining = new HashSet<>();
		remaining.add(Piece.MrX.MRX);
		for(int i = 0; i < detectives.size(); i++) {
			remaining.add(detectives.get(i).piece());
		}

		return new MyGameState(setup, ImmutableSet.copyOf(remaining), ImmutableList.of(), mrX, detectives);
	}

}
