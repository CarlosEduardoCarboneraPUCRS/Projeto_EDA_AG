package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    public static final int quantidade = 10;
    public static final int profundidade = 2;
    public static final double TxCrossover = 0.9;
    private static final int geracoes = 10;
    private static final int nroFolds = 3;
    private List<Arvores> arvores;

    private int qtdOcorr = 0;

    public DecisionStumps() {
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="2° Métodos Inicializadores da classe e Get´s E Set´s">    
    private void ZerarQtdOcorrencias() {
        //Zerar a quantidade
        this.qtdOcorr = 0;

    }

    private int getQtdOcorrencias() {
        //Retornar a quantidade
        return this.qtdOcorr;

    }

    private void AtualizarQtdOcorrencias(int quantidade) {
        //Atualizar a quantidade de ocorrências
        this.qtdOcorr += quantidade;

    }

    //</editor-fold> 
    //<editor-fold defaultstate="collapsed" desc="3° Definição dos Métodos pertinentes a Geração da População">
    //Tradução da Sigla - AlGenArDe - "Al"goritmo "Ge"nético de "Ar"vore de "De"cisão
    public void AlGenArDe(Instances dados) {
        try {
            //Declaração Variáveis e Objetos
            Instances treino = FormatacaoFonteDados(dados, "T");
            Instances validacao = FormatacaoFonteDados(dados, "V");
            arvores = new ArrayList<>();
            int geracao = 1;

            //Efetuar a Geração da População Inicial
            GeracaoPopulacaoInicial(dados, treino, validacao);

            //Efetuar a geração das novas populações
            while (geracao < geracoes) {
                //Atualizar a Geração
                geracao++;

                //Gerar a nova populacao, utilizando o elitismo (2 indivíduos)
                arvores = new Processamento().NovaGeracaoArvores(dados, arvores, true);

                //Calcular o Fitness e após Ordenar Crescentemente
                CalculoFitnessPopulacao(treino, validacao);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    private void GeracaoPopulacaoInicial(Instances dados, Instances treino, Instances validacao) throws Exception {
        try {
            //Percorrer a quantidade de árvores informado
            for (int i = 0; i < quantidade; i++) {
                //Gerar cada uma das árvores e as arestas respectivamente (Sendo 1 Árvore X Atributo)
                ArrayList<Arvores> temp = ProcessamentoNodos(dados);

                //Selecionar o nodo raiz (sorteado aleatóriamente)
                Arvores arv = temp.get(Processamento.mt.nextInt(dados.numAttributes() - 1));

                //Geração da árvore ATÉ a profundidade estabelecida
                GerarPopulacaoArvores(dados, 1, arv);

                //Adicionar a Árvore Gerada
                arvores.add(arv);

            }

            //Calcular o Fitness e após Ordenar Crescentemente
            CalculoFitnessPopulacao(treino, validacao);

        } catch (Exception e) {
            throw e;

        }

    }

    //Efetuar a Geração da População de Árvores de Decisão
    public void GerarPopulacaoArvores(Instances dados, int prof, Arvores arvore) {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //percorrer todas as arestas do árvore
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                if (Processamento.mt.nextBoolean()) {
                    //Declaração Variáveis e Objetos
                    ArrayList<Arvores> nodos = new ArrayList<>();

                    //Adicionar as árvores originais, devido ao java trabalhar APENAS com a referência dos objetos, ai a cada geração deve-se RECARREGAR as mesmas
                    nodos.addAll(ProcessamentoNodos(dados));

                    // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                    // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                    arvore.SetNodo(arvore.getArestas(i), nodos.get(Processamento.mt.nextInt(nodos.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                    GerarPopulacaoArvores(dados, prof + 1, arvore.getArvoreApartirAresta(i));

                }
                
            }

        }

    }

    public ArrayList<Arvores> ProcessamentoNodos(Instances dados) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> nodos = new ArrayList<>();

        /*
         Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
         ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias
         por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
         */
        for (int i = 0; i < dados.numAttributes(); i++) {
            //1° Passo     - Processar todos os Atributos (Binários e Nominais)
            //2° Parâmetro - Nome do atributo
            //3° Parâmetro - Instâncias e a posição do Atributo
            nodos.add(new Arvores(dados.instance(0).attribute(i).name(), new Processamento().ProcessamentoInstancias(dados, i)));

        }

        //Definir o retorno
        return nodos;

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="4° Definição dos Métodos e Funções Destinadas a Avaliação da População">    
    private void TreinamentoNodosFolhas(Instances treino) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            Processamento proc = new Processamento();

            //Percorrer todas as árvores existentes para atribuição das classes e quantidades dos nodos folhas
            for (Arvores arvore : arvores) {
                //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int i = 0; i < treino.numInstances(); i++) {
                    //Atualizar(Calcular) a quantidade de ocorrências dos atributos na árvore
                    proc.AtribuicaoClasseNodosFolhas(arvore, treino.instance(i));

                }

                //Definição da Classe majoritária de cada um dos nodos "Folhas" da árvore
                proc.DefinicaoClasseMajoritariaNodosFolhas(arvore);

            }

        } catch (Exception e) {
            throw e;

        }

    }

    private void CalculoFitnessPopulacao(Instances treino, Instances validacao) throws Exception {
        try {
            //Efetuar o treinamento - Definir quais classes pertencem os nodos folhas
            TreinamentoNodosFolhas(treino);

            //Execução da Validação para atualizar a quantidade de ocorrência a partir da base montada
            ValidacaoNodoFolhasParaCalculoFitness(validacao);

            //Percorrer todas as árvres existentes e calcula o fitnes de cada uma delas
            for (Arvores arvore : arvores) {
                //Calcular E Setar o Valor do Fitness
                arvore.setFitness(new Processamento().arredondar(1 - ((double) arvore.getQtdOcorrencias() / validacao.numInstances()), 4, 1));

            }

            //Ordenar a população EM ORDEM CRESCENTE, por exemplo.: 0.2, 0.3, 0.4,...1.0
            ordenaPopulacao();

        } catch (Exception e) {
            throw e;

        }

    }

    private Instances FormatacaoFonteDados(Instances dados, String tipo) throws Exception {
        //Declaração Variáveis e Objetos
        Instances regs = new Instances(dados, 0);

        //Estratificar os dados e divisão em 3 folds
        dados.stratify(nroFolds);

        switch (tipo) {
            case "T":
                //"T" - Treinamento - 30% 
                regs = dados.testCV(nroFolds, 0);
                break;

            case "V":
                //"V" - Validação - 35%
                regs = dados.trainCV(nroFolds, 1);
                break;

            //"Z" - Teste - 35%
            case "Z":
                regs = dados.trainCV(nroFolds, 2);
                break;

        }

        //Definir o retorno
        return regs;

    }

    private void ValidacaoNodoFolhasParaCalculoFitness(Instances validacao) throws Exception {
        try {
            //Percorrer todas as árores existentes e Atualiza a quantidade de ocorrências
            for (Arvores arv : arvores) {
                //Zerar a quantidade de Ocorrências
                ZerarQtdOcorrencias();

                //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int i = 0; i < validacao.numInstances(); i++) {
                    //Atualizar(Calcular) a quantidade de ocorrências dos atributos na árvore
                    ValidacaoCalculoFitnessGeracao(arv, validacao.instance(i));

                }

                //Atualizar a Quantidade de ocorrências
                arv.setQtdOcorrencias(getQtdOcorrencias());

            }

        } catch (Exception e) {
            throw e;

        }

    }

    public void ValidacaoCalculoFitnessGeracao(Arvores arvore, Instance avaliacao) {
        //Se o árvore não for nula
        if (arvore != null) {
            //OBSERVAÇÃO.: O for é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
            //somente pelo índice do atributo e NÃO PELO NOME DO MESMO
            //----------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //Percorrer todos os atributos da instância selecionada
            for (int k = 0; k < avaliacao.numAttributes() - 1; k++) {
                //Encontrou o mesmo atributos que o pesquisado
                if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    //Se o atributo for Numérico
                    if (avaliacao.attribute(k).isNumeric()) {
                        //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                        int pos = new Processamento().arredondar(avaliacao.value(k), 4, 1) <= Double.valueOf(arvore.getArestas(0).getAtributo()) ? 0 : 1;

                        //Se for um nodo folha
                        if (arvore.getArestas(pos).getNodo() == null) {
                            //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                            if (arvore.getArestas(pos).getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                //Atualizar a quantidade (Somar 1 na quantidade atual)
                                AtualizarQtdOcorrencias(1);

                            }

                        } else {
                            //Chamada recursiva da função passando como parâmetros a aresta selecionada
                            ValidacaoCalculoFitnessGeracao(arvore.getArestas(pos).getNodo(), avaliacao);

                        }

                    } else {
                        //Percorrer todas as arestas
                        for (Atributos aresta : arvore.getArestas()) {
                            //Se for um nodo folha
                            if (aresta.getNodo() == null) {
                                //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                                if (aresta.getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                    //Atualizar a quantidade (Somar 1 na quantidade atual)
                                    AtualizarQtdOcorrencias(1);

                                }

                            } else {
                                //Chamada recursiva da função passando como parâmetros a aresta selecionada
                                ValidacaoCalculoFitnessGeracao(aresta.getNodo(), avaliacao);

                            }

                        }

                    }

                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="5° Ordenação População em Ordem Crescente - Avaliados p/ Fitness">    
    public void ordenaPopulacao() {
        //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
        Collections.sort(arvores);

    }
    //</editor-fold> 

}
