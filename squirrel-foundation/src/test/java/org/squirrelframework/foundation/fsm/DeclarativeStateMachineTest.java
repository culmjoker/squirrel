package org.squirrelframework.foundation.fsm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrelframework.foundation.fsm.Condition;
import org.squirrelframework.foundation.fsm.ImmutableState;
import org.squirrelframework.foundation.fsm.StateMachine;
import org.squirrelframework.foundation.fsm.StateMachineStatus;
import org.squirrelframework.foundation.fsm.TransitionType;
import org.squirrelframework.foundation.fsm.annotation.LogExecTime;
import org.squirrelframework.foundation.fsm.annotation.State;
import org.squirrelframework.foundation.fsm.annotation.States;
import org.squirrelframework.foundation.fsm.annotation.Transit;
import org.squirrelframework.foundation.fsm.annotation.Transitions;
import org.squirrelframework.foundation.fsm.builder.StateMachineBuilder;
import org.squirrelframework.foundation.fsm.impl.AbstractStateMachine;
import org.squirrelframework.foundation.fsm.impl.StateMachineBuilderImpl;

public class DeclarativeStateMachineTest extends AbstractStateMachineTest {

    @States({
        @State(name="A", entryCallMethod="entryStateA", exitCallMethod="exitStateA"), 
        @State(name="B", entryCallMethod="entryStateB", exitCallMethod="exitStateB"),
        @State(name="C", alias="StateC"),
        @State(name="D", entryCallMethod="entryStateD", exitCallMethod="exitStateD")
    })
    @Transitions({
        @Transit(from="A", to="B", on="ToB", callMethod="fromStateAToStateBOnGotoB"),
        @Transit(from="A", to="A", on="InternalA", callMethod="fromStateAToStateAOnInternalA", type=TransitionType.INTERNAL),
        @Transit(from="B", to="#StateC", on="ToC"),
        @Transit(from="C", to="D", on="ToD", when=ExcellentCondition.class),
        @Transit(from="D", to="A", on="ToA", callMethod="transitionWithException"),
        @Transit(from="D", to="Final", on="ToEnd", callMethod="fromStateDToFinalOnToEnd")
    })
    interface DeclarativeStateMachine extends StateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> {
        // entry states
        void entryStateA(TestState from, TestState to, TestEvent event, Integer context);
        void entryStateB(TestState from, TestState to, TestEvent event, Integer context);
        void entryC(TestState from, TestState to, TestEvent event, Integer context);
        void entryStateD(TestState from, TestState to, TestEvent event, Integer context);

        // transitions
        void fromStateAToStateBOnGotoB(TestState from, TestState to, TestEvent event, Integer context);
        void fromStateAToStateAOnInternalA(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToDWhenExcellentCondition(TestState from, TestState to, TestEvent event, Integer context);
        void transitionWithException(TestState from, TestState to, TestEvent event, Integer context);
        void fromStateDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context);

        // exit states
        void exitStateA(TestState from, TestState to, TestEvent event, Integer context);
        void exitStateB(TestState from, TestState to, TestEvent event, Integer context);
        void exitC(TestState from, TestState to, TestEvent event, Integer context);
        void exitStateD(TestState from, TestState to, TestEvent event, Integer context);

        void beforeTransitionBegin(TestState from, TestEvent event, Integer context);
        void afterTransitionCompleted(TestState from, TestState to, TestEvent event, Integer context);
        void afterTransitionDeclined(TestState from, TestEvent event, Integer context);
        void afterTransitionCausedException(Exception e, TestState fromState, 
                TestState toState, TestEvent event, Integer context);

        void start();
        void terminate();
    }

    static class ExcellentCondition implements Condition<Integer> {
        @Override
        public boolean isSatisfied(Integer context) {
            return context!=null && context>80;
        }

    }

    @SuppressWarnings("serial")
    static class DeclarativeStateMachineException extends RuntimeException {
    }

