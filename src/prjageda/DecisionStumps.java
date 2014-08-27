package prjageda;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;

public class DecisionStumps {

    //<editor-fold defaultstate="collapsed" desc="Definição Atributos e Métodos Construtores da Classe">    
    public static final int profundidade = 2;
    public static final int quantidade = 1;
    public static final double TxCrossover = 0.9;
    private static final int geracoes = 10;
    private int qtdOcorr = 0;
    private List<Arvores> arvores;
    //private static final int nroFolds = 10;

    public DecisionStumps() {
    }

    private void ZerarQtdOcorr() {
        //Setar a propriedade
        this.qtdOcorr = 0;

    }

    private int qtdOcorrencias() {
        //Definir o retorno
        return this.qtdOcorr;

    }

    private void AtualizarQtdOcorr(int qtd) {
        //Atualizar a quantidade de ocorrências
        this.qtdOcorr += qtd;

    }

    //</editor-fold> 
    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas a Geração da População">    
    /**
     * Processamento Geral - Todas as Fases do Ciclo até a última geração
     *
     * @param dados - Leitura obtida apartir de um arquivo .arff
     */
    public void ProcessamentoDS(Instances dados) {
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

                //Calculo do Fitness e Ordenação da População após o Cálculo do Fitness
                CalculoFitnessPopulacao(dados);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

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

                //Adicionar todos os nodos e arestas respectivamente de cada da tributos do dataset
                temp.addAll(ProcessamentoNodos(dados));

                //Declaração Objetos - Definir o nodo raiz (sorteado aleatóriamente)
                Arvores arv = temp.get(Processamento.mt.nextInt(dados.numAttributes() - 1));

                //Geração da árvore ATÉ a profundidade estabelecida
                GerarPopulacaoArvores(dados, 1, arv);

                //Adicionar a Árvore Gerada
                arvores.add(arv);

            }

            //Cálculo do Fitness (Utiliza as instâncias de dados para Teste e Validação) e Ordenação da População
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
    public void GerarPopulacaoArvores(Instances dados, int prof, Arvores arvore) {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //percorrer todas as arestas do árvore
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //if (Processamento.mt.nextBoolean()) {
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Aqui descomentar  --------------------------------------------------IRÀ GERAR TODOS OS NODOS
                //Declaração Variáveis e Objetos
                ArrayList<Arvores> nodos = new ArrayList<>();

                //Adicionar os nodos originais, isto é devido ao java trabalhar APENAS com a referência dos mesmos
                nodos.addAll(ProcessamentoNodos(dados));

                // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                arvore.SetNodo(arvore.getArestas(i), nodos.get(Processamento.mt.nextInt(nodos.size())));

                //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                GerarPopulacaoArvores(dados, prof + 1, arvore.getArvoreApartirAresta(i));

                //}
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

    //<editor-fold defaultstate="collapsed" desc="Funções Destinadas a Avaliação da População">    
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

    /**
     * Efetuar o cálculo do fitness de cada um dos árvores (Após o processamento das instâncias(dataset) de validação), onde todas as inscidências das classes foram calculadas
     *
     * @param dados - Dataset de dados a serem avaliados(definição do espaço amostral)
     */
    private void CalculoFitnessPopulacao(Instances dados) throws Exception {
        try {
            //Efetuar o treinamento - Definir a qual classe pertencem os nodos folhas
            TreinamentoNodoFolhas(dados);

            //Declaração Variáveis e Objetos - "validacao" contendo as instâncias p/ Validação e Cálculo do Fitness da Árvore
            Instances validacao = FormatacaoFonteDados(dados, "V");

            //Execução da Validação para atualizar a quantidade de ocorrência a partir da base montada
            ValidacaoNodoFolhasParaCalculoFitness(validacao);

            //Percorrer todas as árvres existentes e calcula o fitnes de cada uma delas
            for (Arvores arvore : arvores) {
                //Calcular E Setar o Valor do Fitness
                arvore.setFitness(1 - ((double) arvore.getQtdOcorr() / validacao.numInstances()));

            }

            //Ordenar a população EM ORDEM CRESCENTE, por exemplo.: 0.2, 0.3, 0.4,...1.0
            ordenaPopulacao();

        } catch (Exception e) {
            throw e;

        }

    }

    /**
     * @param dados = Instância de Dados a serem processados
     * @param tipo = "T" - Treinamento - 30%, "V" - Validação - 35%, "Z" - Teste - 35%
     */
    private Instances FormatacaoFonteDados(Instances dados, String tipo) throws Exception {
        //Declaração Variáveis e Objetos
        int iTreinamento = (int) ((int) dados.numInstances() * 0.3);
        int iValidacao = iTreinamento + (int) ((int) dados.numInstances() * 0.35);
        Instances regs = new Instances(dados, 0);

        
        //---Remover esta linha
        //---Remover esta linha
        //---Remover esta linha
        //---Remover esta linha
        iTreinamento = 10;
        //---Remover esta linha
        //---Remover esta linha
        //---Remover esta linha
        //---Remover esta linha
        
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
                for (int t = iValidacao; t < dados.numInstances(); t++) {
                    regs.add(dados.instance(Processamento.mt.nextInt(dados.numInstances() - 1)));

                }
                break;

        }

        //Definir o retorno
        return regs;

        /*  
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder        
         //Estratificação dos dados
         regs.stratify(nroFolds);

         //Declaração Variáveis e Objetos
         Instances proc = new Instances(dados, 0);

         //Percorrer o Nro de Folds informado
         for (int n = 0; n < nroFolds; n++) {
         //Declaração Variáveis e Objetos
         Instances treinamento = regs.trainCV(nroFolds, n);
         Instances teste = regs.testCV(nroFolds, n);

         switch (tipo) {
         case "T":
         for (int i = 0; i < teste.numInstances(); i++) {
         proc.add(teste.instance(i));

         }
         break;

         case "V":
         for (int i = 0; i < treinamento.numInstances(); i++) {
         proc.add(treinamento.instance(i));

         }
         break;

         case "Z":
         for (int i = 0; i < treinamento.numInstances(); i++) {
         proc.add(treinamento.instance(i));

         }
         break;

         }

         }

         //Definir o retorno dos dados
         return proc;
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder
         => Aqui ver com o professor Rodrigo de como proceder        
         */
    }

