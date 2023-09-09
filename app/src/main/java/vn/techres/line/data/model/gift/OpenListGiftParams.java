package vn.techres.line.data.model.gift;

import vn.techres.line.data.model.request.BaseRequest;

public class OpenListGiftParams extends BaseRequest {
    public OpenListGiftRequest getParams() {
        return params;
    }

    public void setParams(OpenListGiftRequest params) {
        this.params = params;
    }

    OpenListGiftRequest params = new OpenListGiftRequest();
}
