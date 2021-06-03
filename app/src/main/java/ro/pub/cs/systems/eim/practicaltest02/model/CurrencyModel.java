package ro.pub.cs.systems.eim.practicaltest02.model;

public class CurrencyModel {
    private String updated;
    private String eur_rate;
    private String usd_rate;

    public CurrencyModel() {
        updated = null;
        eur_rate = null;
        usd_rate = null;
    }
    public CurrencyModel(String updated, String eur_rate, String usd_rate) {
        this.updated = updated;
        this.eur_rate = eur_rate;
        this.usd_rate = usd_rate;
    }

    public String getUpdated() {
        return updated;
    }

    public void setUpdated(String updated) {
        this.updated = updated;
    }

    public String getEur_rate() {
        return eur_rate;
    }

    public void setEur_rate(String eur_rate) {
        this.eur_rate = eur_rate;
    }

    public String getUsd_rate() {
        return usd_rate;
    }

    public void setUsd_rate(String usd_rate) {
        this.usd_rate = usd_rate;
    }

    @Override
    public String toString() {
        return "CurrencyModel{" +
                "updated='" + updated + '\'' +
                ", eur_rate='" + eur_rate + '\'' +
                ", usd_rate='" + usd_rate + '\'' +
                '}';
    }
}
