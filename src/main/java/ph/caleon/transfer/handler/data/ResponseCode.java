package ph.caleon.transfer.handler.data;

import lombok.Getter;

/**
 * @author arvin.caleon on 2019-08-10
 **/
@Getter
public enum  ResponseCode {

    SUCCESSFUL("0000", "Successful"),
    GENERIC_ERROR("9600", "Generic Error"),
    REQUEST_VALIDATION_ERROR("9601", "Invalid request body"),
    TRANSFER_ERROR("9602", "Transfer error"),
    INSUFFICIENT_BALANCE("5100", "Insufficient balance"),
    INVALID_ACCOUNT("0500", "Invalid account");

    private final String code;

    private final String description;

    ResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
