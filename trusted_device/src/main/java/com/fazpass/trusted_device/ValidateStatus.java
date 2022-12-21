package com.fazpass.trusted_device;

public class ValidateStatus {
    private final boolean status;
    private final Confidence confidenceRate;

    public ValidateStatus(boolean status, Confidence confidenceRate) {
        this.status = status;
        this.confidenceRate = confidenceRate;
    }

    public boolean isStatus() {
        return status;
    }

    public Confidence getConfidenceRate() {
        return confidenceRate;
    }

    public static class Confidence{
        private final double meta;
        private final double key;
        private final double sim;
        private final double contact;
        private final double location;

        public Confidence(double meta, double key, double sim, double contact, double location) {
            this.meta = meta;
            this.key = key;
            this.sim = sim;
            this.contact = contact;
            this.location = location;
        }

        public double getSummary(){
            return meta + key + sim + contact + location;
        }
    }
}
