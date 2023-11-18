package org.logitrack.utils.payStack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VerificationData {

    private Long id;
    private String domain;
    private String status;
    private String reference;

    @JsonProperty("receipt_number")
    private String receiptNumber;
    private Integer amount;
    private String message;

    @JsonProperty("gateway_response")
    private String gatewayResponse;

    @JsonProperty("paid_at")
    private String paid_at;

    @JsonProperty("created_at")
    private String created_at;
    private String channel;
    private String currency;

    @JsonProperty("ip_address")
    private String ipAddress;
    private String metadata;
    private Log log;
    private Integer fees;

    @JsonProperty("fees_split")
    private Object feesSplit;
    private Authorization authorization;
    private Customer customer;
    private Object plan;
    private Object split;

    @JsonProperty("order_id")
    private Object orderId;
    private String paidAt;
    private String createdAt;

    @JsonProperty("requested_amount")
    private Integer requestedAmount;

    @JsonProperty("pos_transaction_data")
    private Object posTransactionData;
    private Object source;

    @JsonProperty("fees_breakdown")
    private Object feesBreakdown;

    @JsonProperty("transaction_date")
    private String transactionDate;

    @JsonProperty("plan_object")
    private Object planObject;

    @JsonProperty("subaccount")
    private Object subAccount;

}
