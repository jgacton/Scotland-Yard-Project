package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
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
		public class NoSuchRegisteredObserver extends Exception {
			public NoSuchRegisteredObserver(String errorMessage) {
				super(errorMessage);
			}
		}
		private MyGameStateFactory stateFactory;
		private Board.GameState state;
		private Set<Observer> observers = new HashSet<>();

		MyModel(GameSetup setup, Player mrX, ImmutableList<Player> detectives) {
			this.stateFactory = new MyGameStateFactory();
			this.state = stateFactory.build(setup, mrX, detectives);
		}

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return this.state;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if(observer.equals(null)) throw new java.lang.NullPointerException();
			if(this.observers.contains(observer)) throw new IllegalArgumentException();
			this.observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if(observer.equals(null)) throw new java.lang.NullPointerException();
			if(!this.observers.contains(observer)) throw new IllegalArgumentException();
			this.observers = this.observers.stream().filter(x -> !x.equals(observer)).collect(Collectors.toSet());
		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(this.observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			if(state.getWinner().isEmpty()) {
				state.advance(move);
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

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new MyModel(setup, mrX, detectives);
	}
}
