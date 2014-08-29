package prjageda;

import java.util.ArrayList;
import java.util.List;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

public class Processamento {

    //<editor-fold defaultstate="collapsed" desc="ATRIBUTOS E MÉTODO CONSTRUTOR DA CLASSE">    
    private String caminhoDados;
    private Arvores arvTemporaria = null;

    //Declaração de objetos
    public static final MersenneTwister mt = new MersenneTwister();

    //Configuração para mutação do indivíduo
    private static final int limMax = 100;
    private static final int limInfMutacao = 1;
    private static final int limSupMutacao = 5;
    private static final int profMutacao = 2;

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

        /**
         * 1 - Avaliar se o Atributo é Numérico ou Nominal 1.1 - Se Numérico a árvore terá 2 arestas(Árvore será bifurcada) 1.2 - Se Categórico terá o número de arestas em função
         * da quantidade de atributos encontrados no dataset
         * -------------------------------------------------------------------------------------------------------------------------------------------------------------------------
         */
        if (dados.attribute(posicao).isNumeric()) {
            //Percorre todas as Classes Encontradas 
            for (int pos = 0; pos < dados.numClasses(); pos++) {
                //Adicionar as Arestas
                registros.add(new Atributos(String.valueOf(pos), null, "", null));

            }

        } else {
            //Percorrer a Quantidade de Atributos existentes
            for (int i = 0; i < dados.attribute(posicao).numValues(); i++) {
                //Adicionar as Arestas
                registros.add(new Atributos(dados.attribute(posicao).value(i), null, "", null));

            }

        }

