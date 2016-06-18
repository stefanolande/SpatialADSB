package it.unica.bd2.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by serus on 18/06/2016.
 */
public class Puntuale {
    private final StringProperty puntoCercato;
    private final StringProperty sorvoli;

    public Puntuale() {
        this(null, null);
    }

    public Puntuale(String punto, String sorvoli) {
        this.puntoCercato = new SimpleStringProperty(punto);
        this.sorvoli = new SimpleStringProperty(sorvoli);
    }

    public String getPuntoCercato() {
        return puntoCercato.get();
    }

    public void setPuntoCercato(String puntoCercato) {
        this.puntoCercato.set(puntoCercato);
    }

    public StringProperty puntoCercatoProperty() {
        return puntoCercato;
    }

    public String getSorvoli() {
        return sorvoli.get();
    }

    public void setSorvoli(String sorvoli) {
        this.sorvoli.set(sorvoli);
    }

    public StringProperty sorvoliProperty() {
        return sorvoli;
    }
}