    /**
     * Vaidação dos dados para o cálculo do fitness da população
     */
    private void ValidacaoNodoFolhasParaCalculoFitness(Instances validacao) throws Exception {
        try {
            //Percorrer todas as árores existentes e Atualiza a quantidade de ocorrências
            for (Arvores arv : arvores) {
                //Zerar a quantidade de Ocorrências
                ZerarQtdOcorr();

                //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int i = 0; i < validacao.numInstances(); i++) {
                    //Atualizar(Calcular) a quantidade de ocorrências dos atributos na árvore
                    ValidacaoParaCalculoFitnessPopulacao(arv, validacao.instance(i));

                }

                //Atualizar a Quantidade de ocorrências
                arv.setQtdOcorr(qtdOcorrencias());

            }

        } catch (Exception e) {
            throw e;

        }

    }

    public void ValidacaoParaCalculoFitnessPopulacao(Arvores nodo, Instance avaliacao) {
        //Se o nó não for nulo
        if (nodo != null) {
            //OBSERVAÇÃO.: O for é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
            //somente pelo índice do atributo e NÃO PELO NOME DO MESMO
            //----------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //Percorrer todos os atributos da instância selecionada
            for (int k = 0; k < avaliacao.numAttributes() - 1; k++) {
                //Encontrou o mesmo atributos que o pesquisado
                if (avaliacao.attribute(k).name().equals(nodo.getNomeAtr())) {
                    //Percorrer as arestas (se encontrou o valor correspondente da aresta)
                    for (Atributos aresta : nodo.getArestas()) {
                        //Se o valor contido for igual ao pesquisado
                        if (Double.valueOf(aresta.getAtributo()).equals(avaliacao.classValue())) {
                            //Se for um nodo folha
                            if (aresta.getNodo() == null) {
                                //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIA do Nodo
                                if (aresta.getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                    //Atualizar a quantidade
                                    AtualizarQtdOcorr(1);

                                }

                            } else {
                                //Chamada recursiva da função passando como parâmetros a aresta selecionada
                                ValidacaoParaCalculoFitnessPopulacao(aresta.getNodo(), avaliacao);

                            }
                            //Sair do FOR de arestas
                            break;

                        }

                    }

                    //Sair do FOR dos atributos
                    break;

                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="Ordenação População em Ordem Crescente - Avaliados p/ Fitness">    
    public void ordenaPopulacao() {
        //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
        Collections.sort(arvores);

    }
    //</editor-fold> 

}
