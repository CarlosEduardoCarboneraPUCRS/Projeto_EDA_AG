package prjageda;

public class IndiceGini implements Comparable<IndiceGini> {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    private double valor;
    private int quantidade;

    public IndiceGini() {
        //setar o atributo
        this.valor = 0d;
        this.quantidade = 0;

    }

    public IndiceGini(double vlr, int qtd) {
        //setar o atributo
        this.valor = vlr;
        this.quantidade = qtd;

    }
    //</editor-fold>        
   
    //<editor-fold defaultstate="collapsed" desc="2° Definição dos Get´s e Set´s e demais métodos">
    public double getValor() {
        return valor;
    }

    public void setValor(double valor) {
        this.valor = valor;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void adicionar(int quant) {
        this.quantidade += quant;

    }
    //</editor-fold>
    
    @Override
    public int compareTo(IndiceGini o) {
        return (this.valor == ((IndiceGini) o).getValor()) ? 0 : (this.valor > (((IndiceGini) o).getValor())) ? 1 : -1;
        
    }

}
