package it.unica.bd2.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Created by stefano on 17/06/16.
 */
public class Comune {
    private final StringProperty nome;
    private final StringProperty sorvoli;

    public Comune() {
        this(null, null);
    }

    public Comune(String comune, String sorvoli) {
        this.nome = new SimpleStringProperty(comune);
        this.sorvoli = new SimpleStringProperty(sorvoli);
    }

    public String getNome() {
        return nome.get();
    }

    public void setNome(String nome) {
        this.nome.set(nome);
    }

    public StringProperty nomeProperty() {
        return nome;
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
