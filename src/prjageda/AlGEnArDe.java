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
    public static final int _quantidade = 50;
    public static final int _profundidade = 3;   //Defini-se como Nível = Nível + 1;
    public static final double _TxCrossover = 0.9;
    public static final int _qtdDecimais = 4;
    public static ArrayList<Arvores> _nodos = null;
    public static ArrayList<Arvores> _arvores = null;
    public static MersenneTwister mtw = new MersenneTwister();

    //Variáveis Privadas Estáticas
    private static final int _geracoes = 100;
    private static final int _nroFolds = 3;
    private int _qtdOcorr = 0;

    //private static final String localArquivos = "C:\\Geracao\\";
    public static BufferedWriter _escrita;

    //Método Inicializador da classe
    public AlGEnArDe() {
    }
    //</editor-fold>  

    //<editor-fold defaultstate="collapsed" desc="3° Definição dos Métodos pertinentes a Geração da População">
    //Tradução da Sigla - AlGenArDe - "Al"goritmo "Gen"ético de "Ar"vore de "De"cisão
    public void AlGenArDe(Instances dados) throws Throwable {
        try {
            //Estratificar os dados e divisão em 3 Folds - "Treino, Validação e Teste"
            dados.stratify(_nroFolds);

            //Definição da instância de "Treino, Validação e Teste"
            Instances treino = dados.testCV(_nroFolds, 0);
            Instances tempInst = dados.trainCV(_nroFolds, 0);

            //Definição das instâncias de "Validação e Teste"
            Instances validacao = tempInst.testCV(_nroFolds - 1, 0);
            Instances teste = tempInst.trainCV(_nroFolds - 1, 0);

            //Declaração Variáveis e Objetos E Inicializações
            _arvores = new ArrayList<>();
            int geracaoAtual = 1;

            //Efetuar o processamento das Sub-Arvores e suas Aretas (COM TODAS AS INSTÂNCIAS DE DADOS)
            gerarDecisionStumps(dados);

            //Efetuar a Geração da População Inicial, informar a _quantidade de atributos MENOS o atributos classe
            gerarPopulacaoInicial(treino, validacao, _profundidade);

            //Imprimir a melhor árvore da geração
            System.out.println("Geração.: " + geracaoAtual + " - Árvore.: " + _arvores.get(0).getNomeAtributo() + " - Fitness.: " + _arvores.get(0).getFitness());

            //Efetuar a geração das novas populações
            while (geracaoAtual < _geracoes) {
                //Atualizar a Geração
                geracaoAtual++;

                //Carregar com as árvores
                _arvores = new Processamento().gerarPopulacaoArvores(dados, true);

                //Processar Árvores e Eliminar as definições dos Nodos Folhas (Na Geração Inicial NÃO SE FAZ necessário)
                for (int i = Processamento._qtdElitismo; i < _arvores.size(); i++) {
                    //Processamento dos _nodos folhas
                    eliminarClassificacaoNodosFolhas(_arvores.get(i), 1, _profundidade * 2);

                }

                //Calcular o Fitness e após Ordenar Crescente (em função do Crossover e da Mutação a profundidade da árvore poderá duplicar)
                calcularFitnessPopulacao(treino, validacao, _profundidade * 2, Processamento._qtdElitismo);

                //Imprimir a melhor árvore da geração
                System.out.println("Geração.: " + geracaoAtual + " - Árvore.: " + _arvores.get(0).getNomeAtributo() + " - Fitness.: " + _arvores.get(0).getFitness());

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        } finally {
            //Atibuir nulo aos objetos
            _nodos = null;
            _arvores = null;
            _escrita = null;
            _qtdOcorr = 0;

        }

    }

    //Efetuar a Geração da População Inicial
    private void gerarPopulacaoInicial(Instances treino, Instances validacao, int profMaxima) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            ArrayList<Arvores> arvTemp;

            //Percorrer a _quantidade de árvores informado
            for (int i = 0; i < _quantidade; i++) {
                //Inicialização Objetos (DeepCopy dos Decisions Stumps)
                arvTemp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(_nodos);

                //Selecionar o nodo raiz (sorteado aleatóriamente)
                Arvores arv = arvTemp.get(mtw.nextInt(arvTemp.size() - 1));

                //Geração da árvore ATÉ a _profundidade estabelecida (Desconsiderando o nodo do 1° Nível)
                gerarPopulacaoArvores(arvTemp.size() - 1, 2, arv);

                //Adicionar a Árvore Gerada
                _arvores.add(arv);

            }

            //Calcular o Fitness das árvores(Treinamento e Validação) E após Ordenar Crescentemente 
            calcularFitnessPopulacao(treino, validacao, profMaxima, 0);

        } catch (IOException e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Geração da População de Árvores de Decisão
    public void gerarPopulacaoArvores(int nroAtributos, int prof, Arvores arvore) throws IOException {
        //Condição de Parada - Se o grau de _profundidade for máximo
        if (prof <= _profundidade) {
            //percorrer todas as arestas do árvore
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Gerar as Sub-Árvores com 50% de probabilidade                
                if (mtw.nextBoolean()) {
                    //Tratamento dos _nodos (Geração das Sub-Árvores e Atributos)
                    ArrayList<Arvores> arvTemp = (ArrayList<Arvores>) ObjectUtil.deepCopyList(_nodos);

                    // 1°) Sortear um Nodo(Árvore) Qualquer Aleatóriamente p/ Inserção                   
                    // 2°) Inserir na aresta a Árvore Selecionada Aleatóriamente(No Atributo Nodo)
                    arvore.SetNodo(arvore.getArestas(i), arvTemp.get(mtw.nextInt(arvTemp.size())));

                    //Chamada Recursiva para Geração da árvore atualizando o nivel de _profundidade
                    gerarPopulacaoArvores(nroAtributos, prof + 1, arvore.getArvoreApartirAresta(i));

                }

            }

        }

    }

    //Processamento dos _nodos - Definição das árvores e seus _nodos (Numéricos - Bifurcadas / Nominais - Quantidade de arestas definida pela _quantidade de classes)
    public void gerarDecisionStumps(Instances dados) {
        //Inicialização do Objeto
        _nodos = new ArrayList<>();

        //Processamento: PARA CADA COLUNA PERCORRE TODAS AS LINHAS
        //--------------------------------------------------------------------------------------------------------------------------------------------------------------------------
        //Percorrer TODOS os atributos (colunas) existentes e para cada atributo percorre todas as instâncias  por exemplo: Atributo 0, Atributo 1, Atributo 2,...Atributo N-1
        for (int i = 0; i < dados.numAttributes() - 1; i++) {
            //1° Passo     - Processar todos os Atributos (Binários e Nominais)
            //2° Parâmetro - Nome do atributo
            //3° Parâmetro - Instâncias e a posição do Atributo
            _nodos.add(new Arvores(dados.instance(0).attribute(i).name(), new Processamento().processarInstanciasDados(dados, i)));

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="4° Definição dos Métodos e Funções Destinadas a Avaliação da População">    
    //Definição do Cálculo do Fitness 
    // 1° Passo - Efetua-se a classificação das árvores (definição da classe majoritária) com as instâncias de teste
    // 2° Passo - Efetua-se a Validação da árvore pelas instâncias de treinamento
    // 3° Passo - Cálculo do Fitness da árvore 
    private void calcularFitnessPopulacao(Instances treino, Instances validacao, int profMaxima, int posInicial) throws Exception {
        try {
            //Efetuar o treinamento - Definir quais classes pertencem os _nodos folhas
            treinarNodosFolhas(treino, profMaxima, posInicial);

            //Execução da Validação para atualizar a _quantidade de ocorrência a partir da base montada e Calcular o Fitness
            validarNodosFolhas(validacao, profMaxima, posInicial);

            //Ordenar a população EM ORDEM CRESCENTE pelo valor do Fitness, por exemplo.: 0.2, 0.3, 0.4,...1.0
            Collections.sort(_arvores);

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar o treinamento dos _nodos folhas - Atribuição das classes e suas quantidades, a classe que possuir maior _quantidade será a classe dominante
    private void treinarNodosFolhas(Instances treino, int profMaxima, int posInicial) throws Exception {
        try {
            //Declaração Variáveis e Objetos
            Processamento proc = new Processamento();

            //Percorrer todas as árvores existentes para atribuição das classes e quantidades dos _nodos folhas
            for (int i = posInicial; i < _arvores.size(); i++) {
                //1° Passo - Percorre a função recursivamente para chegar a todos os _nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de treino p/ calcular o fitness da árvore(s)
                for (int j = 0; j < treino.numInstances(); j++) {
                    //Atualizar(Calcular) a _quantidade de ocorrências dos atributos na árvore
                    proc.definirClasseNodosFolhas(_arvores.get(i), treino.instance(j), 1, profMaxima);

                }

                //Definir a classe majoritária da aresta
                proc.atribuirClasseNodosFolhas(_arvores.get(i), 1, profMaxima);

                //Eliminar as arestas que não possuem classificação após a execução do "Treino", ou seja, efetua a PODA dos nodos que possuem apenas 1 aresta válida
                //podarArestasSemClassificacao(_arvores.get(i), 1, profMaxima);

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Efetuar a Validação do _nodos folhas p/ o Cálculo do Fitness, resumindo PARA cada árvore percorre-se TODAS as instâncias de Validação e efetua-se o calculo das 
    //quantidades das Classes p/ cada nodo folha
    private void validarNodosFolhas(Instances validacao, int profMaxima, int posInicial) throws Exception {
        try {
            //Percorrer todas as árores existentes e Atualiza a _quantidade de ocorrências
            for (int i = posInicial; i < _arvores.size(); i++) {
                //Zerar a _quantidade de Ocorrências
                this._qtdOcorr = 0;

                //1° Passo - Percorre a função recursivamente para chegar a todos os _nodos folhas e atribuir a(s) propriedades encontradas
                //2° Passo - Executa-se as instância de avaliação p/ calcular o fitness da árvore(s)
                for (int j = 0; j < validacao.numInstances(); j++) {
                    //Atualizar(Calcular) a _quantidade de ocorrências dos atributos na árvore
                    validarCalculoFitnessArvore(_arvores.get(i), validacao.instance(j), 1, profMaxima);

                }

                //Atualizar a Quantidade de ocorrências e o Valor do Fitness Inicial
                _arvores.get(i).setQtdOcorrencias(this._qtdOcorr);
                _arvores.get(i).setFitness(1);

                //Se possuir Ocorrências Efetua o Cálculo
                if (this._qtdOcorr > 0) {
                    //Declaração Variáveis e Objetos
                    double vlrFitness = new Processamento().arredondarValor(1 - ((double) this._qtdOcorr / validacao.numInstances()), _qtdDecimais, 1);

                    //Cálcular Valor do Fitness
                    _arvores.get(i).setFitness(vlrFitness == 0 ? 1 : vlrFitness);

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Cálculo das quantidades (POR CLASSE) de cada um dos _nodos folhas
    public void validarCalculoFitnessArvore(Arvores arvore, Instance avaliacao, int prof, int profMaxima) {
        //Se o árvore for nula
        if (arvore == null) {
            return;

        }

        //Se as arestas forem nulas
        if (arvore.getArestas() == null) {
            return;

        }

        //Condição de Parada - Se o grau de _profundidade for máximo
        if (prof <= (profMaxima + 1)) {
            //Declaração Variáveis e Objetos
            int posicao = 0;

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
                int pos = prc.arredondarValor(avaliacao.value(posicao), _qtdDecimais, 1) <= prc.arredondarValor(valorAresta, _qtdDecimais, 1) ? 0 : 1;

                //Se a árvore não for nula
                if (arvore.getArestas(pos) != null) {
                    //Se for um nodo folha
                    if (arvore.getArestas(pos).getNodo() == null) {
                        //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a _quantidade de OCORRÊNCIAS do Nodo
                        if (arvore.getArestas(pos).getClasseDominante().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                            //Atualizar a _quantidade (Somar 1 na _quantidade atual)
                            this._qtdOcorr += 1;

                        }

                    } else {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        validarCalculoFitnessArvore(arvore.getArestas(pos).getNodo(), avaliacao, prof + 1, profMaxima);

                    }

                }

            } else {
                //Percorrer todas as arestas
                for (Atributos aresta : arvore.getArestas()) {
                    //Se for um nodo folha
                    if (aresta.getNodo() == null) {
                        //Se o valor da aresta for igual ao valor do atributo selecionada da instância processada, atualiza a _quantidade de OCORRÊNCIAS do Nodo
                        if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(aresta.getClasseDominante())) {
                            //Atualizar a _quantidade (Somar 1 na _quantidade atual)
                            this._qtdOcorr += 1;

                        }

                    } else {
                        //Chamada recursiva da função passando como parâmetros a aresta selecionada
                        validarCalculoFitnessArvore(aresta.getNodo(), avaliacao, prof + 1, profMaxima);

                    }

                }

            }

        }

    }

    //Eliminar a Classificação dos _nodos Folhas
    private void eliminarClassificacaoNodosFolhas(Arvores arv, int prof, int profMaxima) {
        //Se o árvore for nula
        if (arv == null) {
            return;

        }

        //Se as arestas forem nulas
        if (arv.getArestas() == null) {
            return;

        }

        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= profMaxima) {
                //Atualizar os Atributos da Árvore
                arv.setQtdOcorrencias(0);
                arv.setFitness(0);

                //Se possuir arestas
                if (arv.getArestas() != null) {
                    //Percorrer todas as arestas
                    for (int i = 0; i < arv.getArestas().size(); i++) {
                        //Se a aresta não for nula
                        if (arv.getArestas(i) != null) {
                            //Atribuições da aresta
                            arv.getArestas(i).setClasses(null);
                            arv.getArestas(i).setClasseDominante("");

                            //Se o nodo não for nulo (Chama Recursivamente o próximo nível) até chegar em um nodo Folha
                            if (arv.getArestas(i).getNodo() != null) {
                                //Chamada Recursiva da Função Avaliando a posição Atual
                                eliminarClassificacaoNodosFolhas(arv.getArestas(i).getNodo(), prof + 1, profMaxima);

                            }

                        }

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }

    //Eliminar as arestas nulas da árvoe após a classificação dos nodos(Após a aplicação das Instâncias de "Treino")
    private void podarArestasSemClassificacao(Arvores arv, int prof, int profMaxima) {
        //Se o árvore for nula
        if (arv == null) {
            return;

        }

        //Se as arestas forem nulas
        if (arv.getArestas() == null) {
            return;

        }

        try {
            //Condição de Parada - Se o grau de _profundidade for máximo
            if (prof <= profMaxima) {
                //Se possuir arestas
                if (arv.getArestas() != null) {
                    //Percorrer todas as arestas
                    for (int i = 0; i < arv.getArestas().size(); i++) {
                        //Declaração Variáveis e Objetos
                        int pos = 0, qtdAretas = 0;

                        while (qtdAretas < arv.getArestas().size()) {
                            //Se o Nodo for Nulo e não possuir classificação
                            if ((arv.getArestas(i).getNodo() == null) && (arv.getArestas(i).getClasseDominante().isEmpty())) {
                                //Eliminar a Aresta selecionada
                                arv.setNuloArestas(arv.getArestas(i));
                                pos = (i == 0 ? 0 : i - 1);

                            }

                            //Atualizar a posição avaliada
                            qtdAretas++;

                        }

                        //Se possuir um apenas um nodo
                        if (arv.getArestas().size() == 1) {
                            //SE for o nodo raiz, não atualiza a posição, SENÃO subtrai 1 da posição atual
                            prof = prof == 1 ? 0 : prof - 1;

                            //Substituir a Árvore Atual pegando a única aresta que existe
                            arv = arv.getArestas(0).getNodo();

                            //Se o nodo não for nulo (Chama Recursivamente o próximo nível) até chegar em um nodo Folha
                            podarArestasSemClassificacao(arv, prof + 1, profMaxima);

                        } else {
                            //Chama Recursivamente o próximo nível
                            podarArestasSemClassificacao(arv.getArestas(i).getNodo(), prof + 1, profMaxima);

                        }

                    }

                }

            }

        } catch (Exception e) {
            System.out.println(e.getMessage());

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="5° Funcionalidades Direcionadas a Impressão das Árvores">    
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
            _escrita.write(temp + "|-------" + arv.getNomeAtributo());
            _escrita.newLine();

        } else {
            _escrita.write(arv.getNomeAtributo() + " - " + arv.getFitness());
            _escrita.newLine();

        }

        ImprimirArvoreHorizontal(arv.getArestas(0).getNodo(), level + 1);

    }
    //</editor-fold>           

}
