package io.apiman.manager.api.beans.actions;

import io.apiman.manager.api.beans.contracts.ContractStatus;

/**
 * @author Marc Savy {@literal <marc@blackparrotlabs.io>}
 */
public class ContractActionDto {
    private Long contractId;
    private ContractStatus status;
    private String rejectionReason;
    private boolean autoPromote = false;

    public ContractActionDto() {
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Long getContractId() {
        return contractId;
    }

    public ContractActionDto setContractId(Long contractId) {
        this.contractId = contractId;
        return this;
    }

    public ContractStatus getStatus() {
        return status;
    }

    public ContractActionDto setStatus(ContractStatus status) {
        this.status = status;
        return this;
    }

    public boolean isAutoPromote() {
        return autoPromote;
    }

    public ContractActionDto setAutoPromote(boolean autoPromote) {
        this.autoPromote = autoPromote;
        return this;
    }
}
