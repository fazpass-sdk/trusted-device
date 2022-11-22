package com.fazpass.trusted_device;

public abstract class HeaderEnrichment {
    public interface Request{
        void onComplete(boolean status);
        void onError(Throwable err);
    }
}
