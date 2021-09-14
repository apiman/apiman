package io.apiman.manager.api.beans.actions;

import io.apiman.manager.api.beans.contracts.ContractStatus;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ContractActionDto {
    private Long contractId;
    private ContractStatus statusBean;

    public Long getContractId() {
        return contractId;
    }

    public ContractActionDto setContractId(Long contractId) {
        this.contractId = contractId;
        return this;
    }

    public ContractStatus getStatusBean() {
        return statusBean;
    }

    public ContractActionDto setStatusBean(ContractStatus statusBean) {
        this.statusBean = statusBean;
        return this;
    }
}