        //Definir o retorno
        return registros;

    }

    //Efetuar a leitura recursiva da árvore, lendo cada instância da base de dados e percorrer toda a árvore
    public ArrayList<Arvores> NovaGeracaoArvores(Instances dados, List<Arvores> arvores, boolean elitismo) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Se tiver elitismo, adicionar (mantém) o melhor árvore da geração atual(ordenada) para a próxima geração
        if (elitismo) {
            //Adicionar as árvores
            populacao.add(arvores.get(0));
            populacao.add(arvores.get(1));

        }

        //Efetua a geração da nova população equanto a população for menor que a população inicialmente estabelecida
        while (populacao.size() < DecisionStumps.quantidade) {
            //Declaração de variáveis e objetos
            List<Arvores> filhos = new ArrayList<>();

            //Adicionar os pais
            filhos.add(SelecaoPorTorneio(arvores));
            filhos.add(SelecaoPorTorneio(arvores));

            //SE Valor Gerado <= TxCrossover, realiza o Crossover entre os pais SENÃO mantém os pais selecionados através de Torneio p/ a próxima geração            
            if (mt.nextDouble() <= DecisionStumps.TxCrossover) {
                //Adicionar os 2 filhos que sofreram Crossover
                populacao.addAll(CrossoverArvores(filhos.get(0), filhos.get(1)));

            } else {
                //Apenas adicionar os 2 filhos selecionados
                populacao.add(filhos.get(0));
                populacao.add(filhos.get(1));

            }

        }

        //Efetuar Mutação dos indivíduos (se selecionado pelo critério do %), Exceto p/ as árvores obtidas por Eletismo
        for (int i = 2; i < populacao.size(); i++) {
            //Sortear um valor até o limite máximo permitido
            int perc = mt.nextInt(limMax);

            //Se extiver compreendido entre o limite Inferior E Superior
            if (perc >= limInfMutacao && perc <= limSupMutacao) {
                //Efetuar a Mutação do indivíduo - "E"xpansão ou "S"ubtração de Nodos
                MutacaoArvores(populacao.get(i), mt.nextBoolean() ? "E" : "R", 1, dados);

            }

        }

        //Definição do retorno da função
        return populacao;

    }

    private Arvores SelecaoPorTorneio(List<Arvores> arvores) {
        //Declaração Variáveis e Objetos
        ArrayList<Arvores> selecao = new ArrayList<>();

        //Selecionar 2 árvores aleatóriamente
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));
        selecao.add(arvores.get(mt.nextInt(arvores.size() - 1)));

        //Definir o retorno - Pelo Menor Fitness
        return selecao.get(0).getFitness() < selecao.get(1).getFitness() ? selecao.get(0) : selecao.get(1);

    }

    //Efetuar o crossover da população de indivíduos
    private ArrayList<Arvores> CrossoverArvores(Arvores arv1, Arvores arv2) {
        //Declaração Variáveis e Objetos e Inicializações
        ArrayList<Arvores> populacao = new ArrayList<>();

        //Remover Sub-Árvore da 1° Árvore transformando em um nodo folha na respectiva posição 
        RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv1, 1);

        //Incluir o nodo removido na 1° Árvore em uma posição aleatório da 2° Árvore (desde que a posição seja um nodo folha)
        IncluirSubArvoreNaArvoreDestino(arv2);

        //Remover Sub-Árvore da 2° Árvore transformando em um nodo folha na respectiva posição 
        RemoverNodoNaOrigemSetandoNuloERetornandoArvore(arv2, 1);

        //Incluir o nodo removido na 1° Árvore em uma posição aleatória da 2° Árvore (desde que a posição seja um nodo folha)
        IncluirSubArvoreNaArvoreDestino(arv1);

        //Adicionar os indivíduos c/ o "CROSSOVER" efetuado e limpar o objeto
        populacao.add(arv1);
        populacao.add(arv2);
        arvTemporaria = null;

        //Definir o retorno
        return populacao;

    }
    //</editor-fold>        

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE MUTAÇÃO">   
    private void MutacaoArvores(Arvores arvore, String tipo, int nivel, Instances dados) {
        //Se a árvore não for nula
        if (arvore != null) {
            //Selecionar uma posição aleatóriamente dentro da possbilidade de amostras
            int posicao = mt.nextInt(arvore.getArestas().size() - 1);

            //SE for "E" - EXPANSÃO - Vai até um nodo FOLHA E ADICIONA um DecisionStumps aleatório
            //SENÃO  "R" - REDUÇÃO  - Vai até um nodo mais profundo que o RAIZ(Definido como parâmetro) E TRANSFORMA o mesmo em um Nodo Folha
            if (tipo.equals("E")) {
                //Se a aresta selecionada não for nula
                if (arvore.getArestas(posicao).getNodo() != null) {
                    //Chamar a função recursivamente até chegar em um nodo raiz
                    MutacaoArvores(arvore.getArestas(posicao).getNodo(), tipo, nivel + 1, dados);

                    //Se atingiu o MAIOR NÍVEL de profundidade da árvore (O último nodo da aresta DEVERÁ SER nulo) - Avaliando a aresta SELECIONADA aleatóriamente
                    if (arvore.getArestas(posicao).getNodo().getArestas(posicao).getNodo() == null) {
                        //Montar os DecisionsStumps 
                        ArrayList<Arvores> temp = new DecisionStumps().ProcessamentoNodos(dados);

                        //Selecionar aleatóriamente uma árvore p/ ser incluida no nodo raiz e Adicionar o nodo na aresta selecionada
                        arvore.getArestas(posicao).getNodo().getArestas(posicao).setNodo(temp.get(Processamento.mt.nextInt(temp.size() - 1)));

                    }

                }

            } else {
                //Se atingiu o nível de profundidade estabelecido para "MUTAÇÃO"
                if (nivel == profMutacao) {
                    //Transformar o Nodo c/ arestas em Nodo Folha (Mutação de "REDUÇÃO") E Sair do processamento
                    arvore.getArestas(posicao).setNodo(null);

                } else {
                    //Chamada recursiva da função atualizando o nível de profundidade
                    MutacaoArvores(arvore.getArestas(posicao).getNodo(), tipo, nivel + 1, dados);

                }

            }

        }

    }
    //</editor-fold> 

    //<editor-fold defaultstate="collapsed" desc="FUNÇÕES PERTINENTES AOS MÉTODOS DE CROSSOVER">       
    //Remover uma Sub-Árvore da Árvore atual e setar nulo a mesma
    public void RemoverNodoNaOrigemSetandoNuloERetornandoArvore(Arvores arvore, int nivel) {
        //Declaração Variáveis e Objetos
        int iCont = 0;

        //Enquant for possível processar
        while (iCont < arvore.getArestas().size()) {
            //Sortear uma posição qualquer dentre as possíveis
            int pos = mt.nextInt(arvore.getArestas().size() - 1);

            //Se o nodo da aresta selecionada aleatóriamente não for nulo processa o mesmo
            if (arvore.getArestas(pos).getNodo() != null) {
                //Chamada da função para processamento do nodo selecionado
                PosicionarNivelNodoOrigem(arvore.getArestas(pos).getNodo(), nivel + 1);

                //Sair do for
                break;

            }

            //Atualizar o contador
            iCont++;

        }

    }

    public void IncluirSubArvoreNaArvoreDestino(Arvores arvore) {
        //Declaração Variáveis e Objetos
        int iCont = 0;

        //Enquant for possível processar
        while (iCont < arvore.getArestas().size()) {
            //Sortear uma posição qualquer dentre as possíveis
            int pos = mt.nextInt(arvore.getArestas().size() - 1);

            //Se a primeira aresta não for nula pesquisa pela mesma e sai fora do for
            if (arvore.getArestas(pos).getNodo() != null) {
                //Chamada recursiva da função
                PesquisarPosicaoArvoreDestino(arvore.getArestas(pos).getNodo());

                //Sair do for
                break;

            }

            //Atualizar o contador
            iCont++;

        }

    }

    public void PesquisarPosicaoArvoreDestino(Arvores arvore) {
        //Se o nó não for nulo
        if (arvore != null) {
            //Selecionar uma posição aleatóriamente (Não importa qual, pois é inclusão de um novo nodo)
            int posicao = mt.nextInt(arvore.getArestas().size() - 1);

            //Se o Nodo da Aresta não for nula, existem Sub-Árvores
            if (arvore.getArestas(posicao).getNodo() != null) {
                //Chamada Recursiva da Função p/ Avaliação da Sub-Árvore informada
                PesquisarPosicaoArvoreDestino(arvore.getArestas(posicao).getNodo());

            } else {
                //Se a aresta informada for nula insere o nodo e finaliza o ciclo
                arvore.getArestas(posicao).setNodo(arvTemporaria);

            }

        }

    }

    private void PosicionarNivelNodoOrigem(Arvores arvore, int nivel) {
        //Se o nó não for nulo
        if (arvore != null) {
            //Selecionar uma posição aleatóriamente
            int posicao = mt.nextInt(arvore.getArestas().size() - 1);

            //Se a primeira aresta não for nula pesquisa pela mesma
            if (arvore.getArestas(posicao).getNodo() != null) {
                //Chamada recursiva da função até atinbgir o último nível
                PosicionarNivelNodoOrigem(arvore.getArestas(posicao).getNodo(), nivel + 1);

                //Se atingiu o NÍVEL DE PROFUNDIDADE ESTABELECIDO, atribui a árvore a variável e seta nulo a mesma
                if (nivel == DecisionStumps.profundidade) {
                    //Atribuir valor a Árvore
                    arvTemporaria = new Arvores();
                    arvTemporaria = arvore.getArestas(posicao).getNodo();

                    //Setar nulo ao nodo atual
                    arvore.getArestas(posicao).setNodo(null);

                }

            }

        }

    }

    public void AtribuicaoClasseNodosFolhas(Arvores arvore, Instance avaliacao) {
        //Se o árvore não for nula
        if (arvore != null) {
            //Percorrer todos os atributos da instância selecionada (Exceto o atributo Classe)
            for (int k = 0; k < avaliacao.numAttributes(); k++) {
                //Se o nome do Atributo Classe for igual ao nome do atributo da instância (Raiz ou nodo folha)
                if (avaliacao.attribute(k).name().equals(arvore.getNomeAtributo())) {
                    //Percorrer as arestas da  arvore
                    for (int i = 0; i < arvore.getArestas().size(); i++) {
                        //Se o valor da aresta for igual ao valor do atributo selecionado
                        if (Double.valueOf(arvore.getArestas(i).getAtributo()).equals(avaliacao.classValue())) {
                            //Se não for um nodo RAIZ, efetua a chamada recursiva da função até chegar em um nodo raiz
                            if (arvore.getArestas(i).getNodo() != null) {
                                //Chama a função recursivamente passando o nodo da aresta
                                AtribuicaoClasseNodosFolhas(arvore.getArestas(i).getNodo(), avaliacao);

                            } else {
                                //Declaração Variáveis e Objetos
                                ArrayList<Classes> classes = new ArrayList<>();

                                //Se a Classe for vazia Inclui o mesmo (Sendo o 1° Registro)
                                if (arvore.getArestas(i).getClasses() == null) {
                                    //Adicionar a Nova classe 
                                    classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                    //Atribuir as classes e sair fora da execução para a aresta selecionada
                                    arvore.getArestas(i).setClasses(classes);

                                } else //Já Existem Registros na Classe, irá atualizar o mesmo
                                {
                                    //Declaração Variáveis e Objetos
                                    boolean bOk = false;
                                    classes = arvore.getArestas(i).getClasses();

                                    //Percorre TODAS as classes do Nodo
                                    for (Classes classe : classes) {
                                        //Se o tipo do atributo for NÃO NUMÉRICO SENÃO SERÁ NOMINAL
                                        if (avaliacao.attribute(k).isNumeric()) {
                                            //Se o valor da aresta FOR IGUAL AO VALOR DO ATRIBUTO DA INSTÂNCIA                                            
                                            //Se o "VALOR" da classe DA INSTÂNCIA SELECIONADA FOR IGUAL a da classe informada atualiza a quantidade
                                            if (avaliacao.classAttribute().value((int) avaliacao.classValue()).equals(classe.getNome())) {
                                                //Atualizar a quantidade (Adicionando 1) de registros X Atributo - Para Definir a Classe dominante
                                                classe.atualizarQtd(1);
                                                bOk = true;

                                            }

                                        } else {
                                            //Percorrer todos os valores existentes da instância selecionada
                                            for (int l = 0; l < avaliacao.numValues(); l++) {
                                                //Se o nome do Atributo FOR IGUAL AO NOME DO ATRIBUTO da instancia de avaliação selecionada
                                                if (arvore.getArestas(i).getAtributo().equals(avaliacao.attribute(k).value(l))) {
                                                    if (classe.getNome().equals(avaliacao.classAttribute().value((int) avaliacao.classValue()))) {
                                                        //Atualizar a quantidade de registros X Atributo - Para Definir a Classe dominante
                                                        classe.atualizarQtd(1);
                                                        bOk = true;

                                                    }

                                                }

                                            }

                                        }

                                    }

                                    //Se não existe o atributo inclui o mesmo
                                    if (!bOk) {
                                        //Se não for nulo (ser o último)
                                        if (avaliacao.classAttribute() != null) {
                                            //Adicionar a Nova classe e atualizar a quantidade
                                            classes.add(new Classes(avaliacao.classAttribute().value((int) avaliacao.classValue()), 1));

                                        }

                                    }
                                    //Atribuir as classes e sair fora da execução para a aresta selecionada
                                    arvore.getArestas(i).setClasses(classes);

                                }

                            }

                        }

                    }
                    //Se processou o atributo sai fora do laço
                    break;

                }

            }

        }

    }

