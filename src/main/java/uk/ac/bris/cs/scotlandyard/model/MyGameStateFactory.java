package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

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
      if(setup.moves.isEmpty()) throw new IllegalArgumentException();
      if(detectives.equals(null)) throw new NullPointerException();
      if(mrX.equals(null)) throw new NullPointerException();

      for(int i=0; i<detectives.size()-1; i++) {
         for(int j =i+1; j<detectives.size(); j++) {
            if(detectives.get(i).getWebColour(detectives.get(i)).equals(detectives.get(j).getWebColour(detectives.get(j)))) {
               throw new IllegalArgumentException();
            }
            if(detectives.get(i).location()==(detectives.get(j).location())) {
               throw new IllegalArgumentException();
            }
         }
      }
      for(int i = 0; i < detectives.size(); i++) {
         if(detectives.get(i).has(ScotlandYard.Ticket.DOUBLE)) {
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
            return null;
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
         public ImmutableSet<Piece> getWinner() {
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
