package prjageda;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;

public class AlGEnArDe {

    //<editor-fold defaultstate="collapsed" desc="1° Definição dos Atributos e método Inicializador da classe">    	
    //Variáveis Públicas Estáticas
    public static final int quantidade = 100;
    public static final int profundidade = 3;
    public static final double TxCrossover = 0.9;
    public static final int qtdDecimais = 4;

    //Variáveis Privadas Estáticas
    private static final int geracoes = 100;
    private static final int nroFolds = 3;
    private static final String localArquivos = "C:\\Geracao\\";
    private List<Arvores> arvores;
    public static BufferedWriter escrita;
    private int qtdOcorr = 0;

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
            //Declaração Variáveis e Objetos
            Instances treino = FormatacaoFonteDados(dados, "T");
            Instances validacao = FormatacaoFonteDados(dados, "V");
            arvores = new ArrayList<>();
            int geracao = 1;

            //Efetuar a Geração da População Inicial
            GeracaoPopulacaoInicial(dados, treino, validacao);

            //Imprimir as 2 melhores árvores da geração
            ImprimirArvoresGeracao(geracao, "H");

            //Efetuar a geração das novas populações
            while (geracao < geracoes) {
                //Atualizar a Geração
                geracao++;

                //Inicialização e Atribuição das árvores
                arvores = new Processamento().NovaGeracaoArvores(dados, arvores, true);

                //Calcular o Fitness e após Ordenar Crescente
                CalculoFitnessPopulacao(treino, validacao);

                //Imprimir as 2 melhores árvores da geração
                ImprimirArvoresGeracao(geracao, "H");

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Geração da População Inicial
    private void GeracaoPopulacaoInicial(Instances dados, Instances treino, Instances validacao) throws Exception {
        try {
            //Efetuar o processamento das arestas dos Decisions Strumps (gravar em arquivo texto) p/ leitura posterior
            ProcessamentoNodos(dados);

            //Percorrer a quantidade de árvores informado
            for (int i = 0; i < quantidade; i++) {
                //Selecionar o nodo raiz (sorteado aleatóriamente)
                Arvores arv = LeituraNodos().get(Processamento.mt.nextInt(dados.numAttributes() - 1));

                //Geração da árvore ATÉ a profundidade estabelecida
                GerarPopulacaoArvores(dados, 1, arv);

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
    public void GerarPopulacaoArvores(Instances dados, int prof, Arvores arvore) throws IOException {
        //Condição de Parada - Se o grau de profundidade máxima
        if (prof <= profundidade) {
            //percorrer todas as arestas do árvore
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Processar Sim ou Não { Inserir Sub-Árvore } - c/ 50% de Probabilidade 
                //Adicionar as árvores originais, devido ao java trabalhar APENAS com a referência dos objetos, ai a cada geração deve-se RECARREGAR as mesmas
                ArrayList<Arvores> nodos = LeituraNodos();

                // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                arvore.SetNodo(arvore.getArestas(i), nodos.get(Processamento.mt.nextInt(nodos.size())));

                //Chamada Recursiva para Geração da árvore atualizando o nivel de profundidade
                GerarPopulacaoArvores(dados, prof + 1, arvore.getArvoreApartirAresta(i));

            }

        }

    }

    //Processamento dos nodos - Definição das árvores e seus nodos (Numéricos - Bifurcadas / Nominais - Quantidade de arestas definida pela quantidade de classes)
    public void ProcessamentoNodos(Instances dados) {
        //Declaração Variáveis e Objetos
        File arquivo = new File(localArquivos + "nodos.txt");

        //Se Existir o arquivo
        if (arquivo.exists()) {
            //Deleta o mesmo
            arquivo.delete();

        }

        //Se Existir o arquivo
        try (FileWriter regs = new FileWriter(arquivo);
                //Declaração Variáveis e Objetos
                BufferedWriter escr = new BufferedWriter(regs)) {

            //Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
            //----------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias  por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
            for (int i = 0; i < dados.numAttributes() - 1; i++) {
                //1° Passo     - Processar todos os Atributos (Binários e Nominais)
                //2° Parâmetro - Nome do atributo
                //3° Parâmetro - Instâncias e a posição do Atributo
                ArrayList<Atributos> atribs = new Processamento().ProcessamentoInstancias(dados, i);
                String complemento = "";

                //Percorrer os Atributos
                for (Atributos atr : atribs) {
                    //Concatenar
                    complemento += atr.getAtributo() + ";";

                }

                //Escrever a linha
                escr.write(dados.instance(0).attribute(i).name().trim() + ";" + complemento.substring(0, complemento.length() - 1));

                //Gerar a nova linha
                escr.newLine();

            }

        } catch (Exception e) {
            System.out.println("Erro na impressão da árvore.: " + e.getMessage());

        }

    }

    //Leitura dos nodos já processados - Definição das árvores e seus nodos (Numéricos - Bifurcadas / Nominais - Quantidade de arestas definida pela quantidade de classes)
    public ArrayList<Arvores> LeituraNodos() throws IOException {
        //Declaração Variáveis e Objetos
        BufferedReader leitura;
        ArrayList<Arvores> nodos;
        ArrayList<Atributos> atributos;

        try (
                //Declaração Variáveis e Objetos
                FileReader fileReader = new FileReader(localArquivos + "nodos.txt")) {

            //Declaração Variáveis e Objetos
            leitura = new BufferedReader(fileReader);
            nodos = new ArrayList<>();
            String linha;

            //Enquanto processar linhas
            while ((linha = leitura.readLine()) != null) {
                //Declaração Variáveis e Objetos
                String[] itens = linha.split(";");
                atributos = new ArrayList<>();

                //Adicionar os Atributos
                atributos.add(new Atributos(itens[1], null, "", null));
                atributos.add(new Atributos(itens[2], null, "", null));

                //Adicionar o nodo
                nodos.add(new Arvores(itens[0], atributos));

            }

        }

        //Fechar o arquivo
        leitura.close();

        //Definir o retorno
        return nodos;

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
                proc.DefinicaoClasseMajoritariaNodosFolhas(arvore);

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

    //Efetuar a Estratificação dos Dados
    private Instances FormatacaoFonteDados(Instances dados, String tipo) throws Exception {
        //Estratificar os dados e divisão em 3 folds
        dados.stratify(nroFolds);

        //Definir o retorno
        return (tipo.equals("T")) ? dados.testCV(nroFolds, 0) : (tipo.equals("V")) ? dados.trainCV(nroFolds, 1) : dados.trainCV(nroFolds, 2);

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
            //OBSERVAÇÃO.: O for é devido as classes do WEKA não permitirem de que apartir da instância selecionada PEGAR um atributo em específico, a pesquisa é feita
            //somente pelo índice do atributo e NÃO PELO NOME DO MESMO
            //----------------------------------------------------------------------------------------------------------------------------------------------------------------------
            //Percorrer todos os atributos da instância selecionada
            for (int k = 0; k < avaliacao.numAttributes() - 1; k++) {
                //Encontrou o mesmo atributos que o pesquisado
                if (avaliacao.attribute(k).name() == arvore.getNomeAtributo()) {
                    //Se tiver arestas válidas
                    if (arvore.getArestas() != null) {
                        //Se o atributo for Numérico
                        if (avaliacao.attribute(k).isNumeric()) {
                            //Declaração Variáveis e Objetos
                            Processamento prc = new Processamento();
                            double valorAresta = Double.valueOf(arvore.getArestas(0).getAtributo().split(" ")[1]);

                            //Se valor posição 0 FOR MENOR IGUAL ao valor atributo selecionado (Então posição igual a 0 SENAO 1)
                            int pos = prc.Arredondar(avaliacao.value(k), qtdDecimais, 1) <= prc.Arredondar(valorAresta, qtdDecimais, 1) ? 0 : 1;

                            //Se for um nodo folha
                            if (arvore.getArestas(pos).getNodo() == null) {
                                //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a quantidade de OCORRÊNCIAS do Nodo
                                if (arvore.getArestas(pos).getClasseDominante() == avaliacao.classAttribute().value((int) avaliacao.classValue())) {
                                    //Atualizar a quantidade (Somar 1 na quantidade atual)
                                    atuQtdOcorr(1);

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
                                    if (aresta.getClasseDominante() == avaliacao.classAttribute().value((int) avaliacao.classValue())) {
                                        //Atualizar a quantidade (Somar 1 na quantidade atual)
                                        atuQtdOcorr(1);

                                    }

                                } else {
                                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                                    ValidacaoCalculoFitnessGeracao(aresta.getNodo(), avaliacao);

                                }

                            }

                        }
                        //Sair for do for
                        break;
                    }

                }

            }

        }

    }

    //Eliminar as definições da árvore (Valor Fitness, Quantidade de Ocorrência, Classe Dominante e Classes)
    private void EliminarClassificacaoNodos() {
        //Processar Árvores e Eliminar as definções dos Nodos Folhas
        for (Arvores arvore : arvores) {
            //Atualizar os Valores da Árvore
            arvore.AtuQtdOcorrencias(0);
            arvore.setFitness(0);

            //Processamento dos nodos folhas
            LimparDefinicaoClassesNodosFolhas(arvore);

        }

    }

    //Eliminar a Classificação dos nodos Folhas
    private void LimparDefinicaoClassesNodosFolhas(Arvores arv) {
        try {
            if (arv != null) {
                //Se possuir arestas
                if (arv.getArestas() != null) {
                    //Percorre todas as arestas até encontrar o atributos selecionado
                    for (int i = 0; i < arv.getArestas().size(); i++) {
                        //Atualizar os Atributos da Árvore
                        arv.setQtdOcorrencias(0);
                        arv.setFitness(0);
                        arv.getArestas(i).setClasses(null);
                        arv.getArestas(i).setClasseDominante("");

                        //Se a aresta não for nula
                        if (arv.getArestas(i) != null) {
                            //Se o nodo não for nulo (Chama Recursivamento o próximo nível) até chegar em um nodo Folha
                            if (arv.getArestas(i).getNodo() != null) {
                                //Chamada Recursiva da Função
                                LimparDefinicaoClassesNodosFolhas(arv.getArestas(i).getNodo());

                            }
                        }

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="5° Definição das Funções de Impressão da Árvore">    
    private void ImprimirArvoresGeracao(int geracao, String tipo) {
        System.out.println("Geração.: " + geracao + " - Árvore.: " + arvores.get(0).getNomeAtributo() + " - Fitness.: " + arvores.get(0).getFitness());
        System.out.println("Geração.: " + geracao + " - Árvore.: " + arvores.get(1).getNomeAtributo() + " - Fitness.: " + arvores.get(1).getFitness());

//        //Declaração Variáveis e Objetos
//        File arquivo = new File(localArquivos + "\\Arvores\\Arvore_" + geracao + ".txt");
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
//            escrita.write(" Iniciando Impressão Geração.: " + geracao);
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
//            escrita.write(" Finalizando Impressão Geração.: " + geracao);
//            escrita.close();
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
