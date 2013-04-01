package org.squirrel.foundation.fsm;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.squirrel.foundation.fsm.TestEvent.ToB;
import static org.squirrel.foundation.fsm.TestEvent.ToC;
import static org.squirrel.foundation.fsm.TestEvent.ToD;
import static org.squirrel.foundation.fsm.TestEvent.ToEnd;
import static org.squirrel.foundation.fsm.TestState.A;
import static org.squirrel.foundation.fsm.TestState.B;
import static org.squirrel.foundation.fsm.TestState.C;
import static org.squirrel.foundation.fsm.TestState.D;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.squirrel.foundation.fsm.ImmutableState;
import org.squirrel.foundation.fsm.StateMachine;
import org.squirrel.foundation.fsm.StateMachineStatus;
import org.squirrel.foundation.fsm.annotation.State;
import org.squirrel.foundation.fsm.annotation.States;
import org.squirrel.foundation.fsm.annotation.Transit;
import org.squirrel.foundation.fsm.annotation.Transitions;
import org.squirrel.foundation.fsm.builder.StateMachineBuilder;
import org.squirrel.foundation.fsm.impl.AbstractStateMachine;
import org.squirrel.foundation.fsm.impl.StateMachineBuilderImpl;

public class StateMachineExtensionTest extends AbstractStateMachineTest {
    
    interface CallSequenceMonitor {
        void entryA(TestState from, TestState to, TestEvent event, Integer context);
        void entryB(TestState from, TestState to, TestEvent event, Integer context);
        void entryC(TestState from, TestState to, TestEvent event, Integer context);
        void entryD(TestState from, TestState to, TestEvent event, Integer context);
        
        void transitFromAToBOnToB(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToCBase(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromBToCOnToCOverride(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromCToDOnToD(TestState from, TestState to, TestEvent event, Integer context);
        void transitFromDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context);
        
        void exitA(TestState from, TestState to, TestEvent event, Integer context);
        void exitB(TestState from, TestState to, TestEvent event, Integer context);
        void afterExitB(TestState from, TestState to, TestEvent event, Integer context);
        void exitC(TestState from, TestState to, TestEvent event, Integer context);
        void exitD(TestState from, TestState to, TestEvent event, Integer context);
        
        void beforeEntryB(TestState from, TestState to, TestEvent event, Integer context);
        void beforeExitA(TestState from, TestState to, TestEvent event, Integer context);
        void afterEntryB(TestState from, TestState to, TestEvent event, Integer context);
        void afterExitA(TestState from, TestState to, TestEvent event, Integer context);
        void beforeTransitToB(TestState from, TestState to, TestEvent event, Integer context);
        void afterTransitToB(TestState from, TestState to, TestEvent event, Integer context);
    }
    
    @State(name="B", entryCallMethod="afterEntryB")
    @Transit(from="A", to="B", on="ToB")
    interface DeclarativeStateMachine extends StateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> {
    }
    
    @States({
        @State(name="A", exitCallMethod="beforeExitA"),
        @State(name="A", exitCallMethod="afterExitA"),
        @State(name="B", entryCallMethod="beforeEntryB")
    })
    @Transitions({
        @Transit(from="B", to="C", on="ToC"),
        @Transit(from="A", to="B", on="ToB", callMethod="beforeTransitToB"),
        @Transit(from="A", to="B", on="ToB", callMethod="afterTransitToB"),
        @Transit(from="C", to="D", on="ToD")
    })
    static class StateMachineImpl extends AbstractDeclarativeStateMachine {

        public StateMachineImpl(
                ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer>> states,
                DeclarativeStateMachine parent, Class<?> type, boolean isLeaf,
                CallSequenceMonitor monitor) {
            super(initialState, states, parent, type, isLeaf, monitor);
        }
        
        protected void beforeEntryB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.beforeEntryB(from, to, event, context);
        }
        
        protected void beforeExitA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.beforeExitA(from, to, event, context);
        }
        
        protected void afterEntryB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.afterEntryB(from, to, event, context);
        }
        
