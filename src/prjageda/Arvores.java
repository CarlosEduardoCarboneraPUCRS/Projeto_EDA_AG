package prjageda;

import java.util.ArrayList;
import java.util.List;

public class Arvores implements Comparable<Arvores> {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos">    
    private String nomeAtr;
    private double fitness;
    private int qtdOcorr;
    private List<Atributos> arestas;

    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="2° Métodos Inicializadores da classe e Get´s E Set´s">
    public Arvores() {
        //Inicializações
        this.nomeAtr = "";
        this.fitness = 0.0;
        this.qtdOcorr = 0;
        this.arestas = null;

    }

    public Arvores(String nome, ArrayList<Atributos> galhos) {
        //Atribuições
        this.nomeAtr = nome;
        this.fitness = 0.0;
        this.qtdOcorr = 0;
        this.arestas = galhos;

    }

    public String getNomeAtr() {
        return this.nomeAtr;

    }

    public void setNomeAtr(String nomeAtr) {
        this.nomeAtr = nomeAtr;

    }

    public List<Atributos> getArestas() {
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

    public int getQtdOcorr() {
        return this.qtdOcorr;
    }

    public void setQtdOcorr(int qtd) {
        this.qtdOcorr = qtd;
    }
    
    public void AtualizarQtdOcorr(int qtd) {
        this.qtdOcorr += qtd;
    }
    
    
    //</editor-fold>    
    //<editor-fold defaultstate="collapsed" desc="3° Métodos de Ordenação dos registros">
    @Override
    public int compareTo(Arvores obj) {
        //Efetuar a ordenação dos registros crescente, por exemplo.: 0.1, 0.2, 0.3...1.0
        return (this.getFitness() < obj.getFitness()) ? -1 : (this.getFitness() > obj.getFitness()) ? 1 : 0;

    }

    //</editor-fold>    
    
}
