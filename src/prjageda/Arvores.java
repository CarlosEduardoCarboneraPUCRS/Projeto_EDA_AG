package prjageda;

import java.util.ArrayList;

public class Arvores implements Comparable<Arvores> {
    //1° Definição dos Atributos
    private String nomeAtr;
    private ArrayList<Atributos> arestas;
    private double fitness;
    

    //2° Métodos Inicializadores da classe
    public Arvores() {
        //Inicializações
        this.nomeAtr = "";
        this.arestas = null;

    }

    public Arvores(String nome, int nivel, ArrayList<Atributos> galhos) {
        //Atribuições
        this.nomeAtr = nome;
        this.arestas = galhos;

    }

    public String getNomeAtr() {
        return this.nomeAtr;
    }

    public void setNomeAtr(String nomeAtr) {
        this.nomeAtr = nomeAtr;
    }

    public ArrayList<Atributos> getArestas() {
        return arestas;
    }

    public Atributos getArestas(int pos) {
        return this.arestas.get(pos);
    }

    public void SetNodo(Atributos atr, Arvores arv) {
        atr.setNodo(arv);
    }

    public void setArestas(ArrayList<Atributos> arestas) {
        this.arestas = arestas;
    }

    public Arvores getArvoreApartirAresta(int pos) {
        //Setar o Nodo na Aresta Selecionada
        return this.arestas.get(pos).getNodo();

    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    //<editor-fold defaultstate="collapsed" desc="Métodos de Ordenação dos registros">
    @Override
    public int compareTo(Arvores obj) {
        //Definir o retorno - ordenar pelo fitness
        return (int) (this.getFitness() - ((Arvores) obj).getFitness());

    }
    
    //</editor-fold>    
}
