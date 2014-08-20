package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="ATRIBUTOS E MÉTODO CONSTRUTOR DA CLASSE">    
    private String caminhoDados;

    //Declaração de objetos
    private static final MersenneTwister mt = new MersenneTwister();
    private static final int qtdAmostras = 2;
    private static Arvores tempNodo = null;

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

    //<editor-fold defaultstate="collapsed" desc="MÉTODOS DE PROCESSAMENTO DIVERSOS">        
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

    //Processamento das instâncias lidas da base de dados
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
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Se tiver elitismo, adicionar (mantém) o melhor indivíduo da geração atual(ordenada) para a próxima geração
        if (elitismo) {
            //Adicionar o indivíduo
            populacao.add(arvores.get(0));

        }

        //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
        while (populacao.size() < DecisionStumps.quantidade) {
            //Selecionar 2 Indivíduos Pais pelo método "TORNEIO"
            ArrayList<Arvores> pais = selecaoTorneio(arvores);
            
            //Declaração de variáveis e objetos
            ArrayList<Arvores> filhos = new ArrayList<>();

            //Adicionar os 2 indivíduos que sofreram a mutação
            filhos.addAll(Mutacao(arvores));

            //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (new MersenneTwister().nextDouble() <= DecisionStumps.TxCrossover) {
                //Adicionar os 2 filhos que sofreram Crossover
                filhos = Crossover(pais.get(0), pais.get(1));

            } else {
                //Calcular o Fitness de cada um dos indivíduos
                CalcularFitness(pais.get(0));
                CalcularFitness(filhos.get(1));

                //Apenas adicionar os 2 filhos selecionados
                filhos.add(pais.get(0));
                filhos.add(pais.get(1));

            }

            //Adicionar os novos filhos 
            populacao.addAll(filhos);

        }

        //Definição do retorno da função
        return populacao;

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

    private ArrayList<Arvores> Crossover(Arvores origem, Arvores destino) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Remover o nodo da árvore origem setando nulo ao mesmo
        RemoverNodoNaOrigem(origem, 1);

        //Se não for nulo o nodo selecionado
        if (tempNodo != null) {
            //Incluir o nodo processado na árvore de destino
            IncluirNodoNoDestino(destino, tempNodo);

            //Calcular o Fitness
            CalcularFitness(destino);

            //Adicionar as duas árvores
            populacao.add(destino);

        }

        //Definir o retorno
        return populacao;

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE MUTAÇÃO">   
    private ArrayList<Arvores> Mutacao(ArrayList<Arvores> regs) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> individuos = new ArrayList<>();
        Arvores individuo1 = regs.get(mt.nextInt(regs.size()));
        Arvores individuo2 = regs.get(mt.nextInt(regs.size()));
        
        /*
         Detalhamento da Mutação
         ---------------------------------------------------------------------------------------------------------------------
         1 Passo - Percorrer o indivíduo até o seu maior nível a partir do nó raiz
         2 Passo - Move o Ramo selecionado para o ramo próximo e transforma o ramo atual em ramo folha
         */
        PosicionarMaiorNivel(individuo1, 1);
        PosicionarMaiorNivel(individuo2, 1);

        //Recalcular o fitness de cada um dos indivíduos
        CalcularFitness(individuo1);
        CalcularFitness(individuo2);

        //Adicionar os indivíduos
        individuos.add(individuo1);
        individuos.add(individuo2);

        //Definir o retorno
        return individuos;

    }

    /**
     * Posicionar individuo maior nível da árvore informada e efetuar a troca do material da primeira aresta pela segunda
     *
     * @param individuo - Indivíduo que sofrerá a mutação
     * @param nivel - Deverá ser maior que 1 pois não se pode efetuar mutação individuo nível raiz
     */
    public void PosicionarMaiorNivel(Arvores individuo, int nivel) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Selecionar uma posição aleatóriamente
            int posicao = 0; //mt.nextInt(1);

            //Se a primeira aresta não for nula pesquisa pela mesma
            if (individuo.getArestas(posicao).getNodo() != null) {
                //Chamada recursiva da função
                PosicionarMaiorNivel(individuo.getArestas(posicao).getNodo(), nivel + 1);

                //Se atingiu o MAIOR OU ÚLTIMO NÍVEL de profundidade E não for o nodo raiz, efetuará a mutação 
                if (nivel == DecisionStumps.profundidade) {
                    //Declaração Variáveis e Objetos
                    Arvores temp = individuo.getArestas(posicao).getNodo();

                    //Setar o nodo da aresta 1 para a aresta 0
                    individuo.getArestas(posicao).setNodo(individuo.getArestas(posicao == 0 ? 1 : 0).getNodo());

                    //Setar o nodo da aresta 0 para a aresta 1
                    individuo.getArestas(posicao == 0 ? 1 : 0).setNodo(temp);


                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE CROSSOVER">       
    /**
     *
     * 1° Passo - Avaliação do Individuo informado - O mesmo deverá ter pelo menos 1 das arestas válidas(com nível maior que 2° Passo - Atualizar a variável
     * passada por parâmetro(declarada antes da chamada do método), atualizada por referência 3° Passo - Setar nulo ao nodo informado
     * -----------------------------------------------------------------------------------------------------------------------
     *
     * @param individuo Indivíduo que sofrerá a mutação
     * @param nivel Deverá ser maior que 1 pois não se pode efetuar mutação individuo nível raiz
     *
     */
    public void RemoverNodoNaOrigem(Arvores individuo, int nivel) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Selecionar uma posição aleatóriamente
            int posicao = 0; // mt.nextInt(individuo.getArestas().size() - 1);

            //Se a primeira aresta não for nula pesquisa pela mesma
            if (individuo.getArestas(posicao).getNodo() != null) {
                //Chamada recursiva da função
                PosicionarMaiorNivelNodoOrigem(individuo.getArestas(posicao).getNodo(), nivel + 1);

            }

        }

    }

    /**
     Incluir o nodo selecionado em algum dos nodos folhas da árvore informada
     -------------------------------------------------------------------------------------------------------------------------
     1° Passo - Percorrer a árvore até o nodo mais profundo, selecionando aleatóriamente a aresta
     2° Passo - Efetua a inclusão do Nodo na Sub-Árvore
     **/
    public void IncluirNodoNoDestino(Arvores individuo, Arvores no) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Selecionar uma posição aleatóriamente (Não importa qual, pois é inclusão de um novo nodo)
            int posicao = 0; //mt.nextInt(individuo.getArestas().size() - 1);

            //Se o Nodo da Aresta não for nula, existem Sub-Árvores
            if (individuo.getArestas(posicao).getNodo() != null) {
                //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                IncluirNodoNoDestino(individuo.getArestas(posicao).getNodo(), no);

            } else {
                //Se a aresta informada for nula insere o nodo e finaliza o ciclo
                individuo.getArestas(posicao).setNodo(no);

            }

        }

    }

    /**
     * Posicionar individuo maior nível da árvore informada e efetuar a troca do material da primeira aresta pela segunda
     *
     * @param individuo - Indivíduo que sofrerá a mutação
     * @param nivel - Deverá ser maior que 1 pois não se pode efetuar mutação individuo nível raiz
     * @param processar - Indicador de processamento (Sim ou Não)
     */
    private void PosicionarMaiorNivelNodoOrigem(Arvores individuo, int nivel) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Selecionar uma posição aleatóriamente
            int posicao = 0; // mt.nextInt(individuo.getArestas().size() - 1);

            //Se a primeira aresta não for nula pesquisa pela mesma
            if (individuo.getArestas(posicao).getNodo() != null) {
                //Chamada recursiva da função
                PosicionarMaiorNivelNodoOrigem(individuo.getArestas(posicao).getNodo(), nivel + 1);

                //Se atingiu o MAIOR OU ÚLTIMO NÍVEL de profundidade E não for o nodo raiz, efetuará a mutação 
                if (nivel == DecisionStumps.profundidade) {
                    //Atribuir valor a Árvore
                    tempNodo = individuo.getArestas(posicao).getNodo();

                    //Setar nulo ao nodo atual
                    individuo.getArestas(posicao).setNodo(null);

                }

            }

        }

    }
    //</editor-fold> 

}
