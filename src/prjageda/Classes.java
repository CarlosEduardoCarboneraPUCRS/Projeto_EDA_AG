package prjageda;

public class Classes {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    
    private String nome;
    private int quantidade;
    
    public Classes() {
        this.nome = "";
        this.quantidade = 0;

    }
    
    public Classes(String clsNm, int qtd) {
        this.nome = clsNm;
        this.quantidade = qtd;

    }
    //</editor-fold>    
    
    //<editor-fold defaultstate="collapsed" desc="2° Definição dos Get´s e Set´s e demais métodos">        
    public String getNome() {
        return this.nome;

    }

    public void setNome(String nome) {
        this.nome = nome;

    }

    public int getQuantidade() {
        return this.quantidade;

    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;

    }

    public void atualizarQtd(int qtd) {
        this.quantidade += qtd;

    }
    //</editor-fold>    
    
}
