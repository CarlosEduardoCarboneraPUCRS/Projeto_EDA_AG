package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    private String caminhoDados;
    private static final int qtdAmostras = 2;
    private ArrayList<Arvores> novaPopulacao = null;
    private static final MersenneTwister mt = new MersenneTwister();

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

    /*
     Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore
     */
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, ArrayList<Arvores> arvores, boolean elitismo) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Se tiver elitismo, adicionar (mantém) o melhor indivíduo da geração atual(ordenada) para a próxima geração
        if (elitismo) {
            //Adicionar o indivíduo
            populacao.add(arvores.get(0));

        }

        //Efetua a geração da nova população equanto a população for menor que 
        //a população inicialmente estabelecida
        while (populacao.size() < DecisionStumps.quantidade) {
            //Selecionar 2 pais pelo método do "TORNEIO"
            ArrayList<Arvores> pais = selecaoTorneio(arvores);

            //Declaração de variáveis e objetos
            ArrayList<Arvores> filhos = new ArrayList<>();

            //A mutação seré efetuada em 2 indivíduos a cada geração
            filhos.addAll(Mutacao(arvores));

            //Se Valor Gerado <= TxCrossover, realiza o Crossover entre os pais 
            //SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (new MersenneTwister().nextDouble() <= DecisionStumps.TxCrossover) {
                filhos = Crossover(pais.get(0), pais.get(1));

            } else {
                filhos.add(pais.get(0));
                filhos.add(pais.get(1));

            }

            //Calcular o Fitness de cada um dos indivíduos
            CalcularFitness(filhos.get(0));
            CalcularFitness(filhos.get(1));

            //Adicionar os novos filhos 
            populacao.add(filhos.get(0));
            populacao.add(filhos.get(1));

        }

        //Definição do retorno da função
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

    private void CalcularFitness(Arvores individuo) {
        //Declaração Variáveis e Objetos
        double valor = 0;

        //Atribuição do valor do fitness
        individuo.setFitness(valor);

    }

    private ArrayList<Arvores> selecaoTorneio(ArrayList<Arvores> arvores) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //seleciona 3 indivíduos aleatóriamente na população
        for (int i = 0; i < qtdAmostras; i++) {
            //Selecionar os 10 indivíduos aleatórios DENTRO da população gerada aleatóriamente no inicio
            selecao.add(arvores.get(new MersenneTwister().nextInt(arvores.size())));

        }

        //Ordenar as árvores selecionadas aleatóriamente
        Collections.sort(selecao);

        //Definir o retorno
        return selecao;

    }

    private ArrayList<Arvores> Crossover(Arvores arvore1, Arvores arvore2) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //Efetuar o crossover
        //Adicionar as duas árvores
        selecao.add(arvore1);
        selecao.add(arvore2);

        //Definir o retorno
        return selecao;

    }

    /*
     Efetuar a mutação da população
     */
    private ArrayList<Arvores> Mutacao(ArrayList<Arvores> regs) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> individuos = new ArrayList<>();
        boolean bProcessar = true;

        /*Sortear 2 Indivíduos Aleatóriamente*/
        Arvores individuo1 = regs.get(mt.nextInt(regs.size()));
        Arvores individuo2 = regs.get(mt.nextInt(regs.size()));

        /*
         Detalhamento da Mutação
         ---------------------------------------------------------------------------------------------------------------------
         1 Passo - Percorrer o indivíduo até o seu maior nível a partir do nó raiz
         2 Passo - Move o Ramo selecionado para o ramo próximo e transforma o ramo atual em ramo folha
         */
        PercorreNivelNodo(individuo1, 0, bProcessar);
        PercorreNivelNodo(individuo2, 0, bProcessar);

        //Recalcular o fitness de cada um dos indivíduos
        CalcularFitness(individuo1);
        CalcularFitness(individuo2);

        //Adicionar os indivíduos
        individuos.add(individuo1);
        individuos.add(individuo2);

        //Definir o retorno
        return individuos;

    }

    /*Ordena a população pelo valor de aptidão de cada indivíduo, do maior valor
     para o menor, assim se eu quiser obter o melhor indivíduo desta população, 
     acesso a posição 0 do array de indivíduos*/
    public void ordenaPopulacao() {
        //Ordernar os registros crescente
        Collections.sort(novaPopulacao);

    }

    /*A mutação ocorrerá SOMENTE com o individuo que tiver a profundidade IGUAL ao limite máximo */
    public void PercorreNivelNodo(Arvores no, int nivel, boolean bProcessar) {
        //Se o nó não for nulo
        if (no != null) {
            //Selecionar uma posição aleatóriamente
            int posicao = mt.nextInt(1);

            //Se a primeira aresta não for nula pesquisa pela mesma
            if (no.getArestas(posicao) != null) {
                //Chamada recursiva da função
                PercorreNivelNodo(no.getArestas(posicao).getNodo(), nivel + 1, bProcessar);

                //Se atingiu o MAIOR OU ÚLTIMO NÍVEL de profundidade E não for o nodo raiz, efetuará a mutação 
                if (bProcessar && nivel >= 1) {
                    //Se processou a alteração não efetuará mais alterações
                    bProcessar = false;

                    //Declaração Variáveis e Objetos
                    Arvores temp = no.getArestas(posicao).getNodo();

                    //Setar o nodo da aresta 1 para a aresta 0
                    no.getArestas(posicao).setNodo(no.getArestas(posicao == 0 ? 1 : 0).getNodo());

                    //Setar o nodo da aresta 0 para a aresta 1
                    no.getArestas(posicao == 0 ? 1 : 0).setNodo(temp);

                }

            }

        }

    }
    //</editor-fold> 

}