        protected void afterExitA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.afterExitA(from, to, event, context);
        }
        
        protected void beforeTransitToB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.beforeTransitToB(from, to, event, context);
        }
        
        protected void afterTransitToB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.afterTransitToB(from, to, event, context);
        }
        
        @Override
        protected void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromBToCOnToCOverride(from, to, event, context);
        }
    }
    
    abstract static class AbstractDeclarativeStateMachine extends 
        AbstractStateMachine<DeclarativeStateMachine, TestState, TestEvent, Integer> implements DeclarativeStateMachine {
        
        protected CallSequenceMonitor monitor;
        
        public AbstractDeclarativeStateMachine(
                ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer> initialState,
                Map<TestState, ImmutableState<DeclarativeStateMachine, TestState, TestEvent, Integer>> states,
                DeclarativeStateMachine parent, Class<?> type, boolean isLeaf, CallSequenceMonitor monitor) {
            super(initialState, states, parent, type, isLeaf);
            this.monitor = monitor;
        }

        protected void entryA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryA(from, to, event, context);
        }

        protected void entryB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryB(from, to, event, context);
        }

        protected void entryC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryC(from, to, event, context);
        }

        protected void entryD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.entryD(from, to, event, context);
        }

        protected void transitFromAToBOnToB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromAToBOnToB(from, to, event, context);
        }

        protected void transitFromBToCOnToC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromBToCOnToCBase(from, to, event, context);
        }

        protected void transitFromCToDOnToD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromCToDOnToD(from, to, event, context);
        }

        protected void transitFromDToFinalOnToEnd(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.transitFromDToFinalOnToEnd(from, to, event, context);
        }

        protected void exitA(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitA(from, to, event, context);
        }

        protected void exitB(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitB(from, to, event, context);
        }

        protected void exitC(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitC(from, to, event, context);
        }

        protected void exitD(TestState from, TestState to, TestEvent event, Integer context) {
            monitor.exitD(from, to, event, context);
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
    }
    
    @Mock
    private CallSequenceMonitor monitor;
    
    private DeclarativeStateMachine stateMachine;
    
    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        StateMachineBuilder<DeclarativeStateMachine, TestState, TestEvent, Integer> builder = 
                StateMachineBuilderImpl.<DeclarativeStateMachine, TestState, TestEvent, Integer>
                newStateMachineBuilder(StateMachineImpl.class, TestState.class, 
                        TestEvent.class, Integer.class, CallSequenceMonitor.class);
        builder.transition().from(D).toFinal().on(ToEnd);
        stateMachine = builder.newStateMachine(A, null, Object.class, true, monitor);
    }
    
    @Test
    public void testTransitions() {
        stateMachine.fire(ToB, null);
        assertThat(stateMachine.getCurrentState(), equalTo(B));
        
        stateMachine.fire(ToC, null);
        assertThat(stateMachine.getCurrentState(), equalTo(C));
        
        stateMachine.fire(ToD, null);
        assertThat(stateMachine.getCurrentState(), equalTo(D));
        
        stateMachine.fire(ToEnd, null);
        assertThat(stateMachine.getStatus(), equalTo(StateMachineStatus.TERMINATED));
    }
    
    @Test
    public void testMethodInvokeSequence() {
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToB, null);
        
        callSequence.verify(monitor, Mockito.times(1)).beforeExitA(A, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).exitA(A, null, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterExitA(A, null, ToB, null);
        
        callSequence.verify(monitor, Mockito.times(1)).beforeTransitToB(A, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromAToBOnToB(A, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterTransitToB(A, B, ToB, null);
        
        callSequence.verify(monitor, Mockito.times(1)).beforeEntryB(null, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).entryB(null, B, ToB, null);
        callSequence.verify(monitor, Mockito.times(1)).afterEntryB(null, B, ToB, null);
    }
    
    @Test
    public void testOverrideMethodInvokeSequence() {
    	stateMachine.fire(TestEvent.ToB, null);
    	
        InOrder callSequence = Mockito.inOrder(monitor);
        stateMachine.fire(TestEvent.ToC, null);
        callSequence.verify(monitor, Mockito.times(1)).exitB(B, null, ToC, null);
        callSequence.verify(monitor, Mockito.times(0)).afterExitB(B, null, ToC, null);
        callSequence.verify(monitor, Mockito.times(0)).transitFromBToCOnToCBase(B, C, ToC, null);
        callSequence.verify(monitor, Mockito.times(1)).transitFromBToCOnToCOverride(B, C, ToC, null);
    }
}