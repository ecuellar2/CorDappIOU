package com.template.state;

import com.template.contract.IOUContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;



/**
 * Define your state object here.
 */
public class IOUState implements ContractState {
    private final int value;
    private final Party lender;
    private final Party borrower;
    private final IOUContract contract = new IOUContract();

    public IOUState(int value, Party lender, Party borrower) {
        this.value = value;
        this.lender = lender;
        this.borrower = borrower;
    }

    public int getValue() {
        return value;
    }

    public Party getLender() {
        return lender;
    }

    public Party getBorrower() {
        return borrower;
    }

    @Override
    // TODO: Once we've defined IOUContract, come back and update this.
    public IOUContract getContract() {
        return contract;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return ImmutableList.of(lender, borrower);
    }
}