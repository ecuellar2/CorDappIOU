package com.template.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contract.IOUContract;
import com.template.state.IOUState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import com.google.common.collect.ImmutableList;
import java.security.PublicKey;
import java.util.List;


@InitiatingFlow
@StartableByRPC
public class IOUFlow extends FlowLogic<Void> {
    private final Integer iouValue;
    private final Party otherParty;

    /**
     * The progress tracker provides checkpoints indicating the progress of the flow to observers.
     */
    private final ProgressTracker progressTracker = new ProgressTracker();

    public IOUFlow(Integer iouValue, Party otherParty) {
        this.iouValue = iouValue;
        this.otherParty = otherParty;
    }

    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    /**
     * The flow logic is encapsulated within the call() method.
     */
    @Suspendable
    @Override
    public Void call() throws FlowException {

        // We retrieve the required identities from the network map.
        final Party me = getServiceHub().getMyInfo().getLegalIdentity();
        final Party notary = getServiceHub().getNetworkMapCache().getAnyNotary(null);

        // We create a transaction builder.
        final TransactionBuilder txBuilder = new TransactionBuilder();
        txBuilder.setNotary(notary);

        // We add the items to the builder.
        IOUState state = new IOUState(iouValue, me, otherParty);
        List<PublicKey> requiredSigners = ImmutableList.of(me.getOwningKey(), otherParty.getOwningKey());
        Command cmd = new Command(new IOUContract.Create(), requiredSigners);
        txBuilder.withItems(state, cmd);


        // Verifying the transaction.
        txBuilder.verify(getServiceHub());


        // Signing the transaction.
        final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

        // Obtaining the counterparty's signature
        final SignedTransaction fullySignedTx = subFlow(new CollectSignaturesFlow(signedTx, CollectSignaturesFlow.Companion.tracker()));


        // Finalising the transaction.
        subFlow(new FinalityFlow(fullySignedTx));


        return null;
    }
}