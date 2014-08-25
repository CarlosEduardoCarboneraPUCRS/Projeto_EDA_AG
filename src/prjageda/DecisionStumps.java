package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Atributos da classe e Métodos Construtores da classe">    
    public static final int profundidade = 2;
    public static final int quantidade = 1;
    public static final double TxCrossover = 0.3;
    private static final int geracoes = 10;
    //private static final int nroFolds = 10;
    private List<Arvores> arvores;

    public DecisionStumps() {
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Métodos de Processamento">
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
                AvaliacaoTreinamentoNodoFolhas(dados);

                //Calculo do Fitness eOrdenação da População
                CalculoFitnessPopulacao(dados);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    //<editor-fold defaultstate="collapsed" desc="Função Comentada de Recursidade para geração dos Indivíduos">    
    public void GerarPopulacaoIndividuos(Instances dados, int prof, Arvores arvore) {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //Declaração Variáveis e Objetos

            //percorrer todas as arestas do indivíduo
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //
                //
                //----------------------------------------------------------------------------------------------------------------------------------------------
                // Comentado pois para fins de teste serã testados todos os níveis da árvore                
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                //
                //
                //if (mt.nextBoolean()) {
                // Comentado pois para fins de teste serã testados todos os níveis da árvore                
                //----------------------------------------------------------------------------------------------------------------------------------------------
                //
                //
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

                //}
            }

        }

    }

    private ArrayList<Arvores> ProcessamentoNodos(Instances dados) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> nodos = new ArrayList<>();

        /*
         Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
         ---------------------------------------------------------------------------------------------------------------------------------------------------
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

    /**
     * Geração da População Inicial
     * --------------------------------------------------------------------------------------------------------------------------------------------------- -
     * Cada Individuo da população será uma Árvore c/ Sub-Árvores - Esta possibilidade será para cada um dos atributos existentes
     *
     */
    private void GeracaoPopulacaoInicial(Instances dados) throws Exception {
        try {
            //Percorrer a quantidade de Individuos p/serem gerados
            for (int i = 0; i < quantidade; i++) {
                /*
                 Adicionar o Indivíduo - Árvore Gerada c/ Sub-Arvores, aonde o nível Pai é selecionado aleatóriamente
                
                 Definição dos Parâmetros
                 -----------------------------------------------------------------------------------------------------------------------------------------------
                 1° Parâmetro - Nivel atual de profundidade da Árvore
                 2° Parâmetro - Lista de Nodos Disponíveis para Sorteio
                 3° Parâmetro - Nodo Sorteado p/ ser o Nodo Raiz
                 */
                //Declaração Variáveis e Objetos
                ArrayList<Arvores> temp = new ArrayList<>();

                //Adicionar todos os nodos processados
                temp.addAll(ProcessamentoNodos(dados));

                //Declaração Objetos - Definir o nodo raiz
                Arvores item = temp.get(Processamento.mt.nextInt(dados.numAttributes() - 1));

                //Chamada da função para a geração dos indivíduos
                GerarPopulacaoIndividuos(dados, 1, item);

                //Adicionar o Indivíduo novo
                arvores.add(item);

            }

            //Definir a qual classe pertencem os nodos folhas
            AvaliacaoTreinamentoNodoFolhas(dados);

            //Calculo do Fitness eOrdenação da População
            CalculoFitnessPopulacao(dados);

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * Ordena a população pelo valor de aptidão de cada indivíduo, do maior valor para o menor, assim se eu quiser obter o melhor indivíduo desta população,
     * acesso a posição 0 do array de indivíduos
     */
    public void ordenaPopulacao() {
        //Ordernar os registros crescente
        Collections.sort(arvores);
    }

    //Efetuar o calculo do fitness de cada um dos indivíduos
    private void AvaliacaoTreinamentoNodoFolhas(Instances dados) throws Exception {
        try {
            //Declaração Variáveis e Objetos  - Variável "avaliacao" contendo as instâncias p/ Classificação dos Nodos Folhas
            Instances avaliacao = FormatacaoFonteDados(dados, "A");

            //Percorrer todas as árores existentes
            for (Arvores arvore : arvores) {
                //Percorrer todas as instâncias para avaliação da árvore selecionada
                for (int i = 0; i < avaliacao.numInstances() - 1; i++) {
                    //Percorrer todos os atributos da instancia selecionada no momento
                    for (int j = 0; j < avaliacao.instance(i).numAttributes() - 1; j++) {
                        //Se o nome do Atributo Raiz for igual ao atributo avaliado, entra no próximo nível senão vai pra a próxima árvore
                        if (arvore.getNomeAtr().equals(avaliacao.instance(i).attribute(j).name())) {
                            //Entra no nivel
                            new Processamento().AtribuicaoClasseNodoFolha(arvore, avaliacao);

                            //Se já processou o atributo sai fora do for
                            break;

                        }

                    }

                }

            }

        } catch (Exception e) {
            throw e;

        }

    }

    //Efetuar o calculo do fitness de cada um dos indivíduos
    private void CalculoFitnessPopulacao(Instances dados) throws Exception {
        try {
            //Execução da Validação para atualizar a quantidade de ocorrência a partir da base montada
            ValidacaoNodoFolhasParaCalculoFitness(dados);

            //Percorrer todas as árvres existentes e calcula o fitnes de cada uma delas
            for (Arvores arvore : arvores) {
                //Calcular o Fitness e setar na propriedade o valor
                arvore.setFitness(1 - (arvore.getQtdOcorr() / dados.numInstances()));

            }

            //Ordenar a população EM ORDEM DECRESCENTE
            ordenaPopulacao();

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * @param dados = Instância de Dados a serem processados
     * @param tipo = Qual o tipo - "A" - Avaliação(Teste) - 30%, "V" - Validação - 35%, "T" - Treinamento - 35%
     *
     */
    private Instances FormatacaoFonteDados(Instances dados, String tipo) throws Exception {
        //Declaração Variáveis e Objetos
        int iTeste = (int) ((int) dados.numInstances() * 0.3);
        int iValidacao = iTeste + (int) ((int) dados.numInstances() * 0.35);
        Instances regs = new Instances(dados, 0);

        //"A" - Avaliação(Teste) - 30% 
        switch (tipo) {
            case "A":
                //Alimentar a classificador de Teste "A" - Avaliação(Teste) - 30% 
                for (int t = 0; t < iTeste; t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

            case "V": //Alimentar a classificador de Validação - "V" - Validação - 35% 
                for (int t = iTeste; t < iValidacao; t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

            case "T": //Alimentar a classificador de Treinamento - "T" - Treinamento - 35% 
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
            //Declaração Variáveis e Objetos  - Variável "Treinamento" contendo as instâncias p/ Cálculo do Fitness
            Instances validacao = FormatacaoFonteDados(dados, "T");

            //Percorrer todas as árores existentes e Atualiza a quantidade de ocorrências
            for (Arvores arvore : arvores) {
                //Percorrer todas as instâncias para avaliação da árvore selecionada
                for (int i = 0; i < validacao.numInstances() - 1; i++) {
                    //Percorrer todos os atributos da instância
                    for (int j = 0; j < validacao.instance(i).numAttributes() - 1; j++) {
                        //Se o nome do Atributo for igual ao da instância selecionada entra no próximo nível senão vai p/ a próxima instância,
                        //devido a possibilidade de ter-se atributos que não existem na seleção
                        if (arvore.getNomeAtr().equals(validacao.instance(i).attribute(j).name())) {
                            //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                            //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness do(s) indivíduo(s)
                            ValidacaoParaCalculoFitnessPopulacao(arvore, validacao);

                            //Se já processou o atributo sai fora do for
                            break;

                        }
                        
                    }

                }

            }

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     *
     * @param individuo = Qual o indivíduo a ser avaliado
     * @param avaliacao = População que servirá de Amostra p/ Avaliação
     */
    public void ValidacaoParaCalculoFitnessPopulacao(Arvores individuo, Instances avaliacao) {
        //Se o nó não for nulo
        if (individuo != null) {
            //Percorrer todas as arestas do nodo selecionado
            for (int i = 0; i < individuo.getArestas().size(); i++) {
                //Se a aresta selecionada não for nula pesquisa pela mesma
                if (individuo.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    ValidacaoParaCalculoFitnessPopulacao(individuo.getArestas(i).getNodo(), avaliacao);

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
                                if (Double.valueOf(individuo.getArestas(i).getAtributo()).equals(avaliacao.instance(j).classValue())) {
                                    //Atualizar a quantidade de ocorrências em 1 para calculo do Fitness
                                    individuo.AtualizarQtdOcorr(1);

                                }

                            } else {
                                //Percorrer todos as arestas do atributo
                                for (int k = 0; k < avaliacao.instance(j).numValues(); k++) {
                                    //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                    if (individuo.getArestas(i).getAtributo().equals(avaliacao.instance(j).attribute(j).value(k))) {
                                        //Atualizar a quantidade de ocorrências em 1 para calculo do Fitness
                                        individuo.AtualizarQtdOcorr(1);

                                    }

                                }

                            }
                            //Se já processou o atributo sai fora
                            break;

                        }

                    }

                }

            }
        }

    }
    //</editor-fold> 
}
