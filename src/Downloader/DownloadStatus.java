package Downloader;

public enum DownloadStatus {
    PENDING,
    SUCCESS,
    INVALID_TICKERS,
    REQUEST_FAILED,
    QUOTA_EXCEED,
    SAVE_FAILED,
    ALREADY_SAVED
}
