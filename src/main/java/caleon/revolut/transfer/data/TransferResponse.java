package caleon.revolut.transfer.data;

/**
 * @author arvin.caleon on 2019-08-03
 **/
public class TransferResponse {

    private String code;

    private String message;

    public TransferResponse() {
    }

    public TransferResponse(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