//Irá percorrer todos os Nodos da árvore(avaliando SOMENTE os nodos folhas)
    public void DefinicaoClasseMajoritariaNodosFolhas(Arvores arvore) {
        //Se o árvore não for nula
        if (arvore != null) {
            //Percorrer TODAS as arestas do árvore selecionado para atribuir uma classe as folhas
            for (int i = 0; i < arvore.getArestas().size(); i++) {
                //Se a aresta selecionada não for NULA pesquisa pela mesma (NULA == Nodo Folha)
                if (arvore.getArestas(i).getNodo() != null) {
                    //Chamada recursiva da função passando como parâmetros a aresta selecionada
                    DefinicaoClasseMajoritariaNodosFolhas(arvore.getArestas(i).getNodo());

                } else //Chegou em um nodo folha
                {
                    //Declaração Variáveis e Objetos
                    ArrayList<Classes> classes = arvore.getArestas(i).getClasses();

                    //Se não for nulo
                    if (classes != null) {
                        //Declaração Variáveis e Objetos
                        String NmClasseMaj = "";
                        double qtdClasse = 0;

                        //Percorre todas as Classes
                        for (Classes classe : classes) {
                            //Se for a 1° Ocorrência
                            if (NmClasseMaj.equals("")) {
                                //Atribuições do nome da classe e da quantidade
                                NmClasseMaj = classe.getNome();
                                qtdClasse = classe.getQuantidade();

                            } else {
                                //Se a quantidade for MAIOR que a ATUAL ALTERA a classe SENÃO mantém a mesma
                                if (classe.getQuantidade() > qtdClasse) {
                                    //Atribuições do nome da classe e da quantidade
                                    NmClasseMaj = classe.getNome();
                                    qtdClasse = classe.getQuantidade();

                                }

                            }

                        }

                        //Setar a classe Majoritária
                        arvore.getArestas(i).setClasseDominante(NmClasseMaj);

                    }

                }

            }

        }

    }
    //</editor-fold> 

}
