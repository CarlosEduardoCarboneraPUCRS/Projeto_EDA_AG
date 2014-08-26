package prjageda;

public class Classes {

    private String nome;
    private int quantidade;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public void atualizarQtd(int qtd) {
        this.quantidade += qtd;
    }
 
    //Método Inicializador 1
    public Classes() {
        this.nome = "";
        this.quantidade = 0;

    }

    //Método Inicializador 2
    public Classes(String clsNm, int qtd) {
        this.nome = clsNm;
        this.quantidade = qtd;

    }

}
