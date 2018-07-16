package com.template.contract;

import com.template.state.IOUState;
import net.corda.core.contracts.AuthenticatedObject;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.crypto.SecureHash;
import net.corda.core.identity.Party;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
import com.google.common.collect.ImmutableList;


public class IOUContract implements Contract {
    // Our Create command.
    public static class Create implements CommandData {}

    @Override
    public void verify(LedgerTransaction tx) {
        final AuthenticatedObject<Create> command = requireSingleCommand(tx.getCommands(), Create.class);

        requireThat(check -> {
            // Constraints on the shape of the transaction.
            check.using("No inputs should be consumed when issuing an IOU.", tx.getInputs().isEmpty());
            check.using("There should be one output state of type IOUState.", tx.getOutputs().size() == 1);

            // IOU-specific constraints.
            final IOUState out = (IOUState) tx.getOutputs().get(0).getData();
            final Party lender = out.getLender();
            final Party borrower = out.getBorrower();
            check.using("The IOU's value must be non-negative.",out.getValue() > 0);
            check.using("The lender and the borrower cannot be the same entity.", lender != borrower);

            // Constraints on the signers.
            //check.using("There must only be one signer.", command.getSigners().size() == 1);
            //check.using("The signer must be the lender.", command.getSigners().contains(lender.getOwningKey()));

            check.using("There must be two signers.", command.getSigners().size() == 2);
            check.using("The borrower and lender must be signers.", command.getSigners().containsAll(
                    ImmutableList.of(borrower.getOwningKey(), lender.getOwningKey())));


            return null;
        });
    }

    // The legal contract reference - we'll leave this as a dummy hash for now.
    private final SecureHash legalContractReference = SecureHash.Companion.getZeroHash();
    @Override public final SecureHash getLegalContractReference() { return legalContractReference; }
}