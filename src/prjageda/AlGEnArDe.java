package prjageda;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import weka.core.Instance;
import weka.core.Instances;

public class AlGEnArDe {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    //Variáveis Públicas Estáticas
    public static final int quantidade = 100;
    public static final int profundidade = 4;
    public static final double TxCrossover = 0.9;
    public static final int qtdDecimais = 4;
    public static ArrayList<Arvores> nodos = null;
    public static ArrayList<Arvores> arvores = null;

    //Variáveis Privadas Estáticas
    private static final int geracoes = 100;
    private static final int nroFolds = 3;
    private int qtdOcorr = 0;

    //private static final String localArquivos = "C:\\Geracao\\";
    public static BufferedWriter escrita;

    //Método Inicializador da classe
    public AlGEnArDe() {
    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="2° Métodos Inicializadores da classe e Get´s E Set´s">    
    private void zerarqtdOcorr() {
        //Zerar a quantidade
        this.qtdOcorr = 0;

    }

    private int getqtdOcorr() {
        //Retornar a quantidade
        return this.qtdOcorr;

    }

    private void atuQtdOcorr(int quantidade) {
        //Atualizar a quantidade de ocorrências
        this.qtdOcorr += quantidade;

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="3° Definição dos Métodos pertinentes a Geração da População">
    //Tradução da Sigla - AlGenArDe - "Al"goritmo "Gen"ético de "Ar"vore de "De"cisão
    public void AlGenArDe(Instances dados) {
        try {
            //Estratificar os dados e divisão em 3 folds - "Treino, Validação e Teste"
            dados.stratify(nroFolds);

            //Definição da instância de "Treino", "Validação" e "Teste"
            Instances treino = dados.testCV(nroFolds, 0);
            Instances tempInst = dados.trainCV(nroFolds, 0);

            //Estratificar os dados e divisão em 2 folds
            tempInst.stratify(nroFolds - 1);

            //Definição das instâncias de "Validação" e "Teste""
            Instances validacao = tempInst.testCV(nroFolds - 1, 0);
            Instances teste = tempInst.trainCV(nroFolds - 1, 0);

            arvores = new ArrayList<>();
            int geracaoAtual = 1;

            //Efetuar o processamento das Sub-Arvores e suas Aretas (COM TODAS AS INSTÂNCIAS DE DADOS)
            ProcessamentoNodos(dados);

            //Efetuar a Geração da População Inicial, informar a quantidade de atributos MENOS o atributos classe
            GeracaoPopulacaoInicial(dados.instance(0).numAttributes() - 1, treino, validacao);

            //Imprimir as 2 melhores árvores da geração
            ImprimirMelhorArvoreGeracao(geracaoAtual, "H");

            //Efetuar a geração das novas populações
            while (geracaoAtual < geracoes) {
                //Atualizar a Geração
                geracaoAtual++;

                //Inicialização e Atribuição das árvores
                arvores = new Processamento().NovaGeracaoArvores(dados, true);

                //Calcular o Fitness e após Ordenar Crescente
                CalculoFitnessPopulacao(treino, validacao);

                //Imprimir as 2 melhores árvores da geração
                ImprimirMelhorArvoreGeracao(geracaoAtual, "H");

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Geração da População Inicial
    private void GeracaoPopulacaoInicial(int nroAtributos, Instances treino, Instances validacao) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            ArrayList<Arvores> arvTemp;

            //Percorrer a quantidade de árvores informado
            for (int i = 0; i < quantidade; i++) {
                //Inicialização Objetos
                arvTemp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(nodos);

                //Selecionar o nodo raiz (sorteado aleatóriamente)
                Arvores arv = arvTemp.get(Processamento.mt.nextInt(nroAtributos));

                //Geração da árvore ATÉ a profundidade estabelecida
                GerarPopulacaoArvores(nroAtributos, 1, arv);

                //Adicionar a Árvore Gerada
                arvores.add(arv);

            }

            //Calcular o Fitness das árvores E após Ordenação Crescente
            CalculoFitnessPopulacao(treino, validacao);

        } catch (IOException e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Geração da População de Árvores de Decisão
    public void GerarPopulacaoArvores(int nroAtributos, int prof, Arvores arvore) throws IOException {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //percorrer todas as arestas do árvore
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Gerar as Sub-Árvores com 50% de probabilidade                
                if (Processamento.mt.nextBoolean()) {
                    //Tratamento dos nodos (Geração das Sub-Árvores e Atributos)
                    ArrayList<Arvores> arvTemp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(nodos);

                    // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                    // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                    arvore.SetNodo(arvore.getArestas(i), arvTemp.get(Processamento.mt.nextInt(arvTemp.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                    GerarPopulacaoArvores(nroAtributos, prof + 1, arvore.getArvoreApartirAresta(i));

                }

            }

        }

    }

    //Processamento dos nodos - Definição das árvores e seus nodos (Numéricos - Bifurcadas / Nominais - Quantidade de arestas definida pela quantidade de classes)
    public void ProcessamentoNodos(Instances dados) {
        //Inicialização do Objeto
        nodos = new ArrayList<>();

        //Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias  por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
        for (int i = 0; i < dados.numAttributes() - 1; i++) {
            //1° Passo     - Processar todos os Atributos (Binários e Nominais)
            //2° Parâmetro - Nome do atributo
            //3° Parâmetro - Instâncias e a posição do Atributo
            nodos.add(new Arvores(dados.instance(0).attribute(i).name(), new Processamento().ProcessamentoInstancias(dados, i)));

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="4° Definição dos Métodos e Funções Destinadas a Avaliação da População">    
    //Efetuar o treinamento dos nodos folhas - Atribuição das classes e suas quantidades, a classe que possuir maior quantidade será a classe dominante
    private void TreinamentoNodosFolhas(Instances treino) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            Processamento proc = new Processamento();

            //Percorrer todas as árvores existentes para atribuição das classes e quantidades dos nodos folhas
            for (Arvores arvore : arvores) {
                //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de treino p/ calcular o fitness da árvore(s)
                for (int i = 0; i < treino.numInstances(); i++) {
                    //Atualizar(Calcular) a quantidade de ocorrências dos atributos na árvore
                    proc.AtribuicaoClasseNodosFolhas(arvore, treino.instance(i));

                }

                //Definição da Classe majoritária de cada um dos nodos "Folhas" da árvore
                for (int i = 0; i < arvore.getArestas().size(); i++) {
                    proc.DefinicaoClasseMajoritariaNodosFolhas(arvore, i);

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Definição do Cálculo do Fitness 
    // 1° Passo - Efetua-se a classificação das árvores (definição da classe majoritária) com as instâncias de teste
    // 2° Passo - Efetua-se a Validação da árvore pelas instâncias de treinamento
    // 3° Passo - Cálculo do Fitness da árvore 
    private void CalculoFitnessPopulacao(Instances treino, Instances validacao) throws Exception {
        try {
            //Eliminar Classificação Nodos
            EliminarClassificacaoNodos();

            //Efetuar o treinamento - Definir quais classes pertencem os nodos folhas
            TreinamentoNodosFolhas(treino);

            //Execução da Validação para atualizar a quantidade de ocorrência a partir da base montada
            ValidacaoNodoFolhasParaCalculoFitness(validacao);

            //Percorrer todas as árvres existentes e calcula o fitnes de cada uma delas
            for (Arvores arvore : arvores) {
                //Calcular E Setar o Valor do Fitness
                arvore.setFitness(new Processamento().Arredondar(1 - ((double) arvore.getQtdOcorrencias() / validacao.numInstances()), qtdDecimais, 1));

            }

            //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
            Collections.sort(arvores);

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Validação do nodos folhas p/ o Cálculo do Fitness, resumindo PARA cada árvore percorre-se TODAS as instâncias de Validação e efetua-se o calculo das 
    //quantidades das Classes p/ cada nodo folha
    private void ValidacaoNodoFolhasParaCalculoFitness(Instances validacao) throws Exception {
        try {
            //Percorrer todas as árores existentes e Atualiza a quantidade de ocorrências
            for (Arvores arv : arvores) {
                //Zerar a quantidade de Ocorrências
                zerarqtdOcorr();

                //1° Passo - Percorre a função recursivamente para chegar a todos os nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int i = 0; i < validacao.numInstances(); i++) {
                    //Atualizar(Calcular) a quantidade de ocorrências dos atributos na árvore
                    ValidacaoCalculoFitnessGeracao(arv, validacao.instance(i));

                }

                //Atualizar a Quantidade de ocorrências
                arv.setQtdOcorrencias(getqtdOcorr());

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Cálculo das quantidades (POR CLASSE) de cada um dos nodos folhas
    public void ValidacaoCalculoFitnessGeracao(Arvores arvore, Instance avaliacao) {
        //Se o árvore não for nula
        if (arvore != null) {
            //Declaração Variáveis e Objetos
            int posicao = 0;

            //Se tiver arestas válidas
            if (arvore.getArestas() != null) {
                //OBSERVAÇÃO.: O "for" é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
                //             somente pelo índice do atributo e NÃO PELO NOME DO MESMO
                //------------------------------------------------------------------------------------------------------------------------------------------------------------------
                //Percorrer todos os atributos da instância selecionada
                for (int k = 0; k < avaliacao.numAttributes() - 1; k++) {
                    //Encontrou o mesmo atributos que o pesquisado
                    if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                        posicao = k;
                        break;

                    }

                }

                //Se o atributo for Numérico
                if (avaliacao.attribute(posicao).isNumeric()) {
                    //Declaração Variáveis e Objetos
                    Processamento prc = new Processamento();
                    double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                    //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                    int pos = prc.Arredondar(avaliacao.value(posicao), qtdDecimais, 1) <= prc.Arredondar(valorAresta, qtdDecimais, 1) ? 0 : 1;

                    //Se for um nodo folha
                    if (arvore.getArestas(pos).getNodo() == null) {
                        //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                        if (arvore.getArestas(pos).getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                            //Atualizar a quantidade (Somar 1 na quantidade atual)
                            atuQtdOcorr(1);

                        }

                    } else {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        ValidacaoCalculoFitnessGeracao(arvore.getArestas(pos).getNodo(), avaliacao);

                    }

                } else {
                    /*    
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     //Percorrer todas as arestas
                     for (Atributos aresta : arvore.getArestas()) {
                     //Se for um nodo folha
                     if (aresta.getNodo() == null) {
                     //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                     if (aresta.getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                     //Atualizar a quantidade (Somar 1 na quantidade atual)
                     atuQtdOcorr(1);

                     }

                     } else {
                     //Chamada recursiva da função passando como parâmetros a aresta selecionada
                     ValidacaoCalculoFitnessGeracao(aresta.getNodo(), avaliacao);

                     }

                     }
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     Aqui Rever  ------------------------------------------------
                     */
                }

            }

        }

    }

    //Eliminar as definições da árvore (Valor Fitness, Quantidade de Ocorrência, Classe Dominante e Classes)
    private void EliminarClassificacaoNodos() {
        //Processar Árvores e Eliminar as definções dos Nodos Folhas
        for (Arvores arvore : arvores) {
            //Processamento dos nodos folhas
            LimparDefinicaoClassesNodosFolhas(arvore, 0);

        }

    }

    //Eliminar a Classificação dos nodos Folhas
    private void LimparDefinicaoClassesNodosFolhas(Arvores arv, int posicao) {
        try {
            if (arv != null) {
                //Atualizar os Atributos da Árvore
                arv.setQtdOcorrencias(0);
                arv.setFitness(0);

                //Se possuir arestas
                if (arv.getArestas() != null) {
                    //Se a aresta não for nula
                    if (arv.getArestas(posicao) != null) {
                        //Atribuições
                        arv.getArestas(posicao).setClasses(null);
                        arv.getArestas(posicao).setClasseDominante("");

                        //Se o nodo não for nulo (Chama Recursivamento o próximo nível) até chegar em um nodo Folha
                        if (arv.getArestas(posicao).getNodo() != null) {
                            //Chamada Recursiva da Função Avaliando a posição Atual
                            LimparDefinicaoClassesNodosFolhas(arv.getArestas(posicao).getNodo(), posicao);

                            //Se a próxima aresta não for nula
                            if (arv.getArestas(posicao + 1) != null) {
                                //Se o nodo não for nulo (Chama Recursivamento o próximo nível) até chegar em um nodo Folha
                                if (arv.getArestas(posicao + 1).getNodo() != null) {
                                    //Chamada Recursiva da Função
                                    LimparDefinicaoClassesNodosFolhas(arv.getArestas(posicao + 1).getNodo(), posicao + 1);

                                }

                            }

                        }

                    } else //Chamada Recursiva da Função
                    {
                        LimparDefinicaoClassesNodosFolhas(arv.getArestas(posicao + 1).getNodo(), posicao + 1);

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="5° Definição das Funções de Impressão da Árvore">    
    private void ImprimirMelhorArvoreGeracao(int geracao, String tipo) {
        System.out.println("Geração.: " + geracao + " - Árvore.: " + arvores.get(0).getNomeAtributo() + " - Fitness.: " + arvores.get(0).getFitness());
        //System.out.println("Geração.: " + geracaoAtual + " - Árvore.: " + arvores.get(1).getNomeAtributo() + " - Fitness.: " + arvores.get(1).getFitness());

//        //Declaração Variáveis e Objetos
//        File arquivo = new File(localArquivos + "\\Arvores\\Arvore_" + geracaoAtual + ".txt");
//
//        //Se Existir o arquivo, deleta o mesmo
//        if (arquivo.exists()) {
//            arquivo.delete();
//
//        }
//
//        //Se Existir o arquivo
//        try (FileWriter regs = new FileWriter(arquivo)) {
//            //Declaração Variáveis e Objetos
//            escrita = new BufferedWriter(regs);
//
//            //Finalização da Geração
//            escrita.write(" Iniciando Impressão Geração.: " + geracaoAtual);
//            escrita.newLine();
//
//            escrita.write("------------------------------------------------------------------------------------------------------------------------------------------------------");
//            escrita.newLine();
//
//            //Percorrer todas as árvores
//            for (Arvores arv : arvores) {
//
//                if ("V".equals(tipo)) {
//                    //Imprimir na horizontal
//                    //new BTreePrinter().printNode(arv);
//
//                } else {
//
//                    //Imprimir na horizontal
//                    ImprimirArvoreHorizontal(arv, 1);
//
//                }
//
//                escrita.newLine();
//                escrita.newLine();
//
//            }
//
//            //Finalização da Geração
//            escrita.newLine();
//            escrita.write("------------------------------------------------------------------------------------------------------------------------------------------------------");
//            escrita.newLine();
//            escrita.write(" Finalizando Impressão Geração.: " + geracaoAtual);
////            escrita.close();
//
//        } catch (Exception e) {
//            System.out.println("Erro na impressão da árvore.: " + e.getMessage());
//
//        }
    }

    public static void ImprimirArvoreHorizontal(Arvores arv, int level) throws IOException {
        if (arv == null) {
            return;
        }

        ImprimirArvoreHorizontal(arv.getArestas(1).getNodo(), level + 1);

        if (level != 0) {
            String temp = "";
            for (int i = 0; i < level - 1; i++) {
                temp += "|\t";

            }
            escrita.write(temp + "|-------" + arv.getNomeAtributo());
            escrita.newLine();

        } else {
            escrita.write(arv.getNomeAtributo() + " - " + arv.getFitness());
            escrita.newLine();

        }

        ImprimirArvoreHorizontal(arv.getArestas(0).getNodo(), level + 1);

    }
    //</editor-fold>     
}
