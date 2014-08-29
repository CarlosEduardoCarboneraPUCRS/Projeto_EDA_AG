package prjageda;

public class Classes {

    //1° Definição dos atributos classes e métodos Inicializadores
    private String nome;
    private int quantidade;

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

    //Definição dos Get´s e Set´s e demais métodos 
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

}
