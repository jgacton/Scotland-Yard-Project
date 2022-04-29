package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {
	private static final class MyModel implements Model {
		private Board.GameState state;
		private Set<Observer> observers = new HashSet<>();

		// Constructor for private inner class MyModel.
		private MyModel(
				GameSetup setup,
				Player mrX,
				ImmutableList<Player> detectives) {

			// Creates a new GameStateFactory and uses it to build a game state with the given parameters.
			MyGameStateFactory stateFactory = new MyGameStateFactory();
			this.state = stateFactory.build(setup, mrX, detectives);
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.state;
		}

		// Checks if the given observer is already registered or is null (by checking hashcode == 0).
		// If not, adds it to list of registered observers.
		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if(observer.hashCode() == 0) throw new java.lang.NullPointerException();
			if(this.observers.contains(observer)) throw new IllegalArgumentException();
			this.observers.add(observer);
		}

		// Checks if the given observer is actually registered or is null.
		// If not, removes it from the list of observers.
		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if(observer.hashCode() == 0) throw new java.lang.NullPointerException();
			if(!this.observers.contains(observer)) throw new IllegalArgumentException();
			this.observers = this.observers
					.stream()
					.filter(x -> !x.equals(observer))
					.collect(Collectors.toSet());
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(this.observers);
		}

		// Checks if the current state has a winner, if so notifies all registered observers that the game is over.
		// If not, advances the current state with the given move and checks if the next state has a winner.
		// If so, notifies observers that the game is over, if not, notifies observers that a move has been made.
		@Override
		public void chooseMove(@Nonnull Move move) {
			if(state.getWinner().isEmpty()) {
				this.state = state.advance(move);
				if(!state.getWinner().isEmpty()) {
					for(Observer O : observers) {
						O.onModelChanged(state, Observer.Event.GAME_OVER);
					}
				} else {
					for(Observer O : observers) {
						O.onModelChanged(state, Observer.Event.MOVE_MADE);
					}
				}
			} else {
				for(Observer O : observers) {
					O.onModelChanged(state, Observer.Event.GAME_OVER);
				}
			}
		}

	}

	// Returns a new MyModel object from the given parameters.
	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {

		return new MyModel(setup, mrX, detectives);
	}
}
