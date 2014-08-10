package prjageda;

public class Atributos {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String atributo;
    private double quantidade;
    private Arvores nodo;

    public Atributos() {
        //setar o atributo
        this.atributo = "";
        this.quantidade = 0;
        this.nodo = null; //propriedade para a Sub-Árvore
        
    }

    public Atributos(String atr, double quant, Arvores no) {
        //setar o atributo
        this.atributo = atr;
        this.quantidade = quant;
        this.nodo = no;
        
    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="Métodos Get´s e Set´s">                
    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public String getAtributo() {
        return atributo;
    }

       public double getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(double quantidade) {
        this.quantidade = quantidade;
    }

    public Arvores getNodo() {
        return nodo;
    }

    public void setNodo(Arvores nodo) {
        this.nodo = nodo;
    }

    //</editor-fold>        

}