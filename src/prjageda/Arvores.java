package prjageda;

import java.util.ArrayList;

public class Arvores {

    //1° Definição dos Atributos
    private String nomeAtr;
    private ArrayList<Atributos> arestas;

    //2° Métodos Inicializadores da classe
    public Arvores() {
        //Inicializações
        nomeAtr = "";
        arestas = null;

    }

    public Arvores(String nome, ArrayList<Atributos> galhos) {
        //Atribuições
        this.nomeAtr = nome;
        this.arestas = galhos;

    }

    public String getNomeAtr() {
        return nomeAtr;
    }

    public void setNomeAtr(String nomeAtr) {
        this.nomeAtr = nomeAtr;
    }

    public ArrayList<Atributos> getArestas() {
        return arestas;
    }

    public void setArestas(ArrayList<Atributos> arestas) {
        this.arestas = arestas;
    }

    public void setNodoApartirAresta(int pos, Arvores nodo) {
        //Setar o Nodo na Aresta Selecionada
        arestas.get(pos).setNodo(nodo);

    }
    
    public Arvores getArvoreApartirAresta(int pos) {
        //Setar o Nodo na Aresta Selecionada
        return arestas.get(pos).getNodo();
    }
    
}
