package com.template.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.template.state.IOUState;
import net.corda.core.contracts.ContractState;
import net.corda.core.flows.FlowException;
import net.corda.core.flows.FlowLogic;
import net.corda.core.flows.InitiatedBy;
import net.corda.core.flows.SignTransactionFlow;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.utilities.ProgressTracker;

import static net.corda.core.contracts.ContractsDSL.requireThat;

@InitiatedBy(IOUFlow.class)
public class IOUFlowResponder extends FlowLogic<Void> {
    private final Party otherParty;

    public IOUFlowResponder(Party otherParty) {
        this.otherParty = otherParty;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
        class signTxFlow extends SignTransactionFlow {
            private signTxFlow(Party otherParty, ProgressTracker progressTracker) {
                super(otherParty, progressTracker);
            }

            @Override
            protected void checkTransaction(SignedTransaction stx) {
                requireThat(require -> {
                    ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be an IOU transaction.", output instanceof IOUState);
                    IOUState iou = (IOUState) output;
                    require.using("The IOU's value can't be too high.", iou.getValue() < 100);
                    return null;
                });
            }
        }

        subFlow(new signTxFlow(otherParty, SignTransactionFlow.Companion.tracker()));

        return null;
    }
}