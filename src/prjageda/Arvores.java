package prjageda;

import java.util.ArrayList;
import java.util.List;

public class Arvores implements Comparable<Arvores> {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    
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

    public String getNomeAtributo() {
        return this.nomeAtr;

    }

    public void setNomeAtributo(String nomeAtr) {
        this.nomeAtr = nomeAtr;

    }

    public List<Atributos> getArestas() {
        return this.arestas;

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
        return this.fitness;

    }

    public void setFitness(double fitness) {
        this.fitness = fitness;

    }

    public int getQtdOcorrencias() {
        return this.qtdOcorr;
    }

    public void setQtdOcorrencias(int qtd) {
        this.qtdOcorr = qtd;
    }

    public void AtuQtdOcorrencias(int qtd) {
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
