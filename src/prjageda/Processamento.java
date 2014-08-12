package prjageda;

import java.util.ArrayList;
import java.util.Random;
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
            //double valor = dados.instance(i).classValue();
            String atributo = dados.classAttribute().value(i).toUpperCase();

            for (int j = 0; j < dados.numInstances(); j++) {
                //Atualizar a quantidade
                qtdRegs += String.valueOf(dados.instance(j).value(posAtr)).toUpperCase().equals(atributo) ? 1 : 0;

            }

            //Adicionar o Registro
            registros.add(new Atributos(atributo, qtdRegs, null));

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore    
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, ArrayList<Arvores> arvores, boolean elitismo) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> novaPopulacao = new ArrayList<Arvores>();

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
        EfetuarMutacao(arvores);

        //Definir o retorno
        return novaPopulacao;

    }

    /*
     private void ProcessamentoEmOrdem(Arvores nos) {
     //Se for nulo retorna
     if (nos == null) {
     return;
     }

     //Se possuir nós filhos (arestas) 
     if (nos.getArestas().size() != 0) {
     //Percorrer todas as arestas do nó selecionado
     for (int i = 0; i <= nos.getArestas().size() - 1; i++) {

     }
     }

     //Percorrer todas as arestas do nó selecionado
     for (int i = 0; i <= nos.getArestas().size() - 1; i++) {
     //Chamada Recursiva de processamento
     ProcessamentoEmOrdem(nos.getArvoreApartirAresta(i));

     }

     }*/
    //</editor-fold>      
    private void EfetuarMutacao(ArrayList<Arvores> arvores) {
        //Sortar as Duas Árvores Aleatóriamente
        Arvores arvore1 = arvores.get(new Random().nextInt(arvores.size())),
                arvore2 = arvores.get(new Random().nextInt(arvores.size()));

    }

    public boolean AvaliarSolucao(ArrayList<Arvores> arvores) {
        //Declaração Variáveis e objetos
        boolean bValido = true;

        //Definição do retorno
        return bValido;
        
    }
}