    private static class DeclarativeStateMachineImpl extends AbstractStateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> implements DeclarativeStateMachine {
        private final DeclarativeStateMachine monitor;

        public DeclarativeStateMachineImpl(
                ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer>> states,
                DeclarativeStateMachine parent, Class<?> type, boolean isLeaf, DeclarativeStateMachine delegator) {
            super(initialState, states, parent, type, isLeaf);
            this.monitor = delegator;
        }

        @Override
        public void entryStateA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryStateA(from, to, event, context);
        }

        @Override
        public void entryStateB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryStateB(from, to, event, context);
        }

        @Override
        public void entryC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryC(from, to, event, context);
        }

        @Override
        public void entryStateD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryStateD(from, to, event, context);
        }

        @Override
        public void fromStateAToStateBOnGotoB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.fromStateAToStateBOnGotoB(from, to, event, context);
        }

        @Override
        public void fromStateAToStateAOnInternalA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.fromStateAToStateAOnInternalA(from, to, event, context);
        }

        @Override
        @LogExecTime
        public void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromBToCOnToC(from, to, event, context);
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                System.out.println("How dare you to wake me up?");
            }
        }

        @Override
        public void transitFromCToDOnToDWhenExcellentCondition(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToDOnToDWhenExcellentCondition(from, to, event, context);
        }

        @Override
        public void transitionWithException(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitionWithException(from, to, event, context);
            throw new IllegalArgumentException("This exception is thrown on purpose.");
        }

        @Override
        public void fromStateDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.fromStateDToFinalOnToEnd(from, to, event, context);
        }

        @Override
        public void exitStateA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitStateA(from, to, event, context);
        }

        @Override
        public void exitStateB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitStateB(from, to, event, context);
        }

        @Override
        public void exitC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitC(from, to, event, context);
        }

        @Override
        public void exitStateD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitStateD(from, to, event, context);
        }

        @Override
        public void beforeTransitionBegin(TestState from, TestEvent event, Integer context) {
            super.beforeTransitionBegin(from, event, context);
            monitor.beforeTransitionBegin(from, event, context);
        }

        @Override
        public void afterTransitionCompleted(TestState from, TestState to, TestEvent event, Integer context) {
            super.afterTransitionCompleted(from, to, event, context);
            monitor.afterTransitionCompleted(from, to, event, context);
        }

        @Override
        public void afterTransitionDeclined(TestState from, TestEvent event, Integer context) {
            super.afterTransitionDeclined(from, event, context);
            monitor.afterTransitionDeclined(from, event, context);
        }

        @Override
        public void afterTransitionCausedException(Exception e, TestState fromState, 
                TestState toState, TestEvent event, Integer context) {
            super.afterTransitionCausedException(e, fromState, toState, event, context);
            throw new DeclarativeStateMachineException();
        }

        @Override
        protected TestEvent getInitialEvent() {
            return null;
        }

        @Override
        protected Integer getInitialContext() {
            return 0;
        }

        @Override
        protected TestEvent getTerminateEvent() {
            return null;
        }

        @Override
        protected Integer getTerminateContext() {
            return -1;
        }

        @Override
        public void start() {
            super.start();
            monitor.start();
        }

        @Override
        public void terminate() {
            super.terminate();
            monitor.terminate();
        }
    }

    @Mock 
    private DeclarativeStateMachine monitor;

    private DeclarativeStateMachine stateMachine;
    
    /**
     * Initializes a test.
     */
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        StateMachineBuilder<DeclarativeStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.<DeclarativeStateMachine, TestState, TestEvent, Integer>
        newStateMachineBuilder(DeclarativeStateMachineImpl.class, TestState.class, 
                TestEvent.class, Integer.class, DeclarativeStateMachine.class);
        stateMachine = builder.newStateMachine(TestState.A, null, Object.class, true, monitor);
    }

    @Test
    public void testInternalTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.InternalA, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.A, TestEvent.InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).exitStateA(
                TestState.A, null, TestEvent.InternalA, null);
        callSequence.verify(monitor, Mockito.times(1)).fromStateAToStateAOnInternalA(
                TestState.A, TestState.A, TestEvent.InternalA, null);
        callSequence.verify(monitor, Mockito.times(0)).entryStateA(
                null, TestState.A, TestEvent.InternalA, null);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(
                TestState.A, TestState.A, TestEvent.InternalA, null);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.A));
    }

    @Test
    public void testExternalTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.A));
        stateMachine.fire(TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.A, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).exitStateA(
                TestState.A, null, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).fromStateAToStateBOnGotoB(
                TestState.A, TestState.B, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).entryStateB(
                null, TestState.B, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(
                TestState.A, TestState.B, TestEvent.ToB, null);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.B));
    }

    @Test
    public void testDeclinedTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        stateMachine.fire(TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.B, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(0)).exitStateB(
                TestState.B, null, TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionDeclined(
                TestState.B, TestEvent.ToB, null);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.B));
    }

    @Test
    public void testInvokeExtensionMethod() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        stateMachine.fire(TestEvent.ToC, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.B, TestEvent.ToC, null);

        callSequence.verify(monitor, Mockito.times(1)).exitStateB(
                TestState.B, null, TestEvent.ToC, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromBToCOnToC(
                TestState.B, TestState.C, TestEvent.ToC, null);
        callSequence.verify(monitor, Mockito.times(1)).entryC(
                null, TestState.C, TestEvent.ToC, null);

        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(
                TestState.B, TestState.C, TestEvent.ToC, null);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.C));
    }

    @Test
    public void testConditionTransition() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        stateMachine.fire(TestEvent.ToC, null);
        stateMachine.fire(TestEvent.ToD, 80);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.C, TestEvent.ToD, 80);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionDeclined(
                TestState.C, TestEvent.ToD, 80);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.C));

        stateMachine.fire(TestEvent.ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.C, TestEvent.ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).exitC(
                TestState.C, null, TestEvent.ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).transitFromCToDOnToDWhenExcellentCondition(
                TestState.C, TestState.D, TestEvent.ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).entryStateD(
                null, TestState.D, TestEvent.ToD, 81);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(
                TestState.C, TestState.D, TestEvent.ToD, 81);
        assertThat(stateMachine.getCurrentState(), equalTo(TestState.D));
    }

    @Test(expected=DeclarativeStateMachineException.class)
    public void testTransitionWithException() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        stateMachine.fire(TestEvent.ToC, null);
        stateMachine.fire(TestEvent.ToD, 81);

        stateMachine.fire(TestEvent.ToA, 50);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.D, TestEvent.ToA, 50);
        callSequence.verify(monitor, Mockito.times(1)).exitStateD(
                TestState.D, null, TestEvent.ToA, 50);
        callSequence.verify(monitor, Mockito.times(1)).transitionWithException(
                TestState.D, TestState.A, TestEvent.ToA, 50);
    }

    @Test
    public void testTransitToFinalState() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).start();
        stateMachine.fire(TestEvent.ToC, null);
        stateMachine.fire(TestEvent.ToD, 81);

        stateMachine.fire(TestEvent.ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitionBegin(
                TestState.D, TestEvent.ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).exitStateD(
                TestState.D, null, TestEvent.ToEnd, null);
        callSequence.verify(monitor, Mockito.times(1)).fromStateDToFinalOnToEnd(
                TestState.D, null, TestEvent.ToEnd, null);
        callSequence.verify(monitor, Mockito.times(0)).terminate();
        callSequence.verify(monitor, Mockito.times(1)).afterTransitionCompleted(
                TestState.D, null, TestEvent.ToEnd, null);
        assertThat(stateMachine.getStatus(), equalTo(StateMachineStatus.TERMINATED));
    }
}