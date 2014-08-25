package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Definição Atributos e Métodos Construtores da Classe">    
    public static final int profundidade = 2;
    public static final int quantidade = 1;
    public static final double TxCrossover = 0.3;
    private static final int geracoes = 10;
    private double qtdOcorr = 0;
    private List<Arvores> arvores;

    public DecisionStumps() {
    }

    //</editor-fold> 
    //<editor-fold defaultstate="collapsed" desc="Funções destinadas a geração da população">    
    /**
     * Processamento Geral - Todas as Fases do Ciclo até a última geração
     *
     * @param dados - Leitura obtida apartir de um arquivo .arff
     */
    public void ProcessamentoDS(Instances dados) throws Exception {
        try {
            //Declaração Objetos
            int geracao = 1;

            //Inicialização do Objeto
            arvores = new ArrayList<>();

            //Efetuar a Geração da População Inicial
            GeracaoPopulacaoInicial(dados);

            //Laço até o critério de parada ser atingido
            while (geracao < geracoes) {
                //Atualizar o número de Gerações
                geracao++;

                //Cria nova populacao, utilizando o elitismo
                arvores = new Processamento().NovaGeracaoArvores(dados, arvores, true);

                //Definir a qual classe pertencem os nodos folhas
                TreinamentoNodoFolhas(dados);

                //Calculo do Fitness e Ordenação da População após o Cálculo do Fitness
                CalculoFitnessPopulacao(dados);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * Geração da População Inicial Cada Individuo da população será uma Árvore c/ Sub-Árvores - Esta possibilidade será para cada um dos atributos existentes
     *
     * @param dados - Leitura obtida apartir de um arquivo .arff
     * @throws java.lang.Exception
     */
    private void GeracaoPopulacaoInicial(Instances dados) throws Exception {
        try {
            //Percorrer a quantidade de Individuos p/serem gerados
            for (int i = 0; i < quantidade; i++) {
                /*
                 Adicionar o Indivíduo - Árvore Gerada c/ Sub-Arvores, aonde o nível Pai é selecionado aleatóriamente
                
                 Definição dos Parâmetros
                 -------------------------------------------------------------------------------------------------------------------------------------------------------------------
                 1° Parâmetro - Nivel atual de profundidade da Árvore
                 2° Parâmetro - Lista de Nodos Disponíveis para Sorteio
                 3° Parâmetro - Nodo Sorteado p/ ser o Nodo Raiz
                 */
                //Declaração Variáveis e Objetos
                ArrayList<Arvores> temp = new ArrayList<>();

                //Adicionar todos os nodos e arestas respectivamente de cada doa tributos lido do dataset
                temp.addAll(ProcessamentoNodos(dados));

                //Declaração Objetos - Definir o nodo raiz (sorteado aleatóriamente)
                Arvores arv = temp.get(Processamento.mt.nextInt(dados.numAttributes() - 1));

                //Geração da árvore de acordo ATÉ a profundidade estabelecida
                GerarPopulacaoIndividuos(dados, 1, arv);

                //Adicionar a Árvore Gerada
                arvores.add(arv);

            }

            //Definir a qual classe pertencem os nodos folhas
            TreinamentoNodoFolhas(dados);

            //Calculo do Fitness eOrdenação da População
            CalculoFitnessPopulacao(dados);

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * Geração das Populações apartir da População Inicial
     *
     * @param dados - Leitura obtida apartir de um arquivo .arff
     * @param prof - Definir a profundidade máxima da árvore
     * @param arvore - Nodo Raiz p/ geração da população
     */
    public void GerarPopulacaoIndividuos(Instances dados, int prof, Arvores arvore) {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //Declaração Variáveis e Objetos

            //percorrer todas as arestas do indivíduo
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //------------------------------------------------------------------------------------------------------------------------------------------------------------------
                // Comentado pois para fins de teste serã testados todos os níveis da árvore                
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                //
                //if (mt.nextBoolean()) {
                //
                // Comentado pois para fins de teste serão testados todos os níveis da árvore                
                //------------------------------------------------------------------------------------------------------------------------------------------------------------------
                //Declaração variáveis e Objetos
                ArrayList<Arvores> nodos = new ArrayList<>();

                //Adicionar os nodos originais, isto é devido ao java trabalhar APENAS com a referência dos mesmos
                nodos.addAll(ProcessamentoNodos(dados));

                /*
                 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                 */
                arvore.SetNodo(arvore.getArestas(i), nodos.get(Processamento.mt.nextInt(nodos.size())));

                //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                GerarPopulacaoIndividuos(dados, prof + 1, arvore.getArvoreApartirAresta(i));

                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                //
                //} 
                //
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<
                // >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>  AQUI TAMBÉM   <<<<<<<<<<<<<<<<<<                
            }

        }

    }

    private ArrayList<Arvores> ProcessamentoNodos(Instances dados) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> nodos = new ArrayList<>();

        /*
         Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
         ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias
         por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
         */
        for (int i = 0; i < dados.numAttributes(); i++) {
            //1° Passo - Processar todos os Atributos (Binários e Nominais)
            // - 1° Parâmetro - Nome do atributo
            // - 2° Parâmetro - Instâncias e a posição do Atributo
            nodos.add(new Arvores(dados.instance(0).attribute(i).name(),
                    new Processamento().ProcessamentoInstancias(dados, i)));

        }

        //Definir o retorno
        return nodos;

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Funções destinadas a Avaliação da população">    
    /**
     * Definição das classes dos nodos folhas da(s) árvore(s) geradas
     *
     * @param dados - Dataset de dados a serem avaliados(definição do espaço amostral)
     *
     */
    private void TreinamentoNodoFolhas(Instances dados) throws Exception {
        try {
            //Declaração Variáveis e Objetos - Variável "treino" contendo as instâncias p/ Treinamento dos Nodos Folhas
            Instances treino = FormatacaoFonteDados(dados, "T");

            //Percorrer todas as árvores existentes
            for (Arvores arvore : arvores) {
                //Processamento das arestas da árvore selecionada p/ atribuição da classe pertencente
                new Processamento().AtribuicaoClasseNodosFolhas(arvore, treino);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * Efetuar o cálculo do fitness de cada um dos indivíduos (Após o processamento das instâncias(dataset) de validação), onde todas as inscidências das classes foram calculadas
     *
     * @param dados - Dataset de dados a serem avaliados(definição do espaço amostral)
     */
    private void CalculoFitnessPopulacao(Instances dados) throws Exception {
        try {
            //Execução da Validação para atualizar a quantidade de ocorrência a partir da base montada
            ValidacaoNodoFolhasParaCalculoFitness(dados);

            //Percorrer todas as árvres existentes e calcula o fitnes de cada uma delas
            for (Arvores arvore : arvores) {
                //Calcular o Fitness e setar na propriedade o valor
                arvore.setFitness(1 - (arvore.getQtdOcorr() / dados.numInstances()));

            }

            //Ordenar a população EM ORDEM CRESCENTE, por exemplo.: 0.2, 0.3, 0.4,...1.0
            ordenaPopulacao();

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     *
     * @param dados = Instância de Dados a serem processados
     * @param tipo = "T" - Treinamento - 30%, "V" - Validação - 35%, "Z" - Teste - 35%
     *
     */
    private Instances FormatacaoFonteDados(Instances dados, String tipo) throws Exception {
        //Declaração Variáveis e Objetos
        int iTreinamento = (int) ((int) dados.numInstances() * 0.3);
        int iValidacao = iTreinamento + (int) ((int) dados.numInstances() * 0.35);
        Instances regs = new Instances(dados, 0);

        //"T" - Treinamento - 30% 
        switch (tipo) {
            case "T":
                //"T" - Treinamento - 30%  - Alimentar o classificador
                for (int t = 0; t < iTreinamento; t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

            case "V":
                //"V" - Validação - 35%  - Alimentar o classificador
                for (int t = iTreinamento; t < iValidacao; t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

            case "Z":
                //"Z" - Teste - 35%  - Alimentar o classificador
                for (int t = iValidacao; t < dados.numInstances() - 1; t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

        }

        //Definir o retorno dos dados
        return regs;

    }

    /**
     * Vaidação dos dados para o cálculo do fitness da população
     */
    private void ValidacaoNodoFolhasParaCalculoFitness(Instances dados) throws Exception {
        try {
            //Declaração Variáveis e Objetos  - Variável "validacao" contendo as instâncias p/ Validação e Cálculo do Fitness da árvore
            Instances validacao = FormatacaoFonteDados(dados, "V");

            //Percorrer todas as árores existentes e Atualiza a quantidade de ocorrências
            for (Arvores arvore : arvores) {
                //Inicializações
                InicializarQtdOcorrencias();

                //Percorrer todas as instâncias para avaliação da árvore selecionada
                for (int i = 0; i < validacao.numInstances() - 1; i++) {
                    //Percorrer todos os atributos da instância
                    for (int j = 0; j < validacao.instance(i).numAttributes() - 1; j++) {
                        /**
                         * OBSERVAÇÃO.: O for é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
                         * somente pelo índice do atributo e NÃO PELO NOME DO MESMO
                         * ---------------------------------------------------------------------------------------------------------------------------------------------------------
                         */
                        //Se o nome do Atributo for igual ao da instância selecionada entra no próximo nível senão vai p/ a próxima instância,
                        //devido a possibilidade de ter-se atributos que não existem na seleção
                        if (validacao.instance(i).attribute(j).name().equals(arvore.getNomeAtr())) {
                            //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                            //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness do(s) indivíduo(s)
                            ValidacaoParaCalculoFitnessPopulacao(arvore, validacao);

                            //Se já processou o atributo sai fora do for
                            break;

                        }

                    }

                }

                //Atualizar a Quantidade de ocorrências
                arvore.setFitness(qtdOcorrencias());

            }

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     *
     * @param arv = Qual o indivíduo a ser avaliado
     * @param avaliacao = População que servirá de Amostra p/ Avaliação
     */
    public void ValidacaoParaCalculoFitnessPopulacao(Arvores arv, Instances avaliacao) {
        //Se o nó não for nulo
        if (arv != null) {
            //Percorrer todas as arestas do nodo selecionado
            for (int i = 0; i < arv.getArestas().size(); i++) {
                //Se a aresta selecionada não for nula pesquisa pela mesma
                if (arv.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    ValidacaoParaCalculoFitnessPopulacao(arv.getArestas(i).getNodo(), avaliacao);

                } else //Chegou em um nodo folha
                {
                    //Percorrer as amostras de avaliação e todas as suas arestas, SE ENCONTRAR ALGUMA ARESTA IGUAL atribui a classe a aresta atual
                    for (int j = 0; j < avaliacao.numInstances() - 1; j++) {
                        /**
                         * OBSERVAÇÃO.: O for é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
                         * somente pelo índice do atributo e NÃO PELO NOME DO MESMO
                         * ---------------------------------------------------------------------------------------------------------------------------------------------------------
                         */
                        //Percorrer todos os atributos da instância selecionada
                        for (int k = 0; k < avaliacao.instance(j).numAttributes() - 1; k++) {
                            //Se o nome do Atributo Classe for igual ao nome do atributo selecionado
                            if (avaliacao.instance(j).attribute(k).name().equals(arv.getNomeAtr())) {
                                //SE o atributo for "Numérico" SENÃO o atributo será "Nominal"
                                if (avaliacao.instance(j).attribute(j).isNumeric()) {
                                    //Se o VALOR DO ATRIBUTO FOR IGUAL AO VALOR DO ATRIBUTO da instância selecionada
                                    if (Double.valueOf(arv.getArestas(i).getAtributo()).equals(avaliacao.instance(j).classValue())) {
                                        //Atualizar a quantidade de ocorrências em 1 para Cálculo do Fitness
                                        AtualizarQtdOcorr(1);

                                    }

                                } else {
                                    //Percorrer todos as arestas do atributo
                                    for (int l = 0; l < avaliacao.instance(j).numValues(); l++) {
                                        //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                        if (avaliacao.instance(j).attribute(j).value(l).equals(arv.getArestas(i).getAtributo())) {
                                            //Atualizar a quantidade de ocorrências em 1 para Cálculo do Fitness
                                            AtualizarQtdOcorr(1);

                                        }

                                    }

                                }
                                //Se PROCESSOU o atributo sai fora do laço
                                break;

                            }

                        }

                    }

                }

            }

        }

    }

    private void InicializarQtdOcorrencias() {
        //Inicializar a variável
        this.qtdOcorr = 0;

    }

    private double qtdOcorrencias() {
        //Definir o retorno
        return this.qtdOcorr;

    }

    private void AtualizarQtdOcorr(int qtd) {
        //Atualizar a quantidade de ocorrências
        this.qtdOcorr += qtd;

    }

    //</editor-fold> 
    //<editor-fold defaultstate="collapsed" desc="Funções de Ordenação crescente da população - Critério de avaliação - Fitness">    
    public void ordenaPopulacao() {
        //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
        Collections.sort(arvores);

    }
    //</editor-fold> 

}
