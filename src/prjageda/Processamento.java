package prjageda;

import java.util.ArrayList;
import java.util.List;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="ATRIBUTOS E MÉTODO CONSTRUTOR DA CLASSE">    
    private String caminhoDados;

    //Declaração de objetos
    public static final MersenneTwister mt = new MersenneTwister();
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
                /*
                 //double valor = dados.instance(i).classValue();
                 for (int j = 0; j < dados.numInstances(); j++) {
                 //Atualizar a quantidade
                 qtdRegs += dados.instance(j).value(posicao) == pos ? 1 : 0;

                 }*/
                //Adicionar o Registro
                registros.add(new Atributos(String.valueOf(pos), null, ""));

            }

        } else {
            //Percorrer a Quantidade de Atributos existentes
            for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                //Adicionar o nodo                
                registros.add(new Atributos(dados.attribute(posicao).value(i), null, ""));

            }

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, List<Arvores> arvores, boolean elitismo) {
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
            List<Arvores> pais = SelecaoPorTorneio(arvores);

            //Declaração de variáveis e objetos
            ArrayList<Arvores> filhos = new ArrayList<>();

            //Adicionar os 2 indivíduos que sofreram a mutação
            filhos.addAll(MutacaoIndividuo(arvores));

            //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (mt.nextDouble() <= DecisionStumps.TxCrossover) {
                //Adicionar os 2 filhos que sofreram Crossover
                filhos = CrossoverIndividuo(pais.get(0), pais.get(1));

            } else {
                //Calcular o Fitness de cada um dos indivíduos

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
    
    private List<Arvores> SelecaoPorTorneio(List<Arvores> arvores) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //seleciona 3 indivíduos aleatóriamente na população
        for (int i = 0; i < qtdAmostras; i++) {
            //Selecionar os 10 indivíduos aleatórios DENTRO da população gerada aleatóriamente no inicio
            selecao.add(arvores.get(mt.nextInt(arvores.size())));

        }

        //Ordenar as árvores selecionadas aleatóriamente
        

        //Definir o retorno
        return selecao;

    }

    private ArrayList<Arvores> CrossoverIndividuo(Arvores origem, Arvores destino) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Remover o nodo da árvore origem setando nulo ao mesmo
        RemoverNodoNaOrigem(origem, 1);

        //Se não for nulo o nodo selecionado
        if (tempNodo != null) {
            //Incluir o nodo processado na árvore de destino
            IncluirNodoNoDestino(destino, tempNodo);

            //Adicionar as duas árvores
            populacao.add(destino);

        }

        //Definir o retorno
        return populacao;

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE MUTAÇÃO">   
    private ArrayList<Arvores> MutacaoIndividuo(List<Arvores> populacao) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> individuos = new ArrayList<>();
        Arvores indiv1 = populacao.get(mt.nextInt(populacao.size()));
        Arvores indiv2 = populacao.get(mt.nextInt(populacao.size()));

        /*
         Detalhamento da Mutação
         ---------------------------------------------------------------------------------------------------------------------
         1 Passo - Percorrer o indivíduo até o seu maior nível a partir do nó raiz
         2 Passo - Move o Ramo selecionado para o ramo próximo e transforma o ramo atual em ramo folha
         */
        PosicionarMaiorNivel(indiv1, 1);
        PosicionarMaiorNivel(indiv2, 1);
        
        //Adicionar os indivíduos
        individuos.add(indiv1);
        individuos.add(indiv2);

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

                //Se atingiu o MAIOR NÍVEL de profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo)
                //if (nivel == DecisionStumps.profundidade) {
                if (individuo.getArestas(posicao).getNodo().getArestas(0).getNodo() == null) {
                    //Declaração Variáveis e Objetos
                    ArrayList<Atributos> arestas = new ArrayList<>();
                    Arvores temp = individuo.getArestas(posicao).getNodo();

                    //Adicionar as arestas Trocando de Posição (Aresta 0 -> Aresta 1 E Aresta 1 -> Aresta 0) - EFETIVAR A MUTAÇÃO
                    arestas.add(new Atributos(temp.getArestas(posicao == 0 ? 1 : 0).getAtributo(),
                            temp.getArestas(posicao == 0 ? 1 : 0).getNodo(), ""));

                    //Adicionar a aresta que estava na primeira posição
                    arestas.add(new Atributos(temp.getArestas(posicao).getAtributo(),
                            temp.getArestas(posicao).getNodo(), ""));

                    //Setar o "NOVO" nodo com as arestas mutadas
                    individuo.getArestas(posicao).getNodo().setArestas(arestas);

                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE CROSSOVER">       
    /**
     *
     * 1° Passo - Avaliação do Individuo informado - O mesmo deverá ter pelo menos 1 das arestas válidas(com nível maior que
     * 2° Passo - Atualizar a variável passada por parâmetro(declarada antes da chamada do método), atualizada por referência
     * 3° Passo - Setar nulo ao nodo informado
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
     * Incluir o nodo selecionado em algum dos nodos folhas da árvore informada
     * -------------------------------------------------------------------------------------------------------------------------
     * 1° Passo - Percorrer a árvore até o nodo mais profundo, selecionando aleatóriamente a aresta 2° Passo - Efetua a
     * inclusão do Nodo na Sub-Árvore
     *
     * @param individuo -Individuo que sofrerá o Crossover
     * @param no - Nodo a ser incluso no indvíduo
     */
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
                //Chamada recursiva da função até atinbgir o último nível
                PosicionarMaiorNivelNodoOrigem(individuo.getArestas(posicao).getNodo(), nivel + 1);

                //Se atingiu o MAIOR OU ÚLTIMO NÍVEL de profundidade E não for o nodo raiz, efetuará a mutação 
                //if (nivel == DecisionStumps.profundidade) {
                if (individuo.getArestas(posicao).getNodo() == null) {
                    //Atribuir valor a Árvore
                    tempNodo = individuo.getArestas(posicao).getNodo();

                    //Setar nulo ao nodo atual
                    individuo.getArestas(posicao).setNodo(null);

                }

            }

        }

    }

    public void AtribuicaoClasseNodoFolha(Arvores individuo, Instances avaliacao) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Percorrer todas as arestas do nodo selecionado
            for (int i = 0; i < individuo.getArestas().size(); i++) {
                //Se a aresta selecionada não for nula pesquisa pela mesma
                if (individuo.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    AtribuicaoClasseNodoFolha(individuo.getArestas(i).getNodo(), avaliacao);

                } else //Chegou em um nodo folha
                {
                    //Percorrer as amostras de avaliação e todas as suas arestas, SE ENCONTRAR ALGUMA ARESTA IGUAL                   
                    //atribui a classe a aresta atual
                    for (int j = 0; j < avaliacao.numInstances(); j++) {
                        //Se o nome do Atributo Classe for igual ao nome do atributo selecionado
                        if (individuo.getNomeAtr().equals(avaliacao.instance(j).attribute(j).name())) {
                            /*
                             Se o tipo do atributo for numérico SENÃO será Nominal
                             */
                            if (avaliacao.instance(j).attribute(j).isNumeric()) {
                                //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                if (Double.valueOf(individuo.getArestas(i).getAtributo()).equals(
                                        avaliacao.instance(j).classValue())) {
                                    //Atribuir a classe a qual o nodo folha pertence
                                    individuo.getArestas(i).setClasse(avaliacao.instance(j).classAttribute().value(
                                            (int) avaliacao.instance(j).classValue()));

                                }

                            } else {
                                //Percorrer todos as arestas do atributo
                                for (int k = 0; k < avaliacao.instance(j).numValues(); k++) {
                                    //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                    if (individuo.getArestas(i).getAtributo().equals(avaliacao.instance(j).attribute(j).value(k))) {
                                        //Atribuir a classe a qual o nodo folha pertence
                                        individuo.getArestas(i).setClasse(avaliacao.instance(j).classAttribute().name());
                                        
                                    }

                                }
                                //Atualizar a quantidade de ocorrências em 1 para calculo do Fitness
                                //individuo.AtualizarQtdOcorr(1);

                            }
                            //Se já processou o atributo sai fora
                            break;

                        }

                    }

                }

            }
            //</editor-fold> 
        }
    }
}
