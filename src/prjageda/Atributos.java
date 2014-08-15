package prjageda;

public class Atributos {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String atributo;
    private double quantidade;
    private double acuracia;
    private Arvores nodo;

    public Atributos() {
        //setar o atributo
        this.atributo = "";
        this.quantidade = 0;
        this.acuracia = 0;
        this.nodo = null; //propriedade para a Sub-Árvore

    }

    public Atributos(String atr, double quant, Arvores no, double acuracia) {
        //setar o atributo
        this.atributo = atr;
        this.quantidade = quant;
        this.nodo = no;
        this.acuracia = acuracia;

    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="Métodos Get´s e Set´s">                
    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public String getAtributo() {
        return this.atributo;
    }

    public double getQuantidade() {
        return this.quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public Arvores getNodo() {
        return this.nodo;
    }

    public void setNodo(Arvores nodo) {
        this.nodo = nodo;
    }

    public void setNodo(Atributos atr, Arvores arv) {
        atr.setNodo(arv);
    }

    public double getAcuracia() {
        return this.acuracia;
    }

    public void setAcuracia(double acuracia) {
        this.acuracia = acuracia;
    }
    //</editor-fold>        

}
