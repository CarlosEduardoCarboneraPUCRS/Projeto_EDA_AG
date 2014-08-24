package prjageda;

public class Atributos {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String atributo;
    private String classe;
    private Arvores nodo;

    public Atributos() {
        //setar o atributo
        this.atributo = "";
        this.classe = "";
        this.nodo = null; //propriedade para a Sub-Árvore
    }

    public Atributos(String atr, Arvores no, String cls) {
        //setar o atributo
        this.atributo = atr;
        this.nodo = no;
        this.classe = cls;

    }
    //</editor-fold>    

    //<editor-fold defaultstate="collapsed" desc="Métodos Get´s e Set´s">                
    public void setAtributo(String atributo) {
        this.atributo = atributo;
    }

    public String getAtributo() {
        return this.atributo;
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

    public String getClasse() {
        return classe;
    }

    public void setClasse(String classe) {
        this.classe = classe;
    }
    //</editor-fold>        
}
