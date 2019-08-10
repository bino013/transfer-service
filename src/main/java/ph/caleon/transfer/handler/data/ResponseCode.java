package ph.caleon.transfer.handler.data;

import lombok.Getter;

/**
 * @author arvin.caleon on 2019-08-10
 **/
@Getter
public enum  ResponseCode {

    SUCCESSFUL("0000", "Successful"),
    REQUEST_VALIDATION_ERROR("9601", "Invalid request body"),
    TRANSFER_ERROR("9602", "Transfer error"),
    INSUFFICIENT_BALANCE("5100", "Transfer error");

    private final String code;

    private final String description;

    ResponseCode(String code, String description) {
        this.code = code;
        this.description = description;
    }
}
