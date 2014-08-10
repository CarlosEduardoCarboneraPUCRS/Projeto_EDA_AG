package prjageda;

import java.util.ArrayList;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String caminhoDados;

    public String getCaminhoDados() {
        return caminhoDados;
    }

    public void setCaminhoDados(String caminho) {
        this.caminhoDados = caminho;
    }

    public Processamento(String local) {
        //Inicialização dos atributos
        this.caminhoDados = local;

    }

    public Processamento() {

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento">        
    public Instances LeituraArquivo() {
        //Declaração Variáveis e Objetos
        Instances dados = null;

        try {
            //Inicialização da Leitura
            dados = new DataSource(caminhoDados).getDataSet();

            //Setar o atributo classe
            if (dados.classIndex() == -1) {
                dados.setClassIndex(dados.numAttributes() - 1);
            }

        } catch (Exception e) {
            //Se ocorreu alguma exceção
            System.out.println(e.getMessage());

        }

        //Definição do Retorno
        return dados;

    }

    public ArrayList<Atributos> ProcessamentoInstancias(Instances dados, int posAtr) {
        //Declaração Variáveis e Objetos
        ArrayList<Atributos> registros = new ArrayList<>();

        //Percorre todas as Classes Encontradas
        for (int i = 0; i < dados.numClasses(); i++) {
            //Declaração Variáveis e Objetos
            Double qtdRegs = 0d;
            double valor = dados.instance(i).classValue();

            for (int j = 0; j < dados.numInstances(); j++) {
                //Atualizar a quantidade
                qtdRegs += dados.instance(j).value(posAtr) == valor ? 1 : 0;

            }

            //Adicionar o Registro
            registros.add(new Atributos(String.valueOf(valor), qtdRegs, null));

        }

        //Definir o retorno
        return registros;

    }

    /*
     Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore    
    10/08/2014- Alteraado Carlos
     */
    public void ProcessamentoArvores(Instances dados, ArrayList<Arvores> arvores) {
        //Percorrer todas as instâncias encontradas
        for (int i = 0; i < dados.numInstances(); i++) {
            //Percorrer toda a árvore
            for (int j = 0; j < arvores.size(); j++) {
                //percorer todos os nodos da árvore

            }
        }

    }
    //</editor-fold>      

}
