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
    /*Efetuar a leitura do arquivo .arff e atribuir os parâmetros*/
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

    /*
     Processamento das instâncias lidas da base de dados
     */
    public ArrayList<Atributos> ProcessamentoInstancias(Instances dados, int posicao) {
        //Declaração Variáveis e Objetos
        ArrayList<Atributos> registros = new ArrayList<>();

        /*
         1 - Avaliar se o Atributo é Numérico ou Nominal
         1.1 - Se Numérico a árvore terá 2 arestas (Calcular o Indice Gini)
         1.2 - Se Categórico terá o número de arestas em função da quantidade de atributos
         --------------------------------------------------------------------------------------------------------------------------------------------------------
         */
        //1.1 - Atributo Numérico (Árvore será bifurcada)
        if (dados.attribute(posicao).isNumeric()) {
            //Percorre todas as Classes Encontradas
            for (int pos = 0; pos < dados.numClasses(); pos++) {
                //Declaração Variáveis e Objetos
                Double qtdRegs = 0d;

                //double valor = dados.instance(i).classValue();
                for (int j = 0; j < dados.numInstances(); j++) {
                    //Atualizar a quantidade
                    qtdRegs += dados.instance(j).value(posicao) == pos ? 1 : 0;

                }

                //Adicionar o Registro
                registros.add(new Atributos(String.valueOf(pos), qtdRegs, null, (1 - Math.pow(qtdRegs / dados.numInstances(), 2))));

            }

        } else {
            //Percorrer a Quantidade de Atributos existentes
            for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                //Declaração Variáveis e Objetos
                Double qtdRegs = 0d;

                //Declaração Variáveis e Objetos
                String nomeAtr = dados.attribute(posicao).value(i);

                for (int j = 0; j < dados.numInstances(); j++) {
                    //Atualizar a quantidade
                    //qtdRegs += String.valueOf(dados.instance(j).value(posicao)) == nomeAtr ? 1 : 0;
                    qtdRegs += String.valueOf(dados.instance(j).attribute(posicao).value(i)) == nomeAtr ? 1 : 0;

                }

                //Adicionar o nodo                
                registros.add(new Atributos(nomeAtr, qtdRegs, null, (1 - Math.pow(qtdRegs / dados.numInstances(), 2))));

            }

        }

        //Definir o retorno
        return registros;

    }

//Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore    
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, ArrayList<Arvores> arvores, boolean elitismo) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> novaPopulacao = new ArrayList<>();

        //Se tiver elitismo, mantém o melhor indivíduo da geração atual
        if (elitismo) {
            novaPopulacao.set(0, arvores.get(0));
        }

        /*
         //Percorrer todas as instâncias encontradas
         for (int i = 0; i < dados.numInstances(); i++) {
         //Percorrer toda a árvore
         for (int j = 0; j < arvores.size(); j++) {
         //percorer todos os nodos da árvore

         }
         }
         */
        //Efetuar a Seleção por Torneio
        //Efetuar a Mutação das Árvores
        Mutacao(arvores);

        //Efetuar a Mutação das Árvores
        Crossover(arvores);

        //Definir o retorno
        return novaPopulacao;

    }

    /*
     Efetuar a mutação da população
     */
    private ArrayList<Arvores> Mutacao(ArrayList<Arvores> arvores) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        //percorrer todos os registros
        for (Arvores reg : arvores) {

        }

        //Definir o retorno
        return populacao;

    }

    /*
     Avaliar se a condição de parada foi atingida
     */
    public boolean AvaliarSolucao(ArrayList<Arvores> arvores) {
        //Declaração Variáveis e objetos
        boolean bValido = true;

        //Avaliar se atingiu a condição de parada
        //Definição do retorno
        return bValido;

    }

    private void Crossover(ArrayList<Arvores> arvores) {

    }

    //</editor-fold>          
    private double OcorrenciasAtributo(String atributo, Instances dados, int pos) {
        //Declaração Variáveis e Objetos
        double quantidade = 0;

        //Percorrer todas as instâncias
        for (int j = 0; j < dados.numInstances(); j++) {
            //Se for igual ao valor informado
            if (dados.instance(j).attribute(pos).equals(atributo)) {
                //Atualizar a Quantidade
                quantidade += 1;

            }

        }

        //Definir o retorno
        return quantidade;

    }

}
