package prjageda;

import java.util.ArrayList;

public class Atributos {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String atributo;
    private String classeDominante;
    private ArrayList<Classes> classes;
    private Arvores nodo;

    public Atributos() {
        //setar o atributo
        this.atributo = "";
        this.classeDominante = "";
        this.nodo = null; //propriedade para a Sub-Árvore
        this.classes = null;
    }

    public Atributos(String atr, Arvores no, String clsDominante, ArrayList<Classes> cls) {
        //setar o atributo
        this.atributo = atr;
        this.nodo = no;
        this.classeDominante = clsDominante;
        this.classes = cls;

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

    public String getClasseDominante() {
        return this.classeDominante;
    }

    public void setClasseDominante(String classe) {
        this.classeDominante = classe;
    }

    public ArrayList<Classes> getClasses() {
        return this.classes;
    }

    public void setClasses(ArrayList<Classes> classes) {
        this.classes = classes;
    }
    //</editor-fold>        
}
