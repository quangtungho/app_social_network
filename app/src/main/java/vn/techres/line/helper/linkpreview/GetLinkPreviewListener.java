package vn.techres.line.helper.linkpreview;

public interface GetLinkPreviewListener {
    void onSuccess(LinkPreview link);
    void onFailed(Exception e);
}